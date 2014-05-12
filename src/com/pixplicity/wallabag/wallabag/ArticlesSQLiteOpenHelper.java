package com.pixplicity.wallabag.wallabag;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static com.pixplicity.wallabag.wallabag.Helpers.PREFS_NAME;
import static com.pixplicity.wallabag.wallabag.Helpers.zeroUpdate;


public class ArticlesSQLiteOpenHelper extends SQLiteOpenHelper {

    public static final int VERSION = 2;
    public static final String DB_NAME = "article_db.sqlite";
    public static final String MY_ID = "my_id";
    public static final String ARTICLE_TABLE = "article";
    public static final String ARTICLE_DATE = "update_date";
    public static final String ARTICLE_ID = "article_id";
    public static final String ARTICLE_AUTHOR = "author";
    public static final String ARTICLE_CONTENT = "content";
    public static final String ARTICLE_TITLE = "title";
    public static final String ARTICLE_URL = "url";
    public static final String ARCHIVE = "archive";
    public static final String ARTICLE_SYNC = "sync";
    public static final String ARTICLE_READAT = "read_at";
    public static final String FAV = "favorite";
    public static final String ARTICLE_SUMMARY = "summary";
    Context c;
    
    public ArticlesSQLiteOpenHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
            c = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
            createTables(db);
    }

    
    @Override
    public void onOpen(SQLiteDatabase db) {
    	// TODO Auto-generated method stub
    	super.onOpen(db);
    }
    
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.delete(ARTICLE_TABLE, null, null);
            SharedPreferences preferences = c.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("previous_update", zeroUpdate);
            editor.commit();
    }

    protected void createTables(SQLiteDatabase db) {
            db.execSQL(
                            "create table " + ARTICLE_TABLE + " (" +
                            MY_ID + " integer primary key autoincrement not null, " +
                            ARTICLE_AUTHOR + " text, " +
                            ARTICLE_DATE + " datetime, " +
                            ARTICLE_CONTENT + " text, " +
                            ARTICLE_TITLE + " text, " +
                            ARTICLE_URL + " text, " +
                            ARTICLE_ID + " integer, " +
                            ARCHIVE + " integer, " +
                            FAV + " integer, " +
                            ARTICLE_SYNC + " integer," +
                            ARTICLE_READAT + " integer," +
                            ARTICLE_SUMMARY + " text," +
                            "UNIQUE (" + ARTICLE_URL + ")" +
                            ");"
            );
    }

    public void truncateTables(SQLiteDatabase db) {
    	db.execSQL("DELETE FROM " + ARTICLE_TABLE + ";");
    }

}
