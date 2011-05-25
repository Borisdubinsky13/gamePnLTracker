/**
 * 
 */
package com.gamesPnL;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
	private String username = "";
	private String	workRecord = "";
	final Calendar c = Calendar.getInstance();
	
    Spinner gmTypeSp;
    Spinner gmLimitSp;
	
    static final int MAIN_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;
    
    Button 	dateB;
    String	evYearS;
    String	evDayS;
    String	evMonthS;
    

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.displayitem);
    }
    
    @Override
    protected void onResume()
    {
    	super.onPause();

        final ArrayAdapter<CharSequence> gmLimit = ArrayAdapter.createFromResource(
                this, R.array.gameLimitLst, android.R.layout.simple_spinner_item);
 		
        gamesLogger.i(TAG, SubTag + "Started.");
		SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
		username = pref.getString(PREF_USERNAME, null);
		idIndex = pref.getString(PREF_ID, null);
		int	maxI = Integer.parseInt(idIndex);
		
		this.setTitle("User: " + username);
		
		gamesLogger.i(TAG, SubTag + "Working with record #" + idIndex + " Name: " + username );
		// Get the record with all the values, populate all the fields for display
		String	query = "name = '" + username + "'";
		Cursor	result;
		Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
		String[] projection = new String[] {
				"_id",
				"name",
				"amount",
				"evYear",
				"evMonth",
				"evDay",
				"gameType",
				"gameLimit",
				"eventType",
				"notes"
		};
		
		// result = getContentResolver().query(tmpUri, null, null, null, null);
		result = managedQuery(tmpUri, projection, query, null, null);
		gamesLogger.i(TAG, SubTag + "Number of records:  " + result.getCount());
		
		if ( result.moveToFirst() )
		{
			for ( int i = 0; i < maxI && result.moveToNext(); i++ )
				gamesLogger.i(TAG, SubTag + "Scanned record " + i);
		}
		
		workRecord = result.getString(result.getColumnIndex("_id"));
		gamesLogger.i(TAG, SubTag + "ID: " + result.getString(result.getColumnIndex("_id")));
		EditText amount = (EditText)findViewById(R.id.Amount);
        amount.setText(result.getString(result.getColumnIndex("amount")));
        gamesLogger.i(TAG, SubTag + "Amount from DB: " + result.getString(result.getColumnIndex("amount")));
        dateB = (Button)findViewById(R.id.dateButton);
        String tmpStr = result.getString(result.getColumnIndex("evMonth")) +
        		"/" + result.getString(result.getColumnIndex("evDay")) +
        		"/" + result.getString(result.getColumnIndex("evYear"));
        gamesLogger.i(TAG, SubTag + "Date from DB: " + tmpStr);
        dateB.setText(tmpStr);
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
    	
        evYearS = result.getString(result.getColumnIndex("evYear"));
        evMonthS = result.getString(result.getColumnIndex("evMonth"));
        evDayS = result.getString(result.getColumnIndex("evDay"));
    	dateB.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) 
            {
            	gamesLogger.i(TAG, SubTag + "DATE button is clicked");
            	new DatePickerDialog(DetailDisplay.this, mDateSetListener, mYear, mMonth, mDay).show();
            }
        });
        String evnt = result.getString(result.getColumnIndex("eventType"));
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
        String	gameQuery = "addedBy = '" + username + "'" + " OR addedBy = 'gamePnL'";
		Cursor typeResult = getContentResolver().query(typeUri, null, gameQuery, null, null);
		startManagingCursor(typeResult);
		gamesLogger.i(TAG, SubTag + "Everything is ready for the Spinner. # of records: " + typeResult.getCount());
		if ( typeResult.moveToFirst() )
		{
			do
			{
				String	tmp = typeResult.getString(typeResult.getColumnIndex("game"));
				gamesLogger.i(TAG, SubTag + "Adding " + tmp + " to the Spinner" );
				items.add(tmp);
			} while (typeResult.moveToNext());
		}
        items.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        gmTypeSp.setAdapter(items);
        String tmp = result.getString(result.getColumnIndex("gameType"));
        int	indx = items.getPosition(tmp);
        gamesLogger.i(TAG, SubTag + "Game type for this record: " + tmp + " index: " + indx);
        gmTypeSp.setSelection(indx);
        
        gmLimitSp = (Spinner) findViewById(R.id.gLimit);
        gmLimit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gmLimitSp.setAdapter(gmLimit);
        gmLimitSp.setSelection(gmLimit.getPosition(result.getString(result.getColumnIndex("gameLimit"))));
        
        EditText nts = (EditText)findViewById(R.id.notes);
        nts.setText(result.getString(result.getColumnIndex("notes")));
        
        // Setup buttons
        final Button deleteB = (Button)findViewById(R.id.delete);
        deleteB.setOnClickListener(new View.OnClickListener()
        {
        	public void onClick(View v) 
        	{
        		gamesLogger.i(TAG, SubTag + "Deleting record with ID# " + workRecord);
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
            	
		    	 gamesLogger.i(TAG, SubTag + "ACCEPT button is clicked");
		    	 EditText amount = (EditText)findViewById(R.id.Amount);
		    	 // String dateT = (String) dateB.getText();
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
		         vals.put("evYear", evYearS);
		         vals.put("evMonth", evMonthS);
		         vals.put("evDay", evDayS);
		         vals.put("eventType", eventStr);
		         vals.put("gameType", gameT);
		         vals.put("gameLimit", gameL);
		         vals.put("notes", nts.getText().toString());

		    	 where = "_id = " + workRecord;

		    	 ContentResolver cr = getContentResolver();
		    	 gamesLogger.i(TAG, SubTag + "Got content resolver");
		    	 Uri	tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
		    	 tmpUri = Uri.withAppendedPath(tmpUri,"pnldata");
		    	 gamesLogger.i(TAG, SubTag + "Got URI populated");        			
		    	 cr.update(tmpUri, vals, where, null);            	
		    	 finish(); 
		     }

	     });
	}
    // Creating dialog
    @Override
    protected Dialog onCreateDialog(int id) 
    {
    	gamesLogger.i(TAG, SubTag + "onCreateDialog() started....");
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
    	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
    	{
    		gamesLogger.i(TAG, SubTag + "onDateSet() started....");
    		dateB.setText(String.valueOf(monthOfYear+1)+"/"+String.valueOf(dayOfMonth)+"/"+String.valueOf(year));
            evYearS = String.valueOf(year);
            evMonthS = String.valueOf(monthOfYear+1);
            evDayS = String.valueOf(dayOfMonth);
    		
    	}

    };
}
