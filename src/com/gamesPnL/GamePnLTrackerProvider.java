/**
 * 
 */
package com.gamesPnL;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * @author Boris
 *
 */
public class GamePnLTrackerProvider extends ContentProvider 
{
	public static String TAG="gamePnLTracker";
	public static String SubTag="GamePnLTrackerProvider: ";

	private static final String DATABASE_NAME = "gamepnltracker.db";
	private static final int DATABASE_VERSION = 9;
	
	private static final String USER_TABLE_NAME = "gUsers";
	private static final String PNL_TABLE_NAME = "gPNLData";
	private static final String	PNL_STATUS_TABLE_NAME = "gStatus";
	private static final String PNL_GAMES_TABLE_NAME = "gGames";

	private	static	HashMap<String, String> USER_PROJECTION_MAP;
	private	static	HashMap<String, String> PNL_PROJECTION_MAP;
	private	static	HashMap<String, String> PNL_STAT_PROJECTION_MAP;
	private static	HashMap<String, String> PNL_GAMES_PROJECTION_MAP;
	
	public static final String AUTHORITY = 
		"com.gamesPnL.provider.userContentProvider";

	static String[] gms = { "TexasHold'em", "Omaha", "Stud" };
	static String[] desc = { "Texas Holdem", "Omaha", "Stud" };

	private static final int USER = 1;
	private static final int PNLDATA = 2;
	private static final int PNLSTATUS = 3;
	private static final int PNL1RECORD = 4;
	private static final int PNLGAMES = 5;
	
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
        } catch(NoSuchAlgorithmException e) 
        {
        	gamesLogger.e(TAG, SubTag + e.getMessage());
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
		matcher.addURI(AUTHORITY, "pnlgames", PNLGAMES);
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
			gamesLogger.i(TAG, SubTag + "Creating Users table");
			try 
			{
				Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" +
						USER_TABLE_NAME + "'", null);
				if (c.getCount()==0) 
				{
					db.execSQL("CREATE TABLE " + USER_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
								"name TEXT UNIQUE, " +
								"passwd TEXT, " +
								"firstName TEXT, " +
								"lastName TEXT, " +
								"email TEXT);");				}
			
				gamesLogger.i(TAG, SubTag + "Creating PnLdata table");
				c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"  +
						PNL_TABLE_NAME + "'", null);

				if (c.getCount()==0) 
				{
					db.execSQL("CREATE TABLE " + PNL_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
							"uid TEXT, " +
							"name TEXT, " +
							"amount TEXT, " +
							"evYear TEXT, " +
							"evMonth TEXT, " +
							"evDay TEXT, " +
							"evDate DATE, " +
							"eventType TEXT, " +
							"gameType TEXT, " +
							"gameLimit TEXT, " +
							"notes TEXT" +
 							");");
				}

				gamesLogger.i(TAG, SubTag + "Creating PnL status table");
				c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"  +
						PNL_STATUS_TABLE_NAME + "'", null);
				if (c.getCount()==0) 
				{
					db.execSQL("CREATE TABLE " + PNL_STATUS_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
							"name TEXT UNIQUE, " +
							"status TEXT);");
				}
			
				gamesLogger.i(TAG, SubTag + "Creating PnL games table");
				c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"  +
					PNL_GAMES_TABLE_NAME + "'", null);

				if (c.getCount()==0) 
				{
					String sql = "CREATE TABLE " + PNL_GAMES_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
							"game TEXT UNIQUE, " +
							"description TEXT, " +
							"addedBy TEXT" +
 							");";
					gamesLogger.i(TAG, SubTag + "SQL: " + sql);
					db.execSQL( sql );

					gamesLogger.i(TAG, SubTag + "Populating PnL games table");

					db.execSQL("INSERT INTO " + PNL_GAMES_TABLE_NAME + " (game,description,addedBy) values ('TexasHold''em', 'Texas Hold''em', 'gamePnL');" );
					db.execSQL("INSERT INTO " + PNL_GAMES_TABLE_NAME + " (game,description,addedBy) values ('Omaha', 'Omaha', 'gamePnL');" );
					db.execSQL("INSERT INTO " + PNL_GAMES_TABLE_NAME + " (game,description,addedBy) values ('Stud', 'Stud', 'gamePnL');" );
				}
			}
			catch (Exception e)
			{
				gamesLogger.e(TAG, SubTag + e.getMessage());
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			String sql = null;
			Cursor from;
			gamesLogger.i(TAG , SubTag + "starting an upgrade from " + oldVersion + " to " + newVersion);
			// First rename the table to <table>_OLD

			try
			{
				if ( oldVersion <= 6 )
				{
					// Start with user table
					sql = "ALTER TABLE " + USER_TABLE_NAME + " RENAME TO " + USER_TABLE_NAME + "_OLD;";
					gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
					db.execSQL(sql);

					// Create a user table
					sql = "CREATE TABLE " + 
						USER_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"name TEXT UNIQUE, " +
						"passwd TEXT, " +
						"firstName TEXT, " +
						"lastName TEXT, " +
						"email TEXT);";
					gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
					db.execSQL(sql);
					// copy all the records from the <table>_old to <table>

					sql = "SELECT name,email,passwd FROM " + USER_TABLE_NAME + "_OLD;";
					gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
					from = db.rawQuery(sql,null);
					if ( from.moveToFirst() )
					{
						do
						{
							ContentValues vals = new ContentValues();
							vals.put("name", from.getString(0));
							vals.put("email", from.getString(1));
							vals.put("passwd", from.getString(2));
							gamesLogger.i(TAG, SubTag + "adding record");
							db.insert(USER_TABLE_NAME,null,vals);
						} while ( from.moveToNext() );
						// Delete the <table>_OLD
						sql = "DROP TABLE IF EXISTS " + USER_TABLE_NAME + "_OLD;";
						gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
						db.execSQL(sql);
					}
				}
		
				// Next is data table
				sql = "ALTER TABLE " + PNL_TABLE_NAME + " RENAME TO " + PNL_TABLE_NAME + "_OLD;";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);

				// Create the table
				sql = "CREATE TABLE " + PNL_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"uid TEXT, " +
						"name TEXT, " +
						"amount TEXT, " +
						"evYear TEXT, " +
						"evMonth TEXT, " +
						"evDay TEXT, " +
						"evDate DATE, " +
						"eventType TEXT, " +
						"gameType TEXT, " +
						"gameLimit TEXT, " +
						"notes TEXT" +
					");";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);
				
				// copy all the records from the <table>_old to <table>
				sql = "SELECT uid,name,amount,eventType,gameType,gameLimit,notes,date FROM " + PNL_TABLE_NAME + "_OLD;";
				boolean dateExist = true;
				try
				{
					from = db.rawQuery(sql, null);
					dateExist = true;
				}
				catch (Exception e)
				{
					sql = "SELECT uid,name,amount,eventType,gameType,gameLimit,notes,EvYear,EvMonth,EvDay FROM " + PNL_TABLE_NAME + "_OLD;";
					from = db.rawQuery(sql,null);
					dateExist = false;
				}
				
				if ( from.moveToFirst() )
				{
					do
					{
						ContentValues vals = new ContentValues();

						if ( !from.getString(2).equals("") &&
							 !from.getString(2).equals("-"))
						{
							int		startLoc;
							int		endLoc;
							String	uidS = null;
							String	nameS = null;
							String	amountS = null;
							String	dateS;
							String	yearS = null;
							String	monthS = null;
							String	dayS = null;
							String	eventTypeS = null;
							String	gameTypeS = null;
							String	gameLimitS = null;
							String	notesS = null;
							String	newDateF = null;
							String	tmp;
							 
							switch ( oldVersion )
							{
							case 1:
							case 2:
							case 3:
							case 4:
							case 5:
							case 6:
							case 7:
							case 8:
								uidS = from.getString(0);
								nameS = from.getString(1);
								amountS = from.getString(2);
								eventTypeS = from.getString(3);
								gameTypeS = from.getString(4);
								gameLimitS = from.getString(5);
								notesS = from.getString(6);
								if ( dateExist )
								{
									dateS = from.getString(7);
									// The current format of the field is mm/dd/yyyy
									startLoc = 0;
									endLoc = dateS.indexOf('/');
									monthS = dateS.substring(startLoc, endLoc);
									tmp = dateS.substring(endLoc+1);
									startLoc = 0;
									endLoc = tmp.indexOf('/');
									dayS = tmp.substring(startLoc, endLoc);
									yearS = tmp.substring(endLoc+1);
								}
								else
								{
									yearS = from.getString(7);
									monthS = from.getString(8);
									dayS = from.getString(9);
								}
								dayS = String.format("%02d", Integer.parseInt(dayS));
								monthS = String.format("%02d", Integer.parseInt(monthS));
								yearS = String.format("%04d", Integer.parseInt(yearS));
								newDateF = yearS + "-" + monthS + "-" + dayS;
								
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
								db.insert(PNL_TABLE_NAME,null,vals);

								gamesLogger.i(TAG, SubTag + "Year: " + yearS + " Month: " + monthS + " Day: " + dayS + "(" + newDateF + ")");
								break;
							default:
								break;
							}
						}
					} while ( from.moveToNext() );
				}
				// Delete the <table>_OLD
				sql = "DROP TABLE IF EXISTS " + PNL_TABLE_NAME + "_OLD;";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);
				
				// Fainally, status table
				sql = "ALTER TABLE " + PNL_STATUS_TABLE_NAME + " RENAME TO " + PNL_STATUS_TABLE_NAME + "_OLD;";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);

				// Create the table
				sql = "CREATE TABLE " + PNL_STATUS_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"name TEXT, " +
				"status TEXT " +
					");";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);
				
				// copy all the records from the <table>_old to <table>
				sql = "SELECT name,status FROM " + PNL_STATUS_TABLE_NAME + "_OLD;";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				from = db.rawQuery(sql, null);
				if ( from.moveToFirst() )
				{
					do
					{
						ContentValues vals = new ContentValues();
						vals.put("name", from.getString(0));
						vals.put("status", from.getString(1));
						db.insert(PNL_STATUS_TABLE_NAME,null,vals);
					} while ( from.moveToNext() );
				}
				// Delete the <table>_OLD
				sql = "DROP TABLE IF EXISTS " + PNL_STATUS_TABLE_NAME + "_OLD;";
				gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
				db.execSQL(sql);
				
				if ( oldVersion <= 4 )
				{
					// Create a table that would have list of all games
					sql = "CREATE TABLE " + PNL_GAMES_TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"game TEXT, " +
					"description TEXT, " +
					"addedBy TEXT" +
						");";
					gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
					db.execSQL(sql);
					// Need to populate this table with some of the games
					gamesLogger.i(TAG, SubTag + "Populating PnL games table");
					
					sql = "INSERT INTO " + PNL_GAMES_TABLE_NAME + " (game,description,addedBy) values ('TexasHold''em', 'Texas Hold''em', 'gamePnL');";
					gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
					db.execSQL(sql);
					sql = "INSERT INTO " + PNL_GAMES_TABLE_NAME + " (game,description,addedBy) values ('Omaha', 'Omaha', 'gamePnL');";
					gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
					db.execSQL(sql);
					sql = "INSERT INTO " + PNL_GAMES_TABLE_NAME + " (game,description,addedBy) values ('Stud', 'Stud', 'gamePnL');";
					gamesLogger.i(TAG, SubTag + "exec sql: " + sql);
					db.execSQL(sql);
				}
			}
			catch (Exception e)
			{
				gamesLogger.e(TAG, SubTag + e.getMessage());
			}
			gamesLogger.i(TAG , SubTag + "Upgrade is complete!");
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
	      gamesLogger.i(TAG, SubTag + "Deleting SQL: " + where);
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
	      	case PNLGAMES:
	      		count = db.delete(PNL_GAMES_TABLE_NAME, where, whereArgs);
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
	      	case PNLGAMES:
	      		return "vnd.gamePnLTracker.cursor.item/pnlgames";
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
			case PNLGAMES:
				db.insert(PNL_GAMES_TABLE_NAME, null, values);
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
		gamesLogger.i(TAG, SubTag + "Created dbHelper");	
		
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
    	
		try
		{
			String	sqlStm = "SELECT ";
			// final DbAdapter dba = new DbAdapter(getContext());
			SQLiteDatabase dba = dbHelper.getReadableDatabase();
			gamesLogger.i(TAG, SubTag + "Started Query");
			switch (sURIMatcher.match(uri)) 
			{
				case USER:
					gamesLogger.i(TAG, SubTag + "building query for USER table");
					sqlStm += "name FROM ";
					sqlStm += USER_TABLE_NAME;
					if ( selection != null )
					{
						sqlStm += " WHERE ";
						sqlStm += selection;
					}
					gamesLogger.i(TAG, SubTag + "sql: " + sqlStm);
					break;
				case PNLDATA:
					gamesLogger.i(TAG, SubTag + "building query for DATA table");
					sqlStm += "_id,name,amount,evYear,evMonth,evDay,eventType,gameType,gameLimit,notes FROM ";
					sqlStm += PNL_TABLE_NAME;
					if ( selection != null )
					{
						sqlStm += " WHERE ";
						sqlStm += selection;
					}
					sqlStm += " order by evYear asc, evMonth asc, evDay asc";
					gamesLogger.i(TAG, SubTag + "sql: " + sqlStm);
					break;
				case PNLSTATUS:
					gamesLogger.i(TAG, SubTag + "building query for STATUS table");
					sqlStm += "_id,name,status FROM ";
					sqlStm += PNL_STATUS_TABLE_NAME;
					if ( selection != null )
					{
						sqlStm += " WHERE ";
						sqlStm += selection;
					}
					gamesLogger.i(TAG, SubTag + "sql: " + sqlStm);
					break;
				case PNLGAMES:
					gamesLogger.i(TAG, SubTag + "building query for GAMES table");
					sqlStm += "_id,game,description,addedBy FROM ";
					sqlStm += PNL_GAMES_TABLE_NAME;
					if ( selection != null )
					{
						sqlStm += " WHERE ";
						sqlStm += selection;
					}
					gamesLogger.i(TAG, SubTag + "sql: " + sqlStm);
					break;
				default:
					gamesLogger.i(TAG, SubTag + "Unknown request");
					throw new IllegalArgumentException("Unknown URI " + uri);
			}
			gamesLogger.i(TAG, SubTag + "Trying to execute a query.");
			// result = qb.query(db, projection, selection, selectionArgs, null, null, null);
			result = dba.rawQuery(sqlStm, null);
			gamesLogger.i(TAG, SubTag + "Returning query result");
		}
		catch (Exception e)
		{
			gamesLogger.e(TAG, SubTag + e.getMessage());
			result = null;
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) 
	{
		int count = 0;
		try
		{
			gamesLogger.i(TAG, SubTag + "Started....");
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
		    	gamesLogger.i(TAG, SubTag + "Updating " + PNL_TABLE_NAME);
		    	
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
		      	case PNLGAMES:
		            count = db.update(
		            		PNL_GAMES_TABLE_NAME, 
	            			values,
	            			selection, 
	            			selectionArgs);
		      		break;
		      	default:
		      		throw new IllegalArgumentException("Unknown URI " + uri);
		      }
		      getContext().getContentResolver().notifyChange(uri, null);
		      gamesLogger.i(TAG, SubTag + "Ended....");
		}
		catch (Exception e)
		{
			gamesLogger.e(TAG, SubTag + e.getMessage());
		}
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
		
		PNL_GAMES_PROJECTION_MAP = new HashMap<String,String>();
		PNL_GAMES_PROJECTION_MAP.put(PNL_GAMES_TABLE_NAME, "game");
		PNL_GAMES_PROJECTION_MAP.put(PNL_GAMES_TABLE_NAME, "description");
		PNL_GAMES_PROJECTION_MAP.put(PNL_GAMES_TABLE_NAME, "addBy");
	}
}
