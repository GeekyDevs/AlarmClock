package com.geekydevs.alarmclock;

import java.lang.Math;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class AlarmService extends Service {

	public static final String ACTION_SET_ALARM = "set_alarm";
	public static final String ACTION_STOP_ALARM = "stop_alarm";
	public static final String ACTION_SET_NEXT_ALARM = "set_next_alarm";
	public static final String ACTION_SHOW_NOTIF = "show_notif";
	public static final String ACTION_CANCEL_NOTIF = "cancel_notif";
	public static final String ACTION_LAUNCH_SNOOZE = "launch_snooze";
	
	public static final String EXTRA_DONT_DISABLE = "don't disable";
	
	private static final int NOTIFY_MAIN = R.layout.main;
	private static final int NOTIFY_ALARM_SET = R.layout.alarm_edit;
	
	private NotificationManager notifManager;
	private AlarmDBAdapter dbAdapter;
	private Alarm alarm;
	
	@Override
	public IBinder onBind(Intent arg) {
		return null;
	}
	
	@Override
	public void onCreate() {
		
		super.onCreate();

		dbAdapter = new AlarmDBAdapter(this);
		dbAdapter.open();
		
		notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}
	
	@Override
	public void onDestroy() {
		
		//setNextAlarm();
		dbAdapter.close();
		super.onDestroy();	
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		
		super.onStart(intent, startId);
		performAction(intent);
	}
	
	/*
	 * Reads in the intent and the id of the alarm selected and perform the 
	 * required action (E.g. set alarm, set up snooze, ...) 
	 */
	private void performAction(Intent intent) {
		
		String action = intent.getAction();

		if (action.equals(ACTION_SET_ALARM)) {

			Cursor enabledCursors = dbAdapter.fetchEnabledAlarms();
			Log.d(null, enabledCursors.getCount() + " rows");
			
			if (enabledCursors.moveToFirst()) {
				
				Calendar toSchedule = nextAvailableSchedule(enabledCursors);
				int alarmPos = 0;
				
				if (toSchedule != null) {
					alarmPos = alarmPosition(enabledCursors);
					
					Cursor c = dbAdapter.fetchAlarmById(alarmPos);
					c.moveToFirst();
					setAlarm(toSchedule, c);
				}
			}

		} else if (action.equals(ACTION_STOP_ALARM)) {
			
			Intent stopIntent = new Intent(this, AlarmReceiver.class);
			
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(),
											0, stopIntent, 0);

			AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
			alarmManager.cancel(pendingIntent);	
			
			// Commented out for testing purposes!!!!!!!!!
			performAction(new Intent().setAction(ACTION_SET_ALARM));
			
		} else if (action.equals(ACTION_SHOW_NOTIF)) {
			
			Calendar schedule = Calendar.getInstance();
			String dateFormat = "";
			String timeFormat = "";
			
			Cursor c = dbAdapter.fetchEnabledAlarms();

			if (c.getCount() > 0) {
				c.moveToFirst();
				schedule = nextAvailableSchedule(c);
				if (schedule != null) {
					timeFormat = Alarm.formatTime(schedule.get(schedule.HOUR_OF_DAY), schedule.get(schedule.MINUTE));
					dateFormat = schedule.getTime().toString().substring(0, 11) + timeFormat;
					setNotification(dateFormat);
				} else {
					setNotification("None");
				}
			} else {
				cancelNotification();
			}
			
		}
	}
	
	/*
	 * Schedule the alarm.
	 */
	private void setAlarm(Calendar c, Cursor cursor) {
		
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent(this, AlarmReceiver.class);
		int flag = 0;
		
		// Check if failSafe mode has been enabled. If yes, pass the snooze limit to broadcast receiver.
		if (cursor.getInt(11) > 0)
			i.putExtra("failsafe_on", cursor.getInt(11));
			i.putExtra("snooze_count", cursor.getInt(15));
			flag = PendingIntent.FLAG_UPDATE_CURRENT;
		
		if (cursor.getInt(12) > 0)
			i.putExtra("challenge_on", cursor.getInt(12));
			i.putExtra("challenge_level", cursor.getString(17));
		
		if (cursor.getInt(13) > 0)
			i.putExtra("vibrate", 1);
		else 
			i.putExtra("vibrate", 0);
		
		i.putExtra("sound", cursor.getString(14));

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				this.getApplicationContext(), 0, i, flag);
		
		Calendar calendar = Calendar.getInstance();
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		long secondsDif = (c.getTimeInMillis() - calendar.getTimeInMillis()) / 1000;
        
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, (int) secondsDif);
		
		// Testing purposes
		//calendar.add(Calendar.SECOND, 5);

		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);	
	}

	/*
	 * Finds the next available alarm time
	 */
	private Calendar pickNextAlarmTime(Cursor c) {
		
		Calendar fCalendar = Calendar.getInstance();
		Calendar cCalendar = Calendar.getInstance();

		// Future date and time
		int fHour = c.getInt(1);
		int fMinute = c.getInt(2);
		int fSecond = 0;
		int fDay = 0;
		
		// To-do: calculate if next schedule time will be the next month/year.
		// Currently using current month and year.
		int fMonth = fCalendar.get(Calendar.MONTH);
		int fYear = fCalendar.get(Calendar.YEAR);
		
		// Current day and time
		int day = cCalendar.get(Calendar.DAY_OF_WEEK);
		int hour = cCalendar.get(Calendar.HOUR_OF_DAY);
		int minute = cCalendar.get(Calendar.MINUTE);

		Boolean repeatFlag = false;
		
		// Check if there are any repeat days.
		for (int j = Alarm.ALARM_SUNDAY; j <= Alarm.ALARM_SATURDAY; j++) {
			if (c.getInt(j) > 0) {
				repeatFlag = true;
				break;
			}
		}
		
		// Staring index
		int daysInWeek = 7;
		int i = Alarm.ALARM_SUNDAY;
		int dif = Alarm.ALARM_SATURDAY - daysInWeek;
		
		int temp = 0;
		
		if (repeatFlag) {
			// Iterate each day, starting with Sunday
			while (i <= Alarm.ALARM_SATURDAY) {
				
				if (c.getInt(i) > 0) { // if the day has a repeat
					if (i-dif < day) {
						// Keep only the first day before current day
						if (temp == 0) { temp = i-3; } 
					} else if (i-dif == day) {
						// Keep day same as current day if time scheduled hasn't pass yet
						if ((fHour > hour) || ((fHour == hour) && (fMinute > minute))) {
							fDay = i-dif;
						} else {
							temp = i-dif;
						}
						break;
					// Grab the first day after current day
					} else if (i-dif > day) {
						fDay = i-dif;
						break;
					}
				}
				i++;
			}
		} else {
			if ((fHour > hour) || ((fHour == hour) && (fMinute > minute))) {
				fDay = fCalendar.get(Calendar.DAY_OF_WEEK);
			} else {
				if (day < daysInWeek) {
					fDay = day + 1;
				} else {
					fDay = 1;
				}
			}
		}
		
		if (fDay == 0){
			fDay = fCalendar.get(Calendar.DAY_OF_MONTH) + (daysInWeek - fCalendar.get(Calendar.DAY_OF_WEEK) + temp);
		} else {
			fDay = fCalendar.get(Calendar.DAY_OF_MONTH) + Math.abs(fCalendar.get(Calendar.DAY_OF_WEEK) - fDay);
		}
		
		fCalendar.set(fYear, fMonth, fDay, fHour, fMinute, fSecond);
		//Toast.makeText(this, "Scheduled for " + fCalendar.getTime().toString(), Toast.LENGTH_LONG).show();
		
		return fCalendar;
	}
	
	/*
	 * Look for the earliest alarm schedule to appear in the notification bar.
	 */
	private Calendar nextAvailableSchedule(Cursor c) {
		
		Calendar bestDate = Calendar.getInstance();
		Calendar temp = Calendar.getInstance();

		boolean assigned = false;
		
		if (c.moveToFirst()) {
			bestDate = pickNextAlarmTime(c);
			assigned = true;
		}
		
		while(c.moveToNext()) {
			if (!assigned) {
				bestDate = pickNextAlarmTime(c);
				assigned = true;
			} else {
				temp = pickNextAlarmTime(c);
				if (temp.compareTo(bestDate) == -1) {
					bestDate = temp;
				}
			}
		}
		
		if (assigned) {
			return bestDate;
		} else {
			return null;
		}
	}
	
	private int alarmPosition (Cursor c) {
		
		Calendar bestDate = Calendar.getInstance();
		Calendar temp = Calendar.getInstance();

		int position = 0;
		int bestPosition = -1;
		
		boolean assigned = false;
		
		if (c.moveToFirst()) {
			bestDate = pickNextAlarmTime(c);
			bestPosition = 0;
			assigned = true;
		}
		
		while(c.moveToNext()) {
			position += 1;
			if (!assigned) {
				bestDate = pickNextAlarmTime(c);
				bestPosition = 1;
				assigned = true;
			} else {
				temp = pickNextAlarmTime(c);
				if (temp.compareTo(bestDate) == -1) {
					bestDate = temp;
					bestPosition = position;
				}
			}
			
		}
		
		return bestPosition;
	}
	
	/*
	 * Set the notification in the widget bar
	 */
	private void setNotification(String timeString) {

		String message = "Next alarm: " + timeString; 
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, AlarmClock.class), 0);
		Notification notif = new Notification(R.drawable.alarm_icon, message, System.currentTimeMillis());
		notif.setLatestEventInfo(this, "GeekyAlarm Scheduled", timeString, contentIntent);
		notif.flags = Notification.FLAG_ONGOING_EVENT;
		notifManager.notify(NOTIFY_ALARM_SET, notif);	
	}
	
	/*
	 * Cancel the notification in the widget bar
	 */
	private void cancelNotification() {
		notifManager.cancel(NOTIFY_ALARM_SET);
	}
}
