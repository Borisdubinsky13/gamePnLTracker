package com.tracker.gamesPnL;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;

public class MyBackupAgent extends BackupAgentHelper {

	private static final String DB_NAME = "gamepnltracker.db";
	public String TAG = "gamePnLTracker";
	public String SubTag = "MyBackupAgent: ";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.app.backup.BackupAgentHelper#onBackup(android.os.ParcelFileDescriptor
	 * , android.app.backup.BackupDataOutput, android.os.ParcelFileDescriptor)
	 */
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
			ParcelFileDescriptor newState) throws IOException {
		gamesLogger.i(TAG, SubTag + "OnBackup()...");
		super.onBackup(oldState, data, newState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.backup.BackupAgentHelper#onRestore(android.app.backup.
	 * BackupDataInput, int, android.os.ParcelFileDescriptor)
	 */
	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException {
		gamesLogger.i(TAG, SubTag + "OnRestore()...");
		super.onRestore(data, appVersionCode, newState);
	}

	@Override
	public void onCreate() {
		gamesLogger.i(TAG, SubTag + "OnCreate()...");
		FileBackupHelper helper = new FileBackupHelper(this, getDatabasePath(
				DB_NAME).getAbsolutePath());
		addHelper("dbs", helper);
	}
}
