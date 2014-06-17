package com.pixplicity.wallabag.activities;

import com.pixplicity.easyprefs.library.Prefs;
import com.pixplicity.wallabag.ApiService;
import com.pixplicity.wallabag.Constants;
import com.pixplicity.wallabag.R;
import com.pixplicity.wallabag.Utils;
import com.pixplicity.wallabag.adapters.DrawerListAdapter;
import com.pixplicity.wallabag.adapters.ReadingListAdapter;
import com.pixplicity.wallabag.db.ArticleLoader;
import com.pixplicity.wallabag.models.Article;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Main Activity of the app.
 * Shows the list of articles and the navigation drawer.
 */
public class ListArticlesActivity extends Activity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ListArticlesActivity.class.getSimpleName();

    private int themeId;
    private int sortType;
    private int listFilterOption;
    private ReadingListAdapter mAdapter;
    private ListView mListView;
    private DrawerLayout drawerLayout;
    private ViewGroup drawerContainer;
    private DrawerListAdapter mDrawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private View mSettings;
    private View mNoArticles;
    private TextView mNoArticlesText;
    private boolean mIsLoading = false;

    private IntentFilter mServiceIntentFilter;

    private final BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {

        @SuppressLint("AppCompatMethod")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras.getBoolean(ApiService.EXTRA_FINISHED_LOADING, false)) {
                int unread = extras.getInt(ApiService.EXTRA_COUNT_UNREAD, 0);
                if (unread >= 0) {
                    // Note: if unread < 0, an article has been deleted.
                    Toast.makeText(ListArticlesActivity.this, getResources()
                                    .getQuantityString(R.plurals.unread_articles, unread, unread),
                            Toast.LENGTH_SHORT
                    ).show();
                }
                setBusy(false);
            } else {
                int done = extras.getInt(ApiService.EXTRA_PROGRESS, 0);
                int total = extras.getInt(ApiService.EXTRA_PROGRESS_TOTAL, 100);
                float factor = (Window.PROGRESS_END - Window.PROGRESS_START) / total;
                int progress = (int) (done * factor + Window.PROGRESS_START);
                setProgress(progress);
                setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }
//            getLoaderManager().restartLoader(
//                    R.id.loader_articles,
//                    null,
//                    ListArticlesActivity.this);
            updateList();
        }
    };

    @SuppressLint("AppCompatMethod")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);

        getSettings();
        Utils.setTheme(this, true);
        setContentView(R.layout.activity_list_articles);

        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        //Utils.setActionBarIcon(actionBar, themeId);
        actionBar.setLogo(R.drawable.actionbar_wide);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // 'no articles yet'
        mNoArticles = findViewById(R.id.no_articles_container);
        mNoArticlesText = (TextView) findViewById(R.id.no_articles_text);

        //Listview
        mListView = (ListView) findViewById(R.id.list_articles);
        mAdapter = new ReadingListAdapter(getBaseContext());
        mListView.setAdapter(mAdapter);
        setupList();

        //Drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerContainer = (ViewGroup) findViewById(R.id.left_drawer);
        ListView drawerList = (ListView) findViewById(R.id.lv_drawer);
        mDrawerAdapter = new DrawerListAdapter(this, listFilterOption, themeId);
        drawerList.setAdapter(mDrawerAdapter);
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                if (mDrawerAdapter.getActivePosition() != pos) {
                    if (pos == Constants.SETTINGS) {
                        Intent intent = new Intent(ListArticlesActivity.this,
                                SettingsActivity.class);
                        startActivityForResult(
                                intent,
                                Constants.REQUEST_SETTINGS);
                    } else {
                        mDrawerAdapter.setActivePosition(pos);
                        mDrawerAdapter.notifyDataSetChanged();
                        setListFilterOption(pos);
                        updateList();
                        setTitle(mDrawerAdapter.getItem(pos).mTitle);
                    }
                }
                closeDrawer();
            }
        });
        drawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                drawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer icon */
                R.string.drawer_open, /* "open drawer" description */
                R.string.drawer_close /* "close drawer" description */
        ) {

            /**
             * Called when a drawer has settled in a completely closed
             * state.
             */
            @Override
            public void onDrawerClosed(View view) {
                setActionBarLogo();
            }

            /**
             * Called when a drawer has settled in a completely open
             * state.
             */
            @Override
            public void onDrawerOpened(View drawerView) {
                getActionBar().setLogo(R.drawable.actionbar_wide);
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.setScrimColor(getResources().getColor(R.color.drawer_scrim));

        // In case of no articles, link to the settings:
        mSettings = findViewById(R.id.bt_settings);
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListArticlesActivity.this,
                        AccountSettingsActivity.class);
                startActivityForResult(
                        intent,
                        Constants.REQUEST_SETTINGS);
            }
        });
        if (Prefs.contains(Constants.PREFS_KEY_WALLABAG_URL)) {
            mSettings.setVisibility(View.GONE);
        }

        // Set logo (must happen after setting drawer mAdapter)
        setActionBarLogo();

        // Prepare IntentFilter for registration
        if (mServiceIntentFilter == null) {
            mServiceIntentFilter = new IntentFilter(getString(R.string.broadcast_articles_loaded));
            mServiceIntentFilter.addAction(getString(R.string.broadcast_archive_loaded));
        }

        // Auto-refresh if needed
        int auto = Prefs.getInt(Constants.PREFS_KEY_AUTO_REFRESH, Constants.DEFAULT_AUTO_REFRESH);
        long last = Prefs.getLong(Constants.PREFS_LAST_REFRESH, 0);
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if ((auto == Constants.REFRESH_ALWAYS
                || (auto == Constants.REFRESH_WIFI && wifi.isConnected()))
                && last < System.currentTimeMillis() - Constants.AUTO_REFRESH_TIMEOUT) {
            refresh();
            Prefs.putLong(Constants.PREFS_LAST_REFRESH, System.currentTimeMillis());
        }
    }

    private void setActionBarLogo() {
        int res;
        switch (mDrawerAdapter.getActivePosition()) {
            case Constants.UNREAD:
                res = R.drawable.actionbar_unread;
                break;
            case Constants.READ:
                res = R.drawable.actionbar_archive;
                break;
            case Constants.FAVS:
                res = R.drawable.actionbar_favorites;
                break;
//            case Constants.ALL:
//                res = R.drawable.actionbar_all; break;
            default:
                res = R.drawable.actionbar_wide;
                break;
        }
        getActionBar().setLogo(res);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        getSettings();
        registerReceiver(mServiceReceiver, mServiceIntentFilter);
//        getLoaderManager()
//                .restartLoader(R.id.loader_articles, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mServiceReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_list, menu);
        if (mIsLoading) {
            menu.findItem(R.id.refresh).setVisible(false);
        }
        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_READ_ARTICLE) {
            updateList(resultCode);
        }

        if (requestCode == Constants.REQUEST_SETTINGS) {
            if (resultCode == Constants.RESULT_LIST_SHOULD_CHANGE) {
                updateList();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(drawerContainer)) {
                    drawerLayout.closeDrawer(drawerContainer);
                } else {
                    drawerLayout.openDrawer(drawerContainer);
                }
                return true;
            case R.id.refresh:
                refresh();
                return true;
            case R.id.settings:
                startActivityForResult(
                        new Intent(getBaseContext(), SettingsActivity.class),
                        Constants.REQUEST_SETTINGS);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks the settings and adjusts the theme and other options if necessary.
     */
    private void getSettings() {
        int newThemeId = Prefs.getInt(
                LookAndFeelSettingsActivity.DARK_THEME,
                R.style.Theme_Wallabag);
        if (themeId != 0 && newThemeId != themeId) {
            themeId = newThemeId;
            Utils.restartActivity(this);
        } else {
            themeId = newThemeId;
        }

        sortType = Prefs.getInt(
                Constants.PREFS_SORT_TYPE,
                Constants.SORT_NEWER);
        listFilterOption = Prefs.getInt(
                Constants.LIST_FILTER_OPTION,
                Constants.UNREAD);
    }

    /**
     * Checks for a data connection and initiates a data request
     * through the ApiService.
     */
    public void refresh() {
        // Check for a data connection
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (!Prefs.contains(Constants.PREFS_KEY_WALLABAG_URL)) {
            Utils.showToast(this, getString(R.string.txtConfigNotSet));
            setBusy(false);
        } else if (activeNetwork != null && activeNetwork.isConnected()) {
            setBusy(true);
            Intent intent = new Intent(this, ApiService.class);
            intent.setAction(ApiService.REFRESH_ARTICLES);
            startService(intent);
        } else {
            //
            Utils.showToast(this, getString(R.string.txtNetOffline));
            setBusy(false);
        }
    }

    /**
     * Toggles the progress spinner in the actionbar
     */
    @SuppressLint("AppCompatMethod")
    private void setBusy(boolean busy) {
        mIsLoading = busy;
        invalidateOptionsMenu();
        if (busy) {
            mSettings.setVisibility(View.GONE);
            mNoArticlesText.setText(R.string.syncing);
            setProgress(Window.PROGRESS_START);
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
        } else {
            mNoArticlesText.setText(R.string.no_articles);
            setProgress(Window.PROGRESS_END);
            setProgressBarIndeterminateVisibility(Boolean.FALSE);
        }
    }

    public void setupList() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Intent i = new Intent(getBaseContext(), ReadArticleActivity.class);
                i.putExtra("id", mAdapter.getItem(position).mId);
                startActivityForResult(i, Constants.REQUEST_READ_ARTICLE);
            }
        });

        // Add footer view to allow scrolling above the navigation buttons
        // (navigation buttons overlaps with ui on SDK 19 and above)
        if (Build.VERSION.SDK_INT >= 19) {
            View foot = new View(this);
            foot.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.navigation_size));
            mListView.addFooterView(foot);
        }

        updateList();
    }

    private void checkIfHasNoArticles() {
        if (mAdapter.getCount() == 0) {
            mNoArticles.setVisibility(View.VISIBLE);
        } else {
            mNoArticles.setVisibility(View.GONE);
        }
    }

    public void updateList() {
        getLoaderManager().restartLoader(R.id.loader_articles, null, this);
    }

    public void updateList(int result) {
        if (Utils.hasToggledRead(result)) {
            if (listFilterOption == Constants.READ || listFilterOption == Constants.UNREAD) {
                updateList();
            }
        }
        if (Utils.hasToggledFavorite(result)) {
            if (listFilterOption == Constants.FAVS) {
                updateList();
            }
        }
    }

    public void closeDrawer() {
        drawerLayout.closeDrawer(drawerContainer);
    }

    public void setListFilterOption(int option) {
        listFilterOption = option;
        Prefs.putInt(Constants.LIST_FILTER_OPTION, option);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        getSettings();
        return new ArticleLoader(this, sortType, listFilterOption);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        checkIfHasNoArticles();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (mAdapter != null) {
            mAdapter.swapCursor(null);
        }
    }
}
