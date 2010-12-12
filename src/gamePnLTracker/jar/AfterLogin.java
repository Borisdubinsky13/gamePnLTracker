/**
 * 
 */
package gamePnLTracker.jar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

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
		Intent iAbout = new Intent(this, AboutHandler.class);
		Intent iDataEntry = new Intent(this, DataEntry.class);
		Intent iView = new Intent(this, AfterLogin.class);

		// Handle item selection
	    switch (item.getItemId()) 
	    {
	    case R.id.about:
	    	Log.i(TAG, SubTag + "User " + "trying to start ABOUT");
	    	// Intent iAbout = new Intent(new Intent(this, AboutHandler.class));
	        startActivity(iAbout);
	        return true;
	    case R.id.AddResult:
	    	Log.i(TAG, SubTag + "User " + "trying to start SETUP");
	        startActivity(iDataEntry);
	        return true;
	    case R.id.ViewStats:
	    	Log.i(TAG, SubTag + "User " + "trying to start SETUP");
	        startActivity(iView);
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
/*
	}
	
	@Override
	protected void onResume()
	{
*/
		// get all the records with the current id ad add all the amounts
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);
    	
		Cursor	result;
		String	value;
		double	sum = 0;
		double	dValue;
		MyDbAdapter dB = new MyDbAdapter(this);
		final TextView pnlStr = (TextView)findViewById(R.id.PNL);
		final String strHead = this.getString(R.string.cEarnings);
		
		String	query = "SELECT amount FROM gPNLData WHERE name = '" + username+ "';";
		Log.i(TAG, SubTag + "Query: " + query);
		dB.open();

		result = dB.getRecord(query);
		result.moveToFirst();
		while ( !result.isAfterLast() )
		{	
			value = result.getString(0);
			Log.i(TAG, SubTag + "got Value:  " + value);
			dValue = Double.parseDouble(value);
			
			sum += dValue;
			result.moveToNext();
		}
		result.deactivate();
		dB.close();
		Log.i(TAG, SubTag + "Sum:  " + sum);
		pnlStr.setText(strHead + sum);
		if ( sum > 0 )
			pnlStr.setBackgroundColor(0xFF00FF00);
		else
			pnlStr.setBackgroundColor(0xFFFF0000);
	}
}
