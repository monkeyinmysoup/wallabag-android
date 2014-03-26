package fr.gaulupeau.apps.settings;

import fr.gaulupeau.apps.wallabag.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Settings extends SettingsBase {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// list.setAdapter(new SettingsAdapter(this));
	}

	@Override
	protected void createUI(ListView list, GeneralPurposeListViewAdapter adapter,
			LayoutInflater inflater) {
		int[] images = new int[] { R.drawable.ic_action_settings,
				R.drawable.ic_action_brightness_medium,
				R.drawable.ic_action_accounts, R.drawable.ic_action_about };

		int[] strings = new int[] { R.string.general, R.string.look_and_feel,
				R.string.account, R.string.about };

		Class<?>[] activities = new Class[] { SettingsAccount.class,
				SettingsLookAndFeel.class, SettingsAccount.class,
				SettingsAccount.class };

		for (int i = 0; i < 4; i++) {
			Intent optionIntent;
			OnClickListener listener;

			View view = inflater.inflate(R.layout.settings_element, null);

			ImageView imageView = (ImageView) view
					.findViewById(R.id.settings_element_image);
			TextView textView = (TextView) view
					.findViewById(R.id.settings_element_text);

			imageView.setImageDrawable(getResources().getDrawable(images[i]));
			textView.setText(getString(strings[i]));

			optionIntent = new Intent(this, activities[i]);
			listener = new SettingsOnClickListener(this, optionIntent);
			view.setOnClickListener(listener);
			adapter.addView(view);

		}

		list.setAdapter(adapter);
	}

	@Override
	protected void saveSettings() {
	}
}
