/**
 * 
 */
package com.gamesPnL;

import java.util.Calendar;

import com.gamesPnL.R;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author Boris
 *
 */

public class DataEntry extends Activity 
{
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	public String TAG="gamePnLTracker";
	public String SubTag="DataEntry ";
    final Calendar c = Calendar.getInstance();

    static final int MAIN_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;
    static final int GAME_SELECTION_DIALOG_ID = 2;

    String	date_selected;
	CharSequence text = "No text";
	int duration = Toast.LENGTH_SHORT;	
    Button dateB;
    Spinner gmTypeSp;
    Spinner gmLimitSp;
    String	username;
    
    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dataentry);
    }
    
    @Override
    protected void onResume()
    {
    	super.onPause();
/*
        ArrayAdapter gmType = ArrayAdapter.createFromResource(
                this, R.array.gameType, android.R.layout.simple_spinner_item);
*/
        final ArrayAdapter<CharSequence> gmLimit = ArrayAdapter.createFromResource(
                this, R.array.gameLimit, android.R.layout.simple_spinner_item);

        // go to data entry window
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	username = pref.getString(PREF_USERNAME, null);
    	Log.i(TAG, SubTag + " Started data entry window for user: " + username);
       
        // setup buttons
        final Button clearB = (Button)findViewById(R.id.clear);
        final Button winB = (Button)findViewById(R.id.Win);
        final Button looseB = (Button)findViewById(R.id.Loss);
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        dateB = (Button)findViewById(R.id.dateButton);
    	dateB.setText( new StringBuilder()
    		// Month is 0 based so add 1
        	.append(mMonth + 1).append("/")
        	.append(mDay).append("/")
        	.append(mYear).append(" "));

    	dateB.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) 
            {
            	Log.i(TAG, SubTag + "DATE button is clicked");
            	new DatePickerDialog(DataEntry.this, mDateSetListener, mYear, mMonth, mDay).show();
            }
        });
    	
		Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		tmpUri = Uri.withAppendedPath(tmpUri,"pnlgames");
		
		ArrayAdapter<String> items = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        gmTypeSp = (Spinner) findViewById(R.id.gType);
        gmTypeSp.setAdapter(items);
        gmTypeSp.setSelection(0);
		Cursor result = getContentResolver().query(tmpUri, null, null, null, null);
		startManagingCursor(result);
		Log.i(TAG, SubTag + "Everything is ready for the Spinner. # of records: " + result.getCount());
		if ( result.moveToFirst() )
		{
			do
			{
				items.add(result.getString(1));
			} while (result.moveToNext());
		}
        items.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        gmTypeSp.setAdapter(items);
        gmTypeSp.setSelection(0);
        
        gmLimitSp = (Spinner) findViewById(R.id.gLimit);
        gmLimit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gmLimitSp.setAdapter(gmLimit);
    	
       	clearB.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) 
            {
            	Log.i(TAG, SubTag + "CLEAR button is clicked");
            	
            	EditText amount = (EditText)findViewById(R.id.Amount);
            	amount.setText("");
            	
            	int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
            	dateB.setText( new StringBuilder()
                    // Month is 0 based so add 1
                    .append(mMonth + 1).append("/")
                    .append(mDay).append("/")
                    .append(mYear).append(" "));
            	
             	gmTypeSp.setSelection(0);
             	gmLimitSp.setSelection(0);
             	
               	EditText nts = (EditText)findViewById(R.id.notes);
            	nts.setText("");
            }
        });
       	
        winB.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) 
            {
            	ContentValues vals = new ContentValues();
            	String eventStr = "Unknown";
            	
            	Log.i(TAG, SubTag + "WIN button is clicked");
             	EditText amount = (EditText)findViewById(R.id.Amount);
             	String dateT = (String) dateB.getText();
            	EditText nts = (EditText)findViewById(R.id.notes);
            	String gameT = (String) gmTypeSp.getSelectedItem().toString();
            	String gameL = (String) gmLimitSp.getSelectedItem().toString();
            	RadioButton tourneyRB = (RadioButton) findViewById(R.id.idTourney);
            	RadioButton cashRB = (RadioButton) findViewById(R.id.idCash);
            	
            	if ( tourneyRB.isChecked())
            		eventStr = "Tourney";
            	else if ( cashRB.isChecked())
            		eventStr = "Cash";
            		
            	if ( !amount.getText().toString().equals(""))
            	{
	            	vals.put("name", username);
	            	vals.put("amount", amount.getText().toString());
	            	vals.put("date", dateT);
	            	vals.put("eventType", eventStr);
	            	vals.put("gameType", gameT);
	            	vals.put("gameLimit", gameL);
	            	vals.put("notes", nts.getText().toString());
	    			ContentResolver cr = getContentResolver();
	    			Log.i(TAG, SubTag + "Got content resolver");
	    			Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
	    			tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
	    			Log.i(TAG, SubTag + "Got URI populated");        			
	    			cr.insert(tmpUri, vals);            	
	            	finish();
            	}
            	else
            	{
        			String text = "Please enter the amount!";
        			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        			toast.show();
        			return;
            	}
            }
        });
        
        looseB.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) 
            {
            	ContentValues vals = new ContentValues();
            	String eventStr = "Unknown";
            	String realAmount = "-";
            	
            	Log.i(TAG, SubTag + "LOSS button is clicked");
             	EditText amount = (EditText)findViewById(R.id.Amount);

            	if ( !amount.getText().toString().equals(""))
            	{
	             	realAmount += amount.getText().toString(); 
	             	String dateT = (String) dateB.getText();
	            	EditText nts = (EditText)findViewById(R.id.notes);
	            	String gameT = (String) gmTypeSp.getSelectedItem().toString();
	            	String gameL = (String) gmLimitSp.getSelectedItem().toString();
	            	RadioButton tourneyRB = (RadioButton) findViewById(R.id.idTourney);
	            	RadioButton cashRB = (RadioButton) findViewById(R.id.idCash);
	            	
	            	if ( tourneyRB.isChecked())
	            		eventStr = "Tourney";
	            	else if ( cashRB.isChecked())
	            		eventStr = "Cash";
	          		
	            	vals.put("name", username);
	            	vals.put("amount", realAmount );
	            	vals.put("date", dateT);
	            	vals.put("eventType", eventStr);
	            	vals.put("gameType", gameT);
	            	vals.put("gameLimit", gameL);
	            	vals.put("notes", nts.getText().toString());
	    			ContentResolver cr = getContentResolver();
	    			Log.i(TAG, SubTag + "Got content resolver");
	    			Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
	    			tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
	    			Log.i(TAG, SubTag + "Got URI populated");        			
	    			cr.insert(tmpUri, vals);            	
	            	finish();
            	}
            	else
            	{
           			String text = "Please enter the amount!";
        			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
        			toast.show();
        			return;
            	}
            }
        });
    }

    // Creating dialog
    @Override
    protected Dialog onCreateDialog(int id) 
    {
    	Log.i(TAG, SubTag + "onCreateDialog() started....");
    	Calendar c = Calendar.getInstance();
    	int cyear = c.get(Calendar.YEAR);
    	int cmonth = c.get(Calendar.MONTH);
    	int cday = c.get(Calendar.DAY_OF_MONTH);
    	switch (id) 
    	{
    	case DATE_DIALOG_ID:
    		return new DatePickerDialog(this,  mDateSetListener,  cyear, cmonth, cday);
    	}
    	return null;
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() 
    {
    	// onDateSet method
    	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    		dateB.setText(String.valueOf(monthOfYear+1)+"/"+String.valueOf(dayOfMonth)+"/"+String.valueOf(year));
    	}

    };
}