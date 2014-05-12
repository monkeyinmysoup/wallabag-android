package com.pixplicity.wallabag.activities;

import com.pixplicity.wallabag.adapters.FragmentAdapter;

import fr.gaulupeau.apps.wallabag.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class WelcomeActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_base);
		
		ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
		
		FragmentAdapter adapter = new FragmentAdapter(getFragmentManager());
		
		pager.setAdapter(adapter);
		
//		CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.circles);
//		circlePageIndicator.setViewPager(pager);
	}
}