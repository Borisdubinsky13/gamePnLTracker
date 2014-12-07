/**
 *
 */
package com.gamesPnL;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


/**
 * @author Boris
 */
public class DisplayQueryData extends Activity {

	public String TAG = "gamePnLTracker";
	public String SubTag = "DisplayQueryData: ";
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	final Calendar c = Calendar.getInstance();

	static final int MAIN_DIALOG_ID = 0;
	static final int DATE_DIALOG_ID = 1;
	static final int GAME_SELECTION_DIALOG_ID = 2;

	String date_selected;
	CharSequence text = "No text";
	int duration = Toast.LENGTH_SHORT;
	Button StartDateB;
	Button EndDateB;
	Spinner evTypeSp;
	Spinner gmTypeSp;
	Spinner gmLimitSp;
	String username;
	protected TextView dateB;
	protected String evYearS;
	protected String evMonthS;
	protected String evDayS;
	String IntentQ;
	String startSearchDate = null;
	String endSearchDate = null;

	private DbHelper db;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.displayquerydata);
		db = new DbHelper(this);
	}

	@Override
	protected void onResume() {

		super.onPause();
		setContentView(R.layout.displayquerydata);

		gamesLogger.i(TAG, SubTag + "Start....");

		ArrayAdapter<CharSequence> gmLimit = ArrayAdapter.createFromResource(
				this, R.array.gameLimitSearch,
				android.R.layout.simple_spinner_item);
		gamesLogger.i(TAG, SubTag + "ArrayAdapter gmLimit is created!");
		ArrayAdapter<CharSequence> evType = ArrayAdapter.createFromResource(
				this, R.array.evTypeSearch,
				android.R.layout.simple_spinner_item);

		gamesLogger.i(TAG, SubTag + "ArrayAdapters are created!");
		// go to data entry window
		SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		username = pref.getString(PREF_USERNAME, null);
		this.setTitle("");

		gamesLogger.i(TAG, SubTag + "Started data entry window for user: "
				+ username);

		// setup buttons
		final Button reportB = (Button) findViewById(R.id.report);
		final Button graphB = (Button) findViewById(R.id.graph);

		final int mYear = c.get(Calendar.YEAR);
		final int mMonth = c.get(Calendar.MONTH) + 1;
		final int mDay = c.get(Calendar.DAY_OF_MONTH);

		gamesLogger.i(TAG, SubTag + "Setting up START date button");
		StartDateB = (Button) findViewById(R.id.startDateButton);
		StartDateB.setText("Start Date");
		StartDateB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				gamesLogger.i(TAG, SubTag + "Start date button is clicked");
				new DatePickerDialog(DisplayQueryData.this,
						mStartDateSetListener, mYear, mMonth - 1, mDay).show();
			}
		});

		gamesLogger.i(TAG, SubTag + "Setting up END date button");
		EndDateB = (Button) findViewById(R.id.endDateButton);
		EndDateB.setText("End Date");
		EndDateB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				gamesLogger.i(TAG, SubTag + "End date button is clicked");
				new DatePickerDialog(DisplayQueryData.this,
						mEndDateSetListener, mYear, mMonth - 1, mDay).show();
			}
		});

		gamesLogger.i(TAG, SubTag + "Setting up Event Type spinner");
		evTypeSp = (Spinner) findViewById(R.id.eTypeSearch);

		gamesLogger.i(TAG, SubTag + "Event Type array is created");
		evType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gamesLogger.i(TAG, SubTag + "Event Type dropdown is setup");
		evTypeSp.setAdapter(evType);

		gamesLogger.i(TAG, SubTag + "Setting up Game Type spinner");

		ArrayAdapter<String> items = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		gmTypeSp = (Spinner) findViewById(R.id.gType);
		gmTypeSp.setAdapter(items);
		gmTypeSp.setSelection(0);

		String query = "addedBy = '" + username + "'"
				+ " OR addedBy = 'gamePnL'";
		query = null;
		Cursor result = db.getData("gGames", query, " Distinct game ");
		gamesLogger.i(TAG, SubTag
				+ "Everything is ready for the game Spinner. # of records: "
				+ result.getCount());
		items.add("All");
		if (result.moveToFirst()) {
			do {
				items.add(result.getString(0));
			} while (result.moveToNext());
		}
		if (result != null)
			result.close();

		items.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gmTypeSp.setAdapter(items);
		gmTypeSp.setSelection(0);

		gmLimitSp = (Spinner) findViewById(R.id.gLimit);
		gmLimit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gmLimitSp.setAdapter(gmLimit);

		reportB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				gamesLogger.i(TAG, SubTag + "REPORT button is clicked");
				populateQuesryString();
				// Intent iViewRes = new
				// Intent(DisplayQueryData.this,ListRes.class);
				Intent iViewRes = new Intent(DisplayQueryData.this,
						ListRes.class);

				gamesLogger.i(TAG, SubTag + "Query: " + IntentQ);
				iViewRes.putExtra("queStr", IntentQ);
				startActivity(iViewRes);
			}
		});

		graphB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				gamesLogger.i(TAG, SubTag + "GRAPH button is clicked");
				populateQuesryString();
				Intent iGraphRes = new Intent(DisplayQueryData.this,
						GraphData.class);
				gamesLogger.i(TAG, SubTag + "Query: " + IntentQ);
				iGraphRes.putExtra("queStr", IntentQ);
				startActivity(iGraphRes);
			}
		});
	}

	private void populateQuesryString() {
		String t;
		// Build a query string that will be sent to the intent
		IntentQ = "name = '" + username + "'";

		if (!StartDateB.getText().equals("Start Date")) {
			IntentQ += " AND ";
			IntentQ += "evDate >= '";
			IntentQ += startSearchDate;
			IntentQ += "'";
		}
		if (!EndDateB.getText().equals("End Date")) {
			IntentQ += " AND ";
			IntentQ += "evDate <= '";
			IntentQ += endSearchDate;
			IntentQ += "'";
		}
		if (!evTypeSp.getSelectedItem().toString().equalsIgnoreCase("All")) {
			IntentQ += " AND ";
			IntentQ += "eventType = '";
			t = (String) evTypeSp.getSelectedItem().toString();
			t = t.replace("\'", "\'\'");
			IntentQ += t;
			IntentQ += "'";
			gamesLogger.i(TAG, SubTag + "Selection: " + t);

		}
		if (!gmTypeSp.getSelectedItem().toString().equalsIgnoreCase("All")) {
			IntentQ += " AND ";
			IntentQ += "gameType = '";
			t = (String) gmTypeSp.getSelectedItem().toString();
			t = t.replace("\'", "\'\'");
			IntentQ += t;
			IntentQ += "'";
			gamesLogger.i(TAG, SubTag + "Selection: " + t);
		}
		if (!gmLimitSp.getSelectedItem().toString().equalsIgnoreCase("All")) {
			IntentQ += " AND ";
			IntentQ += "gameLimit = '";
			t = (String) gmLimitSp.getSelectedItem().toString();
			t = t.replace("\'", "\'\'");
			IntentQ += t;
			IntentQ += "'";
			gamesLogger.i(TAG, SubTag + "Selection: " + t);
		}
	}

	private DatePickerDialog.OnDateSetListener mStartDateSetListener = new DatePickerDialog.OnDateSetListener() {
		// onDateSet method
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			gamesLogger.i(TAG, SubTag + "executing onDateSet()");

			evYearS = String.format("%04d", year);
			evMonthS = String.format("%02d", monthOfYear + 1);
			evDayS = String.format("%02d", dayOfMonth);
			String dateStr = evYearS + "/" + evMonthS + "/" + evDayS;
			gamesLogger.i(TAG, SubTag + "Date String for button: " + dateStr);
			StartDateB.setText(dateStr);
			startSearchDate = evYearS + "/" + evMonthS + "/" + evDayS;
		}
	};

	private DatePickerDialog.OnDateSetListener mEndDateSetListener = new DatePickerDialog.OnDateSetListener() {
		// onDateSet method
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			gamesLogger.i(TAG, SubTag + "executing onDateSet()");
			evYearS = String.format("%04d", year);
			evMonthS = String.format("%02d", monthOfYear + 1);
			evDayS = String.format("%02d", dayOfMonth);
			String dateStr = evYearS + "/" + evMonthS + "/" + evDayS;
			gamesLogger.i(TAG, SubTag + "Date String for button: " + dateStr);
			EndDateB.setText(dateStr);
			endSearchDate = evYearS + "/" + evMonthS + "/" + evDayS;

		}
	};

}
