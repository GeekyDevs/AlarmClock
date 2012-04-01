package com.geekydevs.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context ctx, Intent arg1) {
		
		/*
		PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "AppAlarmReceiver");
		wl.acquire(120000);
		*/
		
		Intent i = new Intent(ctx, Snooze.class);
		
		boolean challengeOn = arg1.getExtras().getInt("challenge_on") > 0;
		boolean failSafeOn = arg1.getExtras().getInt("failsafe_on") > 0;
		
		if (challengeOn) {
			i = new Intent(ctx, Challenge.class);
			if (failSafeOn) {
				int snoozeCount = arg1.getExtras().getInt("snooze_count");
				if (snoozeCount > 0) {
					i.putExtra("snooze_count", snoozeCount);
				} else {
					i = new Intent(ctx, FailSafe.class);
				}
			}
		} 
		else if (failSafeOn) 
		{
			int snoozeCount = arg1.getExtras().getInt("snooze_count");
			if (snoozeCount > 0) {
				i = new Intent(ctx, Snooze.class);
				i.putExtra("snooze_count", snoozeCount); 
			} else {
				i = new Intent(ctx, FailSafe.class);
			}
		}
		
		if (arg1.hasExtra("vibrate")) {
			i.putExtra("vibrate", 1);
		}
		
		i.putExtra("sound", arg1.getExtras().getString("sound"));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(i);

	}
}
