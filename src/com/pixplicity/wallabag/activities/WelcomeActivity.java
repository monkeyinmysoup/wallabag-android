package com.pixplicity.wallabag.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.pixplicity.wallabag.R;
import com.pixplicity.wallabag.adapters.FragmentAdapter;

/**
 * Activity with a viewpager with usage instructions
 * 
 */
public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_base);

		ViewPager pager = (ViewPager) findViewById(R.id.view_pager);

		FragmentAdapter adapter = new FragmentAdapter(getFragmentManager());

		pager.setAdapter(adapter);
	}
}