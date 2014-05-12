package com.pixplicity.wallabag.wallabag;

import static com.pixplicity.wallabag.wallabag.ArticlesSQLiteOpenHelper.ARCHIVE;
import static com.pixplicity.wallabag.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT;
import static com.pixplicity.wallabag.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_DATE;
import static com.pixplicity.wallabag.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_SUMMARY;
import static com.pixplicity.wallabag.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_SYNC;
import static com.pixplicity.wallabag.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TABLE;
import static com.pixplicity.wallabag.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TITLE;
import static com.pixplicity.wallabag.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_URL;
import static com.pixplicity.wallabag.wallabag.ArticlesSQLiteOpenHelper.FAV;
import static com.pixplicity.wallabag.wallabag.ArticlesSQLiteOpenHelper.MY_ID;
import static com.pixplicity.wallabag.wallabag.Helpers.PREFS_NAME;

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
import java.util.List;

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

import com.pixplicity.wallabag.settings.Settings;
import com.pixplicity.wallabag.settings.SettingsAccount;
import com.pixplicity.wallabag.settings.SettingsGeneral;
import com.pixplicity.wallabag.settings.SettingsLookAndFeel;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import fr.gaulupeau.apps.wallabag.R;

public class ListArticles extends Activity {
	private ActionBar actionBar;

	private static int maxChars = 250;

	private ListView readList;
	private static SQLiteDatabase database;
	private ReadingListAdapter adapter;

	private SharedPreferences settings;
	private String wallabagUrl;
	private String apiUsername;
	private String apiToken;

	private int themeId;

	private int sortType;
	
	private int listFilterOption;

	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSettings();
		setTheme(themeId);
		
		actionBar = getActionBar();
		
		Utils.setActionBarIcon(actionBar, themeId);

		setContentView(R.layout.list);
		
//		if(wallabagUrl.contains("pireddss")){
//			startActivity(new Intent(getBaseContext(), Welcome.class));
//		}
		
		//Pull to refresh
		//		pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
		//
		//		ActionBarPullToRefresh.from(this).allChildrenArePullable()
		//				.listener(new OnRefreshListener() {
		//					@Override
		//					public void onRefreshStarted(View view) {
		//						refresh();
		//					}
		//				}).setup(pullToRefreshLayout);

		//Database
		setupDB();
		
		
		//Listview
		readList = (ListView) findViewById(R.id.liste_articles);
		adapter = new ReadingListAdapter(getBaseContext());
		readList.setAdapter(adapter);
		
		setupList();

		
		//Drawer
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);

		DrawerListAdapter adapter = new DrawerListAdapter(this, listFilterOption);
		drawerList.setAdapter(adapter);

		drawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		drawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer icon */
		R.string.drawer_open, /* "open drawer" description */
		R.string.drawer_close /* "close drawer" description */
		) {

			/** Called when a drawer has settled in a completely closed state. */
			@Override
			public void onDrawerClosed(View view) {
			}

			/** Called when a drawer has settled in a completely open state. */
			@Override
			public void onDrawerOpened(View drawerView) {
			}
		};

		// Set the drawer toggle as the DrawerListener
		drawerLayout.setDrawerListener(drawerToggle);
		drawerLayout.setScrimColor(Color.parseColor("#77000000"));
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onResume() {
		super.onResume();
		getSettings();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		database.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_list, menu);
		return true;

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == Constants.REQUEST_READ_ARTICLE) {
			updateList(resultCode);
		}
		
		if(requestCode == Constants.REQUEST_SETTINGS) {
			if(resultCode == Constants.RESULT_LIST_SHOULD_CHANGE) {
				setupList();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if(drawerLayout.isDrawerOpen(drawerList)) {
				drawerLayout.closeDrawer(drawerList);
			} else {
				drawerLayout.openDrawer(drawerList);
			}
			return true;
		case R.id.refresh:
			refresh();
			return true;
		case R.id.settings:
			startActivityForResult(
					new Intent(getBaseContext(), Settings.class),
					Constants.REQUEST_SETTINGS);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void setupDB() {
		ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(this);
		database = helper.getWritableDatabase();
	}

	private void getSettings() {
		settings = getSharedPreferences(PREFS_NAME, 0);
		wallabagUrl = settings.getString(SettingsAccount.SERVER_URL, "https://");
		apiUsername = settings.getString(SettingsAccount.USER_ID, "");
		apiToken = settings.getString(SettingsAccount.TOKEN, "");

		int newThemeId = settings.getInt(SettingsLookAndFeel.DARK_THEME,
				R.style.AppThemeWhite);
		if (themeId != 0 && newThemeId != themeId) {
			themeId = newThemeId;
			Utils.restartActivity(this);
		} else {
			themeId = newThemeId;
		}

		sortType = settings.getInt(SettingsGeneral.SORT_TYPE,
				SettingsGeneral.NEWER);
		
		listFilterOption = settings.getInt(Constants.LIST_FILTER_OPTION, Constants.ALL);
	}

	public void refresh() {
		// VÃ©rification de la connectivitÃ© Internet
		final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (wallabagUrl.equals("https://")) {
			Utils.showToast(this, getString(R.string.txtConfigNotSet));
			finishedRefreshing();
		} else if (activeNetwork != null && activeNetwork.isConnected()) {
			// ExÃ©cution de la synchro en arriÃ¨re-plan
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					parseRSS();
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
//					super.onPostExecute(result);
					finishedRefreshing();
					updateList();
				}
			}.execute();
		} else {
			// Afficher alerte connectivitÃ©
			Utils.showToast(this, getString(R.string.txtNetOffline));
			finishedRefreshing();
		}
	}

	private void finishedRefreshing() {
		//		if(pullToRefreshLayout != null) {
		//			pullToRefreshLayout.setRefreshComplete();
		//		}
	}

	public void parseRSS() {

		URL url;
		try {
			// Set the url (you will need to change this to your RSS URL
			url = new URL(wallabagUrl + "/?feed&type=home&user_id=" + apiUsername
					+ "&token=" + apiToken);
			System.out.println(url);
			// Setup the connection
			HttpsURLConnection conn_s = null;
			HttpURLConnection conn = null;
			if (wallabagUrl.startsWith("https")) {
				trustEveryone();
				conn_s = (HttpsURLConnection) url.openConnection();
			} else {
				conn = (HttpURLConnection) url.openConnection();
			}

			if (((conn != null) && (conn.getResponseCode() == HttpURLConnection.HTTP_OK))
					|| ((conn_s != null) && (conn_s.getResponseCode() == HttpURLConnection.HTTP_OK))) {

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

				// This sets up some arrays to hold the data parsed
				// arrays.PodcastTitle = new String[itemLst.getLength()];
				// arrays.PodcastURL = new String[itemLst.getLength()];
				// arrays.PodcastContent = new String[itemLst.getLength()];
				// arrays.PodcastMedia = new String[itemLst.getLength()];
				// arrays.PodcastDate = new String[itemLst.getLength()];

				ArrayList<String> urlsInBD = new ArrayList<String>();
				String[] getStrColumns = new String[] { ARTICLE_URL };
				Cursor ac = database.query(ARTICLE_TABLE, getStrColumns, null,
						null, null, null, null);
				ac.moveToFirst();
				if (!ac.isAfterLast()) {
					do {
						urlsInBD.add(ac.getString(0));
					} while (ac.moveToNext());
				}
				// Loop through the XML passing the data to the arrays
				for (int i = itemLst.getLength() - 1; i >= 0; i--) {

					Node item = itemLst.item(i);
					if (item.getNodeType() == Node.ELEMENT_NODE) {
						Element ielem = (Element) item;

						String articleTitle;
						String articleDate;
						String articleUrl;
						String articleContent;

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
						try {
							articleUrl = link.item(0).getChildNodes().item(0)
									.getNodeValue();
						} catch (NullPointerException e) {
							e.printStackTrace();
							articleUrl = "Echec";
						}
						articleUrl = Html.fromHtml(articleUrl).toString();

						System.out.println(articleUrl);

						if (urlsInBD.contains(articleUrl)) {
							urlsInBD.remove(articleUrl);
							continue;
						}
						try {
							articleTitle = cleanString(title.item(0)
									.getChildNodes().item(0).getNodeValue());
						} catch (NullPointerException e) {
							e.printStackTrace();
							articleTitle = "Echec";
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
							articleContent = "Echec";
						}

						ContentValues values = new ContentValues();
						values.put(ARTICLE_TITLE, Html.fromHtml(articleTitle)
								.toString());

						values.put(ARTICLE_CONTENT,
								changeImagesUrl(articleContent));
						values.put(ARTICLE_SUMMARY,
								makeDescription(articleContent));
						values.put(ARTICLE_URL, articleUrl);
						values.put(ARTICLE_DATE, articleDate);
						values.put(ARCHIVE, 0);
						values.put(FAV, 0);
						values.put(ARTICLE_SYNC, 0);

						try {
							database.insertOrThrow(ARTICLE_TABLE, null, values);
						} catch (SQLiteConstraintException e) {
							continue;
						} catch (SQLiteException e) {
							database.execSQL("ALTER TABLE " + ARTICLE_TABLE
									+ " ADD COLUMN " + ARTICLE_DATE
									+ " datetime;");
							database.insertOrThrow(ARTICLE_TABLE, null, values);
						}
					}
				}
				removeDeletedArticlesFromDB(urlsInBD);
			}
			updateUnread();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Utils.showToast(this, getString(R.string.fail_to_update));
		} catch (DOMException e) {
			e.printStackTrace();
			Utils.showToast(this, getString(R.string.fail_to_update));
		} catch (IOException e) {
			e.printStackTrace();
			Utils.showToast(this, getString(R.string.fail_to_update));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			Utils.showToast(this, getString(R.string.fail_to_update));
		} catch (SAXException e) {
			e.printStackTrace();
			Utils.showToast(this, getString(R.string.fail_to_update));
		} catch (Exception e) {
			e.printStackTrace();
			Utils.showToast(this, getString(R.string.fail_to_update));
		}

	}

	private void removeDeletedArticlesFromDB(ArrayList<String> urlsInBD) {
		for (String url : urlsInBD) {
			database.execSQL("DELETE FROM " + ARTICLE_TABLE + " WHERE "
					+ ARTICLE_URL + "=" + "'" + url + "'" + ";");
		}
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
			context.init(null, new X509TrustManager[] { new X509TrustManager() {
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
			} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context
					.getSocketFactory());
		} catch (Exception e) { // should never happen
			e.printStackTrace();
		}
	}

	private void updateUnread() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				int news = database.query(ARTICLE_TABLE, null, ARCHIVE + "=0",
						null, null, null, null).getCount();
				if (news == 0) {
					Utils.showToast(ListArticles.this, getString(R.string.no_unread_articles));
				} else if (news == 1) {
					Utils.showToast(ListArticles.this, getString(R.string.one_unread_article));
				} else {
					Utils.showToast(ListArticles.this, String.format(
							getString(R.string.many_unread_articles), news));
				}
			}
		});
	}

	public void setupList() {
		List<Article> articlesList = getArticlesList();
		
		adapter.setListArticles(articlesList);

		readList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(getBaseContext(), ReadArticle.class);
				i.putExtra("id", ((Article)adapter.getItem(position)).id);
				startActivityForResult(i, Constants.REQUEST_READ_ARTICLE);
			}
		});
		
		checkIfHasNoArticles();
	}
	
	private void checkIfHasNoArticles() {
		TextView tvNoArticles = (TextView) findViewById(R.id.no_articles_text);
		if (adapter.getCount() == 0) {
			tvNoArticles.setVisibility(View.VISIBLE);
		} else {
			tvNoArticles.setVisibility(View.GONE);
		}
	}

	public void updateList(){
		List<Article> articlesList = getArticlesList();
		adapter.setListArticles(articlesList);
		checkIfHasNoArticles();
	}
	
	public void updateList(int result){
		System.out.println(result);
		if(Utils.hasToggledRead(result)) {
			if(listFilterOption == Constants.READ || listFilterOption == Constants.UNREAD){
				updateList();
				return;
			}
		}
				
		if(Utils.hasToggledFavorite(result)) {
			if(listFilterOption == Constants.FAVS){
				updateList();
				return;
			}
		}
		
	}
	
	private List<Article> getArticlesList(){
		getSettings();
		String orderBy = Utils.getOrderBy(sortType);
		String filter = Utils.getFilter(listFilterOption);
		
		List<Article> articlesList = new ArrayList<Article>();

		String[] getStrColumns = new String[] { ARTICLE_URL, MY_ID,
				ARTICLE_TITLE, ARCHIVE, FAV, ARTICLE_SUMMARY };
		Cursor ac = database.query(ARTICLE_TABLE, getStrColumns, filter, null,
				null, null, orderBy);
		
		ac.moveToFirst();
		if (!ac.isAfterLast()) {
			do {
				Article tempArticle = new Article(ac.getString(0),
						ac.getString(1), ac.getString(2),
						ac.getString(3), ac.getString(4), ac.getString(5));
				articlesList.add(tempArticle);
			} while (ac.moveToNext());
		}
		ac.close();
		
		return articlesList;
	}

	private String changeImagesUrl(String html) {
		int lastImageTag = 0;

		while (true) {
			int openTagPosition = html.indexOf("<img", lastImageTag);

			if (openTagPosition == -1) {
				break;
			}

			int closeTagPosition = html.indexOf('>', openTagPosition);

			if (closeTagPosition == -1) {
				throw new RuntimeException("Error while parsing html");
			}

			lastImageTag = closeTagPosition + 1;

			String tagContent = html.substring(openTagPosition,
					closeTagPosition + 1);

			String[] tagParams = tagContent.split(" ");
			String imageSource = "";
			int sourceIndex = 0;
			for (String param : tagParams) {
				if (param.startsWith("src")) {
					imageSource = param;
					break;
				}
				sourceIndex++;
			}

			imageSource = imageSource.replaceAll("src=", "");
			imageSource = imageSource.replaceAll("\"", "");
			imageSource = imageSource.trim();

			File imageFileDestination = getImageFileDestination("" + imageSource.hashCode());
			
			if(!imageFileDestination.exists()){
				
			
				Bitmap bitmap = getBitmapFromURL(imageSource);

				if (bitmap == null) {
					continue;
				}
			
			if(!saveBitmap(bitmap, imageFileDestination)) {
				continue;
			}
			}
			
			tagParams[sourceIndex] = "src=\"file://" + imageFileDestination.getAbsolutePath() + "\"";

			String newTag = recreateTag(tagParams);

			html = html.replace(tagContent, newTag);
		}

		return html;
	}

	private String recreateTag(String[] tagParams) {
		String tag = "";
		for (String param : tagParams) {
			tag += param + " ";
		}

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
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean saveBitmap(Bitmap bitmap, File saveLocation) {

			FileOutputStream outputStream;
			
			try {
				outputStream = new FileOutputStream(saveLocation);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
				outputStream.close();
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
	}
	
	public File getImageFileDestination(String imageUrl){
		File saveFolder = Utils.getSaveDir(this);
		if (!saveFolder.exists()) {
			saveFolder.mkdirs();
		}
		
		return new File(saveFolder, imageUrl);
	}

	private String makeDescription(String html) {
		int chars = 0;
		String desc = "";
		String tmp = Html.fromHtml(html).toString();

		tmp = tmp.replaceAll("￼", "");
		tmp = tmp.replaceAll("\n", " ");
		tmp = tmp.replace("\t", "");
		tmp = tmp.replaceAll(" [ ]*", " ");

		String[] words = tmp.split(" ");

		for (int i = 0; i < words.length && chars < maxChars; i++) {
			chars += words[i].length();
			desc += words[i] + " ";
		}
		return desc;
	}

	private static String cleanString(String s) {

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

	public void closeDrawer() {
		drawerLayout.closeDrawer(drawerList);
	}

	public void setListFilterOption(int option) {
		listFilterOption = option;
		Editor editor = settings.edit();
		
		editor.putInt(Constants.LIST_FILTER_OPTION, option);
		editor.commit();
	}
}
