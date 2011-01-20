/**
 * 
 */
package com.gamesPnL;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

/**
 * @author Boris
 *
 */
public class DetailDisplay extends Activity 
{
	public String TAG="gamePnLTracker";
	public String SubTag="DetailDisplay: ";
	
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_ID = "dataTBL_ID";
	private static final String PREF_USERNAME = "username";
	private String idIndex="";
	private String name = "";
	private String	workRecord = "";
	
    Spinner gmTypeSp;
    Spinner gmLimitSp;
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
        final ArrayAdapter<CharSequence> gmType = ArrayAdapter.createFromResource(
                this, R.array.gameType, android.R.layout.simple_spinner_item);
        final ArrayAdapter<CharSequence> gmLimit = ArrayAdapter.createFromResource(
                this, R.array.gameLimit, android.R.layout.simple_spinner_item);
        
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.displayitem);
		
		Log.i(TAG, SubTag + "Started.");
		SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
		name = pref.getString(PREF_USERNAME, null);
		idIndex = pref.getString(PREF_ID, null);
		int	maxI = Integer.parseInt(idIndex);
		
		Log.i(TAG, SubTag + "Working with record #" + idIndex + " Name: " + name );
		// Get the record with all the values, populate all the fields for display
		String	query = "name = '" + name + "'";
		Cursor	result;
		Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
		String[] projection = new String[] {
				"_ID",
				"name",
				"amount",
				"date",
				"gameType",
				"gameLimit",
				"eventType",
				"notes"
		};
		
		// result = getContentResolver().query(tmpUri, null, null, null, null);
		result = managedQuery(tmpUri, projection, query, null, null);
		Log.i(TAG, SubTag + "Number of records:  " + result.getCount());
		
		if ( result.moveToFirst() )
		{
			for ( int i = 0; i < maxI && result.moveToNext(); i++ )
				Log.i(TAG, SubTag + "Scanned record " + i);
		}
		Log.i(TAG, SubTag + "ID: " + result.getString(0));
		workRecord = result.getString(0);
		EditText amount = (EditText)findViewById(R.id.Amount);
        amount.setText(result.getString(2));
        final Button dateB = (Button)findViewById(R.id.dateButton);
        dateB.setText(result.getString(3));
        String evnt = result.getString(4);
    	RadioButton tourneyRB = (RadioButton) findViewById(R.id.idTourney);
    	RadioButton cashRB = (RadioButton) findViewById(R.id.idCash);
        if ( evnt.equalsIgnoreCase("Cash") )
        {
        	cashRB.setChecked(true);
        	tourneyRB.setChecked(false);
        }
        else if ( evnt.equalsIgnoreCase("Tourney") )
        {
        	tourneyRB.setChecked(true);
        	cashRB.setChecked(false);
        }
        else
        {
        	tourneyRB.setChecked(false);
        	cashRB.setChecked(false);
        }
     
		Uri	typeUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		typeUri = Uri.withAppendedPath(typeUri,"pnlgames");
		ArrayAdapter<String> items = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        gmTypeSp = (Spinner) findViewById(R.id.gType);
        gmTypeSp.setAdapter(items);
		Cursor typeResult = getContentResolver().query(typeUri, null, null, null, null);
		startManagingCursor(typeResult);
		Log.i(TAG, SubTag + "Everything is ready for the Spinner. # of records: " + typeResult.getCount());
		if ( typeResult.moveToFirst() )
		{
			do
			{
				items.add(typeResult.getString(1));
			} while (typeResult.moveToNext());
		}
        items.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        gmTypeSp.setAdapter(items);
        gmTypeSp.setSelection(gmType.getPosition(result.getString(5)));
        
        gmLimitSp = (Spinner) findViewById(R.id.gLimit);
        gmLimit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gmLimitSp.setAdapter(gmLimit);
        gmLimitSp.setSelection(gmLimit.getPosition(result.getString(6)));
        
        EditText nts = (EditText)findViewById(R.id.notes);
        nts.setText(result.getString(7));
        
        // Setup buttons
        final Button deleteB = (Button)findViewById(R.id.delete);
        deleteB.setOnClickListener(new View.OnClickListener()
        {
        	public void onClick(View v) 
        	{
        		Log.i(TAG, SubTag + "Deleting record with ID# " + workRecord);
		    	ContentResolver cr = getContentResolver();
		    	String	query = "_ID = '" + workRecord + "'";
		    	Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		    	 
		    	tmpUri = Uri.withAppendedPath(tmpUri,"pnl1record");
		    	cr.delete(tmpUri, query, null);
		    	finish();
        	}
	     });
	     final Button updateB = (Button)findViewById(R.id.update);
	     updateB.setOnClickListener(new View.OnClickListener()
	     {
		     public void onClick(View v) 
		     {
		    	 ContentValues vals = new ContentValues();
		    	 String eventStr = "Unknown";
		    	 String where = "";
            	
		    	 Log.i(TAG, SubTag + "ACCEPT button is clicked");
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
		    	 
		         vals.put("amount", amount.getText().toString());
		         vals.put("date", dateT);
		         vals.put("eventType", eventStr);
		         vals.put("gameType", gameT);
		         vals.put("gameLimit", gameL);
		         vals.put("notes", nts.getText().toString());

		    	 where = "_ID = " + workRecord;

		    	 ContentResolver cr = getContentResolver();
		    	 Log.i(TAG, SubTag + "Got content resolver");
		    	 Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		    	 tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
		    	 Log.i(TAG, SubTag + "Got URI populated");        			
		    	 cr.update(tmpUri, vals, where, null);            	
		    	 finish(); 
		     }

	     });
	}
}
