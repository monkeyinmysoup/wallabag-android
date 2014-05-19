package com.pixplicity.wallabag.adapters;

import com.pixplicity.wallabag.R;
import com.pixplicity.wallabag.models.Article;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class ReadingListAdapter extends BaseAdapter {

    private final Context context;

    private List<Article> listArticles;

    public ReadingListAdapter(Context context, List<Article> listArticles) {
        this(context);
        this.listArticles = listArticles;
    }

    public ReadingListAdapter(Context context) {
        this.context = context;
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
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.li_article, null);
        }
        TextView tvTitle = (TextView) convertView
                .findViewById(R.id.listitem_title);
        TextView tvDescription = (TextView) convertView
                .findViewById(R.id.listitem_description);

        Article entry = listArticles.get(position);
        tvTitle.setText(entry.title);
        String description = entry.summary;
        tvDescription.setText(description);

        return convertView;
    }
}
