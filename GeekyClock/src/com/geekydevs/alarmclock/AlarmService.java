package com.geekydevs.alarmclock;

import java.lang.Math;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class AlarmService extends Service {

	public static final String ACTION_SET_ALARM = "set_alarm";
	public static final String ACTION_STOP_ALARM = "stop_alarm";
	public static final String ACTION_SHOW_NOTIF = "show_notif";
	public static final String ACTION_LAUNCH_SNOOZE = "launch_snooze";
	
	public static final String EXTRA_DONT_DISABLE = "don't disable";

	private static final int NOTIFY_ALARM_SET = R.layout.alarm_edit;
	private static final int WEEK_INTERVAL = 604800000;
	
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
		
		if (intent != null) {
		
			String action = intent.getAction();
	
			if (action.equals(ACTION_SET_ALARM)) {
	
				Cursor enabledCursors = dbAdapter.fetchEnabledAlarms();
				
				if (enabledCursors.moveToFirst()) {
					
					Calendar toSchedule = nextAvailableSchedule(enabledCursors);
					
					if (toSchedule != null) {
						
						int alarmPos = alarmPosition(enabledCursors)[1];
						
						Cursor c = dbAdapter.fetchAlarmById(alarmPos);
						c.moveToFirst();
	
						Log.d("Checking", "Scheduling Alarm " + alarmPos);

						setAlarm(toSchedule, c);
	
						performAction(new Intent().setAction(ACTION_SHOW_NOTIF));
					}
				}

			} else if (action.equals(ACTION_STOP_ALARM)) {
				
				Intent stopIntent = new Intent(this, AlarmReceiver.class);
				
				int id = intent.getExtras().getInt(Alarm.PACKAGE_PREFIX + ".id");
				
				PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(),
												id, stopIntent, 0);
	
				AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
				alarmManager.cancel(pendingIntent);	
				
				Cursor enabledCursors = dbAdapter.fetchEnabledAlarms();
				
				if (enabledCursors.getCount() > 0) {
					performAction(new Intent().setAction(ACTION_SET_ALARM));
				}
				
			} else if (action.equals(ACTION_SHOW_NOTIF)) {
				
				Calendar schedule = Calendar.getInstance();
				String dateFormat = "";
				String timeFormat = "";
				
				Cursor c = dbAdapter.fetchEnabledAlarms();
	
				if ((c.getCount() > 0)) {
					c.moveToFirst();
					schedule = nextAvailableSchedule(c);
					if (schedule != null) {
						timeFormat = Alarm.formatTime(schedule.get(Calendar.HOUR_OF_DAY), schedule.get(Calendar.MINUTE));
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
			i.putExtra(Alarm.PACKAGE_PREFIX + ".failsafe_on", cursor.getInt(11));
			i.putExtra(Alarm.PACKAGE_PREFIX + ".snooze_count", cursor.getInt(15));
			flag = PendingIntent.FLAG_UPDATE_CURRENT;
		
		if (cursor.getInt(12) > 0)
			i.putExtra(Alarm.PACKAGE_PREFIX + ".challenge_on", cursor.getInt(12));
			i.putExtra(Alarm.PACKAGE_PREFIX + ".challenge_level", cursor.getString(17));
		
		if (cursor.getInt(13) > 0) {
			i.putExtra(Alarm.PACKAGE_PREFIX + ".vibrate", 1);
		} else {
			i.putExtra(Alarm.PACKAGE_PREFIX + ".vibrate", 0);
		}
		
		i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", cursor.getString(14));
		i.putExtra(Alarm.PACKAGE_PREFIX + ".id", cursor.getInt(0));
		i.putExtra(Alarm.PACKAGE_PREFIX + ".has_repeat", haveRepeat(cursor));
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				this.getApplicationContext(), cursor.getInt(0), i, flag);
		
		Calendar calendar = Calendar.getInstance();
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		long millisDif = (c.getTimeInMillis() - calendar.getTimeInMillis());
		long secondsDif = (c.getTimeInMillis() - calendar.getTimeInMillis()) / 1000;

		Toast toast = Toast.makeText(this, "Alarm set for " + Alarm.getDurationBreakdown(millisDif) + " from now.", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);  
		toast.show();
		
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, (int) secondsDif);
		
		// Testing purposes
		//calendar.add(Calendar.SECOND, 5);

		if (haveRepeat(cursor)){
			am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), WEEK_INTERVAL, pendingIntent);
		} else {
			am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);	
		}
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

		Boolean repeatFlag = haveRepeat(c);

		// Staring index
		int daysInWeek = 7;
		int i = Alarm.ALARM_SUNDAY;
		int dif = Alarm.ALARM_SATURDAY - daysInWeek; //3
		
		int temp = 0;
		
		if (repeatFlag) {
			// Iterate each day, starting with Sunday
			while (i <= Alarm.ALARM_SATURDAY) {
				
				if (c.getInt(i) > 0) { // if the day has a repeat
					if (i-dif < day) {
						// Keep only the first day before current day
						//if (i-dif < day) { 
						if (temp == 0) {
							temp = i-dif; 
						}
						//} 
					} else if (i-dif == day) {
						// Keep day same as current day if time scheduled hasn't pass yet
						if ((fHour > hour) || ((fHour == hour) && (fMinute > minute))) {
							fDay = i-dif;
							break;
						} else {
							if (temp == 0) {
								temp = i-dif;
							}
						}
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
		
		if (repeatFlag) {
			if (fDay == fCalendar.get(Calendar.DAY_OF_WEEK)) {
				fDay = fCalendar.get(Calendar.DAY_OF_MONTH);
			} else if (fDay == 0){
				fDay = fCalendar.get(Calendar.DAY_OF_MONTH) + (daysInWeek - fCalendar.get(Calendar.DAY_OF_WEEK) + temp);
			} else {
				fDay = fCalendar.get(Calendar.DAY_OF_MONTH) + Math.abs(fCalendar.get(Calendar.DAY_OF_WEEK) - fDay);
			}
		} else {
			if (fDay == fCalendar.get(Calendar.DAY_OF_WEEK)) {
				fDay = fCalendar.get(Calendar.DAY_OF_MONTH);
			} else {
				if (fDay > 1) {
					fDay = fCalendar.get(Calendar.DAY_OF_MONTH) + Math.abs(fCalendar.get(Calendar.DAY_OF_WEEK) - fDay);
				} else {
					fDay = fCalendar.get(Calendar.DAY_OF_MONTH) + 1;
				}
			}
		}
	
		fCalendar.set(fYear, fMonth, fDay, fHour, fMinute, fSecond);
		
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
	
	private boolean haveRepeat(Cursor c) {
		
		// Check if there are any repeat days.
		for (int j = Alarm.ALARM_SUNDAY; j <= Alarm.ALARM_SATURDAY; j++) {
			if (c.getInt(j) > 0) {
				return true;
			}
		}
		
		return false;
	}
	
	/*
	 * Returns the position of the alarm in the list to be scheduled next.
	 */
	private int[] alarmPosition (Cursor c) {

		Calendar bestDate = Calendar.getInstance();
		Calendar temp = Calendar.getInstance();

		int secondBestPosition = -1;
		int bestPosition = -1;
		
		int pos = -1;
		
		if (c.moveToFirst()) {
			bestDate = pickNextAlarmTime(c);
			bestPosition = c.getInt(0);
			/*
			if (!haveRepeat(c)) {
				secondBestPosition = bestPosition;
			}
			*/
			pos = 0;
		}
		
		while(c.moveToNext()) {
			pos += 1;
			temp = pickNextAlarmTime(c);
			if (temp.compareTo(bestDate) == -1) {
				bestDate = temp;
				//secondBestPosition = bestPosition;
				bestPosition = c.getInt(0);	
			}
		}
		
		int[] positions = new int [] {secondBestPosition, bestPosition}; 
		
		return positions;
	}
	
	/*
	 * Set the notification in the widget bar
	 */
	private void setNotification(String timeString) {

		String message = "Next alarm: " + timeString; 
		
		Intent intent = new Intent(this, AlarmClock.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		Notification notif = new Notification(R.drawable.alarm_icon, message, System.currentTimeMillis());
		notif.setLatestEventInfo(this, "Perk Up Alarm Scheduled", timeString, contentIntent);
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
