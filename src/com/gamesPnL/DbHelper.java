/**
 * 
 */
package com.gamesPnL;

import java.util.Locale;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author boris
 * 
 */
public class DbHelper extends SQLiteOpenHelper {

	public static String TAG = "DbHelper";
	public static String SubTag;

	private static final String DATABASE_NAME = "gamepnltracker.db";
	private static final int DATABASE_VERSION = 12;

	private static final String ID_KEY = "_id";
	private static final String USER_TABLE_NAME = "gUsers";
	private static final String PNL_TABLE_NAME = "gPNLData";
	private static final String PNL_STATUS_TABLE_NAME = "gStatus";
	private static final String PNL_GAMES_TABLE_NAME = "gGames";
	/*
	 * private static HashMap<String, String> USER_PROJECTION_MAP; private
	 * static HashMap<String, String> PNL_PROJECTION_MAP; private static
	 * HashMap<String, String> PNL_STAT_PROJECTION_MAP; private static
	 * HashMap<String, String> PNL_GAMES_PROJECTION_MAP;
	 */
	public static final String AUTHORITY = "com.gamesPnL.provider.userContentProvider";

	static String[] gms = { "TexasHold'em", "Omaha", "Stud", "BlackJack" };
	static String[] desc = { "Texas Holdem", "Omaha", "Stud", "BlackJack" };

	private static BackupManager bkpMgm = null;

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		bkpMgm = new BackupManager(context);
	}

	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 * @param errorHandler
	 */
	public DbHelper(Context context, String name, CursorFactory factory,
			int version, DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		SubTag = "onCreate";
		gamesLogger.i(TAG, SubTag + "Creating Users table");
		try {
			Cursor c = db.rawQuery(
					"SELECT name FROM sqlite_master WHERE type='table' AND name='"
							+ USER_TABLE_NAME + "'", null);
			if (c.getCount() == 0) {
				db.execSQL("CREATE TABLE " + USER_TABLE_NAME
						+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "name TEXT UNIQUE, " + "passwd TEXT, "
						+ "firstName TEXT, " + "lastName TEXT, "
						+ "email TEXT);");
			}

			gamesLogger.i(TAG, SubTag + "Creating PnLdata table");
			c = db.rawQuery(
					"SELECT name FROM sqlite_master WHERE type='table' AND name='"
							+ PNL_TABLE_NAME + "'", null);

			if (c.getCount() == 0) {
				db.execSQL("CREATE TABLE " + PNL_TABLE_NAME
						+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "uid TEXT, " + "name TEXT, " + "amount TEXT, "
						+ "evYear TEXT, " + "evMonth TEXT, " + "evDay TEXT, "
						+ "evDate DATE, " + "eventType TEXT, "
						+ "gameType TEXT, " + "gameLimit TEXT, " + "notes TEXT"
						+ ");");
			}

			gamesLogger.i(TAG, SubTag + "Creating PnL status table");
			c = db.rawQuery(
					"SELECT name FROM sqlite_master WHERE type='table' AND name='"
							+ PNL_STATUS_TABLE_NAME + "'", null);
			if (c.getCount() == 0) {
				db.execSQL("CREATE TABLE " + PNL_STATUS_TABLE_NAME
						+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "name TEXT UNIQUE, " + "status TEXT);");
			}

			gamesLogger.i(TAG, SubTag + "Creating PnL games table");
			c = db.rawQuery(
					"SELECT name FROM sqlite_master WHERE type='table' AND name='"
							+ PNL_GAMES_TABLE_NAME + "'", null);

			if (c.getCount() == 0) {
				String sql = "CREATE TABLE " + PNL_GAMES_TABLE_NAME
						+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "game TEXT UNIQUE, " + "description TEXT, "
						+ "addedBy TEXT" + ");";
				gamesLogger.i(TAG, SubTag + "SQL: " + sql);
				db.execSQL(sql);

				gamesLogger.i(TAG, SubTag + "Populating PnLgames table");

				db.execSQL("INSERT INTO "
						+ PNL_GAMES_TABLE_NAME
						+ " (game,description,addedBy) values ('TexasHold''em', 'Texas Hold''em', 'gamePnL');");
				db.execSQL("INSERT INTO "
						+ PNL_GAMES_TABLE_NAME
						+ " (game,description,addedBy) values ('Omaha', 'Omaha', 'gamePnL');");
				db.execSQL("INSERT INTO "
						+ PNL_GAMES_TABLE_NAME
						+ " (game,description,addedBy) values ('Stud', 'Stud', 'gamePnL');");
				db.execSQL("INSERT INTO "
						+ PNL_GAMES_TABLE_NAME
						+ " (game,description,addedBy) values ('Blackjack', 'BlackJack', 'gamePnL');");
			}
			/*
			 * gamesLogger.i(TAG, SubTag + "Backup: dataChanged()");
			 * bkpMgm.dataChanged();
			 */
		} catch (Exception e) {
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		SubTag = "onUpdate";
		String sql = null;
		Cursor from;
		gamesLogger.i(TAG, SubTag + "starting an upgrade from " + oldVersion
				+ " to " + newVersion);
		// First rename the table to <table>_OLD

		try {
			if (oldVersion <= 6) {
				// Start with user table
				sql = "ALTER TABLE " + USER_TABLE_NAME + " RENAME TO "
						+ USER_TABLE_NAME + "_OLD;";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);

				// Create a user table
				sql = "CREATE TABLE " + USER_TABLE_NAME
						+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "name TEXT UNIQUE, " + "passwd TEXT, "
						+ "firstName TEXT, " + "lastName TEXT, "
						+ "email TEXT);";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);
				// copy all the records from the <table>_old to <table>

				sql = "SELECT name,email,passwd FROM " + USER_TABLE_NAME
						+ "_OLD;";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				from = db.rawQuery(sql, null);
				if (from.moveToFirst()) {
					do {
						ContentValues vals = new ContentValues();
						vals.put("name", from.getString(0));
						vals.put("email", from.getString(1));
						vals.put("passwd", from.getString(2));
						gamesLogger.i(TAG, SubTag + "adding record");
						db.insert(USER_TABLE_NAME, null, vals);
					} while (from.moveToNext());
					// Delete the <table>_OLD
					sql = "DROP TABLE IF EXISTS " + USER_TABLE_NAME + "_OLD;";
					gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
					db.execSQL(sql);
				}
			}

			// Next is data table
			sql = "ALTER TABLE " + PNL_TABLE_NAME + " RENAME TO "
					+ PNL_TABLE_NAME + "_OLD;";
			gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
			db.execSQL(sql);

			// Create the table
			sql = "CREATE TABLE " + PNL_TABLE_NAME
					+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "uid TEXT, " + "name TEXT, " + "amount TEXT, "
					+ "evYear TEXT, " + "evMonth TEXT, " + "evDay TEXT, "
					+ "evDate DATE, " + "eventType TEXT, " + "gameType TEXT, "
					+ "gameLimit TEXT, " + "notes TEXT" + ");";
			gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
			db.execSQL(sql);

			// copy all the records from the <table>_old to <table>
			sql = "SELECT uid,name,amount,eventType,gameType,gameLimit,notes,date FROM "
					+ PNL_TABLE_NAME + "_OLD;";
			boolean dateExist = true;
			try {
				from = db.rawQuery(sql, null);
				dateExist = true;
			} catch (Exception e) {
				sql = "SELECT uid,name,amount,eventType,gameType,gameLimit,notes,EvYear,EvMonth,EvDay FROM "
						+ PNL_TABLE_NAME + "_OLD;";
				from = db.rawQuery(sql, null);
				dateExist = false;
			}

			if (from.moveToFirst()) {
				do {
					ContentValues vals = new ContentValues();

					if (!from.getString(2).equals("")
							&& !from.getString(2).equals("-")) {
						int startLoc;
						int endLoc;
						String uidS = null;
						String nameS = null;
						String amountS = null;
						String dateS;
						String yearS = null;
						String monthS = null;
						String dayS = null;
						String eventTypeS = null;
						String gameTypeS = null;
						String gameLimitS = null;
						String notesS = null;
						String newDateF = null;
						String tmp;

						switch (oldVersion) {
						case 1:
						case 2:
						case 3:
						case 4:
						case 5:
						case 6:
						case 7:
						case 8:
						case 9:
							// There is no change in format, it's a bug fix.
							// the bug was that sometimes the date was stored with '-' instead
							// of '/'
						case 10:
							uidS = from.getString(0);
							nameS = from.getString(1);
							amountS = from.getString(2);
							eventTypeS = from.getString(3);
							gameTypeS = from.getString(4);
							gameLimitS = from.getString(5);
							notesS = from.getString(6);
							if (dateExist) {
								dateS = from.getString(7);
								// The current format of the field is
								// mm/dd/yyyy
								// Make sure that the date does not have '-'
								dateS = dateS.replace('-','/');
								startLoc = 0;
								endLoc = dateS.indexOf('/');
								monthS = dateS.substring(startLoc, endLoc);
								tmp = dateS.substring(endLoc + 1);
								startLoc = 0;
								endLoc = tmp.indexOf('/');
								dayS = tmp.substring(startLoc, endLoc);
								yearS = tmp.substring(endLoc + 1);
							} else {
								yearS = from.getString(7);
								monthS = from.getString(8);
								dayS = from.getString(9);
							}
							dayS = String.format(Locale.getDefault(), "%02d",
									Integer.parseInt(dayS));
							monthS = String.format(Locale.getDefault(), "%02d",
									Integer.parseInt(monthS));
							yearS = String.format(Locale.getDefault(), "%04d",
									Integer.parseInt(yearS));
							newDateF = yearS + "/" + monthS + "/" + dayS;

							vals.put("uid", uidS);
							vals.put("name", nameS);
							vals.put("amount", amountS);
							vals.put("evYear", yearS);
							vals.put("evMonth", monthS);
							vals.put("evDay", dayS);
							vals.put("evDate", newDateF);
							vals.put("eventType", eventTypeS);
							vals.put("gameType", gameTypeS);
							vals.put("gameLimit", gameLimitS);
							vals.put("notes", notesS);
							db.insert(PNL_TABLE_NAME, null, vals);

							gamesLogger.i(TAG, SubTag + "Year: " + yearS
									+ " Month: " + monthS + " Day: " + dayS
									+ "(" + newDateF + ")");
							break;
						default:
							break;
						}
					}
				} while (from.moveToNext());
			}
			// Delete the <table>_OLD
			sql = "DROP TABLE IF EXISTS " + PNL_TABLE_NAME + "_OLD;";
			gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
			db.execSQL(sql);

			// Finally, status table
			sql = "ALTER TABLE " + PNL_STATUS_TABLE_NAME + " RENAME TO "
					+ PNL_STATUS_TABLE_NAME + "_OLD;";
			gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
			db.execSQL(sql);

			// Create the table
			sql = "CREATE TABLE " + PNL_STATUS_TABLE_NAME
					+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "name TEXT, " + "status TEXT " + ");";
			gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
			db.execSQL(sql);

			// copy all the records from the <table>_old to <table>
			sql = "SELECT name,status FROM " + PNL_STATUS_TABLE_NAME + "_OLD;";
			gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
			from = db.rawQuery(sql, null);
			if (from.moveToFirst()) {
				do {
					ContentValues vals = new ContentValues();
					vals.put("name", from.getString(0));
					vals.put("status", from.getString(1));
					db.insert(PNL_STATUS_TABLE_NAME, null, vals);
				} while (from.moveToNext());
			}
			// Delete the <table>_OLD
			sql = "DROP TABLE IF EXISTS " + PNL_STATUS_TABLE_NAME + "_OLD;";
			gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
			db.execSQL(sql);

			if (oldVersion <= 4) {
				// Create a table that would have list of all games
				sql = "CREATE TABLE " + PNL_GAMES_TABLE_NAME
						+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "game TEXT, " + "description TEXT, " + "addedBy TEXT"
						+ ");";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);
				// Need to populate this table with some of the games
				gamesLogger.i(TAG, SubTag + "Populating PnL games table");

				sql = "INSERT INTO "
						+ PNL_GAMES_TABLE_NAME
						+ " (game,description,addedBy) values ('TexasHold''em', 'Texas Hold''em', 'gamePnL');";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);
				sql = "INSERT INTO "
						+ PNL_GAMES_TABLE_NAME
						+ " (game,description,addedBy) values ('Omaha', 'Omaha', 'gamePnL');";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);
				sql = "INSERT INTO "
						+ PNL_GAMES_TABLE_NAME
						+ " (game,description,addedBy) values ('Stud', 'Stud', 'gamePnL');";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);
			}
			if (oldVersion <= 9) {
				sql = "INSERT INTO "
						+ PNL_GAMES_TABLE_NAME
						+ " (game,description,addedBy) values ('blackjack', 'Black Jack', 'gamePnL');";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);
			}
		} catch (Exception e) {
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}

		gamesLogger.i(TAG, SubTag + "Backup: dataChanged() after db upgrade");
		bkpMgm.dataChanged();
		gamesLogger.i(TAG, SubTag + "Upgrade is complete!");
	}

	public void deleteTbl(String table) {
		SubTag = "deleteTbl(): ";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			String sql = "DROP TABLE IF EXISTS " + table + ";";
			db.execSQL(sql);
			// Create empty table, so insert would not fail
			onCreate(db);
		} catch (Exception e) {
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}
	}

	public void insert(String table, ContentValues values) {
		SubTag = "insert(): ";
		try {
			gamesLogger.i(TAG, SubTag + "Inserting a record");

			SQLiteDatabase db = this.getWritableDatabase();

			db.insert(table, null, values);
			db.close();
			gamesLogger.i(TAG, SubTag + "Done inserting a record");
			bkpMgm.dataChanged();

		} catch (Exception e) {
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}
	}

	public Cursor getData(String table) {
		SubTag = "getData(): ";
		String countQuery;

		countQuery = "Select * FROM " + table + ";";
		SQLiteDatabase db = this.getReadableDatabase();
		gamesLogger.i(TAG, SubTag + "SQL: " + countQuery);
		return db.rawQuery(countQuery, null);
	}

	public Cursor getData(String table, String query) {
		SubTag = "getData(): ";
		String countQuery;
		if (query == null)
			countQuery = "Select * FROM " + table + ";";
		else
			countQuery = "Select * FROM " + table + " WHERE " + query + ";";
		SQLiteDatabase db = this.getReadableDatabase();
		gamesLogger.i(TAG, SubTag + "SQL: " + countQuery);
		return db.rawQuery(countQuery, null);
	}

	public Cursor getData(String table, String query, String fields) {
		String countQuery = null;
		SubTag = "getData(): ";
		if (query == null)
			countQuery = "Select " + fields + "FROM " + table + ";";
		else
			countQuery = "Select " + fields + "FROM " + table + " WHERE "
				+ query + ";";
		SQLiteDatabase db = this.getReadableDatabase();
		gamesLogger.i(TAG, SubTag + "SQL: " + countQuery);
		return db.rawQuery(countQuery, null);
	}

	public int getRecordCount(String table, String query) {
		SubTag = "getRecordCount(): ";
		int cnt = 0;
		try {
			String countQuery = query + " FROM " + table;
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(countQuery, null);
			cnt = cursor.getCount();
			cursor.close();
		} catch (Exception e) {
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}
		return cnt;
	}

	public int getRecordCount(String table) {
		SubTag = "getRecordCount(): ";
		int cnt = 0;
		try {
			String countQuery = "SELECT  * FROM " + table;
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(countQuery, null);
			cnt = cursor.getCount();
			cursor.close();
		} catch (Exception e) {
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}
		return cnt;
	}

	public void deleteRecord(String table, int recId) {
		SubTag = "deleteRecord(): ";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.delete(table, ID_KEY + " = ?",
					new String[] { String.valueOf(recId) });
			db.close();
			bkpMgm.dataChanged();
		} catch (Exception e) {
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}
	}

	public void deleteRecord(String table, String q) {
		SubTag = "deleteRecord(): ";
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			db.delete(table, q, null);
			db.close();
			bkpMgm.dataChanged();
		} catch (Exception e) {
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}
	}

	public int updateDataRecord(String table, ContentValues values, String q) {
		SubTag = "updateDataRecord(): ";
		int rc;
		SQLiteDatabase db = this.getWritableDatabase();

		// updating row
		rc = db.update(table, values, q, null);
		db.close();

		bkpMgm.dataChanged();
		return rc;
	}
	
	public int getDBVersion() {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.getVersion();
	}
}
