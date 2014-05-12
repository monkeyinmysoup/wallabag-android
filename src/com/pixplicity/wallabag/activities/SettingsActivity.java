package com.pixplicity.wallabag.activities;


import com.pixplicity.wallabag.Utils;
import com.pixplicity.wallabag.ui.SettingsOnClickListener;

import fr.gaulupeau.apps.wallabag.R;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SettingsActivity extends AbstractSettingsActivity {

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
	
	private static final int[] texts_ids = new int[] {
			R.id.general_text,
			R.id.look_and_feel_text,
			R.id.account_text,
			R.id.about_text };
	

	private static final Class<?>[] activities = new Class[] {
			GeneralSettingsActivity.class, LookAndFeelSettingsActivity.class,
			AccountSettingsActivity.class, AccountSettingsActivity.class };

	@Override
	protected void createUI() {

		int[] imagesArray = Utils.isDarkTheme(themeId) ? images_dark : images;
		
		for (int i = 0; i < 4; i++) {
			Intent optionIntent;
			OnClickListener listener;

			TextView textView = (TextView) findViewById(texts_ids[i]);

			Drawable drawble = getResources().getDrawable(
					imagesArray[i]);
			
			textView.setCompoundDrawablesWithIntrinsicBounds(drawble, null, null, null);

			optionIntent = new Intent(this, activities[i]);
			listener = new SettingsOnClickListener(this, optionIntent);
			textView.setOnClickListener(listener);
		}
	}

	@Override
	protected void saveSettings() {
	}

	@Override
	protected int getContentView() {
		return R.layout.settings;
	}
}
