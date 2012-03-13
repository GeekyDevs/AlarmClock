package com.geekydevs.alarmclock;

import java.lang.reflect.Method;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import java.lang.Math;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;


public class AlarmService extends Service {

	public static final String ACTION_SET_ALARM = "set_alarm";
	public static final String ACTION_LAUNCH_ALARM = "launch_alarm";
	public static final String ACTION_STOP_ALARM = "stop_alarm";
	
	public static final String EXTRA_DONT_DISABLE = "don't disable";
	
	private static final int NOTIFY_MAIN = R.layout.main;
	private static final int NOTIFY_ALARM_SET = R.layout.alarm_edit;
	
	private AlarmDBAdapter dbAdapter;
	private Alarm alarm;
	
	private NotificationManager notificationManager;
	
	@Override
	public IBinder onBind(Intent arg) {
		return null;
	}
	
	@Override
	public void onCreate() {
		
		super.onCreate();

		dbAdapter = new AlarmDBAdapter(this);
		dbAdapter.open();
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
		performAction(intent, intent.getExtras());
	}
	
	/*
	 * Reads in the intent and the id of the alarm selected and perform the 
	 * required action (E.g. set alarm, set up snooze, ...) 
	 */
	private void performAction(Intent intent, Bundle b) {
		
		String action = intent.getAction();

		if (action.equals(ACTION_SET_ALARM)) {

			Cursor c = dbAdapter.fetchAlarmById((Integer) b.get("_id"));
			
			if (c.moveToFirst()) {
				setAlarm(pickNextAlarmTime(c));
			}
		} else if (action.equals(ACTION_STOP_ALARM)) {
			
			Intent stopIntent = new Intent(this, AlarmReceiver.class);
			
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(),
											0, stopIntent, 0);

			AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

			alarmManager.cancel(pendingIntent);

		}
	}
	
	private void setAlarm(Calendar c) {
		
		Intent i = new Intent(this, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				this.getApplicationContext(), 0, i, 0);
		
		Calendar calendar = Calendar.getInstance();

		long secondsDif = (c.getTimeInMillis() - calendar.getTimeInMillis()) / 1000;
        
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, (int) secondsDif);
		
		// Testing purposes
		//calendar.add(Calendar.SECOND, 10);
		
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
		
		//Toast.makeText(this, "Alarm will be scheduled", Toast.LENGTH_LONG).show();
		
	}
	
	private Calendar pickNextAlarmTime(Cursor c) {
		
		Calendar fCalendar = Calendar.getInstance();
		Calendar cCalendar = Calendar.getInstance();
		
		//String [] daysOfWeek = new String [] {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
		
		// Future date and time
		int fHour = c.getInt(1);
		int fMinute = c.getInt(2);
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
		
		fCalendar.set(fYear, fMonth, fDay, fHour, fMinute);
		Toast.makeText(this, "Scheduled for " + fCalendar.getTime().toString(), Toast.LENGTH_LONG).show();
		
		return fCalendar;
	}
	
	/*
	private void actionLaunchAlarm(boolean dontDisable) {
		
		turnOnForeground(getNotification(R.string.launch_msg,R.string.app_launched));
		//alarm = dbAdapter.getAlarmById(getNextAlarm());
		
		try {
			Thread.sleep(200);
			stopOrSet();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private Notification getNotification(int message) {
		return getNotification(message, message);
	}
	
	private Notification getNotification(int message, int ticker) {
		
		Notification notif = new Notification(R.drawable.stat_notify_alarm, getString(ticker), System.currentTimeMillis());
		return notif;
	}
	
	private void turnOnForeground (Notification notif) {
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		fullWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "GeekyAlarmTag");
		fullWakeLock.acquire();
		
		try {
			Method m = Service.class.getMethod("startForeground", new Class[] {int.class, Notification.class});
			m.invoke(this, NOTIFY_MAIN, notif);
		} catch (Exception e) {
			notificationManager.notify(NOTIFY_MAIN, notif);
		}
	}
	
	private void stopOrSet() {
		if (safeToStop()) {
			stopSelf();
		} else {
			setNextAlarm();
		}
	}
	
	private boolean safeToStop() {
		return !(playingBackup || isCounting);
	}
	
	private void setNextAlarm() {
		
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), 0);
		AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
		manager.cancel(sender);
		
		Cursor c = dbAdapter.fetchEnabledAlarms();
		if (c != null) {
			c.moveToFirst();
			if (!c.isAfterLast()) {
				//long nextAlarmTimeInMilis = 0;
				long nextAlarmId = 0;
				
				Alarm a = null;
				while (!c.isAfterLast()) {
					a = new Alarm(c);
					nextAlarmId = (Long) a.getAll().get("_id");
					c.moveToNext();
				}
			} else {
				notificationManager.cancel(NOTIFY_ALARM_SET);
			}
			c.close();
		} else {
			notificationManager.cancel(NOTIFY_ALARM_SET);
		}
	}
	*/
}
