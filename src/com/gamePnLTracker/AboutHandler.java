/**
 * 
 */
package com.gamePnLTracker;

import com.admob.android.ads.AdView;
import com.gamePnLTracker.R;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Boris
 *
 */
public class AboutHandler extends Activity
{
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
/*
        AdManager.setTestDevices(new String[] 
    	{
        		AdManager.TEST_EMULATOR,
        		"426F72697360732050686F6E65"
        });
*/
        AdView	adView = (AdView)findViewById(R.id.adAbout);
        adView.requestFreshAd();
 	}
}
