package com.pixplicity.wallabag.models;

import android.text.Html;

import com.pixplicity.wallabag.Constants;

public class Article {

    public String url;
    public String id;
    public String title;
    public String archive;
    public String isFav;
    public String summary;
    public String domain;
    public String[] tags;
    public String imageUrl;

    public Article(String url, String id, String title, String archive, String isFav, String summary, String domain, String tags, String imageUrl) {
        this.url = url;
        this.id = id;
        this.title = title;
        this.archive = archive;
        this.isFav = isFav;
        this.summary = summary;
        this.domain = domain;
        this.tags = tags.split(",");
        this.imageUrl = imageUrl;
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
        return imageUrl != null;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
