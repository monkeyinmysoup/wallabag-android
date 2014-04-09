package fr.gaulupeau.apps.settings;

import fr.gaulupeau.apps.wallabag.R;
import fr.gaulupeau.apps.wallabag.Utils;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class Settings extends SettingsBase {

	private static final int[] images = new int[] {
			R.drawable.ic_action_settings,
			R.drawable.ic_action_brightness_medium,
			R.drawable.ic_action_accounts,
			R.drawable.ic_action_about };

	private static final int[] images_dark = new int[] {
			R.drawable.ic_action_settings_dark,
			R.drawable.ic_action_brightness_medium_dark,
			R.drawable.ic_action_accounts_dark,
			R.drawable.ic_action_about_dark };
	
	private static final int[] layouts_ids = new int[] {
			R.id.general_layout,
			R.id.look_and_feel_layout,
			R.id.account_layout,
			R.id.about_layout };
	
	private static final int [] images_resources_id = new int [] {
			R.id.image_general,
			R.id.image_look_and_feel,
			R.id.image_account,
			R.id.image_about };

	private static final Class<?>[] activities = new Class[] {
			SettingsGeneral.class, SettingsLookAndFeel.class,
			SettingsAccount.class, SettingsAccount.class };

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);

		createUI();
	}

	@Override
	protected void createUI() {

		int[] imagesArray = Utils.isDarkTheme(themeId) ? images_dark : images;
		
		for (int i = 0; i < 4; i++) {
			Intent optionIntent;
			OnClickListener listener;

			View view = findViewById(layouts_ids[i]);

			ImageView imageView = (ImageView) findViewById(images_resources_id[i]);

				imageView.setImageDrawable(getResources().getDrawable(
						imagesArray[i]));

			optionIntent = new Intent(this, activities[i]);
			listener = new SettingsOnClickListener(this, optionIntent);
			view.setOnClickListener(listener);
		}
	}

	@Override
	protected void saveSettings() {
	}
}
