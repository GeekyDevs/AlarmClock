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
	
	/*
	 * Retrieves all the setting information associated with the existing alarm opened 
	 * for editing.
	 */
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
		
		return mDb.rawQuery("SELECT * FROM " + TABLE_NAME, null);
	}
	
	/*
	 * Fetch the alarm settings with that id.
	 */
	public Cursor fetchAlarmById (int id) {
	
		return mDb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id=" + id, null);
	}
	
	/*
	 * Retrieve all alarms that have been enabled (i.e. check box been checked)
	 */
	public Cursor fetchEnabledAlarms() {
		return mDb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE alarm_enabled=1", null);
	}

	/*
	 * Retrieve alarm with id that have been enabled (i.e. check box been checked)
	 */
	public boolean fetchEnabledById(int id) {

		Cursor c = mDb.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id=" + id, null);
		c.moveToFirst();
		return (c.getInt(16) > 0);
	}
	
	/*
	 * Counts the number of rows in the table. Used to assign a new alarm the id that haven't
	 * been assigned yet. 
	 */
	public int getNewId() {
		
		Cursor c = fetchAllAlarms();
		if (c.moveToLast()) {
			return c.getInt(0) + 1;
		} else {
			return 0;
		}
	}
	
	public void setAlarmToDB(int id, boolean enabled) {
		
		ContentValues values = new ContentValues();
		values.put("alarm_enabled", enabled);
		mDb.update(TABLE_NAME, values, "_id=" + id, null);
	}
	
	/*
	 * Takes in the cursor of information and updates the database for the alarm 
	 * with that id.
	 */
	public boolean saveAlarm(ContentValues values) {
		
		 return mDb.update(TABLE_NAME, values, "_id=" + values.get("_id"), null) > 0;
	}
	
	/*
	 * Delete the alarm with that id from the database.
	 */
	public void deleteAlarm(long ID) {
		mDb.delete(TABLE_NAME, "_id=" + ID, null);
	}
}
