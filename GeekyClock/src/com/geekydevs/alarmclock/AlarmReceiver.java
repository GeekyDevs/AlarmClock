package com.geekydevs.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context ctx, Intent arg1) {
		
		boolean timeChanged = false;
		
		if ((arg1.getAction() != null) && (arg1.getAction().equals(Intent.ACTION_TIME_CHANGED))) {
	      Log.d("AlarmReceiver: ", " ACTION_TIME_CHANGED received");
	      timeChanged = true;
	    }

		if (!timeChanged) {
			if (arg1 != null) {
				AlarmDBAdapter dbAdapter = new AlarmDBAdapter(ctx);
				dbAdapter.open();
				
				boolean challengeOn = arg1.hasExtra(Alarm.PACKAGE_PREFIX + ".challenge_on");
				boolean failSafeOn = arg1.hasExtra(Alarm.PACKAGE_PREFIX + ".failsafe_on");
				boolean hasSnooze = false;
				
				Intent i = new Intent(ctx, Snooze.class);
				
				if (challengeOn) {
					i = new Intent(ctx, Challenge.class);
					i.putExtra(Alarm.PACKAGE_PREFIX + ".challenge_level", arg1.getExtras().getString(Alarm.PACKAGE_PREFIX + ".challenge_level"));
					if (failSafeOn) {
						hasSnooze = true;
						int snoozeCount = arg1.getExtras().getInt(Alarm.PACKAGE_PREFIX + ".snooze_count");
						if (snoozeCount > 0) {
							i.putExtra(Alarm.PACKAGE_PREFIX + ".snooze_count", snoozeCount);
						} else {
							hasSnooze = false;
							i = new Intent(ctx, FailSafe.class);
						}
					} else {
						hasSnooze = false;
					}
				} 
				else if (failSafeOn) 
				{
					hasSnooze = true;
					int snoozeCount = arg1.getExtras().getInt(Alarm.PACKAGE_PREFIX + ".snooze_count");
					if (snoozeCount > 0) {
						i = new Intent(ctx, Snooze.class);
						i.putExtra(Alarm.PACKAGE_PREFIX + ".snooze_count", snoozeCount); 
					} else {
						hasSnooze = false;
						i = new Intent(ctx, FailSafe.class);
					}
				}
	
				if (arg1.hasExtra(Alarm.PACKAGE_PREFIX + ".sound")) {
					i.putExtra(Alarm.PACKAGE_PREFIX + ".vibrate", arg1.getExtras().getInt(Alarm.PACKAGE_PREFIX + ".vibrate"));
					i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", arg1.getExtras().getString(Alarm.PACKAGE_PREFIX + ".sound"));
					i.putExtra(Alarm.PACKAGE_PREFIX + ".has_repeat", arg1.getExtras().getBoolean(Alarm.PACKAGE_PREFIX + ".has_repeat"));
				}
				
				if ((!hasSnooze) && (arg1.hasExtra(Alarm.PACKAGE_PREFIX + ".has_repeat"))) {
					if (!arg1.getExtras().getBoolean(Alarm.PACKAGE_PREFIX + ".has_repeat")) {
						dbAdapter.setAlarmToDB(arg1.getExtras().getInt(Alarm.PACKAGE_PREFIX + ".id"), false);
					}
				}
				
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ctx.startActivity(i);
				dbAdapter.close();
			}
		} else {
			Intent i = new Intent(ctx, AlarmService.class);
			i.setAction(AlarmService.ACTION_SET_ALARM);
			ctx.startService(i);
		}
	}
}
