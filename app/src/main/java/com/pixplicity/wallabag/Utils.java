package com.pixplicity.wallabag;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;
import com.pixplicity.wallabag.activities.GeneralSettingsActivity;
import com.pixplicity.wallabag.activities.ListArticlesActivity;
import com.pixplicity.wallabag.activities.LookAndFeelSettingsActivity;

import java.io.File;
import java.net.URI;

import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARCHIVE;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.ARTICLE_TITLE;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.FAV;
import static com.pixplicity.wallabag.db.ArticlesSQLiteOpenHelper.MY_ID;

public final class Utils {

    public static final int RESULT_CHANGE_THEME = 42;

    /**
     * Sets the theme on the Activity as specified in the Prefs.
     * @param activity The Activity to set the theme on
     * @param overlap Whether or not to use the 'overlap' version of the theme (only applicable on SDK 19 and up)
     */
    public static void setTheme(Activity activity, boolean overlap) {
        int normal, dark;
        if (overlap) {
            if (activity instanceof ListArticlesActivity) {
                normal = R.style.Theme_Wallabag_OverlapTop;
                dark = R.style.Theme_Wallabag_Dark_OverlapTop;
            } else {
                normal = R.style.Theme_Wallabag_Overlap;
                dark = R.style.Theme_Wallabag_Dark_Overlap;
            }
        } else {
            normal = R.style.Theme_Wallabag;
            dark = R.style.Theme_Wallabag_Dark;
        }
        int themeId = Prefs.getInt(LookAndFeelSettingsActivity.DARK_THEME, normal);
        if (themeId == R.style.Theme_Wallabag) {
            activity.setTheme(normal);
        } else if (themeId == R.style.Theme_Wallabag_Dark) {
            activity.setTheme(dark);
        }
    }

    public static boolean isDarkTheme(int themeId) {
        return themeId == R.style.Theme_Wallabag_Dark;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void setActionBarIcon(ActionBar actionBar, int themeId) {
        if (isDarkTheme(themeId)) {
            actionBar.setLogo(R.drawable.actionbar_dark);
        } else {
            //actionBar.setLogo(R.drawable.actionbar);
            actionBar.setLogo(R.drawable.actionbar_dark);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void restartActivity(final Activity activity) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    activity.recreate();
                } else {
                    Intent intent = activity.getIntent();
                    activity.finish();
                    activity.startActivity(intent);
                }
            }
        }, 1);
    }

    public static File getSaveDir(Context ctx) {
        File f = ctx.getExternalFilesDir(null);
        if (!f.exists() ) {
            return ctx.getFilesDir();
        }
        return f;
    }

    public static void showToast(Context ctx, final String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static String getFilter(int filterOption) {
        switch (filterOption) {
            case Constants.ALL:
                return null;

            case Constants.UNREAD:
                return ARCHIVE + " = 0";

            case Constants.READ:
                return ARCHIVE + " = 1";

            case Constants.FAVS:
                return FAV + " = 1";

            default:
                return null;
        }
    }

    /**
     * Returns the proper sort order for the given type.
     * The {@code sortType} parameter must be either of the
     * values {@link com.pixplicity.wallabag.activities.GeneralSettingsActivity#NEWER},
     * {@link com.pixplicity.wallabag.activities.GeneralSettingsActivity#OLDER} or
     * {@link com.pixplicity.wallabag.activities.GeneralSettingsActivity#ALPHA}.
     * @param sortType
     * @return
     */
    public static String getOrderBy(int sortType) {
        switch (sortType) {
            case GeneralSettingsActivity.NEWER:
                return MY_ID + " DESC";
            case GeneralSettingsActivity.OLDER:
                return MY_ID;
            case GeneralSettingsActivity.ALPHA:
                return ARTICLE_TITLE + " COLLATE NOCASE";
            default:
                throw new RuntimeException("Unknown sort order " + sortType);
        }
    }

    public static boolean hasToggledFavorite(int result) {
        return (result & Constants.RESULT_TOGGLE_FAVORITE) == Constants.RESULT_TOGGLE_FAVORITE;
    }

    public static boolean hasToggledRead(int result) {
        return (result & Constants.RESULT_TOGGLE_READ) == Constants.RESULT_TOGGLE_READ;
    }

    /**
     * Cleans up a String by removing quotes, accents and other invalid characters
     * from a title of an Article.
     *
     * @param s
     * @return
     */
    public static String cleanString(String s) {
        s = s.replace("&Atilde;&copy;", "&eacute;")
                .replace("&Atilde;&uml;", "&egrave;")
                .replace("&Atilde;&ordf;", "&ecirc;")
                .replace("&Atilde;&laquo;", "&euml;")
                .replace("&Atilde;&nbsp;", "&agrave;")
                .replace("&Atilde;&curren;", "&auml;")
                .replace("&Atilde;&cent;", "&acirc;")
                .replace("&Atilde;&sup1;", "&ugrave;")
                .replace("&Atilde;&raquo;", "&ucirc;")
                .replace("&Atilde;&frac14;", "&uuml;")
                .replace("&Atilde;&acute;", "&ocirc;")
                .replace("&Atilde;&para;", "&ouml;")
                .replace("&Atilde;&reg;", "&icirc;")
                .replace("&Atilde;&macr;", "&iuml;")
                .replace("&Atilde;&sect;", "&ccedil;")
                .replace("&amp;", "&amp;");
        return s;
    }

    /**
     * Returns the domain of the url, excluding www. if present.s
     * @param url The url to strip, e.g. "https://search.google.com/something"
     * @return The bare domain name, e.g. "search.google.com"
     */
    public static String getDomainFromUrl(String url) {
        String domain = Uri.parse(url).getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
}
