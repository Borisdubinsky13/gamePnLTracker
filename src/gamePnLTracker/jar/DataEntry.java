/**
 * 
 */
package gamePnLTracker.jar;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
	public String SubTag="DataEntry";
    final Calendar c = Calendar.getInstance();
	private MyDbAdapter dB = new MyDbAdapter(this);

    static final int MAIN_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;

    String	date_selected;
	CharSequence text = "No text";
	int duration = Toast.LENGTH_SHORT;	
    Button dateB;
    Spinner gmTypeSp;
    Spinner gmLimitSp;
    String	username;
    
    /** Called when the activity is first created. */
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
		Intent iSetup = new Intent(this, SetupWin.class);
		Intent iAddRes = new Intent(this, DataEntry.class);

		// Handle item selection
	    switch (item.getItemId()) 
	    {
	    case R.id.about:
	    	Log.i(TAG, SubTag + "User " + "trying to start ABOUT");
	    	// Intent iAbout = new Intent(new Intent(this, AboutHandler.class));
	        startActivity(iAbout);
	        return true;
	    case R.id.ViewStats:
	    	Log.i(TAG, SubTag + "User " + "trying to start View Stats");
	        startActivity(iSetup);
	        return true;
	    case R.id.AddResult:
	    	Log.i(TAG, SubTag + "User " + "trying to start Add Results");
	    	startActivity(iAddRes);
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dataentry);

        final ArrayAdapter<CharSequence> gmType = ArrayAdapter.createFromResource(
                this, R.array.gameType, android.R.layout.simple_spinner_item);
        final ArrayAdapter<CharSequence> gmLimit = ArrayAdapter.createFromResource(
                this, R.array.gameLimit, android.R.layout.simple_spinner_item);

        // go to data entry window
    	SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
    	username = pref.getString(PREF_USERNAME, null);
    	Log.i(TAG, SubTag + "Started data entry window for user: " + username);
       
        // setup buttons
        final Button clearB = (Button)findViewById(R.id.clear);
        final Button acceptB = (Button)findViewById(R.id.accept);
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
    	
        gmTypeSp = (Spinner) findViewById(R.id.gType);
        gmType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gmTypeSp.setAdapter(gmType);
        
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
       	
        acceptB.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) 
            {
            	ContentValues vals = new ContentValues();;
            	
            	Log.i(TAG, SubTag + "ACCEPT button is clicked");
             	EditText amount = (EditText)findViewById(R.id.Amount);
             	String dateT = (String) dateB.getText();
            	EditText nts = (EditText)findViewById(R.id.notes);
            	String gameT = (String) gmTypeSp.getSelectedItem().toString();
            	String gameL = (String) gmLimitSp.getSelectedItem().toString();
            	
            	vals.put("name", username);
            	vals.put("amount", amount.getText().toString());
            	vals.put("date", dateT);
            	vals.put("gameType", gameT);
            	vals.put("gameLimit", gameL);
            	vals.put("notes", nts.getText().toString());
            	
            	dB.open();
            	dB.insertRecord("gPNLData",vals);
            	dB.close();
            	finish();
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

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
    	// onDateSet method
    	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    		dateB.setText(String.valueOf(monthOfYear+1)+"/"+String.valueOf(dayOfMonth)+"/"+String.valueOf(year));
    	}

    };
}