package com.pixplicity.wallabag.models;

public class Article {
    public String url;
    public String id;
    public String title;
    public String archive;
    public String isFav;
    public String summary;
    
    public Article(String url, String id, String title, String archive, String isFav, String summary) {
                this.url = url;
                this.id = id;
                this.title = title;
                this.archive = archive;
                this.isFav = isFav;
                this.summary = summary;
        }
}
