package fr.gaulupeau.apps.wallabag;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DrawerListAdapter extends BaseAdapter {
	
	private Context context;
	private int choosen;

	public DrawerListAdapter(Context context, int choosen){
		this.context = context;
		this.choosen = choosen;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater;
		if(convertView == null){
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.drawer_list_item, null);
		}
		
		TextView text = (TextView)convertView.findViewById(R.id.drawer_element_text);
		
		switch (position) {
		case 0:
			text.setText("One");
			break;
		case 1:
			text.setText("Two");
			break;
		case 2:
			text.setText("Three");
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
				choosen = position;
				notifyDataSetChanged();
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
		return 3;
	}

}
