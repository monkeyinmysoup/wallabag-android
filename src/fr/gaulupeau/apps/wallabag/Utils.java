package fr.gaulupeau.apps.wallabag;

import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARCHIVE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TITLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.FAV;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.MY_ID;

import java.io.File;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;
import fr.gaulupeau.apps.settings.SettingsGeneral;

public class Utils {

	public static final int RESULT_CHANGE_THEME = 42;

	public static final boolean isDarkTheme(int themeId) {
		return themeId == R.style.AppThemeBlack;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static final void setActionBarIcon(ActionBar actionBar, int themeId) {
		if (isDarkTheme(themeId)) {
			actionBar.setLogo(R.drawable.actionbar_dark);
		} else {
			actionBar.setLogo(R.drawable.actionbar);
		}
	}


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static final void restartActivity(final Activity activity) {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					activity.recreate();
				} else {
					Intent intent = activity.getIntent();
					activity.finish();
					activity.startActivity(intent);
				}
			}
		}, 1);

	}

	public static final File getSaveDir(Activity activity) {
		return new File(Environment.getExternalStorageDirectory()
				+ "/Android/data/"
				+ activity.getApplicationContext().getPackageName() + "/files");
	}

	public static final void showToast(Activity activity, final String msg) {
		showToast(activity, msg, Toast.LENGTH_SHORT);
	}
	
	public static final void showToast(Activity activity, final String msg, int length) {
		Toast.makeText(activity, msg, length).show();
	}
	
	public static final String getFilter(int filterOption){
		switch (filterOption) {
		case Constants.ALL:
			return null;
			
		case Constants.UNREAD:
			return ARCHIVE + " = 0";
			
		case Constants.READ:
			return ARCHIVE + " = 1";
			
		case Constants.FAVS:
			return FAV + " = 1";
			
		default:
			return null;
		}
	}
	
	public static final String getOrderBy(int sortType){
		switch (sortType) {
		case SettingsGeneral.NEWER:
			return MY_ID + " DESC";

		case SettingsGeneral.OLDER:
			return MY_ID;

		case SettingsGeneral.ALPHA:
			return ARTICLE_TITLE + " COLLATE NOCASE";

		default:
			System.out.println(sortType);
			return "";
		}
	}

	public static boolean hasToggledFavorite(int result) {
		return (result & Constants.RESULT_TOGGLE_FAVORITE) == Constants.RESULT_TOGGLE_FAVORITE;
	}

	public static boolean hasToggledRead(int result) {
		return (result & Constants.RESULT_TOGGLE_READ) == Constants.RESULT_TOGGLE_READ;
	}
}
