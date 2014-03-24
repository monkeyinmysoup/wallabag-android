package fr.gaulupeau.apps.wallabag;

public abstract class Style {
	public final static String fontFamilySans = "Arial, Helvetica, sans-serif;";
	public final static String fontFamilySerif = "\"Times New Roman\", Times, serif;";
	
	public static String getHead(String fontFamily){
		return "<html>" +
				"<head>" +
				"<style type=\"text/css\">" +
				"body {" +
				"    font-family: " + fontFamily +
				"    font-size: medium;" +
				"    text-align: justify;" +
				"} " +
				"img {" +
				"	max-width: 100%;" +
				"	height: auto;" +
				"	display: block;" +
				"	margin-left: auto;" +
				"	margin-right: auto;" +
				"}" +
				"</style>" +
				"</head>";
	}
}