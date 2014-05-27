package com.pixplicity.wallabag.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.pixplicity.easyprefs.library.Prefs;
import com.pixplicity.wallabag.Constants;
import com.pixplicity.wallabag.R;
import com.pixplicity.wallabag.Utils;

/**
 * Parent class to all settings activities.
 * Sets common UI features such as the ActionBar and provides callbacks to save and load settings.
 */
public abstract class AbstractSettingsActivity extends Activity {

    protected ActionBar actionBar;
    protected int themeId;

    @SuppressLint("AppCompatMethod")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSettings();
        Utils.setTheme(this, false);
        actionBar = getActionBar();
        assert actionBar != null;
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

    /**
     * Default implementation loads the theme from the settings.
     */
    protected void getSettings() {
        themeId = Prefs.getInt(LookAndFeelSettingsActivity.DARK_THEME, R.style.Theme_Wallabag);
    }

    /**
     * Callback indicating changes made by the user should be saved.
     */
    abstract protected void saveSettings();

    abstract protected void createUI();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Utils.RESULT_CHANGE_THEME) {
            getSettings();
            Utils.restartActivity(this);
        }
        if (requestCode == Constants.REQUEST_SETTINGS) {
            setResult(resultCode);
        }
    }
}
