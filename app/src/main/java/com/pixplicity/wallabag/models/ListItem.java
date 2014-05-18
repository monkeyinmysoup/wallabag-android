package com.pixplicity.wallabag.models;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Basic entry for a list adapter, can contain a title, subtitle and icon.
 * <p/>
 * Use {@link com.pixplicity.wallabag.models.ListItem.Holder} as a view holder in adapters.
 */
public class ListItem {

    public final int mId;
    public final int mTitle;
    public final int mSubtitle;
    public final int mIcon;

    public ListItem(int id, int title) {
        this(id, title, -1, -1);
    }

    public ListItem(int id, int title, int subtitle, int icon) {
        mId = id;
        mTitle = title;
        mSubtitle = subtitle;
        mIcon = icon;
    }

    /**
     * View holder for caching the associated ui elements
     */
    public static class Holder {
        public View root;
        public TextView tvTitle;
        public TextView tvSubtitle;
        public ImageView ivIcon;
    }
}
