package fr.gaulupeau.apps.settings;

import static fr.gaulupeau.apps.wallabag.Helpers.PREFS_NAME;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import fr.gaulupeau.apps.wallabag.ArticlesSQLiteOpenHelper;
import fr.gaulupeau.apps.wallabag.R;

public class SettingsGeneral extends SettingsBase {

	private SQLiteDatabase database;
	private static final int[] sortTypeOptions = new int[] {R.string.newer, R.string.older, R.string.alphabetical};
	
	public static final int NEWER = 0;
	public static final int OLDER = 1;
	public static final int ALPHA = 2;
	
	public static final String SORT_TYPE = "SortType";
	
	private int sortType;
	private boolean willAlsoDeleteUserAccount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupDB();
	}
	
	@Override
	protected void saveSettings() {
		Editor editor = settings.edit();
		
		editor.putInt(SORT_TYPE, sortType);
		editor.commit();
	}
	
	@Override
	protected void getSettings(){
		super.getSettings();
		sortType = settings.getInt(SORT_TYPE, 0);
	}

	@Override
	protected void createUI(ListView list,
			GeneralPurposeListViewAdapter adapter, LayoutInflater inflater) {
		
		
		//Sort
		View sortLayout = inflater.inflate(R.layout.general_sort, null);
		final TextView sortTypeView = (TextView) sortLayout.findViewById(R.id.sort_text);
		
		sortTypeView.setText(getStringSortType(sortType));
		
		sortLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsGeneral.this);
				String[] choices = new String[] {getStringSortType(0), getStringSortType(1), getStringSortType(2)};
				builder.setSingleChoiceItems(choices, sortType, new DialogInterface.OnClickListener() {				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sortType = which;
						sortTypeView.setText(getStringSortType(which));
						dialog.dismiss();
					}
				});
				AlertDialog alert = builder.create();
			    alert.show();
			}
		});
		
		
		//Wipe database
		
		View wipeDatabaseLayout = inflater.inflate(R.layout.general_wipe_database, null);
		
		wipeDatabaseLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				wipeDB();				
			}
		});
		
		
		adapter.addView(sortLayout);
		adapter.addView(wipeDatabaseLayout);
		list.setAdapter(adapter);
	}

	
	private void wipeDB() {
		willAlsoDeleteUserAccount = false;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(getString(R.string.wipe_data_base));
		builder.setMessage(getString(R.string.sure));

		View checkBoxView = View.inflate(getBaseContext(),
				R.layout.my_checkbox, null);
		CheckBox checkBox = (CheckBox) checkBoxView
				.findViewById(R.id.checkbox_delete_acoount);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				willAlsoDeleteUserAccount = isChecked;
			}
		});

		builder.setView(checkBoxView);

		builder.setPositiveButton(getString(R.string.yes),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(
								SettingsGeneral.this);
						helper.truncateTables(database);
						deleteFiles();
						if (willAlsoDeleteUserAccount)
							cleanUserInfo();
						dialog.dismiss();
					}

				});

		builder.setNegativeButton(getString(R.string.no),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void cleanUserInfo() {
		SharedPreferences settings;
		SharedPreferences.Editor editor;

		settings = getSharedPreferences(PREFS_NAME, 0);
		editor = settings.edit();

		editor.putString("pocheUrl", "https://");
		editor.putString("APIUsername", "");
		editor.putString("APIToken", "");
		editor.commit();
	}
	
	protected void deleteFiles() {
		File filesDir = new File(Environment.getExternalStorageDirectory()
				+ "/Android/data/" + getApplicationContext().getPackageName()
				+ "/files");

		for (File file : filesDir.listFiles())
			file.delete();
	}
	
	public void setupDB() {
		ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(this);
		database = helper.getWritableDatabase();
	}
	
	private String getStringSortType(int which) {
		return getString(sortTypeOptions[which]);
	}
}
