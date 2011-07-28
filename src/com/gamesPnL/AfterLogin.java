/**
 * 
 */
package com.gamesPnL;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

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
	String[] projection = new String[] {
			"_id",
			"name",
			"mDate",
			"dPrsr",
			"sPrsr",
			"pulse"
	};

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
	    case R.id.Analysis:
	    	gamesLogger.i(TAG, SubTag + "trying to start DataAnalysis");
			// Intent iViewRes = new Intent(this, ListRes.class);
	    	Intent iViewAnalysis = new Intent(this, DataAnalysis.class);
	        startActivity(iViewAnalysis);
	        return true;
	    case R.id.AddGame:
	    	gamesLogger.i(TAG, SubTag + "trying to start Add Game");
	    	Intent iViewAddGame = new Intent(this, AddGame.class);
	        startActivity(iViewAddGame);
	        return true;
	    case R.id.exportDB:
	    	gamesLogger.i(TAG, SubTag + "trying to export data");
/*
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
*/	    	
	    	try
	    	{
	    		String	fname = "/mnt/sdcard/gamesPnL.csv";
	    		gamesLogger.i(TAG, SubTag + "User " + "Trying to export data to CSV. File: " + fname);
		    	File myFile = new File( fname );
				myFile.createNewFile();
				FileOutputStream fOut =  new FileOutputStream(myFile);
				BufferedOutputStream bos = new BufferedOutputStream( fOut );
				
	   			Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
				tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
				
				String query = "name = '" + username + "'";
				gamesLogger.i(TAG, SubTag + "Query: " + query);
				Cursor result = managedQuery(tmpUri, projection, query, null, null);
				gamesLogger.i(TAG, SubTag + "there are " + result.getCount() + " records" );
    			if ( result.getCount() > 0 )
    			{
    				if ( result.moveToFirst() )
    				{
    					String	strOut = "Date,EventType,GameType,GameLimit,Amount,Name,Note\n";	
    					bos.write(strOut.getBytes());

    					do
    					{
    						String	nameStr = "";
    						String	evDateStr = "";
    						String	evTypeStr = "";
    						String	gameTypeStr = "";
    						String	gameLimitStr = "";
    						String	amountStr = "";
    						String	noteStr = "";
    						
    						nameStr = result.getString(result.getColumnIndex("name"));
    						evDateStr = result.getString(result.getColumnIndex("evMonth")) + "/" + 
    									result.getString(result.getColumnIndex("evDay")) + "/" +
    									result.getString(result.getColumnIndex("evYear"));
    						evTypeStr = result.getString(result.getColumnIndex("eventType"));
    						gameTypeStr = result.getString(result.getColumnIndex("gameType"));
    						gameLimitStr = result.getString(result.getColumnIndex("gameLimit"));
    						amountStr = result.getString(result.getColumnIndex("amount"));
    						noteStr = result.getString(result.getColumnIndex("notes"));
    						
    						strOut = evDateStr + "," + evTypeStr + "," + gameTypeStr  + "," + gameLimitStr + "," + 
    										amountStr + "," + nameStr + "," + noteStr + "\n";	
    						gamesLogger.i(TAG, SubTag + "Writing: " + strOut);
    						bos.write(strOut.getBytes());
    					} while (result.moveToNext());
    				}
    				result.close();
    				bos.close();

    				int duration = Toast.LENGTH_LONG;
        			String text = "Data has been exported into " + fname;
        			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        			toast.show();
    			}
    			else
    			{
    				int duration = Toast.LENGTH_SHORT;
        			String text = "No data to export";
        			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        			toast.show();
    			}
            
	    	}
	    	catch (Exception e)
	    	{
	    		gamesLogger.e(TAG, SubTag + e.getMessage());
	    	}
	    	
	    	
	    	return true;
/*	    	
	    case R.id.Logout:
	    	gamesLogger.i(TAG, SubTag + "Logging user out");
	    	
   			Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
			tmpUri = Uri.withAppendedPath(tmpUri,"pnlstatus");

        	String	query = "name = '" + username+ "'";
        	ContentResolver cr = getContentResolver();
        	cr.delete(tmpUri, query, null);
        	
            getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
        	.edit()
        	.putString(PREF_USERNAME, "")
        	.commit();

            gamesLogger.i(TAG, SubTag + "trying to logout");
	        finish();
	        return true;
*/	        
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		
    	// See if this is running on the emulator
    	String androidId = Settings.Secure.getString 
    		(this.getContentResolver(), 
    	 	android.provider.Settings.Secure.ANDROID_ID);
    	if ( androidId == null || androidId.equals("9774d56d682e549c"))
    	{
    		// We are running on the emulator. Debugging should be ON.
    		gamesLogger.e(TAG, SubTag + "Enablig VERBOSE debugging. androidID = " + androidId);
    		gamesLogger.enableLogging(Log.VERBOSE);
    	}
    	else
    	{
    		// We are running on a phone. Debugging should be OFF.
    		gamesLogger.e(TAG, SubTag + "Enablig ERRORS only debugging. androidID = " + androidId);
    		gamesLogger.enableLogging(Log.ERROR);
    	}
    	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.afterlogin);
		
		// First check if there are any users. If not, then switch to setup window to add first user.
		try
		{
			
			Uri tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
			tmpUri = Uri.withAppendedPath(tmpUri,"pnlstatus");
			String[] projection = new String[] {
					"_id",
					"name",
					"status"
			};
			String	query = " status = 'in'";
			String	oldUsr = null;
			Cursor result = managedQuery(tmpUri, projection, query, null, null);
			gamesLogger.i(TAG, SubTag + "got " + result.getCount() + " records");
			if ( result.getCount() >= 1 )
			{
	        	result.moveToFirst();
	        	oldUsr = result.getString(result.getColumnIndex("name"));
	            gamesLogger.i(TAG, SubTag + "ID of the user ");
			}
	        Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
	        gamesLogger.i(TAG, SubTag + "Got account list. Number of entries: " + accounts.length);
	        if ( accounts.length <= 0 )
	        {
	        	int duration = Toast.LENGTH_SHORT;
				String text = "Primary account is not setup. Please setup Google account first.";
				Toast toast = Toast.makeText(getApplicationContext(), text, duration);
				toast.show();
	        }
	        else
	        {
		        String username = accounts[0].name; 
	            getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
	           	.edit()
	        	.putString(PREF_USERNAME, username)
	        	.commit();
		        gamesLogger.i(TAG, SubTag + "My email id that I want: " + username); 
		 
		        // update database to make sure that all entries have username = primary google account.
		        // See if there are any data that has name other then current username
		        
	   			tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
    			tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
    			
    			// Update the table with the new id
            	ContentValues vals = new ContentValues();
            	ContentResolver cr = getContentResolver();
            	vals.put("name", username);
            	if ( oldUsr == null )
            		cr.update(tmpUri, vals, "name != '" + username + "'", null);
            	else
            		cr.update(tmpUri, vals, "name = '" + oldUsr + "'", null);
	        }
		}
		catch (Exception e)
		{
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}
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
	    int mMonth = c.get(Calendar.MONTH) + 1; 

	    // Get this month earnings
		query = "name = '" + username+ "'";
		String startDate = String.format("%04d", mYear) + "-" + String.format("%02d",mMonth) + "-01";
        query += " AND evDate >= '" + startDate + "'";
		result = managedQuery(tmpUri, projection, query, null, null);
		gamesLogger.i(TAG, SubTag + "there are " + result.getCount() + " records" );
		
		sum = 0;
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

		if ( result != null )
			result.close();
		gamesLogger.i(TAG, SubTag + "Done!");				
	}
}
