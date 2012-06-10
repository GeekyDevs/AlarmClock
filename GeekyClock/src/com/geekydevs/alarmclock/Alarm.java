package com.geekydevs.alarmclock;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Alarm {

	private static final String[] allKeys = new String [] {"_id", "hour", "minute", "name", "repeat_sun", "repeat_mon", "repeat_tue", "repeat_wed",
		   "repeat_thu", "repeat_fri", "repeat_sat", "failsafe_on",
		   "wakeup_on", "vibrate_on", "sound", "snooze_value", "alarm_enabled", "challenge_level"};
	
	public static final int ALARM_SUNDAY = 4;
	public static final int ALARM_MONDAY = 5;
	public static final int ALARM_TUESDAY = 6;
	public static final int ALARM_WEDNESDAY = 7;
	public static final int ALARM_THURSDAY = 8;
	public static final int ALARM_FRIDAY = 9;
	public static final int ALARM_SATURDAY = 10;
	
	private static final int MILLIS_IN_DAY = 86400000;
	private static final int MILLIS_IN_HOUR = 3600000;
	private static final int MILLIS_IN_MINUTE = 60000;
	
	public static final String PACKAGE_PREFIX = "com.geekydevs.alarmclock";
	

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
		alarmProperty.put("sound", "Default");
		alarmProperty.put("snooze_value", 5); // 15
		alarmProperty.put("alarm_enabled", true);
		alarmProperty.put("challenge_level", "Medium");
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
			case 17:
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
		} else if (hour == 12) {
			timeFormat += 12 + ":";
			ap = " pm";
		} else if (hour == 0) {
			timeFormat += 12 + ":";
			ap = " am";
		} else if (hour < 12) {
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
	
	/**
     * Convert a millisecond duration to a string format
     * 
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
	public static String getDurationBreakdown(long millis) {
		
		if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = (long)(millis / MILLIS_IN_DAY);
        millis = millis % MILLIS_IN_DAY;
        long hours = (long)(millis / MILLIS_IN_HOUR);
        millis = millis % MILLIS_IN_HOUR;
        long minutes = (long) Math.ceil(millis / (MILLIS_IN_MINUTE + 0.0));

        StringBuilder sb = new StringBuilder(64);
        
        if (days == 0 && hours == 0 && minutes == 1) {
	        if (millis < MILLIS_IN_MINUTE) {
	        	sb.append("less than 1 minute");
	        }
        } else {
        	
        	if (minutes == 60) {
        		hours += 1;
        		minutes = 0;
        	}
        	if (hours == 24) {
        		days += 1;
        		hours = 0;
        	}
        	
	       if ((days > 0)) {
	    	   if (days == 7) {
	    		   sb.append("1 week");
	    	   } else if (days > 0) {
	    		   sb.append(days + " ");
	    		   sb.append((days > 1) ? "days" : "day");
	    	   }
	       }
	       
	       if (hours > 0) {
	    	   sb.append((days > 0) ? ", " : "");
	    	   sb.append(hours + " ");
	    	   sb.append((hours > 1) ? "hours" : "hour");
	       }
	       
	       if (minutes > 0) {
	    	   sb.append((days > 0 || hours > 0) ? " and " : "");
	    	   sb.append(minutes);
	    	   sb.append((minutes > 1) ? " minutes" : " minute");
	       }
        }
        return(sb.toString());

	}
}

