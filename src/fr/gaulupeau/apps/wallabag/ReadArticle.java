package fr.gaulupeau.apps.wallabag;

import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARCHIVE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_AUTHOR;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_ID;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TABLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TITLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_URL;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.FAV;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.MY_ID;
import static fr.gaulupeau.apps.wallabag.Helpers.PREFS_NAME;

import java.util.List;

import android.annotation.SuppressLint;
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
import android.util.TypedValue;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import fr.gaulupeau.apps.settings.SettingsLookAndFeel;

public class ReadArticle extends SherlockActivity {
	private TextView txtTitre;
	private TextView txtAuthor;
	private SQLiteDatabase database;
	private String id = "";
	private MyScrollView view;
	private WebView contentWebView;
	private SharedPreferences preferences;

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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		preferences = getSharedPreferences(PREFS_NAME, 0);
		getSettings();
		setTheme(themeId);
		
		setContentView(R.layout.article);

		actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		view = (MyScrollView) findViewById(R.id.scroll);
		ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(
				getApplicationContext());
		database = helper.getWritableDatabase();
		String[] getStrColumns = new String[] { ARTICLE_URL, MY_ID,
				ARTICLE_TITLE, ARTICLE_CONTENT, ARCHIVE, ARTICLE_AUTHOR, FAV };
		Bundle data = getIntent().getExtras();
		if (data != null) {
			id = data.getString("id");
		}
		Cursor ac = database.query(ARTICLE_TABLE, getStrColumns, MY_ID + "="
				+ id, null, null, null, null);
		ac.moveToFirst();
		txtTitre = (TextView) findViewById(R.id.txtTitre);
		txtTitre.setText(ac.getString(2));

		contentWebView = (WebView) findViewById(R.id.webContent);

		articleContent = ac.getString(3);

		txtAuthor = (TextView) findViewById(R.id.txtAuthor);
		txtAuthor.setText(ac.getString(0));
		articleUrl = ac.getString(0);

		findOutIfIsRead(ac.getInt(4));
		findOutIfIsFav(ac.getInt(6));

		ac.close();
		view.setOnScrollViewListener(new OnViewScrollListener() {
			private int goingDown, goingUp;

			@Override
			public void onScrollChanged(int x, int y, int oldx, int oldy) {

				if (actionBar.isShowing() && goingDown > 100)
					actionBar.hide();

				if (!actionBar.isShowing() && goingUp > 100)
					actionBar.show();

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

	private void loadDataToWebView() {
		contentWebView.loadDataWithBaseURL(null, Style.getHead(fontStyle, textAlign, fontSize, Utils.isDarkTheme(themeId))
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

	@SuppressLint("NewApi")
	private void goImmersive() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			if (canGoImmersive) {
				getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LOW_PROFILE);
			}
			else
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		ContentValues values = new ContentValues();
		values.put("read_at", view.getScrollY());
		database.update(ARTICLE_TABLE, values, ARTICLE_ID + "=" + id, null);
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.option_read, menu);
		this.menu = menu;
		setReadStateIcon();
		setFavStateIcon();
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
					SettingsLookAndFeel.class), this.hashCode());
			return true;
		case R.id.share:
			shareUrl();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void getSettings() {
		fontSize = preferences.getInt(SettingsLookAndFeel.FONT_SIZE, 16);
		//webSettings.setDefaultFontSize(fontSize);

		canGoImmersive = preferences.getBoolean(SettingsLookAndFeel.IMMERSIVE, true);
		
		keepScreenOn = preferences.getBoolean(SettingsLookAndFeel.KEEP_SCREEN_ON, false);
		
		fontStyle = preferences.getInt(SettingsLookAndFeel.FONT_STYLE, 0);

		textAlign = preferences.getInt(SettingsLookAndFeel.ALIGN, 0);
		
		int screenOrientation = preferences.getInt(
				SettingsLookAndFeel.ORIENTATION, 0);

		switch (screenOrientation) {
		case SettingsLookAndFeel.PORTRAIT:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
			break;
		case SettingsLookAndFeel.LANDSCAPE:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			break;
		case SettingsLookAndFeel.DYMAMIC:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
			break;
		default:
			break;
		}
		
		themeId = preferences.getInt(SettingsLookAndFeel.DARK_THEME, R.style.AppThemeWhite);
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

	private void setReadStateIcon() {
		MenuItem item = menu.findItem(R.id.read);

		if (isRead) {
			item.setIcon(R.drawable.ic_action_undo);
			item.setTitle(getString(R.string.unread_title));
		} else {
			item.setIcon(R.drawable.ic_action_accept);
			item.setTitle(getString(R.string.read_title));
		}
	}

	private void setFavStateIcon() {
		MenuItem item = menu.findItem(R.id.fav);

		if (isFav)
			item.setIcon(R.drawable.ic_action_important);
		else
			item.setIcon(R.drawable.ic_action_not_important);
	}

	private void findOutIfIsRead(int read) {

		isRead = read == 1 ? true : false;
	}

	private void findOutIfIsFav(int fav) {

		isFav = fav == 1 ? true : false;
	}

	private void toggleMarkAsRead() {
		int value = isRead ? 0 : 1;

		ContentValues values = new ContentValues();
		values.put(ARCHIVE, value);
		database.update(ARTICLE_TABLE, values, MY_ID + "=" + id, null);

		if (isRead) {
			showToast(getString(R.string.marked_as_unread));
			isRead = false;
		} else {
			showToast(getString(R.string.marked_as_read));
			finish();
		}
		setReadStateIcon();
	}

	private void toggleFav() {
		int value = isFav ? 0 : 1;

		ContentValues values = new ContentValues();
		values.put(FAV, value);
		database.update(ARTICLE_TABLE, values, MY_ID + "=" + id, null);

		if (isFav) {
			showToast(getString(R.string.marked_as_not_fav));
			isFav = false;
		} else {
			showToast(getString(R.string.marked_as_fav));
			isFav = true;
		}
		setFavStateIcon();
	}

	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(ReadArticle.this, toast, Toast.LENGTH_SHORT)
						.show();
			}
		});
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
