package com.geekydevs.alarmclock;

import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmDBAdapter {

	public static final String TABLE_NAME = "alarms";
	public static final String DATABASE_NAME = "dbAlarm";
	public static final int DATABASE_VERSION = 12;
	
	private final Context ctx;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase mDb;
	
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
			
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
	
	public AlarmDBAdapter (Context ctx) {
		this.ctx = ctx;
	}
	
	/*
	 * Open the connection to the database.
	 */
	public AlarmDBAdapter open() {
		dbHelper = new DatabaseHelper(ctx);
		mDb = dbHelper.getWritableDatabase();
		return this;
	}
	
	/*
	 * Close the connection to the SQL database
	 */
	public void close() {
		dbHelper.close();
	}
	
	public int updateSettings(ContentValues values, String whereClause) {
		//mDb.update(TABLE_NAME, values, whereClause, null);
		
		ContentValues v = new ContentValues();
		v.put("hour", 7);
		return mDb.update(TABLE_NAME, v, "ID=0", null);
	}
	
	public Alarm getNewAlarm() {
		return new Alarm();
	}
}
