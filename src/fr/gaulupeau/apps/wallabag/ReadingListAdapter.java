package fr.gaulupeau.apps.wallabag;

import java.util.List;

import fr.gaulupeau.apps.wallabag.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ReadingListAdapter extends BaseAdapter {
	private Context context;
	private List<Article> listArticles;

	public ReadingListAdapter(Context context, List<Article> listArticles) {
		this.context = context;
		this.listArticles = listArticles;
	}

	public int getCount() {
		return listArticles.size();
	}

	public Object getItem(int position) {
		return listArticles.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// Article entry = listArticles.get(position);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.article_list, null);
		}
		TextView tvTitle = (TextView) convertView
				.findViewById(R.id.listitem_title);
		TextView tvDescription = (TextView) convertView
				.findViewById(R.id.listitem_description);
		// Log.e("title", entry.title);
		Article entry = listArticles.get(position);

		tvTitle.setText(entry.title);

		tvDescription.setText(entry.content.subSequence(0, 100));

		return convertView;
	}

}
