package com.tracker.gamesPnL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.tracker.gamesPnL.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ImportActivityYN extends Activity 
{
	public String TAG="gamePnLTracker";
	public String SubTag="ImportActivity: ";
	
	public String username;
	public static final String PREFS_NAME = "gamePnLTrackerFile";

	// private	ProgressDialog	progressDialog;
	private	AlertDialog alert;
	
	private void doImport()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Importing file: \n/mnt/sdcard/gamesPnL.csv\n\nDo you want overwrite existing data?");
		builder.setCancelable(false);
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int id) 
		    {
				gamesLogger.i(TAG, SubTag + "Keep current database");
				/* Perform actual import */
				Intent i = new Intent(ImportActivityYN.this, ImportDoWork.class);
				alert.dismiss();
				
		        startActivity(i);
				finish();
		    }
		});
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int id) 
			{
				gamesLogger.i(TAG, SubTag + "Removing the database");

				ContentResolver cr = getContentResolver();
				gamesLogger.i(TAG, SubTag + "Got content resolver");
				Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
				tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");        			
				cr.delete(tmpUri, null, null);
				gamesLogger.i(TAG, SubTag + "Results data is removed");

				/* Perform actual import */
				Intent i = new Intent(ImportActivityYN.this, ImportDoWork.class);
				
				alert.dismiss();
		        startActivity(i);

				finish();
			}
		});
		alert = builder.create();
		alert.show();
	};
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        doImport();
	}
}
