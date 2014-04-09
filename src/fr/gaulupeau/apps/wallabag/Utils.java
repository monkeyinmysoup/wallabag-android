package fr.gaulupeau.apps.wallabag;

import java.io.File;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

public class Utils {

	public static final int RESULT_CHANGE_THEME = 42;

	public static final boolean isDarkTheme(int themeId) {
		return themeId == R.style.AppThemeBlack;
	}

	public static final void setActionBarIcon(ActionBar actionBar, int themeId) {
		if (isDarkTheme(themeId))
			actionBar.setLogo(R.drawable.actionbar_dark);
		else
			actionBar.setLogo(R.drawable.actionbar);
	}

	@SuppressLint("NewApi")
	public static final void restartActivity(final Activity activity) {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					activity.recreate();
				else {
					Intent intent = activity.getIntent();
					activity.finish();
					activity.startActivity(intent);
				}
			}
		}, 1);

	}

	public static final File getSaveDir(SherlockActivity activity) {
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
}
