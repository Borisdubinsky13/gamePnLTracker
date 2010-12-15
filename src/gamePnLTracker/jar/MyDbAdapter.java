/**
 * 
 */
package gamePnLTracker.jar;

/**
 * @author Boris
 *
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class MyDbAdapter
{
	public static String TAG="gamePnLTracker";
	public static String SubTag="MyDbAdapter: ";
	private static final String DATABASE_NAME = "gamepnltracker.db";
	private static final int DATABASE_VERSION = 1;
	private Context context = null;
	private static SQLiteDatabase db;
	private myDbHelper	dbHelper;

	public MyDbAdapter(Context _context) 
	{
		context = _context;
		dbHelper = new myDbHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public MyDbAdapter open() throws SQLException 
	{
		db = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close()
	{
		db.close();
	}
	
	public void insertRecord(String tbl, ContentValues vals)
	{
		db.insert(tbl, null, vals);
	}
	
	public Cursor getRecord(String query)
	{
		return db.rawQuery(query, null);
	}
	
	private static class myDbHelper extends SQLiteOpenHelper 
	{
	
		public myDbHelper(Context context, String name, CursorFactory factory, int version)
		{
			super(context, name, null, version);
		}
	
		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			Log.i(TAG, SubTag + "Creating Users database");
			Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='gUsers'", null);
			try 
			{
				if (c.getCount()==0) 
				{
					db.execSQL("CREATE TABLE gUsers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT, passwd TEXT);");
				}
			}
			finally 
			{
				c.close();
			}
			Log.i(TAG, SubTag + "Creating PnLdata database");
			c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='gPNLData'", null);
			try 
			{
				if (c.getCount()==0) 
				{
					db.execSQL("CREATE TABLE gPNLData (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
							"name TEXT, " +
							"amount TEXT, " +
							"result TEXT, " +
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
}

