package com.pixplicity.wallabag.activities;

import static com.pixplicity.wallabag.Helpers.PREFS_NAME;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.pixplicity.wallabag.Constants;
import com.pixplicity.wallabag.R;
import com.pixplicity.wallabag.Utils;


public abstract class AbstractSettingsActivity extends Activity {

	protected SharedPreferences settings;
	protected ActionBar actionBar;

	protected int themeId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = getSharedPreferences(PREFS_NAME, 0);
		getSettings();

		setTheme(themeId);

		actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		Utils.setActionBarIcon(actionBar, themeId);

		setContentView(getContentView());

		createUI();
	}

	abstract protected int getContentView();

	@Override
	protected void onPause() {
		saveSettings();
		super.onPause();
	}

	protected void getSettings() {
		themeId = settings.getInt(LookAndFeelSettingsActivity.DARK_THEME, R.style.Theme_Wallabag);
	}

	abstract protected void saveSettings();

	abstract protected void createUI();

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		System.out.println("Result: " + resultCode);
		if (resultCode == Utils.RESULT_CHANGE_THEME) {
			getSettings();
			Utils.restartActivity(this);
		}
		if (requestCode == Constants.REQUEST_SETTINGS) {
			setResult(resultCode);
		}
	}
}
