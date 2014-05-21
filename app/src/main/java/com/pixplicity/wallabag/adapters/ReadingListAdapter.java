package com.pixplicity.wallabag.adapters;

import com.pixplicity.wallabag.R;
import com.pixplicity.wallabag.models.Article;
import com.pixplicity.wallabag.models.ListItem;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class ReadingListAdapter extends BaseAdapter {

    private Context mContext;

    private List<Article> listArticles;

    private int imgSize;

    public ReadingListAdapter(Context context) {
        this.mContext = context;
        this.imgSize = context.getResources().getDimensionPixelSize(R.dimen.li_article_image_size);
    }

    public void setListArticles(List<Article> articlesList) {
        this.listArticles = articlesList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (listArticles != null) {
            return listArticles.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return listArticles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.li_article, parent, false);
            viewHolder = new Holder();
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.listitem_title);
            viewHolder.tvSubtitle = (TextView) convertView.findViewById(R.id.listitem_description);
            viewHolder.tvDomain = (TextView) convertView.findViewById(R.id.listitem_domain);
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_image);
            viewHolder.root = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }

        Article entry = listArticles.get(position);
        viewHolder.tvTitle.setText(entry.title);
        String description = entry.summary;
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
        String domain = entry.domain;
        viewHolder.tvDomain.setText(domain);
        return convertView;
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
