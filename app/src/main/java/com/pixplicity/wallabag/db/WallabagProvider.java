package com.pixplicity.wallabag.db;

import com.pixplicity.wallabag.BuildConfig;
import com.pixplicity.wallabag.models.Article;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Content provider for Articles
 */
public class WallabagProvider extends ContentProvider {

    public static final String BASE_ARTICLE = "article";

    private static final int ARTICLES = 0;
    private static final int ARTICLE = 1;

    private static final String TAG = WallabagProvider.class.getSimpleName();

    private static UriMatcher sMatcher;

    static {
        sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sMatcher.addURI(BuildConfig.PROVIDER_AUTHORITY, BASE_ARTICLE, ARTICLES);
        sMatcher.addURI(BuildConfig.PROVIDER_AUTHORITY, BASE_ARTICLE + "/#", ARTICLE);
    }

    private CupboardSQLiteOpenHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new CupboardSQLiteOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteDatabase db;
        Class<?> clz;
        db = mDatabaseHelper.getReadableDatabase();
        switch (sMatcher.match(uri)) {
            case ARTICLE:
            case ARTICLES:
                clz = Article.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return cupboard().withDatabase(db)
                .query(clz)
                .withProjection(projection)
                .withSelection(selection, selectionArgs)
                .orderBy(sortOrder)
                .getCursor();
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        Class<?> clz;
        long id;
        int match = sMatcher.match(uri);
        switch (match) {
            case ARTICLE:
            case ARTICLES:
                clz = Article.class;
                id = cupboard().withDatabase(db).put(clz, values);
                return Uri.parse(BuildConfig.PROVIDER_AUTHORITY + "/" + BASE_ARTICLE + "/" + id);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int i = sMatcher.match(uri);
        switch (i) {
            case ARTICLE:
            case ARTICLES:
                return cupboard().withDatabase(db).delete(Article.class, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int i = sMatcher.match(uri);
        switch (i) {
            case ARTICLE:
            case ARTICLES:
                return cupboard().withDatabase(db)
                        .update(Article.class, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public synchronized int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        String table;
        int i = sMatcher.match(uri);
        switch (i) {
            case ARTICLE:
            case ARTICLES:
                table = cupboard().getTable(Article.class);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        db.beginTransaction();
        try {
            for (ContentValues value : values) {
                db.insertWithOnConflict(table, null, value, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            db.endTransaction();
        }
        return 1;
    }
}