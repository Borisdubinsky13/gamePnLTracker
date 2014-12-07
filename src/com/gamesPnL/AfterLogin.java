/*
 *
 */
package com.gamesPnL;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/*
 * @author Boris
 */
public class AfterLogin extends Activity {

	public String TAG = "AfterLogin";
	public String SubTag = "AfterLogin: ";
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	public String username;
	private static final String PREF_USERNAME = "username";
	String[] projection = new String[] { "_id", "name", "mDate", "dPrsr",
			"sPrsr", "pulse" };

	// public String currentUser = new String();
	private static String fname; // = "/mnt/sdcard/gamesPnL.csv";
	private static String fnoSDName = "/gamesPnL.csv";
	private ProgressDialog progressDialog;
	private int percentDone;
	private View v;
	private Handler mHandler = new Handler();
	private Context mContext = null;
	private int countRec = 0;
	private final Calendar c = Calendar.getInstance();
	private double lastResult;
	private String lastRecord;

	private void doExport() {
		try {
			// String fname = "/mnt/sdcard/gamesPnL.csv";
			progressDialog = new ProgressDialog(v.getContext());
			progressDialog.setCancelable(true);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setProgress(0);
			progressDialog.setMax(100);
			progressDialog.setMessage("Exporting "
					+ Environment.getExternalStorageDirectory() + fnoSDName
					+ " ...");
			progressDialog.show();

			fname = Environment.getExternalStorageDirectory().toString();
			gamesLogger.i(TAG, SubTag + "User "
					+ "Trying to export data to CSV. File: " + fname);
			new Thread(new Runnable() {
				public void run() {
					int totalRecs = 0;
					File f = new File(Environment.getExternalStorageDirectory()
							+ fnoSDName);
					try {
						f.createNewFile();

						FileOutputStream fOut;

						fOut = new FileOutputStream(f);

						BufferedOutputStream bos = new BufferedOutputStream(
								fOut);

						DbHelper db = new DbHelper(mContext);
						String query = null;
						gamesLogger.i(TAG, SubTag + "Query: " + query);
						Cursor result = db.getData("gPNLData", query);
						gamesLogger.i(TAG,
								SubTag + "there are " + result.getCount()
										+ " records");
						if (result.getCount() > 0) {
							totalRecs = result.getCount();
							progressDialog.setMax((int) totalRecs);
							if (result.moveToFirst()) {
								String strOut = "Date,EventType,GameType,GameLimit,Amount,Name,Note\n";
								bos.write(strOut.getBytes());
								int cPercent = 0;
								do {
									String nameStr = "";
									String evDateStr = "";
									String evTypeStr = "";
									String gameTypeStr = "";
									String gameLimitStr = "";
									String amountStr = "";
									String noteStr = "";

									nameStr = result.getString(result
											.getColumnIndex("name"));
									evDateStr = result.getString(result
											.getColumnIndex("evMonth"))
											+ "/"
											+ result.getString(result
													.getColumnIndex("evDay"))
											+ "/"
											+ result.getString(result
													.getColumnIndex("evYear"));
									evTypeStr = result.getString(result
											.getColumnIndex("eventType"));
									gameTypeStr = result.getString(result
											.getColumnIndex("gameType"));
									gameLimitStr = result.getString(result
											.getColumnIndex("gameLimit"));
									amountStr = result.getString(result
											.getColumnIndex("amount"));
									noteStr = result.getString(result
											.getColumnIndex("notes"));

									strOut = evDateStr + "," + evTypeStr + ","
											+ gameTypeStr + "," + gameLimitStr
											+ "," + amountStr + "," + nameStr
											+ "," + noteStr + "\n";
									gamesLogger.i(TAG, SubTag + "Writing: "
											+ strOut);
									bos.write(strOut.getBytes());
									countRec++;
									// Update the progress bar if needed
									percentDone = (int) (countRec * 100 / totalRecs);
									gamesLogger.i(TAG, SubTag
											+ "currentCount: " + countRec
											+ " Percent: " + percentDone + "%");
									if (cPercent < percentDone) {
										cPercent = percentDone;
										if (percentDone < 100) {
											gamesLogger
													.i(TAG,
															SubTag
																	+ "Updating progress bar to "
																	+ percentDone);
											percentDone++;
											// Update the progress bar
											mHandler.post(new Runnable() {
												public void run() {
													progressDialog
															.setProgress(countRec);
												}
											});
										}
									}
								} while (result.moveToNext());
								gamesLogger.i(TAG, SubTag
										+ "Turning off progress bar");
								progressDialog.dismiss();
							}

							result.close();
							bos.close();

							/*
							 * int duration = Toast.LENGTH_LONG; String text =
							 * "Data has been exported into " + fname; Toast
							 * toast = Toast.makeText(getApplicationContext(),
							 * text, duration); toast.show();
							 */
						} else {
							int duration = Toast.LENGTH_SHORT;
							String text = "No data to export";
							Toast toast = Toast.makeText(
									getApplicationContext(), text, duration);
							toast.show();
						}
					} catch (Exception e) {
						gamesLogger.e(TAG, SubTag + e.getMessage());
					}
				}
			}).start();
		} catch (Exception e) {
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}
		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// get all the records with the current id ad add all the amounts
		SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		username = pref.getString(PREF_USERNAME, null);
		// Handle item selection
		int	getItemId = item.getItemId();
		if (getItemId == R.id.About) {
			gamesLogger.i(TAG, SubTag + "trying to start ABOUT");
			Intent iAbout = new Intent(this, AboutHandler.class);
			startActivity(iAbout);
			return true;
		}
		if (getItemId == R.id.AddResult) {
			gamesLogger.i(TAG, SubTag + "trying to start AddResult");
			Intent iDataEntry = new Intent(this, DataEntry.class);
			startActivity(iDataEntry);
			return true;
		}
		if ( getItemId == R.id.ViewStats ) {
			gamesLogger.i(TAG, SubTag + "trying to start DisplayQueryData");
			// Intent iViewRes = new Intent(this, ListRes.class);
			Intent iViewRes = new Intent(this, DisplayQueryData.class);
			startActivity(iViewRes);
			return true;
		}
		if ( getItemId == R.id.Analysis ) {
			gamesLogger.i(TAG, SubTag + "trying to start DataAnalysis");
			// Intent iViewRes = new Intent(this, ListRes.class);
			Intent iViewAnalysis = new Intent(this, DataAnalysis.class);
			startActivity(iViewAnalysis);
			return true;
		}
		if ( getItemId == R.id.AddGame ) {
			gamesLogger.i(TAG, SubTag + "trying to start Add Game");
			Intent iViewAddGame = new Intent(this, AddGame.class);
			startActivity(iViewAddGame);
			return true;
		}
		if ( getItemId == R.id.importDB ) {
			gamesLogger.i(TAG, SubTag + "trying to import data");
			Intent importAct = new Intent(this, ImportActivityYN.class);
			startActivity(importAct);
			gamesLogger.i(TAG, SubTag + "import is done!");
			// doImport();
			return true;
		}
		if ( getItemId == R.id.exportDB ) {
			gamesLogger.i(TAG, SubTag + "trying to export data");
			doExport();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// See if this is running on the emulator
		gamesLogger.e(TAG, SubTag + "Build.MANUFACTURER is "
				+ Build.MANUFACTURER.toString());
		if (Build.MANUFACTURER.equals("unknown")) {
			// We are running on the emulator. Debugging should be ON.
			gamesLogger.e(TAG, SubTag + "Enablig VERBOSE debugging.");
			gamesLogger.enableLogging(Log.VERBOSE);
		} else {
			// We are running on a phone. Debugging should be OFF.
			gamesLogger.e(TAG, SubTag + "Enablig ERRORS only debugging.");
			gamesLogger.enableLogging(Log.ERROR);
		}

		gamesLogger.enableLogging(Log.VERBOSE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.afterlogin);
/*
		// First check if there are any users. If not, then switch to setup
		// window to add first user.
		try {

			String query = " status = 'in'";
			DbHelper db = new DbHelper(mContext);
			Cursor result = db.getData("pnlstatus", query);
			gamesLogger
					.i(TAG, SubTag + "got " + result.getCount() + " records");
			if (result.getCount() >= 1) {
				result.moveToFirst();
				gamesLogger.i(TAG, SubTag + "ID of the user ");
			}
		} catch (Exception e) {
			gamesLogger.e(TAG, SubTag + " Problem: " + e.getMessage());
		}
*/
		Account[] accounts = AccountManager.get(this).getAccountsByType(
				"com.google");
		gamesLogger.i(TAG, SubTag + "Got account list. Number of entries: "
				+ accounts.length);
		String username;
		if (accounts.length <= 0) {
			int duration = Toast.LENGTH_SHORT;
			String text = "Primary account is not setup. Please setup Google account first.";
			Toast toast = Toast.makeText(getApplicationContext(), text,
					duration);
			toast.show();
			username = "Nobody";
		} else {
			username = accounts[0].name;
		}
		gamesLogger.i(TAG, SubTag + "Name is assigned to " + username);
		getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
				.putString(PREF_USERNAME, username).commit();
		gamesLogger.i(TAG, SubTag + "My email id that I want: " + username);
		DbHelper db = new DbHelper(this);
		gamesLogger.i(TAG, SubTag + "DB Version: " + db.getDBVersion());
		// update database to make sure that all entries have username =
		// primary google account.
		// See if there are any data that has name other then current
		// username
	}

	@Override
	protected void onResume() {
		super.onPause();
		setContentView(R.layout.afterlogin);

		mContext = this; // since Activity extends Context
		mContext = getApplicationContext();
		mContext = getBaseContext();

		v = (View) findViewById(R.id.afterLogin);

		Button buttonPNL = (Button) findViewById(R.id.PNL);
		buttonPNL.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vw) {
				totalAmount(vw);
			}
		});

		Button buttonLastMonth = (Button) findViewById(R.id.PNLMonth);
		buttonLastMonth.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vw) {
				LastMonthAmount(vw);
			}
		});

		Button buttonLastEvent = (Button) findViewById(R.id.PNLLast);
		buttonLastEvent.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vw) {
				LastEventAmount(vw);
			}
		});

		Button buttonAddEvent = (Button) findViewById(R.id.AddScore);
		buttonAddEvent.setOnClickListener(new View.OnClickListener() {
			public void onClick(View vw) {
				AddResult(vw);
			}
		});

		gamesLogger.i(TAG, SubTag + " onResume: Trying to get an add");
		// Initiate a generic request to load it with an ad
	    // Initiate a generic request.
		// AdView adView = new AdView(this);
	    // adView.setAdUnitId("a14d18e1cd0e067");
	    // adView.setAdSize(AdSize.BANNER);
		AdView adView = (AdView) findViewById(R.id.adAfterLogin);
	    AdRequest adRequest = new AdRequest.Builder()
	    	.addTestDevice("1C9D5807CADB9259EB3804DDC582DC3C")
	    	.addTestDevice("5AECA86F6A4E6EB1C1B6907DDFB5086D")
	    	.build();
	    // Load the adView with the ad request.
	    adView.loadAd(adRequest);

		gamesLogger.i(TAG, SubTag + "Got the add");
		// get all the records with the current id ad add all the amounts
		SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String username = pref.getString(PREF_USERNAME, null);

		this.setTitle("");

		Cursor result;
		String value;
		double sum = 0;
		double dValue;
		lastResult = 0;
		DecimalFormat df = new DecimalFormat("$#,##0.00");
		// Button pnlStr = (Button) findViewById(R.id.PNL);
		final String strHead = this.getString(R.string.cEarnings);
		final TextView strHeadStr = (TextView) findViewById(R.id.PNLLabel);

		// Get total career earnings
		String query = null;
		DbHelper db = new DbHelper(mContext);
		gamesLogger.i(TAG, SubTag + "Query: " + query);
		result = db.getData("gPNLData", query);
		gamesLogger.i(TAG, SubTag + "there are " + result.getCount()
				+ " records");
		if (result.moveToFirst()) {
			gamesLogger.i(TAG, SubTag + "got result back from provider");
			do {
				value = result.getString(result.getColumnIndex("amount"));
				if (value.equals(""))
					dValue = 0;
				else
					dValue = Double.parseDouble(value);
				sum += dValue;
				lastResult = dValue;
				lastRecord = result.getString(result.getColumnIndex("_id"));
			} while (result.moveToNext());
		} else
			gamesLogger.i(TAG, SubTag
					+ "No Data returned from Content Provider");

		gamesLogger.i(TAG, SubTag + "Sum:  " + df.format(sum));
		strHeadStr.setText(strHead);
		buttonPNL.setText(df.format(sum));
		// pnlStr.setTextColor(Color.BLACK);
		if (sum >= 0) {
			buttonPNL.setBackgroundColor(Color.rgb(193, 255, 193));
			buttonPNL.setTextColor(getResources().getColor(
					android.R.color.background_dark));
		} else {
			buttonPNL.setBackgroundColor(Color.rgb(150, 0, 0));
			buttonPNL.setTextColor(getResources().getColor(
					android.R.color.background_light));
		}

		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH) + 1;

		// Get this month earnings
		query = null;
		String startDate = String.format("%04d", mYear) + "/"
				+ String.format("%02d", mMonth) + "/01";
		query = "evDate >= '" + startDate + "'";

		gamesLogger.i(TAG, SubTag + "Query: " + query);
		result = db.getData("gPNLData", query);
		gamesLogger.i(TAG, SubTag + "there are " + result.getCount()
				+ " records");

		sum = 0;
		if (result.moveToFirst()) {
			gamesLogger.i(TAG, SubTag + "got result back from provider");
			do {
				value = result.getString(result.getColumnIndex("amount"));
				gamesLogger.i(TAG, SubTag + "got Value:  " + value);
				if (value.equals(""))
					dValue = 0;
				else
					dValue = Double.parseDouble(value);
				sum += dValue;

			} while (result.moveToNext());
		} else
			gamesLogger.i(TAG, SubTag
					+ "No Data returned from Content Provider");

		gamesLogger.i(TAG, SubTag + "Sum:  " + df.format(sum));
		strHeadStr.setText(strHead);
		// pnlStr = (Button) findViewById(R.id.PNLMonth);
		buttonLastMonth.setText(df.format(sum));
		// pnlStr.setTextColor(Color.BLACK);
		if (sum >= 0) {
			buttonLastMonth.setBackgroundColor(Color.rgb(193, 255, 193));
			buttonLastMonth.setTextColor(getResources().getColor(
					android.R.color.background_dark));
		} else {
			buttonLastMonth.setBackgroundColor(Color.rgb(150, 0, 0));
			buttonLastMonth.setTextColor(getResources().getColor(
					android.R.color.background_light));
		}
		// Get last event result
		gamesLogger.i(TAG, SubTag + "Last Result:  " + df.format(lastResult));
		strHeadStr.setText(strHead);
		// pnlStr = (Button) findViewById(R.id.PNLLast);
		buttonLastEvent.setTextColor(getResources().getColor(
				android.R.color.background_light));
		buttonLastEvent.setText(df.format(lastResult));
		// pnlStr.setTextColor(Color.BLACK);
		if (lastResult >= 0) {
			buttonLastEvent.setBackgroundColor(Color.rgb(193, 255, 193));
			buttonLastEvent.setTextColor(getResources().getColor(
					android.R.color.background_dark));
		} else {
			buttonLastEvent.setBackgroundColor(Color.rgb(150, 0, 0));
			buttonLastEvent.setTextColor(getResources().getColor(
					android.R.color.background_light));
		}
		if (result != null)
			result.close();
		gamesLogger.i(TAG, SubTag + "Done!");
	}

	/** Called when the user "career" button */
	public void totalAmount(View view) {
		gamesLogger.i(TAG, SubTag + "Starting totalAmount ...");
		String IntentQ = null;
		Intent iGraphRes = new Intent(this, GraphData.class);
		iGraphRes.putExtra("queStr", IntentQ);
		startActivity(iGraphRes);
	}

	/** Called when the user "last month" button */
	@SuppressLint("DefaultLocale")
	public void LastMonthAmount(View view) {
		gamesLogger.i(TAG, SubTag + "Starting Current month ...");
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH) + 1;
		String IntentQ = String.format("evMonth = '%02d' AND evYear = '%04d'",mMonth, mYear);

		Intent iDispRes = new Intent(this, ListRes.class);
		iDispRes.putExtra("queStr", IntentQ);
		startActivity(iDispRes);
	}

	/** Called when the user "last event" button */
	public void LastEventAmount(View view) {
		gamesLogger.i(TAG, SubTag + "Starting LastEventAmount ...");

		// Save the entry in the preferences, so the display activity can
		// display an appropriate record
		getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().commit();
		Intent iDataEntry = new Intent(this, DisplayItem.class);
		gamesLogger.i(TAG, SubTag + "Editing record # " + lastRecord);
		iDataEntry.putExtra("recID", lastRecord);
		startActivity(iDataEntry);
	}

	/** Called when the user "last month" button */
	public void AddResult(View view) {
		gamesLogger.i(TAG, SubTag + "trying to start AddResult");
		Intent iDataEntry = new Intent(this, DataEntry.class);
		startActivity(iDataEntry);
	}
}