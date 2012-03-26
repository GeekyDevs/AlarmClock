package com.geekydevs.alarmclock;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Calendar;

public class Alarm {

	private static final String[] allKeys = new String [] {"_id", "hour", "minute", "name", "repeat_sun", "repeat_mon", "repeat_tue", "repeat_wed",
		   "repeat_thu", "repeat_fri", "repeat_sat", "failsafe_on",
		   "wakeup_on", "vibrate_on", "sound", "snooze_value", "alarm_enabled"};
	
	public static final int ALARM_SUNDAY = 4;
	public static final int ALARM_MONDAY = 5;
	public static final int ALARM_TUESDAY = 6;
	public static final int ALARM_WEDNESDAY = 7;
	public static final int ALARM_THURSDAY = 8;
	public static final int ALARM_FRIDAY = 9;
	public static final int ALARM_SATURDAY = 10;
	

	private ContentValues alarmProperty = new ContentValues();
	
	public Alarm (int idNum) {
	
		Calendar c = Calendar.getInstance(); 

		alarmProperty.put("_id", idNum);
		alarmProperty.put("hour", c.get(Calendar.HOUR_OF_DAY));
		alarmProperty.put("minute", c.get(Calendar.MINUTE));
		alarmProperty.put("name", "");	
		alarmProperty.put("repeat_sun", false); //4
		alarmProperty.put("repeat_mon", false); 
		alarmProperty.put("repeat_tue", false);
		alarmProperty.put("repeat_wed", false);
		alarmProperty.put("repeat_thu", false);
		alarmProperty.put("repeat_fri", false);
		alarmProperty.put("repeat_sat", false);
		alarmProperty.put("failsafe_on", false); // 11
		alarmProperty.put("wakeup_on", false);
		alarmProperty.put("vibrate_on", false);
		alarmProperty.put("sound", "default");
		alarmProperty.put("snooze_value", 5); // 15
		alarmProperty.put("alarm_enabled", true);
	}
	
	public Alarm (Cursor cursor) {
		
		for(int i=0; i < cursor.getColumnCount(); i++) {
			
			switch (i) {
			case 0: 
			case 1: 
			case 2: 
			case 15:
				alarmProperty.put(allKeys[i], cursor.getInt(i));
				break;
			case 3: 
			case 14:
				alarmProperty.put(allKeys[i], cursor.getString(i));
				break;
			default:
				alarmProperty.put(allKeys[i], cursor.getInt(i) > 0);
				break;
			}
		}
	}
	
	public void assign(String key, int value){
		alarmProperty.put(key, value);
	}
	
	public void assign(String key, boolean value){
		alarmProperty.put(key, value);
	}
	
	public void assign(String key, String value) {
		alarmProperty.put(key, value);
	}

	public ContentValues getAll() {
		return this.alarmProperty;
	}
	
	public static String createTableStatement(String tableName) {
		
		Alarm a = new Alarm(0);
		
		String opening = "CREATE TABLE " + tableName + " ("; 
		String statement = "";

		for (String key : allKeys) {
		    
			statement += ", " + key;
			if (a.alarmProperty.get(key) instanceof Integer) {
				statement += " " + (key.equals("_id") ? "INTEGER PRIMARY KEY AUTOINCREMENT" : "INT");
			}
			else if (a.alarmProperty.get(key) instanceof String) {
				statement += " TEXT NOT NULL";
			}
			else {
				statement += " BOOLEAN NOT NULL";
			}
		}
		
		statement = statement.substring(2) + ") ";
		
		return opening + statement;
	}
	
	/*
	 * Takes in the hour and minute of the day and return a string 
	 * of a 12 hour format.
	 */
	public static String formatTime(int hour, int minute) {
		
		String timeFormat = "";
		String ap = " pm";
		
		if (hour > 12) {
			timeFormat += (hour-12) + ":" ;
		} else if (hour == 0) {
			timeFormat += 12 + ":";
		} else {
			timeFormat += hour + ":";
			ap = " am";
		}
		
		if (minute < 10) {
			timeFormat += "0" + minute;
		} else {
			timeFormat += minute;
		}

		return timeFormat + ap;
	}
	
	public static String formatRepeat(ContentValues days) {
		
		String repeat = "";

		if ((Boolean) days.get("repeat_sun")) { repeat += "Sunday, ";}
		if ((Boolean) days.get("repeat_mon")) { repeat += "Monday, ";}
		if ((Boolean) days.get("repeat_tue")) { repeat += "Tuesday, ";}
		if ((Boolean) days.get("repeat_wed")) { repeat += "Wednesday, ";}
		if ((Boolean) days.get("repeat_thu")) { repeat += "Thursday, ";}
		if ((Boolean) days.get("repeat_fri")) { repeat += "Friday, ";}
		if ((Boolean) days.get("repeat_sat")) { repeat += "Saturday, ";}
		
		if (repeat == "") {
			repeat = "Never";
		} else {
			repeat = repeat.substring(0, repeat.length()-2);
		}

		return repeat;
	}
}

