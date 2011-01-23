
/**
 * 
 */
package com.gamesPnL;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author cbd013
 *
 */
public class AddGame extends Activity 
{
	public String TAG="gamePnLTracker";
	public String SubTag="AddGame";
	
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addgame);
		
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	String username = pref.getString(PREF_USERNAME, null);
    	
    	this.setTitle("User: " + username);

		
		final Button addGButton = (Button)findViewById(R.id.AddGBtn);
		final EditText gameName = (EditText)findViewById(R.id.gameName);
		final EditText gameDesc = (EditText)findViewById(R.id.gameDescr);
		addGButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) 
            {
            	ContentValues vals = new ContentValues();
            	ContentResolver cr = getContentResolver();
    	    	SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);   
    	    	String username = pref.getString(PREF_USERNAME, null);
            	
            	Log.i(TAG, SubTag + "AddGame button is clicked");
            	vals.put("game", gameName.getText().toString());
            	vals.put("description", gameDesc.getText().toString());
            	vals.put("addedBy", username);
    			Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
    			tmpUri = Uri.withAppendedPath(tmpUri,"pnlgames");
    			Log.i(TAG, SubTag + "Got URI populated");        			
    			cr.insert(tmpUri, vals);            	
            	finish();
            }
        });
		
		
	}

}
