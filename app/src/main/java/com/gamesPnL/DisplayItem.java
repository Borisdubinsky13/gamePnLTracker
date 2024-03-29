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
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Boris
 */

public class DisplayItem extends Activity {
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	public String TAG = "gamePnLTracker ";
	public String SubTag = "DisplayItem: ";
	final Calendar c = Calendar.getInstance();

	static final int MAIN_DIALOG_ID = 0;
	static final int DATE_DIALOG_ID = 1;
	static final int GAME_SELECTION_DIALOG_ID = 2;

	String date_selected;
	CharSequence text = "No text";
	int duration = Toast.LENGTH_SHORT;
	Button dateB;
	Button updateB;
	Button deleteB;
	Spinner gmTypeSp;
	Spinner gmLimitSp;
	String username;
	String evYear;
	String evMonth;
	String evDay;
	String recId;
	Cursor result;

	private Context mContext = null;
	private DbHelper db;

	/**
	 * Called when the activity is first created.
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.displayitem);
		mContext = this; // since Activity extends Context
		mContext = getApplicationContext();
		mContext = getBaseContext();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.displayitem);
		/*
		 * ArrayAdapter gmType = ArrayAdapter.createFromResource( this,
		 * R.array.gameType, android.R.layout.simple_spinner_item);
		 */
		final ArrayAdapter<CharSequence> gmLimit = ArrayAdapter
				.createFromResource(this, R.array.gameLimitLst,
						android.R.layout.simple_spinner_item);
		String query;

		// go to data entry window
		SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		username = pref.getString(PREF_USERNAME, null);
		recId = "";
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			recId = extras.getString("recID");
		}
		if (recId == "") {
			int duration = Toast.LENGTH_LONG;
			String text;
			text = "No record to update.";
			Toast toast = Toast.makeText(getApplicationContext(), text,
					duration);
			toast.show();
			finish();
		}

		this.setTitle("");

		gamesLogger.i(TAG, SubTag + " Started data update for user: "
				+ username + "Record id: " + recId);

		// setup buttons
		final Button deleteB = (Button) findViewById(R.id.delete);
		final Button updateB = (Button) findViewById(R.id.update);
		final RadioButton radioTour = (RadioButton) findViewById(R.id.idTourney);
		final RadioButton radioCash = (RadioButton) findViewById(R.id.idCash);
		final int mYear = c.get(Calendar.YEAR);
		final int mMonth = c.get(Calendar.MONTH) + 1;
		final int mDay = c.get(Calendar.DAY_OF_MONTH);

		db = new DbHelper(mContext);
		query = "_id = '" + recId + "'";
		gamesLogger.i(TAG, SubTag + "Query: " + query);
		Cursor result = db.getData("gPNLData", query);
		gamesLogger.i(TAG, SubTag + "Read all the records with id: " + recId
				+ "# of records:" + result.getCount());
		if (result.moveToFirst()) {
			// Start populating fields from the result of the query
			TextView amnt = (TextView) findViewById(R.id.Amount);
			amnt.setText(result.getString(result.getColumnIndex("amount")));
			TextView timePlay = (TextView) findViewById(R.id.TimePlayed);
			timePlay.setText(result.getString(result.getColumnIndex("timePlayed")));
			dateB = (Button) findViewById(R.id.dateButton);
			evYear = result.getString(result.getColumnIndex("evYear"));
			evMonth = result.getString(result.getColumnIndex("evMonth"));
			evDay = result.getString(result.getColumnIndex("evDay"));
			String evDate = evMonth + "/" + evDay + "/" + evYear;
			dateB.setText(evDate);

			String ev = result.getString(result.getColumnIndex("eventType"));
			String gm = result.getString(result.getColumnIndex("gameType"));
			String lm = result.getString(result.getColumnIndex("gameLimit"));
			String pt = result.getString(result.getColumnIndex("timePlayed"));

			gamesLogger.i(TAG, SubTag + "Record with id #" + recId
					+ " has been read");
			dateB.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					gamesLogger.i(TAG, SubTag + "DATE button is clicked");
					new DatePickerDialog(DisplayItem.this, mDateSetListener,
							mYear, mMonth - 1, mDay).show();
				}
			});

			// populate game spinner and set the visible value from the record
			query = "addedBy = '" + username + "'" + " OR addedBy = 'gamePnL'";
			result = db.getData("gGames", null, " Distinct game ");
			ArrayAdapter<String> items = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item);

			if (result.moveToFirst()) {
				do {
					items.add(result.getString(0));
				} while (result.moveToNext());
			}
			items.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			gmTypeSp = (Spinner) findViewById(R.id.gType);
			gmTypeSp.setAdapter(items);
			ArrayAdapter<String> myAdap = (ArrayAdapter<String>) gmTypeSp
					.getAdapter();
			int spinnerPosition = myAdap.getPosition(gm);
			gmTypeSp.setSelection(spinnerPosition);
			gamesLogger.i(TAG, SubTag + "Games spinner is set up");

			gmLimit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			gmLimitSp = (Spinner) findViewById(R.id.gLimit);
			gmLimitSp.setAdapter(gmLimit);
			myAdap = (ArrayAdapter<String>) gmTypeSp.getAdapter();
			spinnerPosition = myAdap.getPosition(lm);
			gmLimitSp.setSelection(spinnerPosition);

			if (ev.equalsIgnoreCase("Cash")) {
				radioCash.setChecked(true);
				radioTour.setChecked(false);
			} else {
				radioCash.setChecked(false);
				radioTour.setChecked(true);
			}
		}

		deleteB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				gamesLogger.i(TAG, SubTag + "DELETE button is clicked");
				String query = "_ID = '" + recId + "'";

				db.deleteRecord("gPNLData", query);
				finish();
			}
		});

		updateB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				gamesLogger.i(TAG, SubTag + "UPDATE button is clicked");

				ContentValues vals = new ContentValues();
				String eventStr = "Unknown";
				String date2db = evYear + "-" + evMonth + "-" + evDay;

				EditText amount = (EditText) findViewById(R.id.Amount);
				EditText pt = (EditText) findViewById(R.id.TimePlayed);
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
				vals.put("amount", amount.getText().toString());
				vals.put("evYear", evYear);
				vals.put("evMonth", evMonth);
				vals.put("evDay", evDay);
				vals.put("evDate", date2db);
				vals.put("eventType", eventStr);
				vals.put("gameType", gameT);
				vals.put("gameLimit", gameL);
				vals.put("notes", nts.getText().toString());
				vals.put("timePlayed", pt.getText().toString());
				gamesLogger.i(TAG, SubTag + "Storing date: " + evMonth + "/"
						+ evDay + "/" + evYear);

				DbHelper db = new DbHelper(mContext);
				db.updateDataRecord("gPNLData", vals, "_id = " + recId);
				finish();
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
			evYear = String.format("%04d", year);
			evMonth = String.format("%02d", monthOfYear + 1);
			evDay = String.format("%02d", dayOfMonth);

			String eventDate = evMonth + "/" + evDay + "/" + evYear;
			dateB.setText(eventDate);
		}
	};
}