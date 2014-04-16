package fr.gaulupeau.apps.settings;

import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import fr.gaulupeau.apps.wallabag.R;

public class SettingsAccount extends SettingsBase {

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
	private EditText editTextUrl; 
	private EditText editTextUsername;
	private EditText editTextToken;
	
	private String serverUrl;
	private String username;
	private String token;
	
	@Override
	protected int getContentView() {
		return R.layout.account_settings;
	}

	@Override
	protected void saveSettings() {
		Editor editor = settings.edit();
		editor.putInt(SERVER_OPTION, selectedSeverOption);
		editor.putString(USER_ID, "1");
		editor.putString(TOKEN, editTextToken.getText().toString());
		
		switch (selectedSeverOption) {
		case SERVER_OPTION_FRAMABAG:
			editor.putString(SERVER_URL, getFramabagUrl());
			editor.putString(USERNAME, editTextUsername.getText().toString());
			break;
		case SERVER_OPTION_ANOTHER_SERVER:
			editor.putString(SERVER_URL, editTextUrl.getText().toString().trim());
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
	}

	@Override
	protected void createUI() {
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupServerType);
		serverUrlLayout = findViewById(R.id.server_url_layout);
		usernameLayout = findViewById(R.id.user_name_layout);
		
		editTextUrl = (EditText) findViewById(R.id.editTextServerUrl);
		editTextUsername = (EditText) findViewById(R.id.editTextUsername);
		editTextToken = (EditText) findViewById(R.id.editTextToken);

		editTextToken.setText(token);
		editTextUrl.setText(serverUrl);
		editTextUsername.setText(username);		
		
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
			break;
		case SERVER_OPTION_ANOTHER_SERVER:
			serverUrlLayout.setVisibility(View.VISIBLE);
			usernameLayout.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

}
