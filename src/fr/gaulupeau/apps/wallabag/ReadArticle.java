package fr.gaulupeau.apps.wallabag;

import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARCHIVE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_AUTHOR;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_ID;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TABLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_TITLE;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.ARTICLE_URL;
import static fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper.MY_ID;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import fr.gaulupeau.apps.InThePoche.R;

public class ReadArticle extends SherlockActivity {
	TextView txtTitre;
	TextView txtContent;
	TextView txtAuthor;
//	Button btnMarkRead;
	SQLiteDatabase database;
	String id = "";
	ScrollView view;
	
	private boolean isRead;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.article);
		view = (ScrollView) findViewById(R.id.scroll);
		ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(
				getApplicationContext());
		database = helper.getWritableDatabase();
		String[] getStrColumns = new String[] { ARTICLE_URL, MY_ID,
				ARTICLE_TITLE, ARTICLE_CONTENT, ARCHIVE, ARTICLE_AUTHOR };
		Bundle data = getIntent().getExtras();
		if (data != null) {
			id = data.getString("id");
		}
		Cursor ac = database.query(ARTICLE_TABLE, getStrColumns, MY_ID + "="
				+ id, null, null, null, null);
		ac.moveToFirst();
		txtTitre = (TextView) findViewById(R.id.txtTitre);
		txtTitre.setText(ac.getString(2));
		txtContent = (TextView) findViewById(R.id.txtContent);
		txtContent.setText(ac.getString(3));

		txtAuthor = (TextView) findViewById(R.id.txtAuthor);
		txtAuthor.setText(ac.getString(0));
//		btnMarkRead = (Button) findViewById(R.id.btnMarkRead);
		// btnMarkRead.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// ContentValues values = new ContentValues();
		// values.put(ARCHIVE, 1);
		// database.update(ARTICLE_TABLE, values, MY_ID + "=" + id, null);
		// finish();
		// }
		// });
		findOutIfIsRead();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		ContentValues values = new ContentValues();
		values.put("read_at", view.getScrollY());
		database.update(ARTICLE_TABLE, values, ARTICLE_ID + "=" + id, null);
		System.out.println(view.getScrollY());
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.option_read, menu);
		setStateIcon(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.read:
			toggleMarkAsRead();
			return true;
		case R.id.settings:
			startActivity(new Intent(getBaseContext(), Settings.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		database.close();
	}

	private void setStateIcon(Menu menu){
		MenuItem item = menu.findItem(R.id.read);
		
		if(isRead)
			item.setIcon(R.drawable.ic_action_undo);
		else
			item.setIcon(R.drawable.ic_action_accept);
	}
	
	private void findOutIfIsRead(){
		String query = "SELECT * FROM " + ARTICLE_TABLE + " WHERE " + MY_ID + " = " + id + " AND " + ARCHIVE + " = 1";
		int read = database.rawQuery(query, null).getCount();
		
		isRead = read == 1 ? true : false;
	}
	
	private void toggleMarkAsRead(){
		int value = isRead ? 0 : 1;
		
		ContentValues values = new ContentValues();
		values.put(ARCHIVE, value);
		database.update(ARTICLE_TABLE, values, MY_ID + "=" + id, null);
		
		if(isRead)
			showToast(getString(R.string.marked_as_unread));
		else
			showToast(getString(R.string.marked_as_read));
		finish();
	}

	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(ReadArticle.this, toast, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
