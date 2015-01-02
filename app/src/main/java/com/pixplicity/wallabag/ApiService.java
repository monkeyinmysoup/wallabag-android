package com.pixplicity.wallabag;

import com.pixplicity.easyprefs.library.Prefs;
import com.pixplicity.wallabag.api.SslTrustManager;
import com.pixplicity.wallabag.api.TrustingHttpClient;
import com.pixplicity.wallabag.models.Article;
import com.squareup.okhttp.OkHttpClient;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;


public class ApiService extends IntentService {

    /**
     * Intent action telling the service to fetch new articles.
     *
     * @see #refreshArticles()
     */
    public static final String REFRESH_ARTICLES = "com.pixplicity.wallabag.REFRESH_ARTICLES";
    /**
     * Intent action telling the service to remove an article locally.
     *
     * @see #deleteArticle(String)
     */
    public static final String DELETE_ARTICLE = "com.pixplicity.wallabag.DELETE_ARTICLE";

    public static final String EXTRA_COUNT_ALL = "all";
    public static final String EXTRA_COUNT_UNREAD = "unread";
    public static final String EXTRA_FINISHED_LOADING = "finished";
    public static final String EXTRA_PROGRESS = "progress";
    public static final String EXTRA_PROGRESS_TOTAL = "progress_total";
    public static final String EXTRA_ARTICLE_URL = "article";

    private static final String TAG = ApiService.class.getSimpleName();

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;

    /**
     * To avoid queries that are too long,
     * we split some operations up in chunks of this many articles:
     */
    public static final int CHUNK_SIZE = 10;

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
            case DELETE_ARTICLE:
                deleteArticle(intent.getStringExtra(EXTRA_ARTICLE_URL));
                break;
            default:
                throw new RuntimeException(
                        "Unknown action passed to service: " + intent.getAction());
        }
    }

    /**
     * Removes an article from all lists by deactivating the database entry.
     * Send a Refresh broadcast afterwards to update any lists of articles.
     * <p>
     * Do not call this method for batch operations, since the refresh broadcast will be
     * sent for every call.
     *
     * @param articleUrl The url of the article to remove.
     */
    private void deleteArticle(String articleUrl) {
        ContentValues values = new ContentValues();
        values.put(Article.FIELD_IS_DELETED, true);
        int result = cupboard().withContext(this).update(
                Article.URI,
                values,
                Article.FIELD_URL + "=?",
                articleUrl);
        // Send intent to inform listeners of the change
        Intent intent = new Intent(getString(R.string.broadcast_articles_loaded));
        intent.putExtra(ApiService.EXTRA_FINISHED_LOADING, true);
        intent.putExtra(ApiService.EXTRA_COUNT_ALL, -result);
        sendOrderedBroadcast(intent, null);
    }

    /**
     * Synchronizes all feeds by calling {@link #refreshArticles(ArticleType)} on each of them
     * sequentially.
     *
     * @see #refreshArticles()
     */
    private void refreshArticles() {
        refreshArticles(ArticleType.UNREAD);
        refreshArticles(ArticleType.FAVORITES);
        refreshArticles(ArticleType.ARCHIVE);
        // TODO synchronize articles by tag
        // ArticleType.TAG
    }

    /**
     * Fetches the specified RSS feed and loads the articles in it.
     * New articles are stored in the database, missing articles are removed
     * from the database.
     * <p>
     * After processing is done a broadcast is send to notify listeners,
     * containing the extras {@link #EXTRA_COUNT_ALL} and {@link #EXTRA_COUNT_UNREAD}
     * indicating the numbers of newly stored articles and total unread articles respectively,
     * or {@link #EXTRA_FINISHED_LOADING} with value {@link false} indicating that this is an
     * intermediate update and the total numbers aren't there yet. Can also contain the extras
     * {@link #EXTRA_PROGRESS} and {@link #EXTRA_PROGRESS_TOTAL} to show the amount of progress.
     *
     * @param feed The type of feed to refresh.
     */
    private void refreshArticles(ArticleType feed) {
        URL url;
        String wallabagUrl = Prefs.getString(Constants.PREFS_KEY_WALLABAG_URL, null);
        String apiUsername = Prefs.getString(Constants.PREFS_KEY_USER_ID, null);
        String apiToken = Prefs.getString(Constants.PREFS_KEY_USER_TOKEN, null);
        HttpsURLConnection conn_s = null;
        try {
            // Set the url
            url = new URL(
                    wallabagUrl
                            + "/?feed&type=" + feed.getQueryString()
                            + "&user_id=" + apiUsername
                            + "&token=" + apiToken);

            // Setup the connection
            //TrustingHttpClient client = new TrustingHttpClient(this);


            // Setup the connection
            HttpURLConnection conn = null;
            if (wallabagUrl.startsWith("https")) {
                conn_s = (HttpsURLConnection) url.openConnection();
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            int newArticles = 0;
            if ((conn != null && conn.getResponseCode() == HttpURLConnection.HTTP_OK)
                    || (conn_s != null && conn_s.getResponseCode() == HttpURLConnection.HTTP_OK)) {

                // Retrieve the XML from the URL
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource(new InputStreamReader(url.openStream()));
                Document doc = db.parse(is);
                doc.getDocumentElement().normalize();

                // This is the root node of each section you want to parse
                NodeList itemList = doc.getElementsByTagName("item");

                // Fetch items currently in database:
                String feedSelection = "";
                if (feed == ArticleType.ARCHIVE) {
                    feedSelection = Article.FIELD_IS_ARCHIVED + "=1 AND ";
                } else if (feed == ArticleType.FAVORITES) {
                    feedSelection = Article.FIELD_IS_FAV + "=1 AND ";
                }
                feedSelection += Article.FIELD_IS_DELETED + "<>1";
                List<Article> urlsInDB = cupboard().withContext(this).query(
                        Article.URI,
                        Article.class)
                        .withSelection(feedSelection)
                        .list();

                // Iterate over all articles in the XML
                int total = itemList.getLength();
                for (int i = total - 1; i >= 0; i--) {
                    Node item = itemList.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        // Parse XML node and add it to the database
                        if (parseArticle(feed, urlsInDB, (Element) item)) {
                            newArticles++;
                        }
                    }
                    if ((i % Constants.UPDATE_LIST_EVERY_X_ITEMS) == 0) {
                        // Intermediate update
                        Intent intent = new Intent(getString(R.string.broadcast_articles_loaded));
                        intent.putExtra(ApiService.EXTRA_PROGRESS, total - i);
                        intent.putExtra(ApiService.EXTRA_PROGRESS_TOTAL, total);
                        intent.putExtra(ApiService.EXTRA_FINISHED_LOADING, false);
                        sendOrderedBroadcast(intent, null);
                    }
                }

                // Remove items from db that are no longer in the feed
                if (feed == ArticleType.ARCHIVE) {
                    removeArticlesFromArchive(urlsInDB);
                } else if (feed == ArticleType.FAVORITES) {
                    removeArticlesFromFavs(urlsInDB);
                } else {
                    removeDeletedArticlesFromDB(urlsInDB);
                }
            }

            // Count unread articles
            Cursor c = cupboard()
                    .withContext(this)
                    .query(Article.URI, Article.class)
                    .withSelection(
                            Article.FIELD_IS_ARCHIVED + "=0 AND " + Article.FIELD_IS_DELETED + "=0")
                    .getCursor();
            int unreadArticles = c.getCount();
            c.close();

            // Refresh status
            Intent intent;
            if (feed == ArticleType.ARCHIVE) {
                intent = new Intent(getString(R.string.broadcast_archive_loaded));
            } else if (feed == ArticleType.FAVORITES) {
                intent = new Intent(getString(R.string.broadcast_favorites_loaded));
            } else {
                intent = new Intent(getString(R.string.broadcast_articles_loaded));
            }
            intent.putExtra(ApiService.EXTRA_COUNT_ALL, newArticles);
            if (feed == ArticleType.UNREAD) {
                intent.putExtra(ApiService.EXTRA_COUNT_UNREAD, unreadArticles);
            }
            intent.putExtra(ApiService.EXTRA_FINISHED_LOADING, true);
            sendOrderedBroadcast(intent, null);
        } catch (DOMException | IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            if (feed == ArticleType.UNREAD) {
                Utils.showToast(this, getString(R.string.fail_to_update));
            }
        }
    }



    /**
     * Parses a single RSS node and stores it as an Article in the database
     *
     * @param feed         The feed this article is being fetched from
     * @param articlesInDB List of articles that are marked for deletion
     * @param item         The RSS item to parse
     * @return {@link true} on a successful insertion, {@link false} on errors or if the article is
     * already present in the database
     */
    private boolean parseArticle(ArticleType feed, List<Article> articlesInDB, Element item) {
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

        // Articles in the feed are removed from the hitlist:
        int pos = findArticleInList(articlesInDB, articleUrl);
        if (pos >= 0) {
            articlesInDB.remove(pos);
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
        values.put(Article.FIELD_TITLE, Html.fromHtml(articleTitle).toString());
        values.put(Article.FIELD_CONTENT, articleContent);
        values.put(Article.FIELD_SUMMARY, Article.makeDescription(articleContent));
        values.put(Article.FIELD_DOMAIN, articleDomain);
        values.put(Article.FIELD_IMAGE_URL,
                ImageUtils.getFirstImageUrl(articleUrl, articleContent));
        values.put(Article.FIELD_URL, articleUrl);
        values.put(Article.FIELD_DATE, articleDate);
        values.put(Article.FIELD_IS_ARCHIVED, feed == ArticleType.ARCHIVE);
        values.put(Article.FIELD_IS_FAV, feed == ArticleType.FAVORITES);
        //values.put(ARTICLE_SYNC, 0);

        Article article = new Article(values);
        cupboard().withContext(this).put(Article.URI, article);

        return false;
    }

    /**
     * Finds an article (specified by url) in the list and returns the position.
     * Returns -1 if the article is not in the list.
     * Runs in linear time.
     *
     * @param articlesInDB List of articles to search through (the haystack)
     * @param articleUrl   The article to look for (the needle)
     * @return The position of the item in the list, or -1 if the item is not found.
     */
    private int findArticleInList(List<Article> articlesInDB, String articleUrl) {
        int i = 0;
        for (Article article : articlesInDB) {
            if (article.mUrl.equals(articleUrl)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * Removes articles from the database that are no longer in the feed
     *
     * @param articlesInDB List of articles that should be removed
     */
    private void removeDeletedArticlesFromDB(List<Article> articlesInDB) {
        if (articlesInDB.size() == 0) {
            return;
        }
        int i;
        boolean first = true;
        // Updated values: is_deleted = 1
        ContentValues values = new ContentValues();
        values.put(Article.FIELD_IS_DELETED, 1);
        // Where: id IN (...)
        StringBuilder selection = new StringBuilder();
        selection.append(Article.FIELD_URL).append(" IN (");
        for (i = 0; i < articlesInDB.size(); i++) {
            if (!first) {
                selection.append(",");
            }
            first = false;
            selection.append(DatabaseUtils.sqlEscapeString(articlesInDB.get(i).mUrl));

            // Execute query every 10 items, to avoid creating queries
            // that are too long
            if (i % CHUNK_SIZE == 0) {
                selection.append(");");
                // Execute query
                cupboard().withContext(this)
                        .update(Article.URI, values, selection.toString(), (String[]) null);
                // Reset query
                selection = new StringBuilder();
                selection.append(Article.FIELD_URL).append(" IN (");
                first = true;
            }
        }
        // Finish last chunk
        if (i % CHUNK_SIZE > 0) {
            selection.append(");");
            // Execute query
            cupboard().withContext(this)
                    .update(Article.URI, values, selection.toString(), (String[]) null);
        }
    }

    private void removeArticlesFromArchive(List<Article> articlesInDB) {
        if (articlesInDB.size() == 0) {
            return;
        }

        int i;
        boolean first = true;
        // Updated values: is_archived = 1
        ContentValues values = new ContentValues();
        values.put(Article.FIELD_IS_ARCHIVED, 1);
        // Where: id IN (...)
        StringBuilder selection = new StringBuilder();
        selection.append(Article.FIELD_URL).append(" IN (");
        for (i = 0; i < articlesInDB.size(); i++) {
            if (!first) {
                selection.append(",");
            }
            first = false;
            selection.append(DatabaseUtils.sqlEscapeString(articlesInDB.get(i).mUrl));
            // Execute query every 10 items, to avoid creating queries
            // that are too long
            if (i % CHUNK_SIZE == 0) {
                selection.append(");");
                // Execute query
                cupboard().withContext(this)
                        .update(Article.URI, values, selection.toString(), (String[]) null);
                // Reset query
                selection = new StringBuilder();
                selection.append(Article.FIELD_URL).append(" IN (");
                first = true;
            }
        }
        // Finish last chunk
        if (i % CHUNK_SIZE > 0) {
            selection.append(");");
            // Execute query
            cupboard().withContext(this)
                    .update(Article.URI, values, selection.toString(), (String[]) null);
        }
    }

    private void removeArticlesFromFavs(List<Article> articlesInDB) {
        if (articlesInDB.size() == 0) {
            return;
        }
        int i;
        boolean first = true;
        // Updated values: is_favorite = 1
        ContentValues values = new ContentValues();
        values.put(Article.FIELD_IS_FAV, 1);
        // Where: id IN (...)
        StringBuilder selection = new StringBuilder();
        selection.append(Article.FIELD_URL).append(" IN (");
        for (i = 0; i < articlesInDB.size(); i++) {
            if (!first) {
                selection.append(",");
            }
            first = false;
            selection.append(DatabaseUtils.sqlEscapeString(articlesInDB.get(i).mUrl));
            // Execute query every 10 items, to avoid creating queries
            // that are too long
            if (i % CHUNK_SIZE == 0) {
                selection.append(");");
                // Execute query
                cupboard().withContext(this)
                        .update(Article.URI, values, selection.toString(), (String[]) null);
                // Reset query
                selection = new StringBuilder();
                selection.append(Article.FIELD_URL).append(" IN (");
                first = true;
            }
        }
        // Finish last chunk
        if (i % CHUNK_SIZE > 0) {
            selection.append(");");
            // Execute query
            cupboard().withContext(this)
                    .update(Article.URI, values, selection.toString(), (String[]) null);
        }
    }
}
