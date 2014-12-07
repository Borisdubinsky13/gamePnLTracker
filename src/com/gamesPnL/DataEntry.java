/**
 *
 */
package com.gamesPnL;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
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
 */

public class DataEntry extends Activity {
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	public String TAG = "gamePnLTracker";
	public String SubTag = "DataEntry: ";
	final Calendar c = Calendar.getInstance();

	static final int MAIN_DIALOG_ID = 0;
	static final int DATE_DIALOG_ID = 1;
	static final int GAME_SELECTION_DIALOG_ID = 2;

	String date_selected;
	CharSequence text = "No text";
	int duration = Toast.LENGTH_SHORT;
	Button dateB;
	Spinner gmTypeSp;
	Spinner gmLimitSp;
	String username;
	String evYearS;
	String evDayS;
	String evMonthS;

	private Context mContext;
	private DbHelper db;

	/*
	 * Called when the activity is first created.
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dataentry);
		mContext = this;
		db = new DbHelper(mContext);
	}

	@Override
	protected void onResume() {
		super.onPause();
		/*
		 * ArrayAdapter gmType = ArrayAdapter.createFromResource( this,
		 * R.array.gameType, android.R.layout.simple_spinner_item);
		 */
		final ArrayAdapter<CharSequence> gmLimit = ArrayAdapter
				.createFromResource(this, R.array.gameLimitLst,
						android.R.layout.simple_spinner_item);

		// go to data entry window
		SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		username = pref.getString(PREF_USERNAME, null);
		this.setTitle("");

		gamesLogger.i(TAG, SubTag + " Started data entry window for user: "
				+ username);

		// setup buttons
		final Button clearB = (Button) findViewById(R.id.clear);
		final Button winB = (Button) findViewById(R.id.Win);
		final Button looseB = (Button) findViewById(R.id.Loss);
		final int mYear = c.get(Calendar.YEAR);
		final int mMonth = c.get(Calendar.MONTH) + 1;
		final int mDay = c.get(Calendar.DAY_OF_MONTH);

		evYearS = String.format("%04d", mYear);
		evMonthS = String.format("%02d", mMonth);
		evDayS = String.format("%02d", mDay);
		String eventDate = evMonthS + "/" + evDayS + " /" + evYearS;

		dateB = (Button) findViewById(R.id.dateButton);
		dateB.setText(eventDate);

		dateB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				gamesLogger.i(TAG, SubTag + "DATE button is clicked");
				new DatePickerDialog(DataEntry.this, mDateSetListener, mYear,
						mMonth - 1, mDay).show();
			}
		});

		String query = "name = '" + username + "'" + " OR name = 'gamePnL'";
		ArrayAdapter<String> items = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		gmTypeSp = (Spinner) findViewById(R.id.gType);

		gamesLogger.i(TAG, SubTag + "Query: " + query);
		Cursor result = db.getData("gGames");
		gamesLogger.i(TAG, SubTag
				+ "Everything is ready for the Spinner. # of records: "
				+ result.getCount());
		if (result.moveToFirst()) {
			do {
				items.add(result.getString(1));
			} while (result.moveToNext());
		}
		items.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gmTypeSp.setAdapter(items);
		gmTypeSp.setSelection(0);

		gmLimitSp = (Spinner) findViewById(R.id.gLimit);
		gmLimit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gmLimitSp.setAdapter(gmLimit);

		clearB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				gamesLogger.i(TAG, SubTag + "CLEAR button is clicked");

				EditText amount = (EditText) findViewById(R.id.Amount);
				amount.setText("");

				final int mYear = c.get(Calendar.YEAR);
				final int mMonth = c.get(Calendar.MONTH) + 1;
				final int mDay = c.get(Calendar.DAY_OF_MONTH);

				evYearS = String.format("%04d", mYear);
				evMonthS = String.format("%02d", mMonth);
				evDayS = String.format("%02d", mDay);
				String eventDate = evMonthS + "/" + evDayS + " /" + evYearS;

				dateB = (Button) findViewById(R.id.dateButton);
				dateB.setText(eventDate);

				gmTypeSp.setSelection(0);
				gmLimitSp.setSelection(0);

				EditText nts = (EditText) findViewById(R.id.notes);
				nts.setText("");
			}
		});

		winB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ContentValues vals = new ContentValues();
				String eventStr = "Unknown";
				String date2db = evYearS + "/" + evMonthS + "/" + evDayS;

				gamesLogger.i(TAG, SubTag + "WIN button is clicked");
				EditText amount = (EditText) findViewById(R.id.Amount);
				// String dateT = (String) dateB.getText();
				EditText nts = (EditText) findViewById(R.id.notes);
				String gameT = (String) gmTypeSp.getSelectedItem().toString();
				String gameL = (String) gmLimitSp.getSelectedItem().toString();
				RadioButton tourneyRB = (RadioButton) findViewById(R.id.idTourney);
				RadioButton cashRB = (RadioButton) findViewById(R.id.idCash);

				if (tourneyRB.isChecked())
					eventStr = "Tourney";
				else if (cashRB.isChecked())
					eventStr = "Cash";

				if (!amount.getText().toString().equals("")) {
					vals.put("name", username);
					vals.put("amount", amount.getText().toString());
					vals.put("evYear", evYearS);
					vals.put("evMonth", evMonthS);
					vals.put("evDay", evDayS);
					vals.put("evDate", date2db);
					vals.put("eventType", eventStr);
					vals.put("gameType", gameT);
					vals.put("gameLimit", gameL);
					vals.put("notes", nts.getText().toString());
					gamesLogger.i(TAG, SubTag + "Storing date: " + evMonthS
							+ "/" + evDayS + "/" + evYearS + "(" + date2db
							+ ")");
					db.insert("gPNLData", vals);
					finish();
				} else {
					String text = "Please enter the amount!";
					Toast toast = Toast.makeText(getApplicationContext(), text,
							duration);
					toast.show();
					return;
				}
			}
		});

		looseB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ContentValues vals = new ContentValues();
				String eventStr = "Unknown";
				String realAmount = "-";
				String date2db = evYearS + "/" + evMonthS + "/" + evDayS;

				gamesLogger.i(TAG, SubTag + "LOSS button is clicked");
				EditText amount = (EditText) findViewById(R.id.Amount);

				if (!amount.getText().toString().equals("")) {
					realAmount += amount.getText().toString();
					// String dateT = (String) dateB.getText();
					EditText nts = (EditText) findViewById(R.id.notes);
					String gameT = gmTypeSp.getSelectedItem().toString();
					String gameL = gmLimitSp.getSelectedItem().toString();
					RadioButton tourneyRB = (RadioButton) findViewById(R.id.idTourney);
					RadioButton cashRB = (RadioButton) findViewById(R.id.idCash);

					if (tourneyRB.isChecked())
						eventStr = "Tourney";
					else if (cashRB.isChecked())
						eventStr = "Cash";

					vals.put("name", username);
					vals.put("amount", realAmount);
					vals.put("evYear", evYearS);
					vals.put("evMonth", evMonthS);
					vals.put("evDay", evDayS);
					vals.put("evDate", date2db);
					vals.put("eventType", eventStr);
					vals.put("gameType", gameT);
					vals.put("gameLimit", gameL);
					vals.put("notes", nts.getText().toString());
					gamesLogger.i(TAG, SubTag + "Storing date: " + evMonthS
							+ "/" + evDayS + "/" + evYearS + "(" + date2db
							+ ")");

					db.insert("gPNLData", vals);
					finish();
				} else {
					String text = "Please enter the amount!";
					Toast toast = Toast.makeText(getApplicationContext(), text,
							duration);
					toast.show();
					return;
				}
			}
		});
	}

	// Creating dialog
	@Override
	protected Dialog onCreateDialog(int id) {
		gamesLogger.i(TAG, SubTag + "onCreateDialog() started....");
		Calendar c = Calendar.getInstance();
		int cyear = c.get(Calendar.YEAR);
		int cmonth = c.get(Calendar.MONTH);
		int cday = c.get(Calendar.DAY_OF_MONTH);
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, cyear, cmonth,
					cday);
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		// onDateSet method
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			evYearS = String.format("%04d", year);
			evMonthS = String.format("%02d", monthOfYear + 1);
			evDayS = String.format("%02d", dayOfMonth);

			String eventDate = evMonthS + "/" + evDayS + "/" + evYearS;
			dateB.setText(eventDate);
		}

	};
}