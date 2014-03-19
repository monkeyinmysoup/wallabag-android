package fr.gaulupeau.apps.settings;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class SettingsOnClickListener implements OnClickListener {

	private Intent intent;
	private Context context;
	
	public SettingsOnClickListener(Context context, Intent intent){
		this.intent = intent;
		this.context = context;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		context.startActivity(intent);
	}

}
