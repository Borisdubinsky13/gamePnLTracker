package com.gamesPnL;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.gamesPnL.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class loginHandler extends Activity 
{
    /** Called when the activity is first created. */
	public static String TAG="gamePnLTracker";
	public static String SubTag="loginHandler: ";
	
	// private MyDbAdapter dB = new MyDbAdapter(this);
	
	int duration = Toast.LENGTH_SHORT;
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	CharSequence text = "No text";
	
	public static String getMd5Hash(String input) 
	{
        try {
        	MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1,messageDigest);
            String md5 = number.toString(16);
           
            while (md5.length() < 32)
            	md5 = "0" + md5;
           
            return md5;
        } 
        catch(NoSuchAlgorithmException e) 
        {
        	gamesLogger.e(TAG, SubTag + e.getMessage());
            return null;
        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.preloginmenu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		Intent iAbout = new Intent(this, AboutHandler.class);
		Intent iSetup = new Intent(this, SetupWin.class);

		// Handle item selection
	    switch (item.getItemId()) 
	    {
	    case R.id.aboutMenu:
	    	gamesLogger.i(TAG, SubTag + "User " + "trying to start ABOUT");
	    	// Intent iAbout = new Intent(new Intent(this, AboutHandler.class));
	        startActivity(iAbout);
	        return true;
	    case R.id.setup:
	    	gamesLogger.i(TAG, SubTag + "User " + "trying to start SETUP");
	        startActivity(iSetup);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	// See if this is running on the emulator
    	String androidId = Settings.Secure.getString 
    		(this.getContentResolver(), 
    	 	android.provider.Settings.Secure.ANDROID_ID);
    	if ( androidId == null )
    	{
    		// We are running on the emulator. Debugging should be ON.
    		gamesLogger.enableLogging(Log.VERBOSE);
    	}
    	else
    	{
    		// We are running on a phone. Debugging should be OFF.
    		gamesLogger.enableLogging(Log.ERROR);
    	}
    	 
    	gamesLogger.i(TAG, SubTag + " Enter onCreate() ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // First check if there are any users. If not, then switch to setup window to add first user.
		Cursor	result;
		Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		tmpUri = Uri.withAppendedPath(tmpUri,"users");
		result = managedQuery(tmpUri, null, null, null, null);
		if ( result.getCount() < 1 )
		{
			// There are no users in the database. Start a setup intent
			Intent iSetup = new Intent(this, SetupWin.class);
			startActivity(iSetup);
		}
        // See if user is still logged in. When user logges out, the status of the user in status table will change to false.
		tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		tmpUri = Uri.withAppendedPath(tmpUri,"pnlstatus");
		String[] projection = new String[] {
				"_id",
				"name",
				"status"
		};
		gamesLogger.i(TAG, SubTag + "Got URI populated");
		String	query = " status = 'in'";
		result = managedQuery(tmpUri, projection, query, null, null);
		gamesLogger.i(TAG, SubTag + "got " + result.getCount() + " records");
		if ( result.getCount() >= 1 )
		{
			// There is an active user. Skip the login window.
        	Intent iDataEntry = new Intent(loginHandler.this,AfterLogin.class);
        	
        	result.moveToFirst();
            getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
           	.edit()
        	.putString(PREF_USERNAME, result.getString(1).toString())
        	.commit();

	        startActivity(iDataEntry);
	        finish();
		}
		
        final Button loginB = (Button)findViewById(R.id.loginB);
        loginB.setOnClickListener(new View.OnClickListener()
        {
        	public boolean isValidUser(String id, String pass)
        	{
        		gamesLogger.i(TAG, SubTag + "User " + id + " is being validated");
        		if ( id.equals("") || pass.equals("") ) 
        		{
        			text = "Please specify user and password!";
        			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        			toast.show();
        			return false;
        		}
        		else
        		{
        			// Check to make sure id and password are in the database.
        			Cursor	result;
        			int		cnt;
        			String[] projection = new String[] {
        					"name"
        			};
        			String selection = "name = '" + id + "' and passwd = '" + pass + "'";
        			
        			ContentResolver cr = getContentResolver();
        			gamesLogger.i(TAG, SubTag + "Got content resolver");
        			Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
        			tmpUri = Uri.withAppendedPath(tmpUri,"users");
        			gamesLogger.i(TAG, SubTag + "Got URI populated");    
        			result = cr.query(tmpUri, projection, selection, null, null);
        			gamesLogger.i(TAG, SubTag + "Populated cursor");
        			cnt = result.getCount();
        			gamesLogger.i(TAG, SubTag + "got count " + cnt);
        			result.deactivate();
        			gamesLogger.i(TAG, SubTag + "deactivating cursor");
        			
        			if ( cnt > 0 )
        				return true;
        			else
        			{
            			text = "Invalid login information. Try again.";
            			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
            			toast.show();
        				return false;
        			}
        		}
        	}

            public void onClick(View v) 
            {
            	gamesLogger.i(TAG, SubTag + "Login button is clicked");
            	// get password 
            	EditText idPass = (EditText)findViewById(R.id.passwd);
            	// Setup a preference class that would keep the user id
            	EditText idName = (EditText)findViewById(R.id.idName);
            	String md5hash = getMd5Hash(idPass.getText().toString());
                // go to main window
                if ( isValidUser(idName.getText().toString(), md5hash) )
                {
                	// Store current user into a database
        			Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
        			tmpUri = Uri.withAppendedPath(tmpUri,"pnlstatus");
                	ContentValues vals = new ContentValues();
                	ContentResolver cr = getContentResolver();
                	vals.put("name", idName.getText().toString());
                	vals.put("status", "in");
                	cr.insert(tmpUri, vals);
                	
                	// store user id in Preferences for everybody to access.
                    getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                	.edit()
                	.putString(PREF_USERNAME, idName.getText().toString())
                	.commit();
                	Intent iDataEntry = new Intent(loginHandler.this,AfterLogin.class);
                	gamesLogger.i(TAG, SubTag + "trying to start afterlogin Activity");
        	        startActivity(iDataEntry);    
                }
            }
        });
    }
}
