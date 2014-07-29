package com.pixplicity.wallabag;

public abstract class Style {

	public static final int SANS = 0;
	public static final int SERIF = 1;
	public final static int AUTO_ALIGN = 0;
	public final static int CENTER_ALIGN = 1;
	public final static int JUSTIFY = 2;
	
	public final static String fontFamilySans = "Helvetica, Arial, sans-serif";
	public final static String fontFamilySerif = "\"Times New Roman\", Times, serif";
	public final static String endTag = "<br /><br /><br /><br /></body></html>";
	public final static String textColorWhite = "#FFF";
	public final static String textColorBlack = "#000";

    // Giggity
	public static String getHead(int fontStyle, int textAlign, int fontSize, boolean isDarkTheme, boolean isRtl){
		String align;
		String textColor;
		String fontFamily;
		String direction;
		switch (textAlign) {
		case AUTO_ALIGN:
			align = "";
			break;
		case CENTER_ALIGN:
			align = "text-align: center;";
			break;
		case JUSTIFY:
			align = "text-align: justify;";
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
		direction = isRtl ? "dir=\"rtl\"" : "";
		
		return 	"<html " + direction + ">" +
				"<head>" +
				"<style type=\"text/css\">" +
				"body {" + 
				"    font-family: " + fontFamily + ";" +
					 align +
				"	font-size: " + fontSize + "pt;" +
				"	color: " + textColor + ";" +
				"	word-wrap: break-word;" +
                "	line-height: 160%;" +
                "	margin: 1eM 5%;" +
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
				"table{" +
				"	display: block;" +
				"	width: 100%;" +
				"	overflow: auto;" +
				"}" +
				"</style>" +
				"</head>" +
				"<body>";
	}
}
