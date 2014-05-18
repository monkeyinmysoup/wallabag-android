package com.pixplicity.wallabag.activities;


import com.pixplicity.easyprefs.library.Prefs;
import com.pixplicity.wallabag.Constants;
import com.pixplicity.wallabag.R;

import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 *
 */
public class AccountSettingsActivity extends AbstractSettingsActivity {

    private static final int SERVER_OPTION_FRAMABAG = R.id.radioFramabag;
    private static final int SERVER_OPTION_ANOTHER_SERVER = R.id.radioAnotherServer;

    private int selectedSeverOption;
    private View serverUrlLayout;
    private View usernameLayout;
    private View userIDLayout;
    private EditText editTextUrl;
    private EditText editTextUsername;
    private EditText editUserID;
    private EditText editTextToken;
    private String serverUrl;
    private String username;
    private String token;
    private String userID;

    @Override
    protected int getContentView() {
        return R.layout.account_settings;
    }

    @Override
    protected void saveSettings() {
        Editor editor = Prefs.getPreferences().edit();
        editor.putInt(Constants.PREFS_KEY_SERVER_OPTION, selectedSeverOption);
        editor.putString(Constants.PREFS_KEY_USER_TOKEN, editTextToken.getText().toString());

        switch (selectedSeverOption) {
            case SERVER_OPTION_FRAMABAG:
                editor.putString(Constants.PREFS_KEY_WALLABAG_URL, getFramabagUrl());
                editor.putString(Constants.PREFS_KEY_USER_NAME, editTextUsername.getText().toString());
                editor.putString(Constants.PREFS_KEY_USER_ID, "1");
                break;
            case SERVER_OPTION_ANOTHER_SERVER:
                editor.putString(Constants.PREFS_KEY_WALLABAG_URL, editTextUrl.getText().toString().trim());
                editor.putString(Constants.PREFS_KEY_USER_ID, editUserID.getText().toString());
                break;
            default:
                break;
        }

        editor.commit();
    }

    private String getFramabagUrl() {
        return "https://framabag.org/u/" + editTextUsername.getText().toString().trim();
    }

    @Override
    protected void getSettings() {
        super.getSettings();
        selectedSeverOption = Prefs.getInt(Constants.PREFS_KEY_SERVER_OPTION, SERVER_OPTION_FRAMABAG);
        serverUrl = Prefs.getString(Constants.PREFS_KEY_WALLABAG_URL, "http://");
        token = Prefs.getString(Constants.PREFS_KEY_USER_TOKEN, "");
        username = Prefs.getString(Constants.PREFS_KEY_USER_NAME, "");
        userID = Prefs.getString(Constants.PREFS_KEY_USER_ID, "1");
    }

    @Override
    protected void createUI() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupServerType);
        serverUrlLayout = findViewById(R.id.server_url_layout);
        usernameLayout = findViewById(R.id.user_name_layout);
        userIDLayout = findViewById(R.id.user_id_layout);

        editTextUrl = (EditText) findViewById(R.id.editTextServerUrl);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextToken = (EditText) findViewById(R.id.editTextToken);
        editUserID = (EditText) findViewById(R.id.editTextUserID);

        editTextToken.setText(token);
        editTextUrl.setText(serverUrl);
        editTextUsername.setText(username);
        editUserID.setText(userID);

        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (selectedSeverOption != checkedId) {
                    selectedSeverOption = checkedId;
                    hideUnnecessaryLayout();
                }
            }
        });

        radioGroup.check(selectedSeverOption);
        hideUnnecessaryLayout();
    }

    private void hideUnnecessaryLayout() {
        switch (selectedSeverOption) {
            case SERVER_OPTION_FRAMABAG:
                serverUrlLayout.setVisibility(View.GONE);
                usernameLayout.setVisibility(View.VISIBLE);
                userIDLayout.setVisibility(View.GONE);
                break;
            case SERVER_OPTION_ANOTHER_SERVER:
                serverUrlLayout.setVisibility(View.VISIBLE);
                usernameLayout.setVisibility(View.GONE);
                userIDLayout.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

}
