package fr.gaulupeau.apps.settings;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import fr.gaulupeau.apps.wallabag.R;

public abstract class SettingsBase extends SherlockActivity{
	protected ListView list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		getSupportActionBar().setHomeButtonEnabled(true);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    setContentView(R.layout.settings);
	    
	    list = (ListView) findViewById(R.id.settingsList);
	    
	    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ListViewAdapter adapter = new ListViewAdapter();

		createUI(list, adapter, inflater);
	}
	
	abstract protected void createUI(ListView list, ListViewAdapter adapter, LayoutInflater inflater);
	
	protected View inflateIfNotNull(View convertView, int layoutId){
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {	
			convertView = inflater.inflate(layoutId, null);
		}
		
		return convertView;
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
