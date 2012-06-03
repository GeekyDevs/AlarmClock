package com.geekydevs.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context ctx, Intent arg1) {
		
		boolean timeChanged = false;
		boolean phoneRestart = false;
		
		if ((arg1.getAction() != null) && (arg1.getAction().equals(Intent.ACTION_TIME_CHANGED))) {
	      Log.d("AlarmReceiver: ", " ACTION_TIME_CHANGED received");
	      timeChanged = true;
	    }
		
		if ((arg1.getAction() != null) && (arg1.getAction().equals(Intent.ACTION_BOOT_COMPLETED))) {
	      Log.d("AlarmReceiver: ", " BOOT_COMPLETED received");
	      phoneRestart = true;
	    }

		if ((!timeChanged) && !(phoneRestart)) {
			if (arg1.getExtras() != null) {
				AlarmDBAdapter dbAdapter = new AlarmDBAdapter(ctx);
				dbAdapter.open();

				int scheduleId = arg1.getExtras().getInt(Alarm.PACKAGE_PREFIX + ".id");

				boolean challengeOn = arg1.hasExtra(Alarm.PACKAGE_PREFIX + ".challenge_on");
				boolean failSafeOn = arg1.hasExtra(Alarm.PACKAGE_PREFIX + ".failsafe_on");
				
				Intent i = new Intent(ctx, Snooze.class);
				
				if (challengeOn) {
					i = new Intent(ctx, Challenge.class);
					i.putExtra(Alarm.PACKAGE_PREFIX + ".challenge_level", arg1.getExtras().getString(Alarm.PACKAGE_PREFIX + ".challenge_level"));
					if (failSafeOn) {
						int snoozeCount = arg1.getExtras().getInt(Alarm.PACKAGE_PREFIX + ".snooze_count");
						if (snoozeCount > 0) {
							i.putExtra(Alarm.PACKAGE_PREFIX + ".snooze_count", snoozeCount);
						} else {
							i = new Intent(ctx, FailSafe.class);
						}
					}
				} 
				else if (failSafeOn) 
				{
					int snoozeCount = arg1.getExtras().getInt(Alarm.PACKAGE_PREFIX + ".snooze_count");
					if (snoozeCount > 0) {
						i = new Intent(ctx, Snooze.class);
						i.putExtra(Alarm.PACKAGE_PREFIX + ".snooze_count", snoozeCount); 
					} else {
						i = new Intent(ctx, FailSafe.class);
					}
				}
	
				if (arg1.hasExtra(Alarm.PACKAGE_PREFIX + ".sound")) {
					i.putExtra(Alarm.PACKAGE_PREFIX + ".vibrate", arg1.getExtras().getInt(Alarm.PACKAGE_PREFIX + ".vibrate"));
					i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", arg1.getExtras().getString(Alarm.PACKAGE_PREFIX + ".sound"));
					i.putExtra(Alarm.PACKAGE_PREFIX + ".has_repeat", arg1.getExtras().getBoolean(Alarm.PACKAGE_PREFIX + ".has_repeat"));
				}

				i.putExtra(Alarm.PACKAGE_PREFIX + ".id", scheduleId);
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
