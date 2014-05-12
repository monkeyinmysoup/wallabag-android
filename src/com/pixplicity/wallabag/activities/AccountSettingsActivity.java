package com.pixplicity.wallabag.activities;


import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.pixplicity.wallabag.R;


public class AccountSettingsActivity extends AbstractSettingsActivity {

	public static final String SERVER_URL = "pocheUrl";
	public static final String USER_ID = "APIUsername";
	public static final String USERNAME = "UserName";
	public static final String TOKEN = "APIToken";
	
	private static final String SERVER_OPTION = "ServerOption";
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
		Editor editor = settings.edit();
		editor.putInt(SERVER_OPTION, selectedSeverOption);
		
		editor.putString(TOKEN, editTextToken.getText().toString());
		
		switch (selectedSeverOption) {
		case SERVER_OPTION_FRAMABAG:
			editor.putString(SERVER_URL, getFramabagUrl());
			editor.putString(USERNAME, editTextUsername.getText().toString());
			editor.putString(USER_ID, "1");
			break;
		case SERVER_OPTION_ANOTHER_SERVER:
			editor.putString(SERVER_URL, editTextUrl.getText().toString().trim());
			editor.putString(USER_ID, editUserID.getText().toString());
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
	protected void getSettings(){
		super.getSettings();
		
		selectedSeverOption = settings.getInt(SERVER_OPTION, SERVER_OPTION_FRAMABAG);
		
		serverUrl = settings.getString(SERVER_URL, "https://");
		token = settings.getString(TOKEN, "");
		username = settings.getString(USERNAME, "");
		userID = settings.getString(USER_ID, "1");
	}

	@Override
	protected void createUI() {
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupServerType);
		//		serverUrlLayout = findViewById(R.id.server_url_layout);
		//		usernameLayout = findViewById(R.id.user_name_layout);
		//		userIDLayout = findViewById(R.id.user_id_layout);
		
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
				if(selectedSeverOption != checkedId){
					selectedSeverOption = checkedId;
					hideUnnecessaryLayout();
				}
			}
		});
		
		radioGroup.check(selectedSeverOption);
		hideUnnecessaryLayout();
	}
	
	private void hideUnnecessaryLayout(){
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
