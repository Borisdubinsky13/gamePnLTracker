/**
 * 
 */
package com.gamesPnL;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.gamesPnL.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Boris
 *
 */
public class SetupWin extends Activity 
{
	public static String TAG="gamePnLTracker";
	public static String SubTag="setupHandler: ";

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
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
        } catch(NoSuchAlgorithmException e) {
                Log.e(TAG, SubTag + e.getMessage());
                return null;
        }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.setupscr);
		final Button addB = (Button)findViewById(R.id.add);
		final Button cancelB = (Button)findViewById(R.id.clear);

        addB.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) 
            {
            	
            	ContentValues	vals = new ContentValues();
            	EditText nm = (EditText)findViewById(R.id.nameSup);
            	EditText em = (EditText)findViewById(R.id.emailSup);
            	EditText pss = (EditText)findViewById(R.id.passSup);
            	String md5hash = getMd5Hash(pss.getText().toString());
            	
            	Log.i(TAG, SubTag + "Add button is clicked");
            	Log.i(TAG, SubTag + "Name: " + nm.getText().toString());
            	Log.i(TAG, SubTag + "e-mail: " + em.getText().toString());
            	Log.i(TAG, SubTag + "Pass: " + md5hash);
            	vals.put("name", nm.getText().toString());
            	vals.put("email", em.getText().toString());
            	vals.put("passwd", md5hash);

    			ContentResolver cr = getContentResolver();
    			Log.i(TAG, SubTag + "Got content resolver");
    			Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
    			tmpUri = Uri.withAppendedPath(tmpUri,"users");
    			Log.i(TAG, SubTag + "Got URI populated");        			
    			cr.insert(tmpUri, vals);

            	finish();
            }
        });
        cancelB.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) 
            {
            	Log.i(TAG, SubTag + "Cancel button is clicked");
            	finish();
            }
        });

	}
}
