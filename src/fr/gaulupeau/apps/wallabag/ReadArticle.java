package fr.gaulupeau.apps.wallabag;

import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARCHIVE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_AUTHOR;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_ID;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TABLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TITLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_URL;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.MY_ID;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.FAV;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.actionbarsherlock.view.Window;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import fr.gaulupeau.apps.InThePoche.R;

public class ReadArticle extends SherlockActivity {
	TextView txtTitre;
	TextView txtContent;
	TextView txtAuthor;
	// Button btnMarkRead;
	SQLiteDatabase database;
	String id = "";
	MyScrollView view;

	private String articleUrl;
	private Menu menu;
	private ActionBar actionBar;
	private boolean isRead;
	private boolean isFav;

	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);
		
		goImmersive();
		
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
		txtContent = (TextView) findViewById(R.id.txtContent);
		txtContent.setText(ac.getString(3));

		txtAuthor = (TextView) findViewById(R.id.txtAuthor);
		txtAuthor.setText(ac.getString(0));
		articleUrl = ac.getString(0);

		// btnMarkRead = (Button) findViewById(R.id.btnMarkRead);
		// btnMarkRead.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// ContentValues values = new ContentValues();
		// values.put(ARCHIVE, 1);
		// database.update(ARTICLE_TABLE, values, MY_ID + "=" + id, null);
		// finish();
		// }
		// });
		findOutIfIsRead(ac.getInt(4));
		findOutIfIsFav(ac.getInt(6));
		
		view.setOnScrollViewListener(new OnViewScrollListener() {
			private int goingDown, goingUp;
			@Override
			public void onScrollChanged(int x, int y, int oldx, int oldy) {
				
				if(actionBar.isShowing() && goingDown > 100)
					actionBar.hide();
	
				if(!actionBar.isShowing() && goingUp > 100)
					actionBar.show();
				
				if(y > oldy){
					goingDown += y - oldy;
					goingUp = 0;
				}
				
				if(y < oldy){
					goingUp += oldy - y;
					goingDown = 0;
				}
				
			}
		});
	}

	@Override
	public void onUserInteraction() {
        super.onUserInteraction();
        goImmersive();
    }
	
	@SuppressLint("NewApi")
	private void goImmersive() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			getWindow().getDecorView().setSystemUiVisibility(
			          View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
			          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			          | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
			          | View.SYSTEM_UI_FLAG_FULLSCREEN
			          | View.SYSTEM_UI_FLAG_IMMERSIVE);
		}
		else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
		
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
			startActivity(new Intent(getBaseContext(), Settings.class));
			return true;
		case R.id.share:
			shareUrl();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void shareUrl() {
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		Intent sendIntent = new Intent(Intent.ACTION_SEND);

		viewIntent.setData(Uri.parse(articleUrl));

		sendIntent.setType("text/plain");
		sendIntent.putExtra(Intent.EXTRA_TEXT, articleUrl);

		Intent intentChooser = createIntentChooserForTwoIntents(viewIntent, sendIntent, getString(R.string.share_title));

		startActivity(intentChooser);
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

	private Intent createIntentChooserForTwoIntents(Intent first, Intent second, String title) {

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
