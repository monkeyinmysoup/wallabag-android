package com.pixplicity.wallabag;

/**
 * Denotes which list of article is returned by an RSS feed:
 * home, favs, archive or tags.
 */
public enum ArticleType {
    UNREAD("home"),
    FAVORITES("fav"),
    ARCHIVE("archive");

    private final String queryString;

    ArticleType(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return queryString;
    }
}
