package com.geekydevs.alarmclock;

import java.util.LinkedHashMap;

public class Alarm {

	public static final LinkedHashMap<String, Integer> ALARM_DEFAULTS_INT = new LinkedHashMap<String, Integer>() {
	{
		put("ID", 0);
		put("hour", 12);
		put("minute", 0);
		put("snooze_limit", 5);
	}};
	
	public static final LinkedHashMap<String, String> ALARM_DEFAULTS_STR = new LinkedHashMap<String, String>() {
	{
		put("name", "");	
		put("sound", "default");
	}};
	
	public static final LinkedHashMap<String, Boolean> ALARM_DEFAULTS_BOOL = new LinkedHashMap<String, Boolean>() {
	{
		put("repeat_mon", false);
		put("repeat_tue", false);
		put("repeat_wed", false);
		put("repeat_thu", false);
		put("repeat_fri", false);
		put("repeat_sat", false);
		put("repeat_sun", false);
		put("failsafe_on", false);
		put("wakeup_on", false);
		put("vibrate_on", false);
	}};
		
	public static String createTableStatement(String tableName) {
		
		String opening = "CREATE TABLE " + tableName + " ("; 
		String statement = "";

		for (String key : ALARM_DEFAULTS_INT.keySet()) {
		   
			statement += ", " + key;
			statement += " INTEGER" + (key.equals("ID") ? " PRIMARY KEY AUTOINCREMENT" : "");
		}
	
		for (String key : ALARM_DEFAULTS_BOOL.keySet()) {
		
			statement += ", " + key;
			statement += " BOOLEAN";
		}
		
		for (String key : ALARM_DEFAULTS_STR.keySet()) {
			
			statement += ", " + key;
			statement += " TEXT";
		}
	
		statement = statement.substring(2) + ") ";
		
		return opening + statement;
	}
	
	/*
	 * Updates the alarm property with the given boolean value.
	 */
	public static void assign(String key, Boolean value) {
		ALARM_DEFAULTS_BOOL.put(key, value);
	}
	
	/*
	 * Updates the alarm property with the given Long value.
	 */
	public static void assign(String key, int value) {
		ALARM_DEFAULTS_INT.put(key, value);
	}
	
	/*
	 * Updates the alarm property with the given String value.
	 */
	public static void assign(String key, String value) {
		ALARM_DEFAULTS_STR.put(key, value);
	}
	
	public static String formatTime() {
		
		String timeFormat = "";
		String ap = " pm";
		
		int hour = (Integer) ALARM_DEFAULTS_INT.get("hour");
		int minute = (Integer) ALARM_DEFAULTS_INT.get("minute");
		
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
}

