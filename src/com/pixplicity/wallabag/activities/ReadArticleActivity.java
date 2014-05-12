package com.pixplicity.wallabag.activities;

import static com.pixplicity.wallabag.Helpers.PREFS_NAME;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARCHIVE;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_AUTHOR;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_READAT;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_TABLE;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_TITLE;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_URL;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.FAV;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.MY_ID;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


import com.pixplicity.wallabag.Constants;
import com.pixplicity.wallabag.Style;
import com.pixplicity.wallabag.Utils;
import com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper;
import com.pixplicity.wallabag.ui.OnViewScrollListener;
import com.pixplicity.wallabag.ui.ResponsiveScrollView;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.text.BidiFormatter;
import android.text.Html;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import fr.gaulupeau.apps.wallabag.R;

public class ReadArticleActivity extends Activity {
	private TextView txtTitle;
	private TextView txtAuthor;
	private SQLiteDatabase database;
	private String id = "";
	private ResponsiveScrollView view;
	private WebView contentWebView;
	private SharedPreferences preferences;

	private int currentResult;
	private boolean isRtl;
	
	private String articleUrl;
	private Menu menu;
	private ActionBar actionBar;
	private boolean isRead;
	private boolean isFav;
	private String articleContent;
	private int fontStyle;
	private int textAlign;
	private boolean canGoImmersive;
	private boolean keepScreenOn;
	private int themeId;
	private int fontSize;
	private int yPositionReadAt;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		preferences = getSharedPreferences(PREFS_NAME, 0);
		getSettings();
		setTheme(themeId);
		
		setContentView(R.layout.article);
		
		actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Utils.setActionBarIcon(actionBar, themeId);
		
		ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(
				getApplicationContext());
		database = helper.getWritableDatabase();
		String[] getStrColumns = new String[] { ARTICLE_URL, MY_ID,
				ARTICLE_TITLE, ARTICLE_CONTENT, ARCHIVE, ARTICLE_AUTHOR, FAV, ARTICLE_READAT};
		Bundle data = getIntent().getExtras();
		if (data != null) {
			id = data.getString("id");
		}
		
		Cursor ac = database.query(ARTICLE_TABLE, getStrColumns, MY_ID + "="
				+ id, null, null, null, null);
		ac.moveToFirst();
		
		txtTitle = (TextView) findViewById(R.id.article_title_text);
		txtTitle.setText(ac.getString(2));
		view = (ResponsiveScrollView) findViewById(R.id.scroll);
		contentWebView = (WebView) findViewById(R.id.webContent);
		
		WebViewClient mWebClient = new WebViewClient(){

	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        	Intent intent = new Intent(Intent.ACTION_VIEW);
	        	intent.setData(Uri.parse(url));
	        	
	        	
	        	Intent bagItIntent = new Intent(ReadArticleActivity.this, SendHandlerActivity.class);
	        	
	        	bagItIntent.setAction(Intent.ACTION_SEND);
	        	bagItIntent.setType("text/plain");
	        	bagItIntent.putExtra(Intent.EXTRA_TEXT, url);
	        	
	        	Intent chooser = Intent.createChooser(intent, url.replace("http://", ""));
	        	chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {bagItIntent});
	        	
	        	startActivity(chooser);
	        	return true;
	       }
	      
	        @Override
	        public void onPageFinished(WebView view, String url) {
	        	super.onPageFinished(view, url);
	        	
	        	System.out.println("finished");
	        	
	        	Handler handler = new Handler();
	    		handler.postDelayed(new Runnable() {

	    			@Override
	    			public void run() {
	    				ReadArticleActivity.this.view.scrollTo(0, yPositionReadAt);
	    			}
	    		}, 500);
	        }
		};
		
		contentWebView.setWebViewClient(mWebClient);
		
		articleContent = ac.getString(3);
		isRtl = BidiFormatter.getInstance().isRtl(Html.fromHtml(articleContent).toString());

		yPositionReadAt = ac.getInt(7);
		
		txtAuthor = (TextView) findViewById(R.id.article_url_text);
		
		articleUrl = ac.getString(0);
		String articleUrlHostName = "";
		try {
			URL url = new URL(articleUrl);
			articleUrlHostName = url.getHost();
		} catch (MalformedURLException e) {}
		
		txtAuthor.setText(articleUrlHostName);

		findOutIfIsRead(ac.getInt(4));
		findOutIfIsFav(ac.getInt(6));

		ac.close();
		view.setOnScrollViewListener(new OnViewScrollListener() {
			private int goingDown, goingUp;

			@Override
			public void onScrollChanged(int x, int y, int oldx, int oldy) {

				if (actionBar.isShowing() && goingDown > 100) {
					actionBar.hide();
				}

				if (!actionBar.isShowing() && goingUp > 100) {
					actionBar.show();
				}

				if (y > oldy) {
					goingDown += y - oldy;
					goingUp = 0;
				}

				if (y < oldy) {
					goingUp += oldy - y;
					goingDown = 0;
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		getSettings();
		goImmersive();
		loadDataToWebView();
		contentWebView.setKeepScreenOn(keepScreenOn);
	}
	
	@Override
	protected void onPause() {
		yPositionReadAt = view.getScrollY();
		super.onPause();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState){
		outState.putInt(ARTICLE_READAT, view.getScrollY());
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState (Bundle savedInstanceState){
		if(savedInstanceState != null && savedInstanceState.containsKey(ARTICLE_READAT)) {
			yPositionReadAt = savedInstanceState.getInt(ARTICLE_READAT);
		}
		
		super.onRestoreInstanceState(savedInstanceState);
	}

	private void loadDataToWebView() {
		
		contentWebView.loadDataWithBaseURL(null, Style.getHead(fontStyle, textAlign, fontSize, Utils.isDarkTheme(themeId), isRtl)
				+ articleContent + Style.endTag, "text/html", "utf-8", null);

		TypedValue a = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
		if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT
				&& a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
			int color = a.data;
			contentWebView.setBackgroundColor(color);
		}
	}
	
	@Override
	public void onUserInteraction() {
		super.onUserInteraction();
		goImmersive();
	}

	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void goImmersive() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (canGoImmersive) {
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LOW_PROFILE);
			} else {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_read, menu);
		this.menu = menu;
		setReadStateIcon();
		setFavStateIcon();
		setShareIcon();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.read:
			toggleMarkAsRead();
			return true;
		case R.id.fav:
			toggleFav();
			return true;
		case R.id.settings:
			startActivityForResult(new Intent(getBaseContext(),
					LookAndFeelSettingsActivity.class), this.hashCode());
			return true;
		case R.id.share:
			shareUrl();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void getSettings() {
		fontSize = preferences.getInt(LookAndFeelSettingsActivity.FONT_SIZE, 16);

		canGoImmersive = preferences.getBoolean(LookAndFeelSettingsActivity.IMMERSIVE, true);
		
		keepScreenOn = preferences.getBoolean(LookAndFeelSettingsActivity.KEEP_SCREEN_ON, false);
		
		fontStyle = preferences.getInt(LookAndFeelSettingsActivity.FONT_STYLE, 0);

		textAlign = preferences.getInt(LookAndFeelSettingsActivity.ALIGN, 0);
		
		int screenOrientation = preferences.getInt(
				LookAndFeelSettingsActivity.ORIENTATION, 0);

		switch (screenOrientation) {
		case LookAndFeelSettingsActivity.PORTRAIT:
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
			
			break;
		case LookAndFeelSettingsActivity.LANDSCAPE:
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			
			break;
		case LookAndFeelSettingsActivity.DYMAMIC:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
			
			break;
		default:
			break;
		}
		
		themeId = preferences.getInt(LookAndFeelSettingsActivity.DARK_THEME, R.style.AppThemeWhite);
	}

	private void shareUrl() {
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		Intent sendIntent = new Intent(Intent.ACTION_SEND);

		viewIntent.setData(Uri.parse(articleUrl));

		sendIntent.setType("text/plain");
		sendIntent.putExtra(Intent.EXTRA_TEXT, articleUrl);

		Intent intentChooser = createIntentChooserForTwoIntents(viewIntent,
				sendIntent, getString(R.string.share_title));

		startActivity(intentChooser);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (resultCode == Utils.RESULT_CHANGE_THEME) {
	        	getSettings();
	            Utils.restartActivity(this);
	        }
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		database.close();
	}
	
	@Override
	public void finish (){
		yPositionReadAt = view.getScrollY();
		setResult(currentResult);
		ContentValues values = new ContentValues();
		
		if(Utils.hasToggledFavorite(currentResult)){
			int value = isFav ? 1 : 0;
			values.put(FAV, value);
		}
		
		if(Utils.hasToggledRead(currentResult)){
			int value = isRead ? 1 : 0;
			values.put(ARCHIVE, value);
		}
		
		values.put(ARTICLE_READAT, yPositionReadAt);
		
		if(values.size() != 0) {
			database.update(ARTICLE_TABLE, values, MY_ID + "=" + id, null);
		}
	
		super.finish();
	}

	private void setReadStateIcon() {
		MenuItem item = menu.findItem(R.id.read);

		if (isRead) {
			if(Utils.isDarkTheme(themeId)) {
				item.setIcon(R.drawable.ic_action_undo_dark);
			} else {
				item.setIcon(R.drawable.ic_action_undo);
			}
			
			item.setTitle(getString(R.string.unread_title));
		} else {
			if(Utils.isDarkTheme(themeId)) {
				item.setIcon(R.drawable.ic_action_accept_dark);
			} else {
				item.setIcon(R.drawable.ic_action_accept);
			}
			item.setTitle(getString(R.string.read_title));
		}
	}

	private void setFavStateIcon() {
		MenuItem item = menu.findItem(R.id.fav);

		if (isFav) {
			if(Utils.isDarkTheme(themeId)) {
				item.setIcon(R.drawable.ic_action_important_dark);
			} else {
				item.setIcon(R.drawable.ic_action_important);
			}
		} else
			if(Utils.isDarkTheme(themeId)) {
				item.setIcon(R.drawable.ic_action_not_important_dark);
			} else {
				item.setIcon(R.drawable.ic_action_not_important);
			}
	}

	private void setShareIcon(){
		MenuItem item = menu.findItem(R.id.share);
		
		if(Utils.isDarkTheme(themeId)) {
			item.setIcon(R.drawable.ic_action_share_dark);
		} else {
			item.setIcon(R.drawable.ic_action_share);
		}
	}
	
	private void findOutIfIsRead(int read) {

		isRead = read == 1 ? true : false;
	}

	private void findOutIfIsFav(int fav) {

		isFav = fav == 1 ? true : false;
	}

	private void toggleMarkAsRead() {
		currentResult ^= Constants.RESULT_TOGGLE_READ;
		
		if (isRead) {
			Utils.showToast(this, getString(R.string.marked_as_unread));
			isRead = false;
		} else {
			Utils.showToast(this, getString(R.string.marked_as_read));
			isRead = true;
			finish();
		}
		setReadStateIcon();
	}

	private void toggleFav() {

		currentResult ^= Constants.RESULT_TOGGLE_FAVORITE;
		
		if (isFav) {
			Utils.showToast(this, getString(R.string.marked_as_not_fav));
			isFav = false;
		} else {
			Utils.showToast(this, getString(R.string.marked_as_fav));
			isFav = true;
		}
		setFavStateIcon();
	}

	private Intent createIntentChooserForTwoIntents(Intent first,
			Intent second, String title) {

		PackageManager pm = getPackageManager();

		Intent chooser = Intent.createChooser(second, title);

		List<ResolveInfo> resInfo = pm.queryIntentActivities(first, 0);
		Intent[] extraIntents = new Intent[resInfo.size()];
		for (int i = 0; i < resInfo.size(); i++) {
			ResolveInfo ri = resInfo.get(i);
			String packageName = ri.activityInfo.packageName;
			Intent intent = new Intent();
			intent.setComponent(new ComponentName(packageName,
					ri.activityInfo.name));
			intent.setAction(first.getAction());
			intent.setData(first.getData());
			extraIntents[i] = intent;
		}

		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);

		return chooser;
	}
}
