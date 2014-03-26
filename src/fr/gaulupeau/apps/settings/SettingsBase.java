package fr.gaulupeau.apps.settings;

import static fr.gaulupeau.apps.wallabag.Helpers.PREFS_NAME;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ListView;
import fr.gaulupeau.apps.wallabag.R;
import fr.gaulupeau.apps.wallabag.Utils;

public abstract class SettingsBase extends SherlockActivity{
	protected SharedPreferences settings;
	
	protected ListView list;
	protected int themeId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = getSharedPreferences(PREFS_NAME, 0);
		getSettings();
		 
		setTheme(themeId);
		
		getSupportActionBar().setHomeButtonEnabled(true);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	    setContentView(R.layout.settings);
	    
	    list = (ListView) findViewById(R.id.settingsList);
	    
	    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		GeneralPurposeListViewAdapter adapter = new GeneralPurposeListViewAdapter();

		createUI(list, adapter, inflater);
	}
	
	@Override
	protected void onPause(){
		saveSettings();
		super.onPause();
	}
	
	protected void getSettings(){
		themeId = settings.getInt(SettingsLookAndFeel.DARK_THEME, R.style.AppThemeWhite);
	}
	abstract protected void saveSettings();

	abstract protected void createUI(ListView list, GeneralPurposeListViewAdapter adapter, LayoutInflater inflater);
	
//	protected View inflateIfNotNull(View convertView, int layoutId){
//		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		if (convertView == null) {	
//			convertView = inflater.inflate(layoutId, null);
//		}
//		
//		return convertView;
//	}
	
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
	}
}
