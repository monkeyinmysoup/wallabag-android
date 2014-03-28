package fr.gaulupeau.apps.wallabag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

public class Utils {
	
	public static final int RESULT_CHANGE_THEME = 42;
	
	public static final boolean isDarkTheme(int themeId){
		return themeId == R.style.AppThemeBlack;
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
}
