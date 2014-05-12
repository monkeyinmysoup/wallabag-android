package fr.gaulupeau.apps.wallabag;

import android.app.Fragment;
import android.app.FragmentManager;

public class MyPagerAdapter extends FragmentPagerAdapter {

	public MyPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return new MyFragment();
	}

	@Override
	public int getCount() {
		return 4;
	}
	
}