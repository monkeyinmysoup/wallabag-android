package fr.gaulupeau.apps.wallabag;

import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARCHIVE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_DATE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_SYNC;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TABLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TITLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_URL;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.FAV;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.MY_ID;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_SUMMARY;
import static fr.gaulupeau.apps.wallabag.Helpers.PREFS_NAME;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import fr.gaulupeau.apps.settings.Settings;

public class ListArticles extends SherlockActivity {

	private static int maxChars = 100;
	
    private ArrayList<Article> readArticlesInfo;
	private ListView readList;
	private SQLiteDatabase database;
	
	private SharedPreferences settings;
	private String pocheUrl;
	private String apiUsername;
	private String apiToken;
	
	private boolean checked;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSettings();
		setContentView(R.layout.list);

		setupDB();
		setupList(false);
	}
	
    public void onResume() {
        super.onResume();
        getSettings();
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
        		wipeDB();
        		return true;
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
	
	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(ListArticles.this, toast, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}
	 public void refresh(){
		// VÃ©rification de la connectivitÃ© Internet
		 final ConnectivityManager conMgr =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		 final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		 if (pocheUrl.equals("https://")) {
			 showToast(getString(R.string.txtConfigNotSet));
		 } else if (activeNetwork != null && activeNetwork.isConnected()) {
			 // ExÃ©cution de la synchro en arriÃ¨re-plan
			 new Thread(new Runnable() {
				 public void run() {
					 parseRSS();
				 }
			 }).start();
		 } else {
			 // Afficher alerte connectivitÃ©
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
	    			InputSource is = new InputSource(
					        new InputStreamReader(
					                url.openStream()));
	    			doc = db.parse(is);
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

	    					// This section adds an entry to the arrays with the
	    					// data retrieved from above. I have surrounded each
	    					// with try/catch just incase the element does not
	    					// exist
	    					try
	    					{
	    						arrays.PodcastTitle[i] = cleanString(title.item(0).getChildNodes().item(0).getNodeValue());
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
	    					
	    					values.put(ARTICLE_CONTENT, changeImagesUrl(arrays.PodcastContent[i]));
	    					values.put(ARTICLE_SUMMARY, makeDescription(arrays.PodcastContent[i]));
	        				values.put(ARTICLE_URL, Html.fromHtml(arrays.PodcastURL[i]).toString());
	        				values.put(ARTICLE_DATE, arrays.PodcastDate[i]);
	        				values.put(ARCHIVE, 0);
	        				values.put(FAV, 0);
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
	    			if(news == 0)
	    				showToast(getString(R.string.no_unread_articles));
	    			else if(news == 1)
	    				showToast(getString(R.string.one_unread_article));
	    			else
	    				showToast(String.format(getString(R.string.many_unread_articles), news));
	    			setupList(false);
	    		}
	    	});
	    }
	    
	    public void setupList(Boolean showAll) {
	    TextView tvNoArticles = (TextView) findViewById(R.id.no_articles_text);
		readList = (ListView) findViewById(R.id.liste_articles);
        readArticlesInfo = new ArrayList<Article>();
        String filter = null;
        if (showAll == false) {
			filter = ARCHIVE + "=0";
		}
        
        ReadingListAdapter ad = getAdapterQuery(filter, readArticlesInfo);
        readList.setAdapter(ad);
        
        if(readArticlesInfo.size() == 0)
        	tvNoArticles.setVisibility(View.VISIBLE);
        else
        	tvNoArticles.setVisibility(View.GONE);
        
        readList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent i = new Intent(getBaseContext(), ReadArticle.class);
				i.putExtra("id", (String) readArticlesInfo.get(position).id);
				startActivity(i);
			}
        	
        });
	}
	
	public ReadingListAdapter getAdapterQuery(String filter, ArrayList<Article> articleInfo) {
		String[] getStrColumns = new String[] {ARTICLE_URL, MY_ID, ARTICLE_TITLE, ARTICLE_CONTENT, ARCHIVE, ARTICLE_SUMMARY};
		Cursor ac = database.query(
				ARTICLE_TABLE,
				getStrColumns,
				filter, null, null, null, ARTICLE_DATE/* + " DESC"*/);
		ac.moveToFirst();
		if(!ac.isAfterLast()) {
			do {
				Article tempArticle = new Article(ac.getString(0),ac.getString(1),ac.getString(2),ac.getString(3),ac.getString(4), ac.getString(5));
				articleInfo.add(tempArticle);
			} while (ac.moveToNext());
		}
		ac.close();
		return new ReadingListAdapter(getBaseContext(), articleInfo);
	}
	
	private void wipeDB(){
		checked = false;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

	    builder.setTitle(getString(R.string.wipe_data_base));
	    builder.setMessage(getString(R.string.sure));
	    
	    View checkBoxView = View.inflate(getBaseContext(), R.layout.my_checkbox, null);
	    CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox_delete_acoount);
	    checkBox.setOnCheckedChangeListener(
	    	new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
			}
		});
	    
	    builder.setView(checkBoxView);
	    
	    builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
	    	@Override
	        public void onClick(DialogInterface dialog, int which) {
	        	ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(ListArticles.this);
        		helper.truncateTables(database);
        		deleteFiles();
        		setupList(false);
        		if(checked)
        			cleanUserInfo();
	            dialog.dismiss();
	        }

	    });

	    builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.dismiss();
	        }
	    });

	    AlertDialog alert = builder.create();
	    alert.show();	
	}
	
	protected void deleteFiles() {
		File filesDir = new File(Environment.getExternalStorageDirectory()
	            + "/Android/data/"
	            + getApplicationContext().getPackageName()
	            + "/files");
		
		for(File file : filesDir.listFiles())
			file.delete();		
	}

	private void cleanUserInfo(){
		System.out.println("called");
		SharedPreferences settings;
		SharedPreferences.Editor editor;
		
		settings = getSharedPreferences(PREFS_NAME, 0);
	    editor = settings.edit();
		
		editor.putString("pocheUrl", "https://");
    	editor.putString("APIUsername", "");
    	editor.putString("APIToken", "");
		editor.commit();
		
		getSettings();
	}
	
	private String changeImagesUrl(String html){
		int lastImageTag = 0;
		while(true){
			int openTagPosition = html.indexOf("<img", lastImageTag);
			
			if(openTagPosition == -1)
				break;
			
			
			int closeTagPosition = html.indexOf('>', openTagPosition);
			
			if(closeTagPosition == -1)
				throw new RuntimeException("Error while parsing html");
			
			String tagContent = html.substring(openTagPosition, closeTagPosition + 1);
			
			String[] tagParams = tagContent.split(" ");
			String imageSource = "";
			int sourceIndex = 0;
			for(String param : tagParams){
				if(param.startsWith("src")){
					imageSource = param;
					break;
				}
				sourceIndex++;
			}
			
			imageSource = imageSource.replaceAll("src=", "");
			imageSource = imageSource.replaceAll("\"", "");
			imageSource = imageSource.trim();
			
			Bitmap bitmap = getBitmapFromURL(imageSource);
			
			String savedLocation = saveBitmap(bitmap, "" + imageSource.hashCode());
			
			tagParams[sourceIndex] = "style=\"max-width: 100%; height: auto; display: block; margin-left: auto;  margin-right: auto;\" src=\"file://" + savedLocation + "\"";
			
			String newTag = recreateTag(tagParams);
			
			html = html.replace(tagContent, newTag);
			
			lastImageTag = closeTagPosition + 1;
		}
		
		return html;
	}
	
	private String recreateTag(String[] tagParams) {
		String tag = "";
		for(String param : tagParams)
			tag += param + " ";
		
		tag = tag.trim();
		return tag;
	}

	public Bitmap getBitmapFromURL(String src) {
	    try {
	        URL url = new URL(src);
	        HttpURLConnection connection = (HttpURLConnection) url
	                .openConnection();
	        connection.setDoInput(true);
	        connection.connect();
	        InputStream input = connection.getInputStream();
	        Bitmap myBitmap = BitmapFactory.decodeStream(input);
	        return myBitmap;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	public String saveBitmap(Bitmap bitmap, String fileName){
		
		File saveFolder = new File(Environment.getExternalStorageDirectory()
	            + "/Android/data/"
	            + getApplicationContext().getPackageName()
	            + "/files");
		
		if(!saveFolder.exists())
			saveFolder.mkdirs();
		
		File saveLocation = new File(saveFolder, fileName);
		
		if(!saveLocation.exists()){
			FileOutputStream outputStream;
			try {
				outputStream = new FileOutputStream(saveLocation);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
				outputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		return saveLocation.getAbsolutePath();
	}
	
	private String makeDescription(String html) {
		int chars = 0;
		String desc = "";
		String tmp = Html.fromHtml(html).toString();
		
		tmp = tmp.replaceAll("\n", " ");
		tmp = tmp.replaceAll(" [ ]*", " ");
		tmp = tmp.replaceAll("￼", "");
		
		String[] words = tmp.split(" ");
		
		for(int i = 0; i < words.length && chars < maxChars; i++){
			
			chars += words[i].length();
			desc += words[i] + " ";
		}
		return desc;
	}
	
	 private static String cleanString(String s){
	    	
	    	s = s.replace("&Atilde;&copy;", "&eacute;");
	    	s = s.replace("&Atilde;&uml;", "&egrave;");
	    	s = s.replace("&Atilde;&ordf;", "&ecirc;");
	    	s = s.replace("&Atilde;&laquo;", "&euml;");
	    	s = s.replace("&Atilde;&nbsp;", "&agrave;");
	    	s = s.replace("&Atilde;&curren;", "&auml;");
	    	s = s.replace("&Atilde;&cent;", "&acirc;");
	    	s = s.replace("&Atilde;&sup1;", "&ugrave;");
	    	s = s.replace("&Atilde;&raquo;", "&ucirc;");
	    	s = s.replace("&Atilde;&frac14;", "&uuml;");
	    	s = s.replace("&Atilde;&acute;", "&ocirc;");
	    	s = s.replace("&Atilde;&para;", "&ouml;");
	    	s = s.replace("&Atilde;&reg;", "&icirc;");
	    	s = s.replace("&Atilde;&macr;", "&iuml;");
	    	s = s.replace("&Atilde;&sect;", "&ccedil;");
	    	s = s.replace("&amp;", "&amp;");	
	    	return s;
	    }
}
