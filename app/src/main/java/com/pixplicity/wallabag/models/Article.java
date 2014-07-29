package com.pixplicity.wallabag.models;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.Html;

import com.pixplicity.wallabag.BuildConfig;
import com.pixplicity.wallabag.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import nl.qbusict.cupboard.annotation.Column;

import static com.pixplicity.wallabag.db.WallabagProvider.*;
import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class Article {

    public static final Uri URI = Uri.parse("content://" + BuildConfig.PROVIDER_AUTHORITY + "/" + BASE_ARTICLE);

    public static final String FIELD_URL = "url";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_IS_ARCHIVED = "is_archived";
    public static final String FIELD_IS_FAV = "is_fav";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_SUMMARY = "summary";
    public static final String FIELD_DOMAIN = "domain";
    public static final String FIELD_AUTHOR = "author";
    public static final String FIELD_IMAGE_URL = "image_url";
    public static final String FIELD_SCROLL_POS = "scroll_pos";
    public static final String FIELD_DATE = "date";

    private static final String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";

    @Column(BaseColumns._ID)
    public Long mId;

    @Column(value=FIELD_URL)
    public String mUrl;

    @Column(FIELD_TITLE)
    public String mTitle;

    @Column(FIELD_CONTENT)
    private String mContent;

    transient private String mContentFromHtml;

    @Column(FIELD_IS_ARCHIVED)
    public boolean mIsArchived;

    @Column(FIELD_IS_FAV)
    public boolean mIsFav;

    @Column(FIELD_IS_DELETED)
    public boolean mIsDeleted;

    @Column(FIELD_SUMMARY)
    public String mSummary;

    @Column(FIELD_DOMAIN)
    public String mDomain;

    @Column(FIELD_AUTHOR)
    public String mAuthor;

    @Column(FIELD_IMAGE_URL)
    public String mImageUrl;

    @Column(FIELD_SCROLL_POS)
    public int mScrollPos;

    @Column(FIELD_DATE)
    public Date mDate;


    transient public String[] mTags;


    public Article() {}

    public Article(String url, long id, String title, boolean archived, boolean isFav, String summary, String domain, String tags, String imageUrl) {
        this.mUrl = url;
        this.mId = id;
        this.mTitle = title;
        this.mIsArchived = archived;
        this.mIsFav = isFav;
        this.mIsDeleted = false;
        this.mSummary = summary;
        this.mDomain = domain;
        this.mTags = tags.split(",");
        this.mImageUrl = imageUrl;
    }

    public Article(long id) {
        mId = id;
    }

    public Article(ContentValues values) {
        mUrl = values.getAsString(Article.FIELD_URL);
        mTitle = values.getAsString(Article.FIELD_TITLE);
        mContent = values.getAsString(Article.FIELD_CONTENT);
        mSummary = values.getAsString(Article.FIELD_SUMMARY);
        mDomain = values.getAsString(Article.FIELD_DOMAIN);
        mIsArchived = values.getAsBoolean(Article.FIELD_IS_ARCHIVED);
        mIsFav = values.getAsBoolean(Article.FIELD_IS_FAV);
        mImageUrl = values.getAsString(Article.FIELD_IMAGE_URL);
        try {
            mDate = new SimpleDateFormat(DATE_FORMAT).parse(values.getAsString(Article.FIELD_DATE));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static String makeDescription(String html) {
        int chars = 0;
        String desc = "";
        String tmp = Html.fromHtml(html).toString();

        tmp = tmp.replaceAll("￼", "");
        tmp = tmp.replaceAll("\n", " ");
        tmp = tmp.replace("\t", "");
        tmp = tmp.replaceAll(" [ ]*", " ");

        String[] words = tmp.split(" ");

        for (int i = 0; i < words.length && chars < Constants.MAX_DESCRIPTION_CHARS; i++) {
            chars += words[i].length();
            desc += words[i] + " ";
        }
        return desc;
    }

    public boolean hasImage() {
        return mImageUrl != null;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getContent() {
        if (mContentFromHtml == null) {
            mContentFromHtml = Html.fromHtml(mContent).toString();
        }
        return mContentFromHtml;
    }

    public static Article get(Context ctx, long id) {
        return cupboard().withContext(ctx)
                .query(Article.URI, Article.class)
                .withSelection(BaseColumns._ID + "=" + id, (String[]) null)
                .get();
   }

    public void store(Context ctx) {
        cupboard()
                .withContext(ctx)
                .put(Article.URI, this);
    }
}
