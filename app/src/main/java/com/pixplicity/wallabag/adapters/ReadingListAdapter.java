package com.pixplicity.wallabag.adapters;

import com.pixplicity.wallabag.R;
import com.pixplicity.wallabag.models.Article;
import com.pixplicity.wallabag.models.ListItem;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;


public class ReadingListAdapter extends CursorAdapter {

    private Context mContext;
    private int imgSize;
    private LayoutInflater mInflater;

    public ReadingListAdapter(Context context) {
        super(context, null, 0);
        this.mContext = context;
        this.imgSize = context.getResources().getDimensionPixelSize(R.dimen.li_article_image_size);
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Article getItem(int position) {
        getCursor().moveToPosition(position);
        return cupboard().withCursor(getCursor()).get(Article.class);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View convertView = mInflater.inflate(R.layout.li_article, parent, false);
        Holder viewHolder = new Holder();
        viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.listitem_title);
        viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.listitem_description);
        viewHolder.tvDomain = (TextView) convertView.findViewById(R.id.listitem_domain);
        viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_image);
        viewHolder.root = convertView;
        convertView.setTag(viewHolder);
        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final Holder viewHolder = (Holder) view.getTag();
        final Article entry = cupboard().withCursor(cursor).get(Article.class);
        viewHolder.tvTitle.setText(entry.mTitle);
        String description = entry.mSummary;
        viewHolder.tvSubtitle.setText(description);
        if (entry.hasImage()) {
            Picasso.with(mContext)
                    .load(entry.getImageUrl())
                            //.placeholder(R.drawable.placeholder)
                            //.error(R.drawable.placeholder)
                    .resize(imgSize, imgSize)
                    .centerCrop()
                    .into(viewHolder.ivIcon, new Callback() {
                        @Override
                        public void onSuccess() {
                            viewHolder.ivIcon.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onError() { }
                    });
        } else {
            viewHolder.ivIcon.setVisibility(View.GONE);
        }
        String domain = entry.mDomain;
        viewHolder.tvDomain.setText(domain);
    }

    /**
     * View holder for caching the associated ui elements
     */
    public static class Holder {
        public View root;
        public TextView tvTitle;
        public TextView tvSubtitle;
        public TextView tvDomain;
        public ImageView ivIcon;
    }
}
