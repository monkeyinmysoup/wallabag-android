package com.pixplicity.wallabag.db;

import com.pixplicity.wallabag.Utils;
import com.pixplicity.wallabag.models.Article;

import android.content.Context;
import android.content.CursorLoader;

/**
 * Simple CursorLoader for loading Articles from the database
 */
public class ArticleLoader extends CursorLoader {

    public ArticleLoader(Context context, int sortType,
            int filterOption) {
        super(context, Article.URI, null, Utils.getFilter(filterOption), null,
                Utils.getOrderBy(sortType));
    }
}
