package com.pixplicity.wallabag;

import android.app.Application;

import com.pixplicity.easyprefs.library.Prefs;
import com.pixplicity.wallabag.models.Article;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardFactory;
import nl.qbusict.cupboard.CupboardBuilder;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;


public class WallabagApplication extends Application {

    static {
        // register our models
        Cupboard instance = new CupboardBuilder().useAnnotations().build();
        CupboardFactory.setCupboard(instance);
        cupboard().register(Article.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the Prefs class
        Prefs.initPrefs(this);
    }
}
