package fr.gaulupeau.apps.settings;

import static fr.gaulupeau.apps.wallabag.Helpers.PREFS_NAME;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import fr.gaulupeau.apps.wallabag.R;

public class SettingsAccount extends SherlockActivity {
//	Button btnDone;
	EditText editPocheUrl;
	EditText editAPIUsername;
	EditText editAPIToken;
	EditText editGlobalToken;

	SharedPreferences settings;
	SharedPreferences.Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().setHomeButtonEnabled(true);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    settings = getSharedPreferences(PREFS_NAME, 0);
	    editor = settings.edit();
	        
		setContentView(R.layout.settings_account);
        //SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String pocheUrl = settings.getString("pocheUrl", "https://");
        String apiUsername = settings.getString("APIUsername", "");
        String apiToken = settings.getString("APIToken", "");
    	editPocheUrl = (EditText)findViewById(R.id.pocheUrl);
    	editPocheUrl.setText(pocheUrl);
    	editAPIUsername = (EditText)findViewById(R.id.APIUsername);
    	editAPIUsername.setText(apiUsername);
    	editAPIToken = (EditText)findViewById(R.id.APIToken);
    	editAPIToken.setText(apiToken);
//        btnDone = (Button)findViewById(R.id.btnDone);
//        btnDone.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//	        	SharedPreferences.Editor editor = settings.edit();
//	        	editor.putString("pocheUrl", editPocheUrl.getText().toString());
//	        	editor.putString("APIUsername", editAPIUsername.getText().toString());
//	        	editor.putString("APIToken", editAPIToken.getText().toString());
//				editor.commit();
//				finish();
//			}
//        });
	}
	
	@Override
	protected void onPause() {
		super.onPause();		
    	editor.putString("pocheUrl", editPocheUrl.getText().toString());
    	editor.putString("APIUsername", editAPIUsername.getText().toString());
    	editor.putString("APIToken", editAPIToken.getText().toString());
		editor.commit();
		
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	      case android.R.id.home:
	        finish();
	    
	      default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
