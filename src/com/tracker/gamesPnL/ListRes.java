/**
 * 
 */
package com.tracker.gamesPnL;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import com.tracker.gamesPnL.R;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Boris
 *
 */
public class ListRes extends ListActivity 
{

	public String TAG="gamePnLTracker";
	public String SubTag="ListRes: ";
	
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_ID = "dataTBL_ID";
	private static final String PREF_USERNAME = "username";
	private String idIndex="";
	private String username = "";
	private String	workRecord = "";
	final Calendar c = Calendar.getInstance();
	private ArrayList<ResultData> m_results;
	private ResultDataAdapter	m_adapter;
	
    Spinner gmTypeSp;
    Spinner gmLimitSp;
	
    static final int MAIN_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;
    
    Button 	dateB;
    String	evYearS;
    String	evDayS;
    String	evMonthS;
    
    private	Context	mContext = null;
    
   	class ResultDataAdapter extends ArrayAdapter<ResultData> {

   		public String TAG="gamePnLTracker";
   		public String SubTag="ResultDataAdapter: ";
   		
    	private ArrayList<ResultData> items;

    	public ResultDataAdapter(Context context, int textViewResourceId, ArrayList<ResultData> items) {
    		super(context, textViewResourceId, items);
    		this.items = items;
    	}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	String funcTag = "getView: ";
        	View v = convertView;
        	DecimalFormat df = new DecimalFormat("#,##0.00");
        	try
        	{
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.rowmyresult, null);
                }
                
                ResultData o = items.get(position);
                if (o != null) {
                	TextView dt = (TextView) v.findViewById(R.id.dt);
                	TextView amnt = (TextView) v.findViewById(R.id.amnt);
                	TextView gm = (TextView) v.findViewById(R.id.gm);
                	TextView lm = (TextView) v.findViewById(R.id.lm);
                	TextView ev = (TextView) v.findViewById(R.id.ev);
                	TextView recId = (TextView) v.findViewById(R.id.recId);

                	if ( (o.getAmount()) >= 0 )
                    {
                    	v.setBackgroundColor(Color.rgb(193, 255, 193));
                    }
                    else
                    {
                    	v.setBackgroundColor(Color.rgb(255, 100, 100));
                    }
                	dt.setText(o.getDateTime() + ":  ");
                	dt.setTextColor(Color.BLACK);
                	amnt.setText("     $" + df.format(o.getAmount()));
                	// amnt.setText(o.getAmountStr());
                	amnt.setTextColor(Color.BLACK);
                	gm.setText(o.getGame());
                	gm.setTextColor(Color.BLACK);
                	lm.setText(o.getLimit());
                	lm.setTextColor(Color.BLACK);
                	ev.setText(o.getEvent());
                	ev.setTextColor(Color.BLACK);
                	
                	recId.setText(String.valueOf(o.getRecId()));
                }
        	}
        	catch (Exception e)
        	{
        		v = null;
        		gamesLogger.e(TAG, SubTag + funcTag + e.getMessage()); 
        	}
            return v;
        }
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listres);
		mContext = this;    // since Activity extends Context
		mContext = getApplicationContext();
		mContext = getBaseContext();
	}
	
    @Override
    protected void onResume()
    {
    	super.onResume();
    	setContentView(R.layout.listres);
/*
        final ArrayAdapter<CharSequence> gmLimit = ArrayAdapter.createFromResource(
                this, R.array.gameLimitLst, android.R.layout.simple_spinner_item);
 */		
        gamesLogger.i(TAG, SubTag + "Started.");
		SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);   
		username = pref.getString(PREF_USERNAME, null);
		idIndex = pref.getString(PREF_ID, null);
		
		this.setTitle("User: " + username);
		
		gamesLogger.i(TAG, SubTag + "Working with record #" + idIndex + " Name: " + username );
		// Get the record with all the values, populate all the fields for display
    	String	query;
		Bundle extras = getIntent().getExtras(); 
    	if(extras !=null)
    	{
    		query = extras.getString("queStr");
    	}
    	else
    	{
    		query = "name = '" + username + "'";
    	}
		Cursor	result = null;
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
		result = mContext.getContentResolver().query(tmpUri, projection, query, null, null);
		gamesLogger.i(TAG, SubTag + "Number of records:  " + result.getCount());
		
		m_results = new ArrayList<ResultData>();
        m_adapter = new ResultDataAdapter(this, R.layout.rowmyresult, m_results);
        
        int	i = 0;
		if ( result.moveToFirst() )
		{
			for ( i = 0; result.moveToNext(); i++ )
			{
				ResultData	tmp = new ResultData();
		
				workRecord = result.getString(result.getColumnIndex("_id"));
				tmp.setRecId(Integer.parseInt(workRecord));
		        tmp.setAmount(Double.parseDouble(result.getString(result.getColumnIndex("amount"))));
		        String evYear = result.getString(result.getColumnIndex("evYear"));
		        String evMonth = result.getString(result.getColumnIndex("evMonth"));
		        String evDay = result.getString(result.getColumnIndex("evDay"));
		        String evDate = evMonth + "/" + evDay + "/" + evYear;
		        tmp.setDateTime(evDate);
		        tmp.setEvent(result.getString(result.getColumnIndex("eventType")));
		        tmp.setGame(result.getString(result.getColumnIndex("gameType")));
		        tmp.setLimit(result.getString(result.getColumnIndex("gameLimit")));
		        m_results.add(tmp);
			}		
		}     
		gamesLogger.i(TAG, SubTag + "Number of record processed: " + i);
        if ( result.getCount() > 0 )
        {
        	setListAdapter(m_adapter);
        	gamesLogger.i(TAG, SubTag + "Adapter has been set!");
        }
        else
        {
        	int duration = Toast.LENGTH_LONG;
        	String text;
       		text = "There are no results found";
			Toast toast = Toast.makeText(getApplicationContext(), text, duration);
			toast.show();
			finish();
        }
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
    
	/* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		super.onListItemClick(l, v, position, id);
		
		ResultData tmpR = m_results.get(position);
		// Save the entry in the preferences, so the display activity can display an appropriate record
        getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
       	.edit()
    	.commit();

    	Intent iDataEntry = new Intent(this, DisplayItem.class);
    	gamesLogger.i(TAG, SubTag + "Clicked on position: " + position + ", RecId: " + tmpR.getRecIdString() );
    	iDataEntry.putExtra("recID", tmpR.getRecIdString());

        startActivity(iDataEntry);
	}	

}
