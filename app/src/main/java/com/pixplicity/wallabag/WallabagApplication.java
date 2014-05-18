package com.pixplicity.wallabag;

import android.app.Application;

import com.pixplicity.easyprefs.library.Prefs;


public class WallabagApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the Prefs class
        Prefs.initPrefs(this);
    }
}
