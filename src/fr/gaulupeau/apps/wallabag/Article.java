package fr.gaulupeau.apps.wallabag;

public class Article {
    public String url;
    public String id;
    public String title;
    public String content;
    public String archive;
    public String summary;
    
    public Article(String url, String id, String title, String content, String archive, String summary) {
                super();
                this.url = url;
                this.id = id;
                this.title = title;
                this.content = content;
                this.archive = archive;
                this.summary = summary;
        }
}
