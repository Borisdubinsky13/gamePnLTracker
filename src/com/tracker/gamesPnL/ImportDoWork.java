package com.tracker.gamesPnL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ProgressBar;

public class ImportDoWork extends Activity {
	
	public String TAG="gamePnLTracker";
	public String SubTag="ImportDoWork: ";
	private static String	fnoSDName = "/gamesPnL.csv";
	
	public String username;
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	
	private	ProgressDialog	progressDialog;
	private	ProgressBar progressBar;

	private void doWork()
	{
		// try opening the myfilename.txt
		try	{
			gamesLogger.i(TAG, SubTag + "Starting an import");
			Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
			tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
			ContentResolver cr = getContentResolver();
			gamesLogger.i(TAG, SubTag + "Got URI populated");
			// open the file for reading
			File	f = new File(Environment.getExternalStorageDirectory()+fnoSDName);
			long	fSize = f.length();
			long	currentCount = 0;
			int		percentDone;
			
			gamesLogger.i(TAG, SubTag + "File: " + Environment.getExternalStorageDirectory()+fnoSDName);
			gamesLogger.i(TAG, SubTag + "File size: " + fSize);
			
			FileInputStream instream = new FileInputStream(f);
		    // if file the available for reading
		    if (instream != null)
		    {
		    	// prepare the file for reading
		    	InputStreamReader inputreader = new InputStreamReader(instream);
		    	BufferedReader buffreader = new BufferedReader(inputreader);
		    	
		    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
		    	String username = pref.getString(PREF_USERNAME, null);
		 
		    	String line;
		    	int	lineNumber = 0;
		    	String	delimeter = null;
		    	
		    	// read every line of the file into the line-variable, on line at the time
		    	while (( line = buffreader.readLine()) != null) 
		    	{
		    		int	startExt;
		    		int endExt;
		    	
		    		currentCount += line.length();
		    		// Figure out the separation field. First line only is the heading, so the first word is "Date". 
		    		// extract the 5th character and that should be the separation character
		    		if ( lineNumber == 0 )
		    		{
		    			startExt = 4;
		    			endExt = 5;
		    			delimeter = line.substring(startExt, endExt);
		    			lineNumber++;
		    		}
		    		else
		    		{
			    		// parse the line and extract the comma separated fields.
			    		startExt = 0;
			    		endExt = line.indexOf(delimeter);
			    		String	date = line.substring(0, endExt);
			    		String	evMonth = date.substring(0,2);
			    		String	evDay = date.substring(3,5);
			    		String	evYear = date.substring(6);
			    		line = line.substring(endExt+1);
			    		endExt = line.indexOf(delimeter);
			    		String evType = line.substring(0, endExt);
			    		line = line.substring(endExt+1);
			    		endExt = line.indexOf(delimeter);
			    		String gameType = line.substring(0, endExt);
			    		line = line.substring(endExt+1);
			    		endExt = line.indexOf(delimeter);
			    		String gameLimit = line.substring(0, endExt);
			    		line = line.substring(endExt+1);
			    		endExt = line.indexOf(delimeter);
			    		String amount = line.substring(0, endExt);
			    		
			    		ContentValues vals = new ContentValues();
		            	vals.put("name", username);
		            	vals.put("amount", amount);
		            	vals.put("evYear", evYear);
		            	vals.put("evMonth", evMonth);
		            	vals.put("evDay", evDay);
		            	date = evYear + "-" + evMonth + "-" + evDay;
		            	vals.put("evDate", date);
		            	vals.put("eventType", evType);
		            	vals.put("gameType", gameType);
		            	vals.put("gameLimit", gameLimit);
        			
		    			cr.insert(tmpUri, vals);            	
			    		lineNumber++;
			    		
			    		// Update the progress bar if needed
			    		percentDone = (int) (currentCount * 100 / fSize);
			    		gamesLogger.i(TAG, SubTag + "currentCount: " + currentCount);
			    		gamesLogger.i(TAG, SubTag + "Progress: " + percentDone + "%");
			    		if ( percentDone <= 100 )
			    		{
			    			gamesLogger.i(TAG, SubTag + "Updating progress bar to " + percentDone);
			    			progressBar.setProgress(percentDone);
			    			progressDialog.setProgress(percentDone);
			    		}
		    		}
		    	}
		    }
		    // close the file
		    instream.close();
		}
		catch (Exception e) 
		{
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}
	}
	
	private void actualDoImport()
	{
        setContentView(R.layout.prgrssbar);
        gamesLogger.i(TAG, SubTag + "Creating progress dialog" );

        try {
        	progressBar =(ProgressBar)findViewById(R.id.progressBar);
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Loading...");
            
        	gamesLogger.i(TAG, SubTag + "Got the progress bar!" );
        	// progressBar.setVisibility(1);
        	gamesLogger.i(TAG, SubTag + "Starting the work" );
        	doWork();
        	finish();
        	// progressBar.setVisibility(0);
        }
	   	catch (Exception e) 
	   	{
	   		gamesLogger.i(TAG, SubTag + e);
	   	}
	} 
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		actualDoImport();
	}

}
