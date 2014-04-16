package fr.gaulupeau.apps.wallabag;

public abstract class Constants {
	
	public static final int ALL = 0;
	public static final int UNREAD = 1;
	public static final int READ = 2;
	public static final int FAVS = 3;
	public static final String LIST_FILTER_OPTION = "ListFilterOption";
	public static final int RESULT_TOGGLE_FAVORITE = Integer.parseInt("1000", 2);
	public static final int RESULT_TOGGLE_READ = Integer.parseInt("10000", 2);
	public static final int REQUEST_READ_ARTICLE = 17;
	public static final int REQUEST_SETTINGS = 15;
	public static final int RESULT_CHANGED_SORT = 13;
}
