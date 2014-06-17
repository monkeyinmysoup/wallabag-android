package com.pixplicity.wallabag.activities;

import com.pixplicity.easyprefs.library.Prefs;
import com.pixplicity.wallabag.ApiService;
import com.pixplicity.wallabag.Constants;
import com.pixplicity.wallabag.R;
import com.pixplicity.wallabag.Style;
import com.pixplicity.wallabag.Utils;
import com.pixplicity.wallabag.models.Article;
import com.pixplicity.wallabag.ui.OnViewScrollListener;
import com.pixplicity.wallabag.ui.ResponsiveScrollView;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.text.BidiFormatter;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ReadArticleActivity extends Activity {

    private static final String TAG = ReadArticleActivity.class.getSimpleName();
    private static final String ARG_ARTICLE_SCROLL = "scroll_pos";

    private long id = -1;
    private ResponsiveScrollView view;
    private WebView contentWebView;
    private int currentResult;
    private boolean isRtl;
    private Menu menu;
    private ActionBar actionBar;
    private int fontStyle;
    private int textAlign;
    private boolean canGoImmersive;
    private boolean keepScreenOn;
    private int themeId;
    private int fontSize;
    private int yPositionReadAt;

    private Article mArticle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getSettings();
        Utils.setTheme(this, true);
        setContentView(R.layout.activity_article);
        actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setLogo(R.drawable.actionbar_wide);
        Utils.setActionBarIcon(actionBar, themeId);

        Bundle data = getIntent().getExtras();
        if (data != null) {
            id = data.getLong("id");
        }

        // Load article from DB
        mArticle = Article.get(this, id);

        TextView txtTitle = (TextView) findViewById(R.id.article_title_text);
        txtTitle.setText(mArticle.mTitle);
        view = (ResponsiveScrollView) findViewById(R.id.scroll);
        contentWebView = (WebView) findViewById(R.id.webContent);

        WebViewClient mWebClient = new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                Intent bagItIntent = new Intent(ReadArticleActivity.this,
                        SendHandlerActivity.class);

                bagItIntent.setAction(Intent.ACTION_SEND);
                bagItIntent.setType("text/plain");
                bagItIntent.putExtra(Intent.EXTRA_TEXT, url);

                Intent chooser = Intent.createChooser(intent, url.replace("http://", ""));
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{
                        bagItIntent
                });

                startActivity(chooser);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        ReadArticleActivity.this.view.scrollTo(0, yPositionReadAt);
                    }
                }, 500);
            }
        };

        contentWebView.setWebViewClient(mWebClient);
        isRtl = BidiFormatter.getInstance().isRtl(mArticle.getContent());

        TextView txtAuthor = (TextView) findViewById(R.id.article_url_text);

        String articleUrlHostName = "";
        try {
            URL url = new URL(mArticle.mUrl);
            articleUrlHostName = url.getHost();
        } catch (MalformedURLException ignored) {
        }

        txtAuthor.setText(articleUrlHostName);

        view.setOnScrollViewListener(new OnViewScrollListener() {

            private int goingDown
                    ,
                    goingUp;

            @Override
            public void onScrollChanged(int x, int y, int oldx, int oldy) {

                if (actionBar.isShowing() && goingDown > 100) {
                    actionBar.hide();
                }

                if (!actionBar.isShowing() && goingUp > 100) {
                    actionBar.show();
                }

                if (y > oldy) {
                    goingDown += y - oldy;
                    goingUp = 0;
                }

                if (y < oldy) {
                    goingUp += oldy - y;
                    goingDown = 0;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSettings();
        goImmersive();
        loadDataToWebView();
        contentWebView.setKeepScreenOn(keepScreenOn);
    }

    @Override
    protected void onPause() {
        yPositionReadAt = view.getScrollY();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_ARTICLE_SCROLL, view.getScrollY());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_ARTICLE_SCROLL)) {
            yPositionReadAt = savedInstanceState.getInt(ARG_ARTICLE_SCROLL);
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    private void loadDataToWebView() {
        contentWebView.loadDataWithBaseURL(null,
                Style.getHead(
                        fontStyle,
                        textAlign,
                        fontSize,
                        Utils.isDarkTheme(themeId),
                        isRtl)
                        + mArticle.getContent() + Style.endTag,
                "text/html",
                "utf-8",
                null
        );

        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            int color = a.data;
            contentWebView.setBackgroundColor(color);
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        goImmersive();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void goImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (canGoImmersive) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LOW_PROFILE);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_read, menu);
        this.menu = menu;
        setReadStateIcon();
        setFavStateIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.read:
                toggleMarkAsRead();
                return true;
            case R.id.delete:
                delete();
                return true;
            case R.id.fav:
                toggleFav();
                return true;
            case R.id.browser:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mArticle.mUrl));
                startActivity(intent);
                return true;
            case R.id.settings:
                startActivityForResult(new Intent(getBaseContext(),
                        LookAndFeelSettingsActivity.class), this.hashCode());
                return true;
            case R.id.share:
                shareUrl();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void getSettings() {
        fontSize = Prefs.getInt(LookAndFeelSettingsActivity.FONT_SIZE, Constants.DEFAULT_FONT_SIZE);
        canGoImmersive = Prefs.getBoolean(LookAndFeelSettingsActivity.IMMERSIVE,
                Constants.DEFAULT_IMMERSIVE_ENABLED);
        keepScreenOn = Prefs.getBoolean(LookAndFeelSettingsActivity.KEEP_SCREEN_ON,
                Constants.DEFAULT_KEEP_SCREEN_ON);
        fontStyle = Prefs
                .getInt(LookAndFeelSettingsActivity.FONT_STYLE, Constants.DEFAULT_FONT_STYLE);
        textAlign = Prefs.getInt(LookAndFeelSettingsActivity.ALIGN, Constants.DEFAULT_TEXT_ALIGN);
        int screenOrientation = Prefs.getInt(LookAndFeelSettingsActivity.ORIENTATION,
                Constants.DEFAULT_ORIENTATION);

        switch (screenOrientation) {
            case LookAndFeelSettingsActivity.PORTRAIT:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

                break;
            case LookAndFeelSettingsActivity.LANDSCAPE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }

                break;
            case LookAndFeelSettingsActivity.DYMAMIC:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

                break;
            default:
                break;
        }

        themeId = Prefs.getInt(LookAndFeelSettingsActivity.DARK_THEME, R.style.Theme_Wallabag);
    }

    private void shareUrl() {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        viewIntent.setData(Uri.parse(mArticle.mUrl));
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, mArticle.mUrl);
        Intent intentChooser = createIntentChooserForTwoIntents(viewIntent,
                sendIntent, getString(R.string.share_title));

        startActivity(intentChooser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Utils.RESULT_CHANGE_THEME) {
            getSettings();
            Utils.restartActivity(this);
        }
    }

    @Override
    public void finish() {
        yPositionReadAt = view.getScrollY();
        setResult(currentResult);
        mArticle.mScrollPos = view.getScrollY();
        mArticle.store(this);
        super.finish();
    }

    private void setReadStateIcon() {
        MenuItem item = menu.findItem(R.id.read);
        if (mArticle.mIsArchived) {
            item.setIcon(R.drawable.ic_action_undo_dark);
            item.setTitle(getString(R.string.unread_title));
        } else {
            item.setIcon(R.drawable.ic_action_accept_dark);
            item.setTitle(getString(R.string.read_title));
        }
    }

    private void setFavStateIcon() {
        MenuItem item = menu.findItem(R.id.fav);
        if (mArticle.mIsFav) {
            item.setIcon(R.drawable.ic_action_important_dark);
        } else {
            item.setIcon(R.drawable.ic_action_not_important_dark);
        }
    }

    private void toggleMarkAsRead() {
        currentResult ^= Constants.RESULT_TOGGLE_READ;
        if (mArticle.mIsArchived) {
            Utils.showToast(this, getString(R.string.marked_as_unread));
            mArticle.mIsArchived = false;
            mArticle.store(this);
        } else {
            Utils.showToast(this, getString(R.string.marked_as_read));
            mArticle.mIsArchived = true;
            mArticle.store(this);
            finish();
        }
        setReadStateIcon();
    }

    private void delete() {
        Utils.showToast(this, getString(R.string.article_deleted));
        finish();
        Intent intent = new Intent(this, ApiService.class);
        intent.setAction(ApiService.DELETE_ARTICLE);
        intent.putExtra(ApiService.EXTRA_ARTICLE_URL, mArticle.mUrl);
        startService(intent);
    }

    private void toggleFav() {
        currentResult ^= Constants.RESULT_TOGGLE_FAVORITE;
        if (mArticle.mIsFav) {
            Utils.showToast(this, getString(R.string.marked_as_not_fav));
            mArticle.mIsFav = false;
        } else {
            Utils.showToast(this, getString(R.string.marked_as_fav));
            mArticle.mIsFav = true;
        }
        setFavStateIcon();
    }

    private Intent createIntentChooserForTwoIntents(Intent first,
            Intent second, String title) {

        PackageManager pm = getPackageManager();

        Intent chooser = Intent.createChooser(second, title);

        List<ResolveInfo> resInfo = pm.queryIntentActivities(first, 0);
        Intent[] extraIntents = new Intent[resInfo.size()];
        for (int i = 0; i < resInfo.size(); i++) {
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName,
                    ri.activityInfo.name));
            intent.setAction(first.getAction());
            intent.setData(first.getData());
            extraIntents[i] = intent;
        }

        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);

        return chooser;
    }
}
