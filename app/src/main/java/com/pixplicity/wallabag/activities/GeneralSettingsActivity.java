package com.pixplicity.wallabag.activities;

import com.pixplicity.easyprefs.library.Prefs;
import com.pixplicity.wallabag.Constants;
import com.pixplicity.wallabag.R;
import com.pixplicity.wallabag.Utils;
import com.pixplicity.wallabag.db.CupboardSQLiteOpenHelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import java.io.File;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class GeneralSettingsActivity extends AbstractSettingsActivity {

    private static final int[] sortTypeOptions = new int[]{
            R.string.newer,
            R.string.older,
            R.string.alphabetical};
    private static final int[] refreshOptions = new int[]{
            R.string.refresh_manual,
            R.string.refresh_wifi,
            R.string.refresh_always};

    private int sortType;
    private boolean willAlsoDeleteUserAccount;
    private int autoRefresh;

    @Override
    protected void saveSettings() {
        Prefs.putInt(Constants.PREFS_SORT_TYPE, sortType);
        Prefs.putInt(Constants.PREFS_KEY_AUTO_REFRESH, autoRefresh);
    }

    @Override
    protected void getSettings() {
        super.getSettings();
        sortType = Prefs.getInt(Constants.PREFS_SORT_TYPE, 0);
        autoRefresh = Prefs.getInt(Constants.PREFS_KEY_AUTO_REFRESH, Constants.DEFAULT_AUTO_REFRESH);
    }

    @Override
    protected void createUI() {
        // Sort
        View sortLayout = findViewById(R.id.sort_layout);
        final TextView sortTypeView = (TextView) sortLayout.findViewById(R.id.sort_text);
        sortTypeView.setText(getStringSortType(sortType));
        sortLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GeneralSettingsActivity.this);
                String[] choices = new String[]{getStringSortType(0), getStringSortType(1),
                        getStringSortType(2)};
                builder.setSingleChoiceItems(choices, sortType,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (sortType != which) {
                                    setResult(Constants.RESULT_LIST_SHOULD_CHANGE);
                                }

                                sortType = which;
                                sortTypeView.setText(getStringSortType(which));
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        // Auto-refresh
        View refreshLayout = findViewById(R.id.auto_refresh_layout);
        final TextView refreshView = (TextView) refreshLayout.findViewById(R.id.auto_refresh_text);
        refreshView.setText(getStringRefresh(autoRefresh));
        refreshLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GeneralSettingsActivity.this);
                String[] choices = new String[]{
                        getStringRefresh(0),
                        getStringRefresh(1),
                        getStringRefresh(2)};
                builder.setSingleChoiceItems(choices, autoRefresh,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                autoRefresh = which;
                                refreshView.setText(getStringRefresh(which));
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        // Wipe database
        View wipeDatabaseLayout = findViewById(R.id.wipe_database_layout);
        wipeDatabaseLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                wipeDB();
            }
        });
    }


    private void wipeDB() {
        willAlsoDeleteUserAccount = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.wipe_data_base));
        builder.setMessage(getString(R.string.sure));

        View checkBoxView = View.inflate(getBaseContext(),
                R.layout.dialog_whipe_db, null);
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


                        CupboardSQLiteOpenHelper helper = new CupboardSQLiteOpenHelper(GeneralSettingsActivity.this);
                        SQLiteDatabase database = helper.getWritableDatabase();
                        cupboard().withDatabase(database).dropAllTables();
                        cupboard().withDatabase(database).createTables();
                        deleteFiles();
                        if (willAlsoDeleteUserAccount) {
                            cleanUserInfo();
                        }
                        database.close();
                        setResult(Constants.RESULT_LIST_SHOULD_CHANGE);
                        dialog.dismiss();
                    }

                }
        );

        builder.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void cleanUserInfo() {
        SharedPreferences.Editor editor = Prefs.getPreferences().edit();
        editor.putString(Constants.PREFS_KEY_WALLABAG_URL, null)
                .putString(Constants.PREFS_KEY_USER_ID, null)
                .putString(Constants.PREFS_KEY_USER_TOKEN, null)
                .commit();
    }

    protected void deleteFiles() {
        File filesDir = Utils.getSaveDir(this);
        for (File file : filesDir.listFiles()) {
            file.delete();
        }
    }

    private String getStringSortType(int which) {
        return getString(sortTypeOptions[which]);
    }

    private String getStringRefresh(int which) {
        return getString(refreshOptions[which]);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_general_settings;
    }
}

