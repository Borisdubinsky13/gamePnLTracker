/**
 * 
 */
package com.gamesPnL;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
	private static final String PREF_ID = "dataTBL_ID";
	
	ArrayList<String> indxNames = new ArrayList<String>();
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
/*		
        AdManager.setTestDevices(new String[] 
        {
        		AdManager.TEST_EMULATOR,
        		"426F72697360732050686F6E65"
        });
 
        AdView	adView = (AdView)findViewById(R.id.adListRes);
        adView.requestFreshAd();
*/       
	}
	@Override
	protected void onResume()
	{
		super.onPause();
		
        DecimalFormat df = new DecimalFormat("#,###.00");
        
		Log.i(TAG, SubTag + "ListRes()"); 
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);
    	
		String	query = "name = '" + username + "'";

 		Cursor	result;
		Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
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
		
		ArrayAdapter<String> items = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		if ( result.moveToFirst() )
		{
			Log.i(TAG, SubTag + "got result back from provider");
			do
			{
				String	tmp = result.getString(3) + ": " + result.getString(4) + ": $" + df.format(Double.parseDouble(result.getString(2)));
				indxNames.add(result.getString(1));
				items.add(tmp);
			} while (result.moveToNext());
		}
		else
			Log.i(TAG, SubTag + "No Data returned from Content Provider");
		
		// ListView list = (ListView)findViewById(R.id.pnlList);

		setListAdapter(items);

        Log.i(TAG, SubTag + "adapter has been created and populated");
	}
	/* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Log.i(TAG, SubTag + "Clicked on position: " + position + " ID: " + indxNames.get(position));
		// Save the entry in the preferences, so the display activity can display an appropriate record
        getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
       	.edit()
    	.putString(PREF_ID, position + "")
    	.commit();

    	Intent iDataEntry = new Intent(this, DetailDisplay.class);

        startActivity(iDataEntry);
	}	

}
