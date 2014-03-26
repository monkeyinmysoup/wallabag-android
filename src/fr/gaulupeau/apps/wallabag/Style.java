package fr.gaulupeau.apps.wallabag;

public abstract class Style {
	public final static int LEFT_ALIGN = 0;
	public final static int CENTER_ALIGN = 1;
	public final static int RIGHT_ALIGN = 2;
	public final static int JUSTIFY = 3;
	
	public final static String fontFamilySans = "Helvetica, Arial, sans-serif;";
	public final static String fontFamilySerif = "\"Times New Roman\", Times, serif;";
	public final static String endTag = "</body></html>";
	
	public static String getHead(String fontFamily, int textAlign, int fontSize){
		String align = ";";
		
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
			break;
		}
		
		return 	"<html>" +
				"<head>" +
				"<style type=\"text/css\">" +
				"body {" +
				"    font-family: " + fontFamily +
				"text-align: " + align + ";" +
				"font-size: " + fontSize + "pt;" +
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
				"}" +
				"</style>" +
				"</head>" +
				"<body>";
	}
}
