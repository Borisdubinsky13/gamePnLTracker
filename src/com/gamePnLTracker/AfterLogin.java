/**
 * 
 */
package com.gamePnLTracker;

import java.text.DecimalFormat;

import com.admob.android.ads.AdView;
import com.gamePnLTracker.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * @author Boris
 *
 */
public class AfterLogin extends Activity 
{
	public String TAG="gamePnLTracker";
	public String SubTag="AfterLogin: ";
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	public String currentUser = new String();

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.mainmenu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Handle item selection
	    switch (item.getItemId()) 
	    {
	    case R.id.About:
	    	Log.i(TAG, SubTag + "trying to start ABOUT");
	    	Intent iAbout = new Intent(this, AboutHandler.class);
	        startActivity(iAbout);
	        return true;
	    case R.id.AddResult:
	    	Log.i(TAG, SubTag + "trying to start SETUP");
	    	Intent iDataEntry = new Intent(this, DataEntry.class);
	        startActivity(iDataEntry);
	        return true;
	    case R.id.ViewStats:
	    	Log.i(TAG, SubTag + "trying to start ViewRes");
			Intent iViewRes = new Intent(this, ListRes.class);
	        startActivity(iViewRes);
	        return true;
	    case R.id.Logout:
	    	Log.i(TAG, SubTag + "Logging user out");
	    	SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);   
	    	String username = pref.getString(PREF_USERNAME, null);
	    	
   			Uri	tmpUri = Uri.parse("content://com.gamePnLTracker.provider.userContentProvider");
			tmpUri = Uri.withAppendedPath(tmpUri,"pnlstatus");

        	String	query = "name = '" + username+ "'";
        	ContentResolver cr = getContentResolver();
        	cr.delete(tmpUri, query, null);
        	
        	// store user id in Preferences for everybody to access.
            getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
        	.edit()
        	.putString(PREF_USERNAME, "")
        	.commit();
        	// Intent iLogin = new Intent(this,loginHandler.class);
	    	Log.i(TAG, SubTag + "trying to logout");
	        finish();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		Log.i(TAG, SubTag + "onCreate() started" );
		super.onCreate(savedInstanceState);
		setContentView(R.layout.afterlogin);
		Log.i(TAG, SubTag + "onCreate() finished" );
	}
	
	@Override
	protected void onResume()
	{

		super.onPause();
/*
        AdManager.setTestDevices(new String[] 
    	{
        		AdManager.TEST_EMULATOR,
        		"426F72697360732050686F6E65"
        });
*/
        AdView	adView = (AdView)findViewById(R.id.adAfterLogin);
        adView.requestFreshAd();
        
		// get all the records with the current id ad add all the amounts
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);

		Cursor	result;
		String	value;
		double	sum = 0;
		double	dValue;
		DecimalFormat df = new DecimalFormat("#,###.00");
		final TextView pnlStr = (TextView)findViewById(R.id.PNL);
		final String strHead = this.getString(R.string.cEarnings);

		String	query = "name = '" + username+ "'";

		Uri	tmpUri = Uri.parse("content://com.gamePnLTracker.provider.userContentProvider");
		tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
		String[] projection = new String[] {
				"_ID",
				"name",
				"amount",
				"date",
				"gameType",
				"gameLimit",
				"eventType",
				"notes"
		};
		
		// result = getContentResolver().query(tmpUri, null, null, null, null);
		result = managedQuery(tmpUri, projection, query, null, null);
		Log.i(TAG, SubTag + "there are " + result.getCount() + " records" );
		
		if ( result.moveToFirst() )
		{
			Log.i(TAG, SubTag + "got result back from provider");
			do
			{
				value = result.getString(2);
				Log.i(TAG, SubTag + "got Value:  " + value);
				dValue = Double.parseDouble(value);
				sum += dValue;
				
			} while (result.moveToNext());
		}
		else
			Log.i(TAG, SubTag + "No Data returned from Content Provider");
				
		Log.i(TAG, SubTag + "Sum:  " + df.format(sum));
		pnlStr.setText(strHead + df.format(sum));
		if ( sum >= 0 )
			pnlStr.setBackgroundColor(0xFF00A000);
		else
			pnlStr.setBackgroundColor(0xFFA00000);

		Log.i(TAG, SubTag + "Done!");				
	}
}
