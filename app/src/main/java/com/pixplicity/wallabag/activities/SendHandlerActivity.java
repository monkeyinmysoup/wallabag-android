package com.pixplicity.wallabag.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.pixplicity.easyprefs.library.Prefs;
import com.pixplicity.wallabag.Constants;
import com.pixplicity.wallabag.R;

import java.io.UnsupportedEncodingException;

/**
 * This activity is called when the user chooses to share a link
 * to Wallabag ('Bag it!') from another app.
 * It currently simple launches the direct-share link in the default browser.
 */
public class SendHandlerActivity extends Activity {

    private static final String TAG = SendHandlerActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dummy);

        // If the user didn't setup the server yet, show them the settings
        if (!Prefs.contains(Constants.PREFS_KEY_WALLABAG_URL)) {
            Toast.makeText(this, R.string.error_share_no_server, Toast.LENGTH_LONG).show();
            Intent settingsIntent = new Intent(this, AccountSettingsActivity.class);
            startActivityForResult(
                    settingsIntent,
                    Constants.REQUEST_SETTINGS);
        }

        // NOTE disabled checking for connection as it's probably more user-friendly to handle this
        // in the browser (with the ability to refresh).
//        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
//        if (activeNetwork != null && activeNetwork.isConnected()) {

        launchBrowser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_SETTINGS) {
            if (Prefs.contains(Constants.PREFS_KEY_WALLABAG_URL)) {
                launchBrowser();
            } else {
                finish();
            }
        }
    }

    private void launchBrowser() {
        Bundle extras = getIntent().getExtras();
        String serverUrl = Prefs.getString(Constants.PREFS_KEY_WALLABAG_URL, null);
        final String pageUrl = extras.getString("android.intent.extra.TEXT");

        // Start to build the poche URL
        Uri.Builder saveUrl = Uri.parse(serverUrl).buildUpon();
        // Add the parameters from the call
        saveUrl.appendQueryParameter("action", "add");
        byte[] data = null;
        try {
            data = pageUrl.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
        saveUrl.appendQueryParameter("url", base64);
        Log.d(TAG, "base64 : " + base64);
        Log.d(TAG, "pageurl : " + pageUrl);

        // Load the constructed URL in the browser
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(saveUrl.build());
        i.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
        // If user has more then one browser installed give them a chance to
        // select which one they want to use
        startActivity(i);
        // That is all this app needs to do, so call finish()
        this.finish();
    }
}
