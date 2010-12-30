/**
 * 
 */
package com.gamePnLTracker;

import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;
import com.gamePnLTracker.R;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * @author Boris
 *
 */
public class ListRes extends ListActivity 
{
	public String TAG="gamePnLTracker";
	public String SubTag="ListRes: ";
	
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listres);
		Log.i(TAG, SubTag + "ListRes() start");
/*		
        AdManager.setTestDevices(new String[] 
        {
        		AdManager.TEST_EMULATOR,
        		"426F72697360732050686F6E65"
        });

        AdView	adView = (AdView)findViewById(R.id.adListRes);
        adView.requestFreshAd();
*/        
		Log.i(TAG, SubTag + "ListRes()"); 
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);
		String	query = "name = '" + username+ "'";

		Cursor	result;
		Uri	tmpUri = Uri.parse("content://com.gamePnLTracker.provider.userContentProvider");
		tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
		String[] projection = new String[] {
				"amount",
				"date",
				"gameType"
		};
		
		// result = getContentResolver().query(tmpUri, null, null, null, null);
		result = managedQuery(tmpUri, projection, query, null, null);
		Log.i(TAG, SubTag + "there are " + result.getCount() + " records" );
		ArrayAdapter<String> items = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		if ( result.moveToFirst() )
		{
			Log.i(TAG, SubTag + "got result back from provider");
			do
			{
				items.add(result.getString(1) + ": " + result.getString(2) + ": $" + result.getString(0));
			} while (result.moveToNext());
		}
		else
			Log.i(TAG, SubTag + "No Data returned from Content Provider");
		setListAdapter(items);

        Log.i(TAG, SubTag + "adapter has been created and populated");
	}

}
