package fr.gaulupeau.apps.wallabag;

import static fr.gaulupeau.apps.wallabag.Helpers.PREFS_NAME;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

import fr.gaulupeau.apps.InThePoche.R;

public class Settings extends SherlockActivity {
//	Button btnDone;
	EditText editPocheUrl;
	EditText editAPIUsername;
	EditText editAPIToken;
	EditText editGlobalToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().setHomeButtonEnabled(true);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	        
		setContentView(R.layout.settings);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
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
	
	protected void onDestroy() {
		super.onDestroy();
		//Toast.makeText(this, "foo", Toast.LENGTH_SHORT).show();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString("pocheUrl", editPocheUrl.getText().toString());
    	editor.putString("APIUsername", editAPIUsername.getText().toString());
    	editor.putString("APIToken", editAPIToken.getText().toString());
		editor.commit();
		finish();
		
    }
}
