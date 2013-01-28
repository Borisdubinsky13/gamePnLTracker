package com.tracker.gamesPnL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


public class ImportActivityYN extends Activity {
    public String TAG = "gamePnLTracker";
    public String SubTag = "ImportActivity: ";

    public String username;
    private static final String PREF_USERNAME = "username";
    public static final String PREFS_NAME = "gamePnLTrackerFile";

    private static String fnoSDName = "/gamesPnL.csv";

    // private	ProgressDialog	progressDialog;
    private ProgressDialog progressDialog;
    // private	ProgressBar progressBar;
    private int percentDone;
    private long currentCount = 0;

    private Handler mHandler = new Handler();

    private void actualDoImport(View v) {
        // setContentView(R.layout.prgrssbar);
        gamesLogger.i(TAG, SubTag + "Creating progress dialog");

        try {
            final File f = new File(Environment.getExternalStorageDirectory() + fnoSDName);
            final long fSize = f.length();

            progressDialog = new ProgressDialog(v.getContext());
            progressDialog.setCancelable(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.setMessage("Importing " + Environment.getExternalStorageDirectory() + fnoSDName + " ...");
            progressDialog.setMax((int) fSize);
            progressDialog.show();

            gamesLogger.i(TAG, SubTag + "Starting the work");

            new Thread(new Runnable() {
                public void run() {
                    int curPercent = 0;
                    // try opening the myfilename.txt
                    try {
                        gamesLogger.i(TAG, SubTag + "Starting an import");

                        Uri tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
                        tmpUri = Uri.withAppendedPath(tmpUri, "pnldata");
                        ContentResolver cr = getContentResolver();
                        gamesLogger.i(TAG, SubTag + "Got URI populated");
                        // open the file for reading
/*
                        File	f = new File(Environment.getExternalStorageDirectory()+fnoSDName);
            			long	fSize = f.length();
            			long	currentCount = 0;
*/

                        gamesLogger.i(TAG, SubTag + "File: " + Environment.getExternalStorageDirectory() + fnoSDName);
                        gamesLogger.i(TAG, SubTag + "File size: " + fSize);

                        FileInputStream instream = new FileInputStream(f);
                        // if file the available for reading
                        if (instream != null) {
                            // prepare the file for reading
                            InputStreamReader inputreader = new InputStreamReader(instream);
                            BufferedReader buffreader = new BufferedReader(inputreader);

                            SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                            String username = pref.getString(PREF_USERNAME, null);

                            String line;
                            int lineNumber = 0;
                            String delimeter = null;
                            currentCount = 0;

                            // read every line of the file into the line-variable, on line at the time
                            while ((line = buffreader.readLine()) != null) {
                                int startExt;
                                int endExt;
                                gamesLogger.i(TAG, SubTag + "Read line:" + line);
                                currentCount += line.length();
                                // Figure out the separation field. First line only is the heading, so the first word is "Date".
                                // extract the 5th character and that should be the separation character
                                if (lineNumber == 0) {
                                    startExt = 4;
                                    endExt = 5;
                                    delimeter = line.substring(startExt, endExt);
                                    lineNumber++;
                                } else {
                                    // parse the line and extract the comma separated fields.
                                    startExt = 0;
                                    endExt = line.indexOf(delimeter);
                                    String date = line.substring(0, endExt);
                                    String evMonth = date.substring(0, 2);
                                    String evDay = date.substring(3, 5);
                                    String evYear = date.substring(6);
                                    line = line.substring(endExt + 1);
                                    endExt = line.indexOf(delimeter);
                                    String evType = line.substring(0, endExt);
                                    line = line.substring(endExt + 1);
                                    endExt = line.indexOf(delimeter);
                                    String gameType = line.substring(0, endExt);
                                    line = line.substring(endExt + 1);
                                    endExt = line.indexOf(delimeter);
                                    String gameLimit = line.substring(0, endExt);
                                    line = line.substring(endExt + 1);
                                    endExt = line.indexOf(delimeter);
                                    String amount = line.substring(0, endExt);

                                    ContentValues vals = new ContentValues();
                                    vals.put("name", username);
                                    vals.put("amount", amount);
                                    vals.put("evYear", evYear);
                                    vals.put("evMonth", evMonth);
                                    vals.put("evDay", evDay);
                                    date = evYear + "-" + evMonth + "-" + evDay;
                                    vals.put("evDate", date);
                                    vals.put("eventType", evType);
                                    vals.put("gameType", gameType);
                                    vals.put("gameLimit", gameLimit);
                                    gamesLogger.i(TAG, SubTag + "Inserting a record");
                                    cr.insert(tmpUri, vals);

                                    lineNumber++;

                                    // Update the progress bar if needed
                                    percentDone = (int) (currentCount * 100 / fSize);
                                    gamesLogger.i(TAG, SubTag + "currentCount: " + currentCount);
                                    if (curPercent < percentDone) {
                                        curPercent = percentDone;

                                        if (percentDone < 100) {
                                            gamesLogger.i(TAG, SubTag + "Updating progress bar to " + percentDone);
                                            percentDone++;
                                            // Update the progress bar
                                            mHandler.post(new Runnable() {
                                                public void run() {
                                                    progressDialog.setProgress((int) currentCount);
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                            // close the file
                            instream.close();
                            finish();
                        }
                    } catch (Exception e) {
                        gamesLogger.e(TAG, SubTag + e.getMessage());
                        progressDialog.dismiss();
                    }
                }
            }).start();
        } catch (Exception e) {
            gamesLogger.i(TAG, SubTag + e);
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.importdb);

        Button nButton = (Button) findViewById(R.id.NoButton);
        nButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
				/* Perform actual import */
                actualDoImport(v);
            }
        });

        Button yButton = (Button) findViewById(R.id.YesButton);
        yButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                gamesLogger.i(TAG, SubTag + "Removing the database");

                try {
                    ContentResolver cr = getContentResolver();
                    gamesLogger.i(TAG, SubTag + "Got content resolver");
                    Uri tmpUri = Uri.parse("content://com.gamesPnL.provider.userContentProvider");
                    tmpUri = Uri.withAppendedPath(tmpUri, "pnldata");
                    cr.delete(tmpUri, null, null);
                    gamesLogger.i(TAG, SubTag + "Results data is removed");
                } catch (Exception e) {
                    gamesLogger.i(TAG, SubTag + e);
                }
                actualDoImport(v);
            }
        });
        // doImport();
    }
}
