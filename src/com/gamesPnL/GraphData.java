/**
 * 
 */
package com.gamesPnL;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * @author Boris
 *
 */
public class GraphData extends Activity 
{
	public String TAG="gamePnLTracker";
	public String SubTag="GraphData: ";
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	public String currentUser = new String();
	DecimalFormat df = new DecimalFormat("#,###.00");
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		Log.e(TAG, SubTag + "Starting GraphData");
		super.onCreate(savedInstanceState);
/*		
        AdView	adView = (AdView)findViewById(R.id.adAfterLogin);
        adView.requestFreshAd();		
*/		
		setContentView(R.layout.graphdata);
		
		Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
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
		// get all the records with the current id ad add all the amounts
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);

		String	query = "name = '" + username+ "'";
		
		Cursor result = managedQuery(tmpUri, projection, query, null, null);
		Log.i(TAG, SubTag + "there are " + result.getCount() + " records" );

		float[] values = new float[result.getCount()+1];
		float	sum = 0;
		float	min = 0, max = 0;
		if ( result.moveToFirst() )
		{
			Log.i(TAG, SubTag + "got result back from provider");
			String value;
			float	fValue;
			int		i=0;
			values[i++] = 0;
			do
			{
				value = result.getString(2);
				if ( value.equals("") )
					fValue = (float) 0.0;
				else
					fValue = Float.parseFloat(value);
				sum += fValue;
				values[i++] = sum;
				if ( i == 1 )
				{
					// First read value. Use it as a min and a max
					min = sum;
					max = sum;
				}
				else
				{
					if ( sum > max)
						max = sum;
					if ( sum < min )
						min = sum;
				}
			} while (result.moveToNext());

		}
		else
			Log.i(TAG, SubTag + "No Data returned from Content Provider");
		
		String[] verlabels = new String[] { "$" + df.format(max), "$" + df.format(((min+max)/2)), "$" + df.format(min) };
		String[] horlabels = new String[] { "", "" };
		GraphView graphView = new GraphView(this, values, "Running Total", horlabels, verlabels, GraphView.LINE);
		setContentView(graphView);
	}
}
