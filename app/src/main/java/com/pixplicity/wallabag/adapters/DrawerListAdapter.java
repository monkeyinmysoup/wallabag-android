package com.pixplicity.wallabag.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.ImageView;

import com.pixplicity.wallabag.Constants;
import com.pixplicity.wallabag.R;
import com.pixplicity.wallabag.models.ListItem;

import java.util.ArrayList;

public class DrawerListAdapter extends SimpleListAdapter {
	
	private int mActive;

    public DrawerListAdapter(Context ctx, int active, int themeId){
        super(ctx, new ArrayList<ListItem>(), themeId, R.layout.li_drawer);
		this.mActive = active;

        //mData.add(new ListItem(Constants.ALL, R.string.drawer_all, -1, R.drawable.drawer_all));
        mData.add(new ListItem(Constants.UNREAD, R.string.drawer_unread, -1, R.drawable.drawer_unread));
        mData.add(new ListItem(Constants.READ, R.string.drawer_read, -1, R.drawable.drawer_archive));
        mData.add(new ListItem(Constants.FAVS, R.string.drawer_favorites, -1, R.drawable.drawer_favorites));
        mData.add(new ListItem(Constants.SETTINGS, R.string.drawer_settings, -1, R.drawable.drawer_options));
	}

    @Override
    protected void putItemInView(int position, ListItem.Holder viewHolder) {
        super.putItemInView(position, viewHolder);
        if(mActive == position) {
            viewHolder.tvTitle.setTypeface(null, Typeface.BOLD);
            if (mThemeId == R.style.Theme_Wallabag_Dark) {
                viewHolder.root.setBackgroundColor(
                        getContext().getResources().getColor(R.color.drawer_active_background_dark));
            } else {
                viewHolder.root.setBackgroundColor(
                        getContext().getResources().getColor(R.color.drawer_active_background));
            }
        } else {
            viewHolder.tvTitle.setTypeface(null, Typeface.NORMAL);
            viewHolder.root.setBackgroundColor(Color.TRANSPARENT);
        }

        ImageView color = (ImageView) viewHolder.root.findViewById(R.id.iv_color);
        switch(position) {
            case Constants.UNREAD:
                color.setImageResource(R.drawable.drawer_bar_unread);
                break;
            case Constants.READ:
                color.setImageResource(R.drawable.drawer_bar_archive);
                break;
            case Constants.FAVS:
                color.setImageResource(R.drawable.drawer_bar_favorites);
                break;
            case Constants.SETTINGS:
                color.setImageResource(R.drawable.drawer_bar_settings);
                break;
//            case Constants.ALL:
//                color.setImageResource(R.drawable.drawer_bar_all);
//                break;
        }
    }

    public int getActivePosition() {
        return mActive;
    }

    public void setActivePosition(int activePosition) {
        this.mActive = activePosition;
    }
}
