package com.pixplicity.wallabag.adapters;

import com.pixplicity.wallabag.fragments.WelcomeFragment;

import android.app.Fragment;
import android.app.FragmentManager;

public class FragmentAdapter extends FragmentPagerAdapter {

	public FragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return new WelcomeFragment();
	}

	@Override
	public int getCount() {
		return 4;
	}
	
}