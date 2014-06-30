package com.pixplicity.wallabag;

import com.pixplicity.wallabag.activities.LookAndFeelSettingsActivity;

public final class Constants {

    // Navigation drawer entries:
	//public static final int ALL = 0;
	public static final int UNREAD = 0;
	public static final int READ = 1;
	public static final int FAVS = 2;
    public static final int SETTINGS = 3;

	public static final String LIST_FILTER_OPTION = "ListFilterOption";

	public static final int RESULT_TOGGLE_FAVORITE = Integer.parseInt("1000", 2);
	public static final int RESULT_TOGGLE_READ = Integer.parseInt("10000", 2);
	public static final int REQUEST_READ_ARTICLE = 17;
	public static final int REQUEST_SETTINGS = 15;
	public static final int RESULT_LIST_SHOULD_CHANGE = 13;

    public static final String PREFS_KEY_SERVER_OPTION = "ServerOption";
    public static final String PREFS_KEY_WALLABAG_URL = "pocheUrl";
    public static final String PREFS_KEY_USER_ID = "APIUsername";
    public static final String PREFS_KEY_USER_TOKEN = "APIToken";
    public static final String PREFS_KEY_PREVIOUS_UPDATE = "previous_update";
    public static final String PREFS_KEY_USER_NAME = "UserName";
    public static final String PREFS_KEY_AUTO_REFRESH = "auto_refresh";
    public static final String PREFS_SORT_TYPE = "sort_type";
    public static final String PREFS_LAST_REFRESH = "last_refresh";

    public static final int MAX_DESCRIPTION_CHARS = 250;
    public static final int JPEG_QUALITY = 90;
    public static final int UPDATE_LIST_EVERY_X_ITEMS = 10;

    public static final int REFRESH_MANUAL = 0;
    public static final int REFRESH_WIFI = 1;
    public static final int REFRESH_ALWAYS = 2;

    public static final int DEFAULT_FONT_SIZE = 11;
    public static final int DEFAULT_FONT_STYLE = Style.SERIF;
    public static final int DEFAULT_TEXT_ALIGN = Style.JUSTIFY;
    public static final int DEFAULT_ORIENTATION = LookAndFeelSettingsActivity.PORTRAIT;
    public static final boolean DEFAULT_IMMERSIVE_ENABLED = false;
    public static final boolean DEFAULT_KEEP_SCREEN_ON = false;
    public static final int DEFAULT_AUTO_REFRESH = REFRESH_MANUAL;

    /**
     * Timeout for automatic refreshes. 5 minutes.
     */
    public static final long AUTO_REFRESH_TIMEOUT = 5 * 60 * 1000;

    public static final int SORT_NEWER = 0;
    public static final int SORT_OLDER = 1;
    public static final int SORT_ALPHA = 2;

    public static final String SSL_KEYSTORE_FILENAME = "keystore";
}
