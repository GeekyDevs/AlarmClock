package com.geekydevs.alarmclock;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class AlarmDBAdapter {

	public static final String TABLE_NAME = "alarms";
	public static final String DATABASE_NAME = "dbAlarm";
	public static final int DATABASE_VERSION = 12;
	
	private final Context ctx;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase mDb;
	
	public AlarmDBAdapter (Context ctx) {
		dbHelper = new DatabaseHelper(ctx);
		this.ctx = ctx;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(Alarm.createTableStatement(TABLE_NAME));
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
			
			Log.w("DBAdapter", "Upgrading database from version " + oldV
					+ " to " + newV + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
	
	/*
	 * Open the connection to the database.
	 */
	public AlarmDBAdapter open() throws SQLException {
		mDb = dbHelper.getWritableDatabase();
		return this;
	}
	
	/*
	 * Close the connection to the SQL database
	 */
	public void close() {
		dbHelper.close();
	}
	
	/*
	 * Initialise the alarm with default values. Called when adding new alarm.
	 */
	public boolean initialise(Alarm alarm){

		return mDb.insert(TABLE_NAME, null, alarm.getAll()) >= 0;
	}
	
	public Alarm getAlarmById(int id) {
		
		Alarm alarm;
		
		Cursor c = mDb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id=" + id, null);

		if (c.moveToFirst()) {
			alarm = new Alarm(c);
		} else {
			alarm = new Alarm(c.getCount());
		}

		return alarm;
	}
	
	/*
	 * Retrieve all the data from the database.
	 */
	public Cursor fetchAllAlarms() {
		
		Cursor c = mDb.rawQuery("SELECT * FROM alarms", null);
		//c.moveToFirst();
		return c;
	}
	
	public int countCursors() {
		
		return fetchAllAlarms().getCount();
	}
	
	/*
	 * Takes in the cursor of information and updates the database for the alarm 
	 * with that id.
	 */
	public boolean saveAlarm(ContentValues values) {
		
		 return mDb.update(TABLE_NAME, values, "_id=" + values.get("_id"), null) > 0;
	}
}
