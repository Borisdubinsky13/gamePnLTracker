/**
 * 
 */
package com.gamesPnL;

import com.admob.android.ads.AdView;
import com.gamesPnL.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
/*
        AdManager.setTestDevices(new String[] 
    	{
        		AdManager.TEST_EMULATOR,
        		"426F72697360732050686F6E65"
        });
*/
        AdView	adView = (AdView)findViewById(R.id.adAbout);
        adView.requestFreshAd();
        
        final Button deleteB = (Button)findViewById(R.id.dispRelNotes);
        deleteB.setOnClickListener(new View.OnClickListener()
        {
        	public void onClick(View v) 
        	{
        		setContentView(R.layout.releasenotes);
        	}
	     });
 	}
}