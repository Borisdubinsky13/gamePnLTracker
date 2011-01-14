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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

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
	
	class ShowViewBinder implements SimpleCursorAdapter.ViewBinder 
	{
		 public boolean setViewValue(View view, Cursor cursor, int columnIndex) 
		 {
			 TextView tv = (TextView) view;
			 DecimalFormat df = new DecimalFormat("#,###.00");

			 Log.i(TAG, SubTag + "ColumnIndex = " + columnIndex);
			 Log.i(TAG, SubTag + "Column 0 = " + cursor.getString(0));
			 Log.i(TAG, SubTag + "Column 1 = " + cursor.getString(1));
			 Log.i(TAG, SubTag + "Column 2 = " + cursor.getString(2));
			 Log.i(TAG, SubTag + "Column 3 = " + cursor.getString(3));
			 Log.i(TAG, SubTag + "Column 4 = " + cursor.getString(4));
			 Log.i(TAG, SubTag + "Column 5 = " + cursor.getString(5));
			 Log.i(TAG, SubTag + "Column 6 = " + cursor.getString(6));
			 Log.i(TAG, SubTag + "Column 7 = " + cursor.getString(7));

			 double	dValue = Double.parseDouble(cursor.getString(2));
			 if ( dValue >= 0 )
			 {
				 tv.setBackgroundColor(0xFF00A000);
			 }
			 else
			 {
				 tv.setBackgroundColor(0xFFA00000);
			 }

			 if ( columnIndex == 2 )
			 {
				 // 1st line
				 tv.setText(cursor.getString(3) + " :  " + cursor.getString(5) + " | " + cursor.getString(6) + " | " + cursor.getString(4));
			 }
			 if ( columnIndex == 3 )
			 {
				 tv.setText("     $" + df.format(dValue));
			 }
			 return true;
		 }
	}
	@Override
	protected void onResume()
	{
		super.onPause();
        
		Log.i(TAG, SubTag + "ListRes()"); 
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);
    	
		String	query = "name = '" + username + "'";

 		// Cursor	result;
		Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
/*
		String[] projection = new String[] {
				"_id",
				"name",
				"amount",
				"date",
				"gameType",
				"gameLimit",
				"eventType",
				"notes"
		};
*/
		Cursor result = getContentResolver().query(tmpUri, null, query, null, null);
		String[] columns = new String[] { "amount", "date", "gameType"  };
		int[] to = new int[] { android.R.id.text1, android.R.id.text2 };
		startManagingCursor(result);
		Log.i(TAG, SubTag + "Everything is ready for the adapter. # of records: " + result.getCount());
		SimpleCursorAdapter items = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, result, columns, to);
        Log.i(TAG, SubTag + "adapter has been created and populated");
        items.setViewBinder(new ShowViewBinder());
		setListAdapter(items);
	}
	/* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		// TODO Auto-generated method stub
		Log.i(TAG, SubTag + "Clicked on position: " + position );
		super.onListItemClick(l, v, position, id);
		
		// Save the entry in the preferences, so the display activity can display an appropriate record
        getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
       	.edit()
    	.putString(PREF_ID, position + "")
    	.commit();

    	Intent iDataEntry = new Intent(this, DetailDisplay.class);

        startActivity(iDataEntry);
	}	

}
