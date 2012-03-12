package com.geekydevs.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.widget.Toast;
import android.speech.tts.TextToSpeech.OnInitListener;

public class AlarmReceiver extends BroadcastReceiver implements OnInitListener {

	@Override
	public void onReceive(Context ctx, Intent arg1) {
		
		/*
		PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "AppAlarmReceiver");
		wl.acquire(120000);
		
		Intent i = new Intent(ctx, AlarmService.class);
		i.setAction(AlarmService.ACTION_LAUNCH_ALARM);
		ctx.startService(i);
		*/
		
		AudioManager amanager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
		//int maxVolume = amanager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
		amanager.setStreamVolume(AudioManager.STREAM_ALARM, 20, 0);

		MediaPlayer mediaPlayer = MediaPlayer.create(ctx, R.raw.alarm1);

		//mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM); // this is important.

		mediaPlayer.start();
	}

    @Override
    public void onInit(int initStatus) {}
}
