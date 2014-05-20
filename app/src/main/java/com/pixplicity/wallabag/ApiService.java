package com.pixplicity.wallabag;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.Log;

import com.pixplicity.easyprefs.library.Prefs;
import com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper;
import com.pixplicity.wallabag.models.Article;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARCHIVE;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_DATE;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_DOMAIN;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_SUMMARY;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_SYNC;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_TABLE;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_TAGS;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_IMAGE;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_TITLE;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_URL;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.FAV;


public class ApiService extends IntentService {

    /**
     * Intent action telling the service to fetch new articles.
     *
     * @see #refreshArticles()
     */
    public static final String REFRESH_ARTICLES = "com.pixplicity.wallabag.REFRESH_ARTICLES";

    public static final String EXTRA_COUNT_ALL = "all";
    public static final String EXTRA_COUNT_UNREAD = "unread";
    public static final String EXTRA_FINISHED_LOADING = "finished";
    public static final String EXTRA_PROGRESS = "progress";
    public static final String EXTRA_PROGRESS_TOTAL = "progress_total";

    private static final String TAG = ApiService.class.getSimpleName();

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent) msg.obj);
            stopSelf(msg.arg1);
        }
    }

    public ApiService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Handles incoming Intents.
     * <p/>
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     *
     * @param intent The value passed to {@link
     *               android.content.Context#startService(Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case REFRESH_ARTICLES:
                refreshArticles();
                break;
            default:
                throw new RuntimeException("Unknown action passed to service: " + intent.getAction());
        }
    }

    /**
     * Fetches the RSS feed of the user and loads the articles in it.
     * New articles are stored in the database, missing articles are removed
     * from the database.
     * After processing is done a broadcast is send to notify listeners,
     * containing the extras {@link #EXTRA_COUNT_ALL} and {@link #EXTRA_COUNT_UNREAD}
     * indicating the numbers of newly stored articles and total unread articles respectively,
     * or {@link #EXTRA_FINISHED_LOADING} with value {@link false} indicating that this is an intermediate
     * update and the total numbers aren't there yet.
     * Can also contain the extras {@link #EXTRA_PROGRESS} and {@link #EXTRA_PROGRESS_TOTAL} to show
     * the amount of progress.
     */
    private void refreshArticles() {
//        new AsyncTask<Void, Void, Void>() {
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                parseRSS();
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void result) {
//                updateList();
//            }
//        }.execute();
        URL url;
        String wallabagUrl = Prefs.getString(Constants.PREFS_KEY_WALLABAG_URL, null);
        String apiUsername = Prefs.getString(Constants.PREFS_KEY_USER_ID, null);
        String apiToken = Prefs.getString(Constants.PREFS_KEY_USER_TOKEN, null);

        SQLiteDatabase database = getDatabase();
        try {
            // Set the url (you will need to change this to your RSS URL
            url = new URL(wallabagUrl + "/?feed&type=home&user_id=" + apiUsername
                    + "&token=" + apiToken);

            // Setup the connection
            HttpsURLConnection conn_s = null;
            HttpURLConnection conn = null;
            if (wallabagUrl.startsWith("https")) {
                trustEveryone();
                conn_s = (HttpsURLConnection) url.openConnection();
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            int newArticles = 0;
            if (((conn != null) && (conn.getResponseCode() == HttpURLConnection.HTTP_OK))
                    || ((conn_s != null) && (conn_s.getResponseCode()
                    == HttpURLConnection.HTTP_OK))) {

                // Retreive the XML from the URL
                DocumentBuilderFactory dbf = DocumentBuilderFactory
                        .newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc;
                InputSource is = new InputSource(new InputStreamReader(
                        url.openStream()));
                doc = db.parse(is);
                doc.getDocumentElement().normalize();

                // This is the root node of each section you want to parse
                NodeList itemLst = doc.getElementsByTagName("item");

                // Fetch items currently in database:
                ArrayList<String> urlsInBD = new ArrayList<>();
                Cursor ac = database.query(
                        ARTICLE_TABLE,
                        new String[]{ARTICLE_URL}, null,
                        null, null, null, null
                );
                ac.moveToFirst();
                if (!ac.isAfterLast()) {
                    do {
                        urlsInBD.add(ac.getString(0));
                    } while (ac.moveToNext());
                }

                // Loop through the XML passing the data to the arrays
                int total = itemLst.getLength();
                for (int i = total - 1; i >= 0; i--) {
                    Node item = itemLst.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        if (parseArticle(urlsInBD, database, (Element) item)) {
                            newArticles++;
                        }
                    }
                    if ((i % 10) == 0) {
                        // Intermediate update
                        Intent intent = new Intent(getString(R.string.broadcast_articles_loaded));
                        intent.putExtra(ApiService.EXTRA_PROGRESS, total - i);
                        intent.putExtra(ApiService.EXTRA_PROGRESS_TOTAL, total);
                        intent.putExtra(ApiService.EXTRA_FINISHED_LOADING, false);
                        sendOrderedBroadcast(intent, null);
                    }
                }

                // Remove items from db that are no longer in the feed
                removeDeletedArticlesFromDB(database, urlsInBD);
            }

            // Count unread articles
            int unreadArticles = database.query(ARTICLE_TABLE, null, ARCHIVE + "=0",
                    null, null, null, null).getCount();

            // Refresh status
            Intent intent = new Intent(getString(R.string.broadcast_articles_loaded));
            intent.putExtra(ApiService.EXTRA_COUNT_ALL, newArticles);
            intent.putExtra(ApiService.EXTRA_COUNT_UNREAD, unreadArticles);
            intent.putExtra(ApiService.EXTRA_FINISHED_LOADING, true);
            sendOrderedBroadcast(intent, null);

        } catch (DOMException | IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            Utils.showToast(this, getString(R.string.fail_to_update));
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showToast(this, getString(R.string.fail_to_update));
        } finally {
            database.close();
        }

    }

    /**
     * Parses a single RSS node and stores it as an Article in the database
     *
     * @param urlsInDB List of articles that are marked for deletion
     * @param database Database connection
     * @param item     The RSS item to parse
     * @return {@link true} on a successful insertion, {@link false} on errors or if the article is
     * already present in the database
     */
    private boolean parseArticle(ArrayList<String> urlsInDB, SQLiteDatabase database, Element item) {
        String articleTitle;
        String articleDate;
        String articleUrl;
        String articleDomain;
        String articleContent;

        // This section gets the elements from the XML
        // that we want to use you will need to add
        // and remove elements that you want / don't want
        NodeList title = item.getElementsByTagName("title");
        NodeList link = item.getElementsByTagName("link");
        NodeList date = item.getElementsByTagName("pubDate");
        NodeList content = item.getElementsByTagName("description");

        // This section adds an entry to the arrays with the
        // data retrieved from above. I have surrounded each
        // with try/catch just in case the element does not
        // exist
        try {
            articleUrl = link.item(0).getChildNodes().item(0)
                    .getNodeValue();
        } catch (NullPointerException e) {
            e.printStackTrace();
            articleUrl = getString(R.string.missing_content);
        }
        articleUrl = Html.fromHtml(articleUrl).toString();
        articleDomain = Utils.getDomainFromUrl(articleUrl);
        Log.d(TAG, articleDomain + "; " + articleUrl);

        // Articles in the feed are remove from the hitlist:
        if (urlsInDB.contains(articleUrl)) {
            urlsInDB.remove(articleUrl);
            return true;
        }

        try {
            articleTitle = Utils.cleanString(
                    title.item(0).getChildNodes().item(0).getNodeValue());
        } catch (NullPointerException e) {
            e.printStackTrace();
            articleTitle = getString(R.string.missing_content);
        }
        try {
            articleDate = date.item(0).getChildNodes().item(0)
                    .getNodeValue();
        } catch (NullPointerException e) {
            e.printStackTrace();
            articleDate = null;
        }
        try {
            articleContent = content.item(0).getChildNodes()
                    .item(0).getNodeValue();
        } catch (NullPointerException e) {
            e.printStackTrace();
            articleContent = getString(R.string.missing_content);
        }

        ContentValues values = new ContentValues();
        values.put(ARTICLE_TITLE, Html.fromHtml(articleTitle)
                .toString());
//        values.put(ARTICLE_CONTENT,
//                ImageUtils.changeImagesUrl(this, articleContent));
        values.put(ARTICLE_CONTENT, articleContent);
        values.put(ARTICLE_SUMMARY,
                Article.makeDescription(articleContent));
        values.put(ARTICLE_DOMAIN, articleDomain);
        values.put(ARTICLE_TAGS, "");
        values.put(ARTICLE_IMAGE, ImageUtils.getFirstImageUrl(articleUrl, articleContent));
        values.put(ARTICLE_URL, articleUrl);
        values.put(ARTICLE_DATE, articleDate);
        values.put(ARCHIVE, 0);
        values.put(FAV, 0);
        values.put(ARTICLE_SYNC, 0);

        try {
            database.insertOrThrow(ARTICLE_TABLE, null, values);
        } catch (SQLiteConstraintException e) {
            return true;
        } catch (SQLiteException e) {
            database.execSQL("ALTER TABLE " + ARTICLE_TABLE
                    + " ADD COLUMN " + ARTICLE_DATE
                    + " datetime;");
            database.insertOrThrow(ARTICLE_TABLE, null, values);
        }
        return false;
    }

    private SQLiteDatabase getDatabase() {
        ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(this);
        return helper.getWritableDatabase();
    }

    /**
     * Removes articles from the database that are no longer in the feed
     *
     * @param db       Database connection
     * @param urlsInBD List of article urls that should be removed
     */
    private void removeDeletedArticlesFromDB(SQLiteDatabase db, ArrayList<String> urlsInBD) {
        if (urlsInBD.size() == 0) {
            return;
        }
        StringBuilder query = new StringBuilder();
        query.append("DELETE FROM ")
                .append(ARTICLE_TABLE)
                .append(" WHERE ")
                .append(ARTICLE_URL)
                .append(" IN (");
        for (int i = 0; i < urlsInBD.size(); i++) {
            if (i > 0) {
                query.append(",");
            }
            query.append("'")
                    .append(urlsInBD.toString())
                    .append("'");
        }
        query.append(");");
        db.execSQL(query.toString());
    }

    private void trustEveryone() {
        try {
            HttpsURLConnection
                    .setDefaultHostnameVerifier(new HostnameVerifier() {

                        @Override
                        public boolean verify(String hostname,
                                              SSLSession session) {
                            return true;
                        }
                    });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{
                    new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context
                    .getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }

}
