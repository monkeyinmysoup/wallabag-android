package com.pixplicity.wallabag.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixplicity.wallabag.R;
import com.pixplicity.wallabag.models.ListItem;

import java.util.List;

/**
 * Adapter for showing {@link com.pixplicity.wallabag.models.ListItem} elements in an AdapterView.
 * Layout {@code li_simple} is used for displaying a ListItem.
 */
public class SimpleListAdapter extends BaseAdapter {

    private final Context mContext;
    protected List<ListItem> mData;

    public SimpleListAdapter(Context context, List<ListItem> items) {
        mContext = context;
        mData = items;
    }

    protected Context getContext() {
        return mContext;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ListItem.Holder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.li_simple, parent, false);
            viewHolder = new ListItem.Holder();
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.tv_subtitle);
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.root = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ListItem.Holder) convertView.getTag();
        }

        putItemInView(position, viewHolder);
        return convertView;
    }

    protected void putItemInView(int position, ListItem.Holder viewHolder) {
        ListItem item = mData.get(position);
        if (item != null) {
            // Title
            viewHolder.tvTitle.setText(item.mTitle);
            // Subtitle (if any)
            if (item.mSubtitle > 0) {
                viewHolder.tvSubtitle.setText(item.mSubtitle);
                viewHolder.tvSubtitle.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tvSubtitle.setVisibility(View.GONE);
            }
            // Icon (if any)
            if (item.mIcon > 0) {
                viewHolder.ivIcon.setImageResource(item.mIcon);
                viewHolder.ivIcon.setVisibility(View.VISIBLE);
            } else {
                viewHolder.ivIcon.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).mId;
    }

    @Override
    public ListItem getItem(int position) {
        return mData.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }
}
