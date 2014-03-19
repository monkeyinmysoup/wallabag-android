package fr.gaulupeau.apps.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.gaulupeau.apps.InThePoche.R;

public class SettingsAdapter extends BaseAdapter {
	private Context context;

	public SettingsAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return 4;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {	
			convertView = inflater.inflate(R.layout.settings_element, null);
		}
		
		ImageView imageView = (ImageView) convertView.findViewById(R.id.settings_element_image);
		TextView textView = (TextView) convertView.findViewById(R.id.settings_element_text);
		
		switch (position) {
		case 0:
			imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_settings));
			textView.setText(context.getString(R.string.general));
			
			Intent optionIntent =  new Intent(context, SettingsAccount.class);
			OnClickListener listener = new SettingsOnClickListener(context,optionIntent);
			convertView.setOnClickListener(listener);
			break;
		case 1:
			imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_brightness_medium));
			textView.setText(context.getString(R.string.look_and_feel));
			break;
		case 2:
			imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_accounts));
			textView.setText(context.getString(R.string.account));
			break;
		case 3:
			imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_about));
			textView.setText(context.getString(R.string.about));
		default:
			break;
		}
		
		return convertView;
	}

}
