package com.pixplicity.wallabag.activities;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.pixplicity.wallabag.Constants;
import com.pixplicity.wallabag.R;
import com.pixplicity.wallabag.Utils;
import com.pixplicity.wallabag.adapters.SimpleListAdapter;
import com.pixplicity.wallabag.models.ListItem;

import java.util.ArrayList;

/**
 * Main entry to the settings. Show a list of sub-activities.
 */
public class SettingsActivity extends AbstractSettingsActivity {

    private static final int[] images_light = new int[]{
            R.drawable.ic_action_settings,
            R.drawable.ic_action_brightness_medium,
            R.drawable.ic_action_accounts,
            R.drawable.ic_action_about};

    private static final int[] images_dark = new int[]{
            R.drawable.ic_action_settings_dark,
            R.drawable.ic_action_brightness_medium_dark,
            R.drawable.ic_action_accounts_dark,
            R.drawable.ic_action_about_dark};

    private ArrayList<ListItem> mItems;
    private SimpleListAdapter mAdapter;

    @Override
    protected void createUI() {
        mItems = new ArrayList<>();
        mAdapter = new SimpleListAdapter(this, mItems);
        refreshSettings();

        // App version
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            ((TextView) findViewById(R.id.tv_version)).setText(getString(R.string.version_display, pInfo.versionName));
        } catch (PackageManager.NameNotFoundException ignored) {}

        // List of setting categories
        ListView list = (ListView) findViewById(R.id.lv_settings);
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent;
                if (id == R.id.setting_general) {
                    intent = new Intent(SettingsActivity.this, GeneralSettingsActivity.class);
                } else if (id == R.id.setting_look_and_feel) {
                    intent = new Intent(SettingsActivity.this, LookAndFeelSettingsActivity.class);
                } else if (id == R.id.setting_account) {
                    intent = new Intent(SettingsActivity.this, AccountSettingsActivity.class);
                } else if (id == R.id.setting_about) {
                    return;
                    //intent = new Intent(SettingsActivity.this, AboutActivity.class);
                } else {
                    throw new RuntimeException("Invalid id in list adapter: " + id);
                }
                startActivityForResult(intent, Constants.REQUEST_SETTINGS);
            }
        });
    }

    private void refreshSettings() {
        mItems.clear();
        int[] images = Utils.isDarkTheme(themeId) ? images_dark : images_light;
        mItems.add(new ListItem(R.id.setting_general, R.string.settings_general, -1, images[0]));
        mItems.add(new ListItem(R.id.setting_look_and_feel, R.string.setting_look_and_feel, -1, images[1]));
        mItems.add(new ListItem(R.id.setting_account, R.string.settings_account, -1, images[2]));
        //mItems.add(new ListItem(R.id.setting_about, R.string.settings_about, -1, images[3]));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void saveSettings() {
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_settings;
    }
}
