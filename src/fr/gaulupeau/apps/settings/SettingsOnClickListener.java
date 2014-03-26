package fr.gaulupeau.apps.settings;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class SettingsOnClickListener implements OnClickListener {

	private Intent intent;
	private Activity activity;
	
	public SettingsOnClickListener(Activity activity, Intent intent){
		this.intent = intent;
		this.activity = activity;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		activity.startActivityForResult(intent, activity.hashCode());
	}

}
