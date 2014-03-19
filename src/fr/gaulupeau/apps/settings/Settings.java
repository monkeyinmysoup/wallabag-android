package fr.gaulupeau.apps.settings;

import android.os.Bundle;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import fr.gaulupeau.apps.InThePoche.R;

public class Settings extends SherlockActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		getSupportActionBar().setHomeButtonEnabled(true);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    setContentView(R.layout.settings);
	    
	    ListView list = (ListView) findViewById(R.id.settingsList);
	    list.setAdapter(new SettingsAdapter(this));
	}
	
	
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	      case android.R.id.home:
	        finish();
	    
	      default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
