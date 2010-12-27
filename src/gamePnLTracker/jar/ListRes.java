/**
 * 
 */
package gamePnLTracker.jar;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

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
		Log.i(TAG, SubTag + "Started ListRes started");
		super.onCreate(savedInstanceState);
		
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);
		String	query = "name = '" + username+ "'";

		Cursor	result;
		Uri	tmpUri = Uri.parse("content://gamePnLTracker.provider.userContentProvider");
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
