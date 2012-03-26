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
		
		//Vibrator v = (Vibrator)ctx.getSystemService(Context.VIBRATOR_SERVICE);
		//v.vibrate(3000);
		
		Intent i = new Intent(ctx, Snooze.class);

		if (arg1.hasExtra("snooze_count")) {
			
			int snoozeCount = arg1.getExtras().getInt("snooze_count");
			if (snoozeCount > 0) {
				i = new Intent(ctx, Snooze.class);
				i.putExtra("snooze_count", arg1.getExtras().getInt("snooze_count")); 
			} else {
				i = new Intent(ctx, FailSafe.class);
			}
		}
		
		if (arg1.hasExtra("vibrate"))
			i.putExtra("vibrate", true);
		
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(i);

	}
}
