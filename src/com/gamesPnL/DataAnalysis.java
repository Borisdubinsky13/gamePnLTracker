/**
 *
 */
package com.gamesPnL;

import java.util.Calendar;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

/**
 * @author boris
 */
public class DataAnalysis extends Activity {
	public String TAG = "DataAnalysis";
	public String SubTag = "DataAnalysis: ";
	public static final String PREFS_NAME = "gamePnLTrackerFile";
	private static final String PREF_USERNAME = "username";
	public String currentUser = new String();
	protected String evYearS;
	protected String evMonthS;
	protected String evDayS;
	final Calendar c = Calendar.getInstance();
	String startSearchDate = null;
	String endSearchDate = null;
	String IntentQ;
	String username;
	final private int[] availableColors = { Color.RED, Color.CYAN,
			Color.YELLOW, Color.GREEN, Color.GRAY, Color.WHITE, Color.LTGRAY,
			Color.MAGENTA, Color.BLACK, Color.DKGRAY, Color.BLUE };

	Button StartDateB;
	Button EndDateB;

	boolean viewPresent;

	private Context mContext = null;
	private DbHelper db;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		gamesLogger.i(TAG, SubTag + "Starting GraphData");
		super.onCreate(savedInstanceState);
		mContext = this; // since Activity extends Context
		mContext = getApplicationContext();
		mContext = getBaseContext();
		viewPresent = false;
		db = new DbHelper(mContext);
	}

	protected void onResume() {
		super.onResume();

		final int mYear = c.get(Calendar.YEAR);
		final int mMonth = c.get(Calendar.MONTH) + 1;
		final int mDay = c.get(Calendar.DAY_OF_MONTH);
		setContentView(R.layout.dataanalysis);

		SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		username = pref.getString(PREF_USERNAME, null);
		this.setTitle("User: " + username);

		AdView adView = (AdView) findViewById(R.id.adDataAnalysis);
		adView.loadAd(new AdRequest());

		final Spinner aBy = (Spinner) findViewById(R.id.ATypeSpin);
		ArrayAdapter<CharSequence> strs = ArrayAdapter.createFromResource(this,
				R.array.analyzeByNames, android.R.layout.simple_spinner_item);
		strs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		aBy.setAdapter(strs);
		aBy.setOnItemSelectedListener(aBylistener);
		StartDateB = (Button) findViewById(R.id.AstartDateButton);
		StartDateB.setText("Start Date");
		StartDateB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				gamesLogger.i(TAG, SubTag + "Start date button is clicked");
				new DatePickerDialog(DataAnalysis.this, mStartDateSetListener,
						mYear, mMonth - 1, mDay).show();
			}
		});
		EndDateB = (Button) findViewById(R.id.AendDateButton);
		EndDateB.setText("End Date");
		EndDateB.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				gamesLogger.i(TAG, SubTag + "End date button is clicked");
				new DatePickerDialog(DataAnalysis.this, mEndDateSetListener,
						mYear, mMonth - 1, mDay).show();
			}
		});
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

	private Spinner.OnItemSelectedListener aBylistener = new Spinner.OnItemSelectedListener() {
		private GraphicalView mChartView = null;

		public void onItemSelected(AdapterView<?> parent, View v, int position,
				long id) {
			Cursor result = null;
			Button StartDateB;
			Button EndDateB;
			String analyzeBy = parent.getSelectedItem().toString();
			// GraphicalView mChartView = null;

			StartDateB = (Button) findViewById(R.id.AstartDateButton);
			EndDateB = (Button) findViewById(R.id.AendDateButton);

			gamesLogger.i(TAG, SubTag + "Start date: " + StartDateB.getText());
			gamesLogger.i(TAG, SubTag + "End date: " + EndDateB.getText());

			if (analyzeBy.equalsIgnoreCase("Event Type")) {
				double cashCount = 0;
				double tourneyCount = 0;

				// Query games based on the event type: Cash or Tournament
				gamesLogger.i(TAG, SubTag + "Analyzing by Event Type");
				// Start with cash
				populateQuesryString();
				String query = IntentQ + " AND eventType = 'Cash'";
				gamesLogger.i(TAG, SubTag + "Q: " + query);

				db = new DbHelper(mContext);
				gamesLogger.i(TAG, SubTag + "Query: " + query);
				result = db.getData("gPNLData", query);

				if (result.moveToFirst()) {
					do {
						double dValue;
						String value = result.getString(result
								.getColumnIndex("amount"));
						if (value.equals(""))
							dValue = 0;
						else
							dValue = Double.parseDouble(value);

						cashCount += dValue;
					} while (result.moveToNext());
				} else
					gamesLogger.i(TAG, SubTag
							+ "No Data returned from Content Provider");

				// Do the count for NoLimit
				query = IntentQ + " AND eventType = 'Tourney'";

				db = new DbHelper(mContext);
				gamesLogger.i(TAG, SubTag + "Query: " + query);
				result = db.getData("gPNLData", query);
				if (result.moveToFirst()) {
					do {
						double dValue;
						String value = result.getString(result
								.getColumnIndex("amount"));
						if (value.equals(""))
							dValue = 0;
						else
							dValue = Double.parseDouble(value);

						tourneyCount += dValue;
					} while (result.moveToNext());
				} else
					gamesLogger.i(TAG, SubTag
							+ "No Data returned from Content Provider");
				// Adjust everything to positive numbers
				gamesLogger.i(TAG, SubTag + "Cash: $" + cashCount
						+ " Tourney: $" + tourneyCount);
				if (cashCount < 0 || tourneyCount < 0) {
					if (cashCount < 0 && tourneyCount > 0) {
						cashCount = 0 - cashCount;
						tourneyCount += cashCount;
					} else {
						tourneyCount = 0 - tourneyCount;
						cashCount += tourneyCount;
					}
				}
				gamesLogger.i(TAG, SubTag + "Cash: $" + cashCount
						+ " Tourney: $" + tourneyCount);
				try {
					DefaultRenderer renderer = new DefaultRenderer();
					renderer.setLabelsTextSize(20);
					renderer.setLegendTextSize(20);
					renderer.setMargins(new int[] { 0, 0, 0, 0 });
					CategorySeries series = new CategorySeries("Earnings");
					if (cashCount != 0) {
						SimpleSeriesRenderer cR = new SimpleSeriesRenderer();
						cR.setColor(Color.GREEN);
						renderer.addSeriesRenderer(cR);
						series.add("Cash", cashCount);
					}
					if (tourneyCount != 0) {
						SimpleSeriesRenderer tR = new SimpleSeriesRenderer();
						tR.setColor(Color.RED);
						renderer.addSeriesRenderer(tR);
						series.add("Tourney", tourneyCount);
					}
					LinearLayout layout = (LinearLayout) findViewById(R.id.aChart);

					// Intent mChartView =
					// ChartFactory.getPieChartIntent(DataAnalysis.this, series,
					// renderer, "Profit/Loss");
					// Intent mChartView =
					// ChartFactory.getLineChartIntent(DataAnalysis.this,
					// mDataset, mRenderer);
					// startActivity(mChartView);

					if (viewPresent && mChartView != null)
						layout.removeView(mChartView);

					mChartView = ChartFactory.getPieChartView(
							DataAnalysis.this, series, renderer);
					layout.addView(mChartView);
					viewPresent = true;
				} catch (Exception e) {
					gamesLogger.e(TAG, SubTag + e.getMessage());
				}
			} else if (analyzeBy.equalsIgnoreCase("Game Type")) {
				gamesLogger.i(TAG, SubTag + "Analyzing by Game Type");

				String query = "addedBy = '" + username + "'"
						+ " OR addedBy = 'gamePnL'";
				DbHelper db = new DbHelper(mContext);
				gamesLogger.i(TAG, SubTag + "Query: " + query);
				result = db.getData("gGames", query);
				gamesLogger.i(TAG, SubTag
						+ "Everything is ready for the Spinner. # of records: "
						+ result.getCount());
				int numGames = result.getCount();
				ArrayAdapter<String> games = new ArrayAdapter<String>(
						DataAnalysis.this, result.getCount());
				if (result.moveToFirst()) {
					do {
						games.add(result.getString(1));
					} while (result.moveToNext());
				}

				CategorySeries series = new CategorySeries("Earnings");
				DefaultRenderer renderer = new DefaultRenderer();
				double[] sums = new double[numGames];
				String[] names = new String[numGames];
				int k = 0;

				for (int cnt = 0; cnt < numGames; cnt++)
					sums[cnt] = 0;

				for (int gk = 0; gk < numGames; gk++) {
					String gameName = games.getItem(gk);

					gameName = gameName.replace("\'", "\'\'");
					names[gk] = gameName;
					gamesLogger.i(TAG, SubTag + "Adding data for " + gameName);
					populateQuesryString();
					query = IntentQ + " AND gameType = '" + gameName + "'";
					// db = new DbHelper(mContext);
					gamesLogger.i(TAG, SubTag + "Query: " + query);
					result = db.getData("gPNLData", query);

					if (result.moveToFirst()) {
						do {
							double dValue;
							String value = result.getString(result
									.getColumnIndex("amount"));
							if (value.equals(""))
								dValue = 0;
							else
								dValue = Double.parseDouble(value);
							sums[gk] += dValue;

						} while (result.moveToNext());
					} else
						gamesLogger.i(TAG, SubTag
								+ "No Data returned from Content Provider");
				}

				// Adjust all numbers to make sure they are all positive
				double minValue = 0;
				for (int cnt = 0; cnt < numGames; cnt++) {
					if (sums[cnt] < minValue)
						minValue = sums[cnt];
				}
				if (minValue < 0) {
					for (int cnt = 0; cnt < numGames; cnt++) {
						if (sums[cnt] != 0)
							sums[cnt] += (minValue + 1);
					}
				}
				for (int cnt = 0; cnt < numGames; cnt++) {
					if (sums[cnt] != 0) {
						series.add(names[cnt], sums[cnt]);
						if (k >= availableColors.length)
							k = 0;

						SimpleSeriesRenderer ssr = new SimpleSeriesRenderer();
						ssr.setColor(availableColors[k++]);
						renderer.addSeriesRenderer(ssr);
					}
				}

				renderer.setLabelsTextSize(20);
				renderer.setLegendTextSize(20);
				renderer.setMargins(new int[] { 0, 0, 0, 0 });

				try {
					LinearLayout layout = (LinearLayout) findViewById(R.id.aChart);

					if (viewPresent && mChartView != null)
						layout.removeView(mChartView);

					mChartView = ChartFactory.getPieChartView(
							DataAnalysis.this, series, renderer);
					layout.addView(mChartView);
					viewPresent = true;

				} catch (Exception e) {
					gamesLogger.e(TAG, SubTag + e.getMessage());
				}
			} else if (analyzeBy.equalsIgnoreCase("Limit")) {
				gamesLogger.i(TAG, SubTag + "Analyzing by Limit");
				String[] gmLimitName = new String[] { "NoLimit", "Limit",
						"PotLimit", "Mixed" };
				double[] sums = new double[gmLimitName.length];

				for (int cnt = 0; cnt < gmLimitName.length; cnt++)
					sums[cnt] = 0;

				for (int cnt = 0; cnt < gmLimitName.length; cnt++) {
					populateQuesryString();
					String query = IntentQ + " AND gameLimit = '"
							+ gmLimitName[cnt] + "'";

					DbHelper db = new DbHelper(mContext);
					gamesLogger.i(TAG, SubTag + "Query: " + query);
					result = db.getData("gPNLData", query);
					if (result.moveToFirst()) {
						do {
							double dValue;
							String value = result.getString(result
									.getColumnIndex("amount"));
							if (value.equals(""))
								dValue = 0;
							else
								dValue = Double.parseDouble(value);
							sums[cnt] += dValue;
						} while (result.moveToNext());
					} else
						gamesLogger.i(TAG, SubTag
								+ "No Data returned from Content Provider");
				}
				if (result != null)
					result.close();

				try {
					CategorySeries series = new CategorySeries("Earnings");

					DefaultRenderer renderer = new DefaultRenderer();
					renderer.setLabelsTextSize(20);
					renderer.setLegendTextSize(20);
					renderer.setMargins(new int[] { 0, 0, 0, 0 });
					// Adjust all numbers to make sure they are all positive
					double minValue = 0;
					for (int cnt = 0; cnt < gmLimitName.length; cnt++) {
						if (sums[cnt] < minValue)
							minValue = sums[cnt];
					}
					if (minValue < 0) {
						for (int cnt = 0; cnt < gmLimitName.length; cnt++) {
							if (sums[cnt] != 0)
								sums[cnt] += (minValue + 1);
						}
					}
					for (int cnt = 0; cnt < gmLimitName.length; cnt++) {
						if (sums[cnt] != 0) {
							SimpleSeriesRenderer ssr = new SimpleSeriesRenderer();
							ssr.setColor(availableColors[cnt]);
							renderer.addSeriesRenderer(ssr);

							series.add(gmLimitName[cnt], sums[cnt]);
						}
					}

					LinearLayout layout = (LinearLayout) findViewById(R.id.aChart);

					if (viewPresent && mChartView != null)
						layout.removeView(mChartView);

					mChartView = ChartFactory.getPieChartView(
							DataAnalysis.this, series, renderer);
					layout.addView(mChartView);
					viewPresent = true;
				} catch (Exception e) {
					gamesLogger.e(TAG, SubTag + e.getMessage());
				}
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			gamesLogger.i(TAG, SubTag + "Analyze by EventType");
		}
	};

	private void populateQuesryString() {
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
		gamesLogger.i(TAG, SubTag + "IntentQ: " + IntentQ);
	}
}
