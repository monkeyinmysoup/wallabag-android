package com.pixplicity.wallabag;

import com.pixplicity.wallabag.activities.LookAndFeelSettingsActivity;

public final class Constants {

    // Navigation drawer entries:
	public static final int ALL = 0;
	public static final int UNREAD = 1;
	public static final int READ = 2;
	public static final int FAVS = 3;
    public static final int SETTINGS = 4;

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

    public static final int MAX_DESCRIPTION_CHARS = 250;
    public static final int JPEG_QUALITY = 90;

    public static final int DEFAULT_FONT_SIZE = 11;
    public static final int DEFAULT_FONT_STYLE = Style.SERIF;
    public static final int DEFAULT_TEXT_ALIGN = Style.JUSTIFY;
    public static final int DEFAULT_ORIENTATION = LookAndFeelSettingsActivity.PORTRAIT;
    public static final boolean DEFAULT_IMMERSIVE_ENABLED = false;
    public static final boolean DEFAULT_KEEP_SCREEN_ON = false;
}
