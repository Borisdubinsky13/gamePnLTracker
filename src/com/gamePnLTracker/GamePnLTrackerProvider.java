/**
 * 
 */
package com.gamePnLTracker;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

/**
 * @author Boris
 *
 */
public class GamePnLTrackerProvider extends ContentProvider 
{
	public static String TAG="gamePnLTracker";
	public static String SubTag="GamePnLTrackerProvider: ";

	private static final String DATABASE_NAME = "gamepnltracker.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String USER_TABLE_NAME = "gUsers";
	private static final String PNL_TABLE_NAME = "gPNLData";

	private	static	HashMap<String, String> USER_PROJECTION_MAP;
	private	static	HashMap<String, String> PNL_PROJECTION_MAP;
	
	public static final String AUTHORITY = 
		"com.gamePnLTracker.provider.userContentProvider";

	private static final int USER = 1;
	private static final int PNLDATA = 2;
	private static final UriMatcher sURIMatcher = buildUriMatcher();
	private static UriMatcher buildUriMatcher() 
	{
		UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, "users", USER);
		matcher.addURI(AUTHORITY, "pnldata", PNLDATA);
		return matcher;
	}
	
	private static class DbAdapter extends SQLiteOpenHelper
	{
		public DbAdapter(Context context) 
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			Log.i(TAG, SubTag + "Creating Users database");
			Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" +
					USER_TABLE_NAME + "'", null);
			try 
			{
				if (c.getCount()==0) 
				{
					db.execSQL("CREATE TABLE " + 
							USER_TABLE_NAME + " (_ID INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, email TEXT, passwd TEXT);");
				}
			}
			finally 
			{
				c.close();
			}
			Log.i(TAG, SubTag + "Creating PnLdata database");
			c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"  +
					PNL_TABLE_NAME + "'", null);
			try 
			{
				if (c.getCount()==0) 
				{
					db.execSQL("CREATE TABLE " + PNL_TABLE_NAME + " (_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
							"uid TEXT, " +
							"name TEXT, " +
							"amount TEXT, " +
							"date TEXT, " +
							"eventType TEXT, " +
							"gameType TEXT, " +
							"gameLimit TEXT, " +
							"notes TEXT" +
 							");");
				}
			}
			finally 
			{
				c.close();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			Log.i(TAG , SubTag + "Upgrading database. Implementation is pending.");
		}

	}

	private DbAdapter dbHelper;

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) 
	{
	      SQLiteDatabase db = dbHelper.getWritableDatabase();
	
	      int count;
			switch (sURIMatcher.match(uri)) 
			{
	      	case USER:
	      		count = db.delete(USER_TABLE_NAME, where, whereArgs);
	      		break;
	      	case PNLDATA:
	      		count = db.delete(PNL_TABLE_NAME, where, whereArgs);
	      		break;
	      	default:
	      		throw new IllegalArgumentException("Unknown URI " + uri);
	      }
	      getContext().getContentResolver().notifyChange(uri, null);
	      return count;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) 
	{
		switch (sURIMatcher.match(uri)) 
		{
			case USER:
				return "vnd.gamePnLTracker.cursor.item/users";
			case PNLDATA:
				return "vnd.gamePnLTracker.cursor.dir/pnldata";
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		switch (sURIMatcher.match(uri)) 
		{
			case USER:
            	db.insert(USER_TABLE_NAME,null,values);
            	db.close();
				break;
			case PNLDATA:
            	db.insert(PNL_TABLE_NAME ,null,values);
            	db.close();
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() 
	{
		dbHelper = new DbAdapter(getContext());
		Log.i(TAG, SubTag + "Created dbHelper");	
		
		return true;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sort) 
	{
		Cursor	result;
		String	sqlStm = "SELECT ";
		// final DbAdapter dba = new DbAdapter(getContext());
		SQLiteDatabase dba = dbHelper.getReadableDatabase();
		Log.i(TAG, SubTag + "Started Query");
		switch (sURIMatcher.match(uri)) 
		{
			case USER:
				sqlStm += "name FROM ";
				sqlStm += USER_TABLE_NAME;
				sqlStm += " WHERE ";
				sqlStm += selection;
				Log.i(TAG, SubTag + "sql: " + sqlStm);
				break;
			case PNLDATA:
				sqlStm += "amount,date,gameType FROM ";
				sqlStm += PNL_TABLE_NAME;
				sqlStm += " WHERE ";
				sqlStm += selection;
				Log.i(TAG, SubTag + "sql: " + sqlStm);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		Log.i(TAG, SubTag + "Trying to execute a query.");
		// result = qb.query(db, projection, selection, selectionArgs, null, null, null);
		result = dba.rawQuery(sqlStm, null);
		Log.i(TAG, SubTag + "Returning query result");
		return result;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	static
	{
		USER_PROJECTION_MAP = new HashMap<String,String>();
		USER_PROJECTION_MAP.put(USER_TABLE_NAME, "name" );
		USER_PROJECTION_MAP.put(USER_TABLE_NAME, "email" );
		USER_PROJECTION_MAP.put(USER_TABLE_NAME, "passwd");

		PNL_PROJECTION_MAP = new HashMap<String,String>();
		PNL_PROJECTION_MAP.put(PNL_TABLE_NAME, "uid" );
		PNL_PROJECTION_MAP.put(PNL_TABLE_NAME, "name" );
		PNL_PROJECTION_MAP.put(PNL_TABLE_NAME, "amount" );
		PNL_PROJECTION_MAP.put(PNL_TABLE_NAME, "date");
		PNL_PROJECTION_MAP.put(PNL_TABLE_NAME, "eventType");
		PNL_PROJECTION_MAP.put(PNL_TABLE_NAME, "gameType");
		PNL_PROJECTION_MAP.put(PNL_TABLE_NAME, "gameLimit");
		PNL_PROJECTION_MAP.put(PNL_TABLE_NAME, "notes");
	}
}
