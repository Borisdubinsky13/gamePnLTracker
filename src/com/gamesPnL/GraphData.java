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
import android.widget.Toast;

import com.admob.android.ads.AdView;

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
	DecimalFormat df = new DecimalFormat("#,##0.00");
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		gamesLogger.i(TAG, SubTag + "Starting GraphData");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graphdata);
		try
		{
		    AdView	adView = (AdView)findViewById(R.id.adAfterLogin);
		    if ( adView == null )
		    	gamesLogger.e(TAG, SubTag + "AdView not found");
		    else
		    	adView.requestFreshAd();
		}
		catch ( Exception e)
		{
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}
		setContentView(R.layout.graphdata);
		// get all the records with the current id ad add all the amounts
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);
		this.setTitle("User: " + username);
		
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

		String	query = "name = '" + username+ "'";
		
		Cursor result = managedQuery(tmpUri, projection, query, null, null);
		gamesLogger.i(TAG, SubTag + "there are " + result.getCount() + " records" );
		if ( result.getCount() > 0 )
		{
			float[] values = new float[result.getCount()+1];
			float	sum = 0;
			float	min = 0, max = 0;
			if ( result.moveToFirst() )
			{
				gamesLogger.i(TAG, SubTag + "got result back from provider");
				String value;
				float	fValue;
				int		i=0;
				values[i++] = 0;
				do
				{
					value = result.getString(result.getColumnIndex("amount"));
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
				gamesLogger.i(TAG, SubTag + "No Data returned from Content Provider");
			
			String[] verlabels = new String[] { "$" + df.format(max), "$" + df.format(((min+max)/2)), "$" + df.format(min) };
			String[] horlabels = new String[] { "", "" };
			GraphView graphView = new GraphView(this, values, "Running Total", horlabels, verlabels, GraphView.LINE);
			setContentView(graphView);
		}
		else
        {
        	int duration = Toast.LENGTH_LONG;
			String text = "Need to add at least one result!";
			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
			toast.show();
			finish();
        }
	}
}
