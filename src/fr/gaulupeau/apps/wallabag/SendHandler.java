package fr.gaulupeau.apps.wallabag;

import static fr.gaulupeau.apps.wallabag.Helpers.PREFS_NAME;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Base64;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class SendHandler extends SherlockActivity {
	SharedPreferences settings;
	static String pocheUrl;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        
        getSettings();
		
		setContentView(R.layout.main);

		final String pageUrl = extras.getString("android.intent.extra.TEXT");
		// Vérification de la connectivité Internet
		final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			// Start to build the poche URL
			Uri.Builder pocheSaveUrl = Uri.parse(pocheUrl).buildUpon();
			// Add the parameters from the call
			pocheSaveUrl.appendQueryParameter("action", "add");
			byte[] data = null;
			try {
				data = pageUrl.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String base64 = Base64.encodeToString(data, Base64.DEFAULT);
			pocheSaveUrl.appendQueryParameter("url", base64);
			System.out.println("base64 : " + base64);
			System.out.println("pageurl : " + pageUrl);

			// Load the constructed URL in the browser
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(pocheSaveUrl.build());
			i.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
			// If user has more then one browser installed give them a chance to
			// select which one they want to use

			startActivity(i);
			// That is all this app needs to do, so call finish()
			this.finish();
		} else {
			// Afficher alerte connectivité
			showToast(getString(R.string.txtNetOffline));
		}
	}
	
	private void getSettings(){
        settings = getSharedPreferences(PREFS_NAME, 0);
        pocheUrl = settings.getString("pocheUrl", "https://");
	}
	
	 public void showToast(final String toast)
	    {
	    	runOnUiThread(new Runnable() {
	    		public void run()
	    		{
	    			Toast.makeText(SendHandler.this, toast, Toast.LENGTH_SHORT).show();
	    		}
	    	});
	    }
}
