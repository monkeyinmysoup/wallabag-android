package com.pixplicity.wallabag.adapters;

import com.pixplicity.wallabag.Constants;
import com.pixplicity.wallabag.activities.ListArticlesActivity;

import fr.gaulupeau.apps.wallabag.R;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerListAdapter extends BaseAdapter {
	
	private ListArticlesActivity listArticles;
	private int choosen;

	public DrawerListAdapter(ListArticlesActivity listArticles, int choosen){
		this.listArticles = listArticles;
		this.choosen = choosen;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater;
		if(convertView == null){
			inflater = (LayoutInflater) listArticles.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.drawer_list_item, null);
		}
		
		TextView text = (TextView)convertView.findViewById(R.id.drawer_element_text);
		ImageView image = (ImageView) convertView.findViewById(R.id.drawer_element_image);
		
		switch (position) {
		case Constants.ALL:
			text.setText(listArticles.getString(R.string.all_text));
			image.setImageResource(R.drawable.ic_action_about_dark);
			break;
		case Constants.UNREAD:
			text.setText(listArticles.getString(R.string.unread_text));
			image.setImageResource(R.drawable.ic_action_accounts_dark);
			break;
		case Constants.READ:
			text.setText(listArticles.getString(R.string.read_text));
			image.setImageResource(R.drawable.ic_action_accept_dark);
			break;
		case Constants.FAVS:
			text.setText(listArticles.getString(R.string.favorites_text));
			image.setImageResource(R.drawable.ic_action_important_dark);
			break;
		default:
			break;
		}
		
		if(choosen == position)
			text.setTypeface(null, Typeface.BOLD);
		else
			text.setTypeface(null, Typeface.NORMAL);
		
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(choosen != position){
					choosen = position;
					notifyDataSetChanged();
					listArticles.setListFilterOption(position);
					listArticles.setupList();
				}
				listArticles.closeDrawer();
			}
		});
		
		return convertView;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public Object getItem(int position) {
		return null;
	}
	
	@Override
	public int getCount() {
		return 4;
	}

}
