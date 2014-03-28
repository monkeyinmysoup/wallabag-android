package fr.gaulupeau.apps.wallabag;

public abstract class Style {
	public static final int SANS = 0;
	public static final int SERIF = 1;
	public final static int LEFT_ALIGN = 0;
	public final static int CENTER_ALIGN = 1;
	public final static int RIGHT_ALIGN = 2;
	public final static int JUSTIFY = 3;
	
	public final static String fontFamilySans = "Helvetica, Arial, sans-serif";
	public final static String fontFamilySerif = "\"Times New Roman\", Times, serif";
	public final static String endTag = "</body></html>";
	public final static String textColorWhite = "#FFF";
	public final static String textColorBlack = "#000";
	
	public static String getHead(int fontStyle, int textAlign, int fontSize, boolean isDarkTheme){
		String align;
		String textColor;
		String fontFamily;
		
		switch (textAlign) {
		case LEFT_ALIGN:
			align = "left";
			break;
		case CENTER_ALIGN:
			align = "center";
			break;
		case RIGHT_ALIGN:
			align = "right";
			break;
		case JUSTIFY:
			align = "justify";
			break;
		default:
			align = "";
			break;
		}
		
		switch (fontStyle) {
		case SANS:
			fontFamily = fontFamilySans;
			break;
		case SERIF:
			fontFamily = fontFamilySerif;
			break;
		default:
			fontFamily = "";
			break;
		}
		
		textColor = isDarkTheme ? textColorWhite : textColorBlack;
		
		return 	"<html>" +
				"<head>" +
				"<style type=\"text/css\">" +
				"body {" +
				"    font-family: " + fontFamily + ";" +
				"	text-align: " + align + ";" +
				"	font-size: " + fontSize + "pt;" +
				"	color: " + textColor + ";" +
				"} " +
				"img {" +
				"	max-width: 100%;" +
				"	height: auto;" +
				"	display: block;" +
				"	margin-left: auto;" +
				"	margin-right: auto;" +
				"}" +
				"pre, code {" +
				"	word-break: break-all;" +
				"	word-wrap: break-word;" +
				"}" +
				"a {" +
				"	word-wrap: break-word;" +
				"	text-decoration: none;" +
				"	color: #3887CF" +
				"}" +
				"</style>" +
				"</head>" +
				"<body>";
	}
}
