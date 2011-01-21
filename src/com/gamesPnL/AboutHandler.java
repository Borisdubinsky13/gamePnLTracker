/**
 * 
 */
package com.gamesPnL;

import com.gamesPnL.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Boris
 *
 */
public class AboutHandler extends Activity
{
	public String TAG="gamePnLTracker";
	public String SubTag="AboutHandler: ";
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_display);

        final TextView appVersion = (TextView)findViewById(R.id.AppVersion);
        String versionName = "";
		try
        {
        	versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
		catch (Exception e)
		{
			Log.e(TAG, SubTag + e.getMessage());
		}
        appVersion.setText(versionName);
        
        final Button dispB = (Button)findViewById(R.id.dispRelNotes);
        dispB.setOnClickListener(new View.OnClickListener()
        {
        	public void onClick(View v) 
        	{
        		setContentView(R.layout.releasenotes);
        	}
	     });
 	}
}
