package fr.gaulupeau.apps.wallabag;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class Welcome extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_base);
		
		ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
		
		MyPagerAdapter adapter = new MyPagerAdapter(getFragmentManager());
		
		pager.setAdapter(adapter);
		
//		CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.circles);
//		circlePageIndicator.setViewPager(pager);
	}
}