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
		
		/*
		if (arg1.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
	      Log.d("AlarmReceiver: ", " ACTION_TIME_CHANGED received");
	    }
		*/
		Intent i = new Intent(ctx, Snooze.class);
		
		boolean challengeOn = arg1.getExtras().getInt("challenge_on") > 0;
		boolean failSafeOn = arg1.getExtras().getInt("failsafe_on") > 0;
		
		if (challengeOn) {
			i = new Intent(ctx, Challenge.class);
			i.putExtra("challenge_level", arg1.getExtras().getString("challenge_level"));
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
		
		i.putExtra("vibrate", arg1.getExtras().getInt("vibrate"));
		i.putExtra("sound", arg1.getExtras().getString("sound"));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(i);

	}
}
