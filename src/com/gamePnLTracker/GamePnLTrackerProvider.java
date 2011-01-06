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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	private static final int DATABASE_VERSION = 2;
	
	private static final String USER_TABLE_NAME = "gUsers";
	private static final String PNL_TABLE_NAME = "gPNLData";
	private static final String	PNL_STATUS_TABLE_NAME = "gStatus";

	private	static	HashMap<String, String> USER_PROJECTION_MAP;
	private	static	HashMap<String, String> PNL_PROJECTION_MAP;
	private	static	HashMap<String, String> PNL_STAT_PROJECTION_MAP;
	
	public static final String AUTHORITY = 
		"com.gamePnLTracker.provider.userContentProvider";

	private static final int USER = 1;
	private static final int PNLDATA = 2;
	private static final int PNLSTATUS = 3;
	private static final int PNL1RECORD = 4;
	
	
	private static final UriMatcher sURIMatcher = buildUriMatcher();
	
	public static String getMd5Hash(String input) 
	{
        try {
        	MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1,messageDigest);
            String md5 = number.toString(16);
           
            while (md5.length() < 32)
            	md5 = "0" + md5;
           
            return md5;
        } catch(NoSuchAlgorithmException e) {
                Log.e(TAG, SubTag + e.getMessage());
                return null;
        }
	}
	
	private static UriMatcher buildUriMatcher() 
	{
		UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, "users", USER);
		matcher.addURI(AUTHORITY, "pnldata", PNLDATA);
		matcher.addURI(AUTHORITY, "pnlstatus", PNLSTATUS);
		matcher.addURI(AUTHORITY, "pnl1record", PNL1RECORD);
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
			Log.i(TAG, SubTag + "Creating Users table");
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
			Log.i(TAG, SubTag + "Creating PnLdata table");
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
			Log.i(TAG, SubTag + "Creating PnL status table");
			c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"  +
					PNL_STATUS_TABLE_NAME + "'", null);
			try 
			{
				if (c.getCount()==0) 
				{
					db.execSQL("CREATE TABLE " + PNL_STATUS_TABLE_NAME + " (_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
							"name TEXT, " +
							"status TEXT " +
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
			Log.i(TAG , SubTag + "starting an upgrade from " + oldVersion + " to " + newVersion);
			
			/* First, if status table does not exist, create it */
			Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"  +
					PNL_STATUS_TABLE_NAME + "'", null);
			try 
			{
				if (c.getCount()==0) 
				{
					String sql = new String("CREATE TABLE " + PNL_STATUS_TABLE_NAME + " (_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
							"name TEXT, " +
							"status TEXT " +
							");");

				
					db.execSQL(sql);
				}
			}			
			finally
			{
				c.close();
			}
			// Convert the passwords from free format to encrypted
			c = db.rawQuery("SELECT name,passwd FROM " + USER_TABLE_NAME + ";", null);
			if ( c.moveToFirst() )
			{
				do
				{
					String nm = c.getString(0);
					String ps = c.getString(1);
					String hashed = getMd5Hash(ps);
					Log.i(TAG, SubTag + "Oldpass: " + ps + ", Newpass: " + hashed);
					ContentValues values = new ContentValues();
					values.put("passwd", hashed);
					String sql = "UPDATE " + USER_TABLE_NAME + " set passwd = '" + hashed + "' WHERE name = '" + nm + "';";
					Log.i(TAG, SubTag + "exec sql: " + sql);
					try
					{
						db.execSQL(sql);
					}
					catch (Exception e)
					{
						Log.e(TAG, SubTag + e.getMessage());
					}
				} while ( c.moveToNext());
			}
			
			
			Log.i(TAG , SubTag + "Upgrade is complete!");
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
	      Log.i(TAG, SubTag + "Deleting SQL: " + where);
	      int count;
			switch (sURIMatcher.match(uri)) 
			{
	      	case USER:
	      		count = db.delete(USER_TABLE_NAME, where, whereArgs);
	      		break;
	      	case PNLDATA:
	      	case PNL1RECORD:
	      		count = db.delete(PNL_TABLE_NAME, where, whereArgs);
	      		break;
	      	case PNLSTATUS:
	      		count = db.delete(PNL_STATUS_TABLE_NAME, where, whereArgs);
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
	      	case PNLSTATUS:
	      		return "vnd.gamePnLTracker.cursor.dir/pnlstatus";
	      	case PNL1RECORD:
	      		return "vnd.gamePnLTracker.cursor.item/pnlrecord";
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
			case PNL1RECORD:
            	db.insert(PNL_TABLE_NAME ,null,values);
            	db.close();
				break;
			case PNLSTATUS:
				db.insert(PNL_STATUS_TABLE_NAME ,null,values);
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
				Log.i(TAG, SubTag + "building query for USER table");
				sqlStm += "name FROM ";
				sqlStm += USER_TABLE_NAME;
				sqlStm += " WHERE ";
				sqlStm += selection;
				Log.i(TAG, SubTag + "sql: " + sqlStm);
				break;
			case PNLDATA:
				Log.i(TAG, SubTag + "building query for DATA table");
				sqlStm += "_ID,name,amount,date,eventType,gameType,gameLimit,notes FROM ";
				sqlStm += PNL_TABLE_NAME;
				sqlStm += " WHERE ";
				sqlStm += selection;
				Log.i(TAG, SubTag + "sql: " + sqlStm);
				break;
			case PNLSTATUS:
				Log.i(TAG, SubTag + "building query for STATUS table");
				sqlStm += "name,status FROM ";
				sqlStm += PNL_STATUS_TABLE_NAME;
				sqlStm += " WHERE ";
				sqlStm += selection;
				Log.i(TAG, SubTag + "sql: " + sqlStm);
				break;
			default:
				Log.i(TAG, SubTag + "Unknown request");
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
			String[] selectionArgs) 
	{
		int count;
		Log.i(TAG, SubTag + "Started....");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		getContext().getContentResolver().notifyChange(uri, null);	  	
		
		switch (sURIMatcher.match(uri)) 
		{
	    case USER:
            count = db.update(
            			USER_TABLE_NAME, 
            			values,
            			selection, 
            			selectionArgs);
	    	break;
	    case PNLDATA:
	    	Log.i(TAG, SubTag + "Updating " + PNL_TABLE_NAME);
	    	
            count = db.update(
            			PNL_TABLE_NAME, 
            			values,
            			selection, 
            			selectionArgs);
	      		break;
	      	case PNLSTATUS:
	            count = db.update(
	            		PNL_STATUS_TABLE_NAME, 
            			values,
            			selection, 
            			selectionArgs);
	      		break;
	      	default:
	      		throw new IllegalArgumentException("Unknown URI " + uri);
	      }
	      getContext().getContentResolver().notifyChange(uri, null);
	      Log.i(TAG, SubTag + "Ended....");
	      return count;
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
		
		PNL_STAT_PROJECTION_MAP = new HashMap<String,String>();
		PNL_STAT_PROJECTION_MAP.put(PNL_STATUS_TABLE_NAME, "name");
		PNL_STAT_PROJECTION_MAP.put(PNL_STATUS_TABLE_NAME, "status");
		
	}
}
