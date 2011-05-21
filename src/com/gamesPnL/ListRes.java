
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
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.*;
import com.google.ads.AdRequest;

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
	private static final String PREF_MONTH = "month";
	private static final String PREF_ID = "dataTBL_ID";
	
	ArrayList<String> indxNames = new ArrayList<String>();
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	}
	
	
	class ShowViewBinder implements SimpleCursorAdapter.ViewBinder 
	{
		 public boolean setViewValue(View view, Cursor cursor, int columnIndex) 
		 {
			 TextView tv = (TextView) view;
			 DecimalFormat df = new DecimalFormat("#,##0.00");

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
		        String tmpStr = cursor.getString(cursor.getColumnIndex("evMonth")) +
	        		"/" + cursor.getString(cursor.getColumnIndex("evDay")) +
	        		"/" + cursor.getString(cursor.getColumnIndex("evYear"));

				 String outS = tmpStr + ": " + 
				 				cursor.getString(cursor.getColumnIndex("gameType")) + " |  " + 
				 				cursor.getString(cursor.getColumnIndex("eventType")) + " | " + 
				 				cursor.getString(cursor.getColumnIndex("gameLimit"));
				 tv.setText(outS);
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
		setContentView(R.layout.listres);

		AdView	adView = (AdView)findViewById(R.id.adListRes);
		// Initiate a generic request to load it with an ad
	    adView.loadAd(new AdRequest());
	        
		gamesLogger.i(TAG, SubTag + "ListRes()"); 
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);
    	String neededMonth = pref.getString(PREF_MONTH, null);
		this.setTitle("User: " + username);
		String query = "";
    	Bundle extras = getIntent().getExtras(); 
    	if(extras !=null)
    	{
    		query = extras.getString("queStr");
    	}
    	gamesLogger.i(TAG, SubTag + "Query: " + query);
 		// Cursor	result;
		Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
		this.setTitle("User: " + username);

		Cursor result = getContentResolver().query(tmpUri, null, query, null, null);
		String[] columns = new String[] { "amount", "evYear", "gameType"  };
		int[] to = new int[] { android.R.id.text1, android.R.id.text2 };
		startManagingCursor(result);
		gamesLogger.i(TAG, SubTag + "Everything is ready for the adapter. # of records: " + result.getCount());
		SimpleCursorAdapter items = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, result, columns, to);
		gamesLogger.i(TAG, SubTag + "adapter has been created and populated");
        if ( result.getCount() > 0 )
        {
        	items.setViewBinder(new ShowViewBinder());
        	setListAdapter(items);
        }
        else
        {
        	int duration = Toast.LENGTH_LONG;
        	String text;
        	if ( neededMonth == null )
        		text = "Need to add at least one result!";
        	else
        		text = "There are no entries for the selected month";
			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
			toast.show();
			finish();
        }
	}
	/* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		// TODO Auto-generated method stub
		gamesLogger.i(TAG, SubTag + "Clicked on position: " + position );
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
