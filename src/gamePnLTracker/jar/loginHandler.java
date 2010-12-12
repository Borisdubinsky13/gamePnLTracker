package gamePnLTracker.jar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
	public String TAG="gamePnLTracker";
	public String SubTag="loginHandler: ";
	
	private MyDbAdapter dB = new MyDbAdapter(this);
	
	int duration = Toast.LENGTH_SHORT;
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	CharSequence text = "No text";
	
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
	    	Log.i(TAG, SubTag + "User " + "trying to start ABOUT");
	    	// Intent iAbout = new Intent(new Intent(this, AboutHandler.class));
	        startActivity(iAbout);
	        return true;
	    case R.id.setup:
	    	Log.i(TAG, SubTag + "User " + "trying to start SETUP");
	        startActivity(iSetup);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final Button loginB = (Button)findViewById(R.id.loginB);
        loginB.setOnClickListener(new View.OnClickListener()
        {
        	public boolean isValidUser(String id, String pass)
        	{
        		Log.i(TAG, SubTag + "User " + id + " is being validated");
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
        			String	query = "SELECT name,passwd FROM gUsers WHERE name = '" + id + "' AND passwd = '" + pass + "';";
        			Log.i(TAG, SubTag + "Query: " + query);
        			dB.open();
        			Log.i(TAG, SubTag + "Database is opened");
        			result = dB.getRecord(query);
        			Log.i(TAG, SubTag + "Populated cursor");
        			cnt = result.getCount();
        			Log.i(TAG, SubTag + "got count " + cnt);
        			result.deactivate();
        			Log.i(TAG, SubTag + "deactivating cursor");
        			dB.close();
        			Log.i(TAG, SubTag + "closing database");
        			
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
            	Log.i(TAG, SubTag + "Login button is clicked");
            	// get password 
            	EditText idPass = (EditText)findViewById(R.id.passwd);
            	// Setup a preference class that would keep the user id
            	EditText idName = (EditText)findViewById(R.id.idName);
            	
                getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                	.edit()
                	.putString(PREF_USERNAME, idName.getText().toString())
                	.commit();
                // go to main window
                if ( isValidUser(idName.getText().toString(), idPass.getText().toString()) )
                {
                	Intent iDataEntry = new Intent(loginHandler.this, AfterLogin.class);
        	    	Log.i(TAG, SubTag + "trying to start afterlogin Activity");
        	        startActivity(iDataEntry);         	
                }
            }
        });
    }
}
