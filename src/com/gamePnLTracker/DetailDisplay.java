/**
 * 
 */
package com.gamePnLTracker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author Boris
 *
 */
public class DetailDisplay extends Activity 
{
	public String TAG="gamePnLTracker";
	public String SubTag="DetailDisplay: ";
	
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_ID = "dataTBL_ID";
	private static final String PREF_USERNAME = "username";
	private String idIndex="";
	private String name = "";
	private String	workRecord = "";
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.displayitem);
		
		Log.i(TAG, SubTag + "Started.");
		SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
		name = pref.getString(PREF_USERNAME, null);
		idIndex = pref.getString(PREF_ID, null);
		int	maxI = Integer.parseInt(idIndex);
		
		Log.i(TAG, SubTag + "Working with record #" + idIndex + " Name: " + name );
		// Get the record with all the values, populate all the fields for display
		String	query = "name = '" + name + "'";
		Cursor	result;
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
		Log.i(TAG, SubTag + "Number of records:  " + result.getCount());
		
		if ( result.moveToFirst() )
		{
			int	tmp = maxI;
			Log.i(TAG, SubTag + "Scanning " + tmp + "records");
			for ( int i = 0; i < maxI && result.moveToNext(); i++ )
				Log.i(TAG, SubTag + "Scanned record " + i);
		}
		Log.i(TAG, SubTag + "ID: " + result.getString(0));
		workRecord = result.getString(0);
		Log.i(TAG, SubTag + "Name: " + result.getString(1));
		Log.i(TAG, SubTag + "Amount: " + result.getString(2));
		Log.i(TAG, SubTag + "Date: " + result.getString(3));
		EditText amount = (EditText)findViewById(R.id.Amount);
        amount.setText(result.getString(2));
        Button dateB = (Button)findViewById(R.id.dateButton);
        dateB.setText(result.getString(3));
        	
        final Button deleteB = (Button)findViewById(R.id.delete);
        deleteB.setOnClickListener(new View.OnClickListener()
        {
        	public void onClick(View v) 
        	{
        		Log.i(TAG, SubTag + "Deleting record with ID# " + workRecord);
		    	ContentResolver cr = getContentResolver();
		    	String	query = "_ID = '" + workRecord + "'";
		    	Uri	tmpUri = Uri.parse("content://com.gamePnLTracker.provider.userContentProvider");
		    	 
		    	tmpUri = Uri.withAppendedPath(tmpUri,"pnl1record");
		    	cr.delete(tmpUri, query, null);
		    	finish();
        	}
	     });
	     final Button updateB = (Button)findViewById(R.id.update);
	     updateB.setOnClickListener(new View.OnClickListener()
	     {
		     public void onClick(View v) 
		     {
		    	 
		     }

	     });
	}
}
