package com.pixplicity.wallabag.db;

import com.pixplicity.wallabag.BuildConfig;
import com.pixplicity.wallabag.models.Article;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;


public class CupboardSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wallabag.db";

    private static final int DATABASE_VERSION = 7;

    public CupboardSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // this will ensure that all tables are created
        cupboard().withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) {
            db.execSQL("DROP TABLE IF EXISTS " + cupboard().getTable(Article.class) + ";");
            cupboard().withDatabase(db).createTables();
            return;
        }
        // this will upgrade tables, adding columns and new tables.
        // Note that existing columns will not be converted
        cupboard().withDatabase(db).upgradeTables();
        // do migration work
    }
}
