package fr.gaulupeau.apps.wallabag;

import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARCHIVE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_DATE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_SYNC;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TABLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TITLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_URL;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.MY_ID;
import static fr.gaulupeau.apps.wallabag.Helpers.PREFS_NAME;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import fr.gaulupeau.apps.InThePoche.R;

public class ListArticles extends SherlockActivity {

    private ArrayList<Article> readArticlesInfo;
	private ListView readList;
	private SQLiteDatabase database;
	
	private SharedPreferences settings;
	private String pocheUrl;
	private String apiUsername;
	private String apiToken;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSettings();
		setContentView(R.layout.list);
		setupDB();
		setupList(false);
	}
	
    public void onResume() {
        super.onResume();
        setupList(false);
    }
    
    public void onDestroy() {
        super.onDestroy();
        database.close();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
           MenuInflater inflater = getSupportMenuInflater();
           inflater.inflate(R.menu.option_list, menu);
           return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.menuShowAll:
        		setupList(true);
        		return super.onOptionsItemSelected(item);
        	case R.id.menuWipeDb:
        		ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(this);
        		helper.truncateTables(database);
        		setupList(false);
        		super.onOptionsItemSelected(item);
        	case R.id.refresh:
        		refresh();
				return true;
        	case R.id.settings:
        		startActivity(new Intent(getBaseContext(), Settings.class));
    		default:
    			return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onBackPressed (){
    	setResult(RESULT_OK);
    	super.onBackPressed();
    }
	
	public void setupDB() {
		ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(this);
		database = helper.getWritableDatabase();
	}
	
	private void getSettings(){
        settings = getSharedPreferences(PREFS_NAME, 0);
        pocheUrl = settings.getString("pocheUrl", "https://");
        apiUsername = settings.getString("APIUsername", "");
        apiToken = settings.getString("APIToken", "");
    }
	
	 public void showToast(final String toast){
	    	runOnUiThread(new Runnable() {
	    		public void run()
	    		{
	    			Toast.makeText(ListArticles.this, toast, Toast.LENGTH_SHORT).show();
	    		}
	    	});
	    }
	 public void refresh(){
		// Vérification de la connectivité Internet
		 final ConnectivityManager conMgr =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		 final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		 if (pocheUrl == "https://") {
			 showToast(getString(R.string.txtConfigNotSet));
		 } else if (activeNetwork != null && activeNetwork.isConnected()) {
			 // Exécution de la synchro en arrière-plan
			 new Thread(new Runnable() {
				 public void run() {
					 //pushRead();
					 parseRSS();
				 }
			 }).start();
		 } else {
			 // Afficher alerte connectivité
			 showToast(getString(R.string.txtNetOffline));
		 }
	 }
	 
	    public void parseRSS(){

	    	URL url;
	    	try
	    	{
	    		// Set the url (you will need to change this to your RSS URL
	    		url = new URL(pocheUrl + "/?feed&type=home&user_id=" + apiUsername + "&token=" + apiToken );
	    		// Setup the connection
	    		HttpsURLConnection conn_s = null;
	    		HttpURLConnection conn = null;
	    		if (pocheUrl.startsWith("https") ) {
	    			trustEveryone();
	    			conn_s = (HttpsURLConnection) url.openConnection();
	    		}else{
	    			conn = (HttpURLConnection) url.openConnection();
	    		}
	    		
	    		if (
	    				((conn != null) && (conn.getResponseCode() == HttpURLConnection.HTTP_OK)) 
	    			|| ((conn_s != null) && (conn_s.getResponseCode() == HttpURLConnection.HTTP_OK))
	    			)
	    		{

	    			// Retreive the XML from the URL
	    			DocumentBuilderFactory dbf = DocumentBuilderFactory
	    					.newInstance();
	    			DocumentBuilder db = dbf.newDocumentBuilder();
	    			Document doc;
//	    			doc = db.parse(url.openStream());
	    			InputSource is = new InputSource(
					        new InputStreamReader(
					                url.openStream()));
	    			doc = db.parse(is);
//	    			doc = db.parse(
//	    				    new InputSource(
//	    				        new InputStreamReader(
//	    				                url.openStream(),
//	    				                "latin-1")));
	    			doc.getDocumentElement().normalize();
	    			
	    			// This is the root node of each section you want to parse
	    			NodeList itemLst = doc.getElementsByTagName("item");

	    			// This sets up some arrays to hold the data parsed
	    			arrays.PodcastTitle = new String[itemLst.getLength()];
	    			arrays.PodcastURL = new String[itemLst.getLength()];
	    			arrays.PodcastContent = new String[itemLst.getLength()];
	    			arrays.PodcastMedia = new String[itemLst.getLength()];
	    			arrays.PodcastDate = new String[itemLst.getLength()];

	    			// Loop through the XML passing the data to the arrays
	    			for (int i = 0; i < itemLst.getLength(); i++)
	    			{

	    				Node item = itemLst.item(i);
	    				if (item.getNodeType() == Node.ELEMENT_NODE)
	    				{
	    					Element ielem = (Element) item;

	    					// This section gets the elements from the XML
	    					// that we want to use you will need to add
	    					// and remove elements that you want / don't want
	    					NodeList title = ielem.getElementsByTagName("title");
	    					NodeList link = ielem.getElementsByTagName("link");
	    					NodeList date = ielem.getElementsByTagName("pubDate");
	    					NodeList content = ielem
	    							.getElementsByTagName("description");
	    					//NodeList media = ielem
	    					//		.getElementsByTagName("media:content");

	    					// This is an attribute of an element so I create
	    					// a string to make it easier to use
	    					//String mediaurl = media.item(0).getAttributes()
	    					//		.getNamedItem("url").getNodeValue();

	    					// This section adds an entry to the arrays with the
	    					// data retrieved from above. I have surrounded each
	    					// with try/catch just incase the element does not
	    					// exist
	    					try
	    					{
	    						arrays.PodcastTitle[i] = Wallabag.cleanString(title.item(0).getChildNodes().item(0).getNodeValue());
	    					} catch (NullPointerException e)
	    					{
	    						e.printStackTrace();
	    						arrays.PodcastTitle[i] = "Echec";
	    					}
	    					try {
								arrays.PodcastDate[i] = date.item(0).getChildNodes().item(0).getNodeValue();
							} catch (NullPointerException e) {
								e.printStackTrace();
	    						arrays.PodcastDate[i] = null;
							}
	    					try
	    					{
	    						arrays.PodcastURL[i] = link.item(0).getChildNodes()
	    								.item(0).getNodeValue();
	    					} catch (NullPointerException e)
	    					{
	    						e.printStackTrace();
	    						arrays.PodcastURL[i] = "Echec";
	    					}
	    					try
	    					{
	    						arrays.PodcastContent[i] = content.item(0)
	    								.getChildNodes().item(0).getNodeValue();
	    					} catch (NullPointerException e)
	    					{
	    						e.printStackTrace();
	    						arrays.PodcastContent[i] = "Echec";
	    					}
	    					
	    					ContentValues values = new ContentValues();
	    					values.put(ARTICLE_TITLE, Html.fromHtml(arrays.PodcastTitle[i]).toString());
	        				values.put(ARTICLE_CONTENT, Html.fromHtml(arrays.PodcastContent[i]).toString());
	        				//values.put(ARTICLE_ID, Html.fromHtml(article.getString("id")).toString());
	        				values.put(ARTICLE_URL, Html.fromHtml(arrays.PodcastURL[i]).toString());
	        				values.put(ARTICLE_DATE, arrays.PodcastDate[i]);
	        				values.put(ARCHIVE, 0);
	        				values.put(ARTICLE_SYNC, 0);
	        				try {
	        					database.insertOrThrow(ARTICLE_TABLE, null, values);
	        				} catch (SQLiteConstraintException e) {
	        					continue;
	        				} catch (SQLiteException e) {
	        				database.execSQL("ALTER TABLE " + ARTICLE_TABLE + " ADD COLUMN " + ARTICLE_DATE + " datetime;");
	        				database.insertOrThrow(ARTICLE_TABLE, null, values);
	    					}
	    				}
	    			}
	    			
	    		}
				showToast(getString(R.string.txtSyncDone));
	    		updateUnread();
	    	} catch (MalformedURLException e)
	    	{
	    		e.printStackTrace();
	    	} catch (DOMException e)
	    	{
	    		e.printStackTrace();
	    	} catch (IOException e)
	    	{
	    		e.printStackTrace();
	    	} catch (ParserConfigurationException e)
	    	{
	    		e.printStackTrace();
	    	} catch (SAXException e)
	    	{
	    		e.printStackTrace();
	    	} catch (Exception e) {
				e.printStackTrace();
			}
	    	

	    }
	
	
	    private void trustEveryone() {
	    	try {
	    		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
	        			public boolean verify(String hostname, SSLSession session) {
	        				return true;
	        			}});
	    		SSLContext context = SSLContext.getInstance("TLS");
	    		context.init(null, new X509TrustManager[]{new X509TrustManager(){
	    			public void checkClientTrusted(X509Certificate[] chain,
	    					String authType) throws CertificateException {}
	    			public void checkServerTrusted(X509Certificate[] chain,
	    					String authType) throws CertificateException {}
	    			public X509Certificate[] getAcceptedIssuers() {
	    				return new X509Certificate[0];
	    			}}}, new SecureRandom());
	    		HttpsURLConnection.setDefaultSSLSocketFactory(
	    				context.getSocketFactory());
	    	} catch (Exception e) { // should never happen
	    		e.printStackTrace();
	    	}
	    }
	    
	    private void updateUnread(){
	    	runOnUiThread(new Runnable() {
	    		public void run()
	    		{
	    			ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(getApplicationContext());
	    			database = helper.getReadableDatabase();
	    			int news = database.query(ARTICLE_TABLE, null, ARCHIVE + "=0", null, null, null, null).getCount();
	    			showToast(String.format(getString(R.string.unread_articles), news));
	    			setupList(false);
	    		}
	    	});
	    }
	    
	    public void setupList(Boolean showAll) {
		readList = (ListView) findViewById(R.id.liste_articles);
        readArticlesInfo = new ArrayList<Article>();
        String filter = null;
        if (showAll == false) {
			filter = ARCHIVE + "=0";
		}
        ReadingListAdapter ad = getAdapterQuery(filter, readArticlesInfo);
        readList.setAdapter(ad);
        
        readList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent i = new Intent(getBaseContext(), ReadArticle.class);
				i.putExtra("id", (String) readArticlesInfo.get(position).id);
				startActivity(i);
			}
        	
        });
	}
	
	public ReadingListAdapter getAdapterQuery(String filter, ArrayList<Article> articleInfo) {
		//Log.e("getAdapterQuery", "running query");
		//String url, String domain, String id, String title, String content
		String[] getStrColumns = new String[] {ARTICLE_URL, MY_ID, ARTICLE_TITLE, ARTICLE_CONTENT, ARCHIVE};
		Cursor ac = database.query(
				ARTICLE_TABLE,
				getStrColumns,
				filter, null, null, null, ARTICLE_DATE + " DESC");
		ac.moveToFirst();
		if(!ac.isAfterLast()) {
			do {
				Article tempArticle = new Article(ac.getString(0),ac.getString(1),ac.getString(2),ac.getString(3),ac.getString(4));
				articleInfo.add(tempArticle);
			} while (ac.moveToNext());
		}
		ac.close();
		return new ReadingListAdapter(getBaseContext(), articleInfo);
	}
	
}
