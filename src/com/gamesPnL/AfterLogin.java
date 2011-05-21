/**
 * 
 */
package com.gamesPnL;

import java.text.DecimalFormat;
import java.util.Calendar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.ads.*;

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
	// public String currentUser = new String();

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
		// get all the records with the current id ad add all the amounts
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);
		// Handle item selection
	    switch (item.getItemId()) 
	    {
	    case R.id.About:
	    	gamesLogger.i(TAG, SubTag + "trying to start ABOUT");
	    	Intent iAbout = new Intent(this, AboutHandler.class);
	        startActivity(iAbout);
	        return true;
	    case R.id.AddResult:
	    	gamesLogger.i(TAG, SubTag + "trying to start SETUP");
	    	Intent iDataEntry = new Intent(this, DataEntry.class);
	        startActivity(iDataEntry);
	        return true;
	    case R.id.ViewStats:
	    	gamesLogger.i(TAG, SubTag + "trying to start DisplayQueryData");
			// Intent iViewRes = new Intent(this, ListRes.class);
	    	Intent iViewRes = new Intent(this, DisplayQueryData.class);
	        startActivity(iViewRes);
	        return true;
	    case R.id.AddGame:
	    	gamesLogger.i(TAG, SubTag + "trying to start Add Game");
	    	Intent iViewAddGame = new Intent(this, AddGame.class);
	        startActivity(iViewAddGame);
	        return true;
	    case R.id.exportDB:
	    	gamesLogger.i(TAG, SubTag + "trying to export data");
	      	SQLiteDatabase checkDB = null;
	      	DatabaseAssistant dba = null;
	        String DB_PATH = "/data/data/com.gamesPnL/databases/";   
	        String DB_NAME = "gamepnltracker.db";
	     	      	 
	    	try
	    	{
	    		String myPath = DB_PATH + DB_NAME;
	    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
	    		gamesLogger.i(TAG, SubTag + "database is open for export");
	    		dba = new DatabaseAssistant(getBaseContext(), checkDB);
	    		gamesLogger.i(TAG, SubTag + "db assisant is ready for export");
	    	}
	    	catch(SQLiteException e)
	    	{
	    		gamesLogger.e(TAG, SubTag + "export data FAILED");
	    	}
	    	if ( dba != null )
	    		dba.exportData();
	    	return true;
	    case R.id.Logout:
	    	gamesLogger.i(TAG, SubTag + "Logging user out");
	    	
   			Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
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
            gamesLogger.i(TAG, SubTag + "trying to logout");
	        finish();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.afterlogin);
	}
	
	@Override
	protected void onResume()
	{
		super.onPause();
		setContentView(R.layout.afterlogin);

		gamesLogger.i(TAG, SubTag + "Trying to get the add");
		AdView	adView = (AdView)findViewById(R.id.adAfterLogin);

	    // Initiate a generic request to load it with an ad
	    adView.loadAd(new AdRequest());
	    gamesLogger.i(TAG, SubTag + "Got the add");
		// get all the records with the current id ad add all the amounts
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);
    	
    	this.setTitle("User: " + username);

		Cursor	result;
		String	value;
		double	sum = 0;
		double	dValue;
		double	lastResult = 0;
		DecimalFormat df = new DecimalFormat("$#,##0.00");
		TextView pnlStr = (TextView)findViewById(R.id.PNL);
		final String strHead = this.getString(R.string.cEarnings);
		final TextView strHeadStr = (TextView)findViewById(R.id.PNLLabel);

		// Get total career earnings
		String	query = "name = '" + username+ "'";
		Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
		String[] projection = new String[] {
				"_id",
				"uid",
				"name",
				"amount",
				"year",
				"month",
				"day",
				"gameType",
				"gameLimit",
				"eventType",
				"notes"
		};
		
		// result = getContentResolver().query(tmpUri, null, null, null, null);
		result = managedQuery(tmpUri, projection, query, null, null);
		gamesLogger.i(TAG, SubTag + "there are " + result.getCount() + " records" );
		
		if ( result.moveToFirst() )
		{
			gamesLogger.i(TAG, SubTag + "got result back from provider");
			do
			{
				value = result.getString(result.getColumnIndex("amount"));
				gamesLogger.i(TAG, SubTag + "got Value:  " + value);
				if ( value.equals("") )
					dValue = 0;
				else
					dValue = Double.parseDouble(value);
				sum += dValue;
				lastResult = dValue;
				
			} while (result.moveToNext());
		}
		else
			gamesLogger.i(TAG, SubTag + "No Data returned from Content Provider");
				
		gamesLogger.i(TAG, SubTag + "Sum:  " + df.format(sum));
		strHeadStr.setText(strHead);
		pnlStr.setTextColor(getResources().getColor(android.R.color.background_light));
		pnlStr.setText(df.format(sum));
		if ( sum >= 0 )
			pnlStr.setBackgroundColor(0xFF00A000);
		else
			pnlStr.setBackgroundColor(0xFFA00000);

	    final Calendar c = Calendar.getInstance(); 
	    int mYear = c.get(Calendar.YEAR); 
	    int mMonth = c.get(Calendar.MONTH); 

	    // Get this month earnings
		query = "name = '" + username+ "'";
        
        query += " AND evMonth = '" + String.valueOf(mMonth) + "'";
        query += " AND evYear = '" + String.valueOf(mYear) + "'";
		result = managedQuery(tmpUri, projection, query, null, null);
		gamesLogger.i(TAG, SubTag + "there are " + result.getCount() + " records" );
		
		if ( result.moveToFirst() )
		{
			gamesLogger.i(TAG, SubTag + "got result back from provider");
			do
			{
				value = result.getString(result.getColumnIndex("amount"));
				gamesLogger.i(TAG, SubTag + "got Value:  " + value);
				if ( value.equals("") )
					dValue = 0;
				else
					dValue = Double.parseDouble(value);
				sum += dValue;
				
			} while (result.moveToNext());
		}
		else
			gamesLogger.i(TAG, SubTag + "No Data returned from Content Provider");
				
		gamesLogger.i(TAG, SubTag + "Sum:  " + df.format(sum));
		strHeadStr.setText(strHead);
		pnlStr = (TextView)findViewById(R.id.PNLMonth);
		pnlStr.setTextColor(getResources().getColor(android.R.color.background_light));
		pnlStr.setText(df.format(sum));
		if ( sum >= 0 )
			pnlStr.setBackgroundColor(0xFF00A000);
		else
			pnlStr.setBackgroundColor(0xFFA00000);

	    // Get last event result
				
		gamesLogger.i(TAG, SubTag + "Last Result:  " + df.format(lastResult));
		strHeadStr.setText(strHead);
		pnlStr = (TextView)findViewById(R.id.PNLLast);
		pnlStr.setTextColor(getResources().getColor(android.R.color.background_light));
		pnlStr.setText(df.format(lastResult));
		if ( lastResult >= 0 )
			pnlStr.setBackgroundColor(0xFF00A000);
		else
			pnlStr.setBackgroundColor(0xFFA00000);

		gamesLogger.i(TAG, SubTag + "Done!");				
	}
}
