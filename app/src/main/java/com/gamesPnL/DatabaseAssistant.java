
package com.gamesPnL;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.*;

public class DatabaseAssistant {
    private static final String EXPORT_FILE_NAME = "/sdcard/gamePnL.xml";
    public static String TAG = "gamePnLTracker";
    public static String SubTag = "DatabaseAssistant: ";

    private Context _ctx;
    private SQLiteDatabase _db;
    private Exporter _exporter;

    public DatabaseAssistant(Context ctx, SQLiteDatabase db) {
        _ctx = ctx;
        _db = db;

        try {
            // create a file on the sdcard to export the
            // database contents to
            File myFile = new File(EXPORT_FILE_NAME);
            myFile.createNewFile();

            FileOutputStream fOut = new FileOutputStream(myFile);
            BufferedOutputStream bos = new BufferedOutputStream(fOut);

            _exporter = new Exporter(bos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportData() {
        gamesLogger.i(TAG, SubTag + "Exporting Data");

        try {
            _exporter.startDbExport(_db.getPath());

            // get the tables out of the given sqlite database
            String sql = "SELECT * FROM sqlite_master";

            Cursor cur = _db.rawQuery(sql, new String[0]);
            gamesLogger.i(TAG, SubTag + "show tables, cur size " + cur.getCount());
            cur.moveToFirst();

            String tableName;
            while (cur.getPosition() < cur.getCount()) {
                tableName = cur.getString(cur.getColumnIndex("name"));
                gamesLogger.i(TAG, SubTag + "table name " + tableName);

                // get the first 6 characters. If the string is "sqlite", then ignore it
                String s6chars = tableName.substring(0, 6);
                gamesLogger.i(TAG, SubTag + "first 6 chars of the name: " + s6chars);
                // don't process these two tables since they are used
                // for metadata
                if (!tableName.equals("android_metadata") &&
                        !s6chars.equals("sqlite")) {
                    exportTable(tableName);
                }

                cur.moveToNext();
            }
            _exporter.endDbExport();
            _exporter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportTable(String tableName) throws IOException {
        _exporter.startTable(tableName);

        // get everything from the table
        String sql = "select * from " + tableName;
        Cursor cur = _db.rawQuery(sql, new String[0]);
        int numcols = cur.getColumnCount();

        gamesLogger.i(TAG, SubTag + "Start exporting table " + tableName);

//		// logging
//		for( int idx = 0; idx < numcols; idx++ )
//		{
//			gamesLogger.i(TAG, SubTag + "column " + cur.getColumnName(idx) );
//		}

        cur.moveToFirst();

        // move through the table, creating rows
        // and adding each column with name and value
        // to the row
        while (cur.getPosition() < cur.getCount()) {
            _exporter.startRow();
            String name;
            String val;
            for (int idx = 0; idx < numcols; idx++) {
                name = cur.getColumnName(idx);
                val = cur.getString(idx);
                gamesLogger.i(TAG, SubTag + "col '" + name + "' -- val '" + val + "'");

                _exporter.addColumn(name, val);
            }

            _exporter.endRow();
            cur.moveToNext();
        }

        cur.close();

        _exporter.endTable();
    }

    class Exporter {
        private static final String CLOSING_WITH_TICK = "'>";
        private static final String START_DB = "<export-database name='";
        private static final String END_DB = "</export-database>";
        private static final String START_TABLE = "<table name='";
        private static final String END_TABLE = "</table>";
        private static final String START_ROW = "<row>";
        private static final String END_ROW = "</row>";
        private static final String START_COL = "<col name='";
        private static final String END_COL = "</col>";

        private BufferedOutputStream _bos;

        public Exporter() throws FileNotFoundException {
            this(new BufferedOutputStream(
                    _ctx.openFileOutput(EXPORT_FILE_NAME,
                            Context.MODE_WORLD_READABLE)));
        }

        public Exporter(BufferedOutputStream bos) {
            _bos = bos;
        }

        public void close() throws IOException {
            if (_bos != null) {
                _bos.close();
            }
        }

        public void startDbExport(String dbName) throws IOException {
            String stg = START_DB + dbName + CLOSING_WITH_TICK;
            _bos.write(stg.getBytes());
        }

        public void endDbExport() throws IOException {
            _bos.write(END_DB.getBytes());
        }

        public void startTable(String tableName) throws IOException {
            String stg = START_TABLE + tableName + CLOSING_WITH_TICK;
            _bos.write(stg.getBytes());
        }

        public void endTable() throws IOException {
            _bos.write(END_TABLE.getBytes());
        }

        public void startRow() throws IOException {
            _bos.write(START_ROW.getBytes());
        }

        public void endRow() throws IOException {
            _bos.write(END_ROW.getBytes());
        }

        public void addColumn(String name, String val) throws IOException {
            String stg = START_COL + name + CLOSING_WITH_TICK + val + END_COL;
            _bos.write(stg.getBytes());
        }
    }

    class Importer {

    }
}
