package com.geekydevs.alarmclock;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class Snooze extends Activity {

	private MediaPlayer mediaPlayer;
	private AudioManager amanager;
	
	private Vibrator vibrate;
	private static long[] pattern = {200, 500};
	
	private TextView snooze;
	
	private boolean isNativeSnooze = true;
	private boolean challengeOn = false;
	private int snooze_flag = 0;
	private int snooze_remaining;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.snooze);
		
		int snooze_cnt = 0;
		
		if (getIntent().hasExtra("snooze_count"))
			snooze_cnt = getIntent().getExtras().getInt("snooze_count"); 
		
		findViewById(R.id.snooze_button).setOnClickListener(snoozeOnClick);
		findViewById(R.id.dismiss_button).setOnClickListener(dismissOnClick);
		
		snooze = (TextView)findViewById(R.id.snooze_remaining);
		
		if (snooze_cnt > 0) {
			isNativeSnooze = false;
			snooze_flag = PendingIntent.FLAG_UPDATE_CURRENT;
			snooze.setVisibility(TextView.VISIBLE);
			snooze.setText("Snooze Remaining: " + snooze_cnt);
			snooze_remaining = snooze_cnt;
		}
		
		if (getIntent().hasExtra("challenge_on"))
			challengeOn = getIntent().getExtras().getInt("challenge_on") > 0;
		
		amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		amanager.setStreamVolume(AudioManager.STREAM_ALARM, 20, 0);

		String sound = getIntent().getExtras().getString("sound");
		if (sound.equals("Default")) {
			mediaPlayer = MediaPlayer.create(this, R.raw.normal);
		} else if (sound.equals("C'mon Man")) {
			mediaPlayer = MediaPlayer.create(this, R.raw.cmon_man);
		} else if (sound.equals("Red Alert")) {
			mediaPlayer = MediaPlayer.create(this, R.raw.red_alert);
		}

		mediaPlayer.start();
		mediaPlayer.setLooping(true);
		
		if (getIntent().getExtras().getInt("vibrate") > 0)
			snooze_flag = PendingIntent.FLAG_UPDATE_CURRENT;
			vibrate = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			vibrate.vibrate(pattern, 0);
	}
	
	private Button.OnClickListener snoozeOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {

			mediaPlayer.stop();
			mediaPlayer.release();
			vibrate.cancel();
			
			Intent i = new Intent(getBaseContext(), AlarmReceiver.class);
			i.putExtra("vibrate", (getIntent().hasExtra("vibrate")));
			i.putExtra("sound", getIntent().getExtras().getString("sound"));
			
			if (challengeOn) 
				i.putExtra("challenge_on", getIntent().getExtras().getInt("challenge_on"));
			
			if (!isNativeSnooze) {
				i.putExtra("failsafe_on", 1);
				i.putExtra("snooze_count", snooze_remaining - 1);
			}

			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					getBaseContext(), 0, i, snooze_flag);
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.SECOND, 5);
			
	        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
	        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);	
			
			finish();
		}
	};
	
	/*
	 * Cancel the alarm entirely and release all resources tied to the alarm.
	 */
	private Button.OnClickListener dismissOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {

			Intent i = new Intent(getBaseContext(), AlarmService.class);
			i.setAction(AlarmService.ACTION_STOP_ALARM);
			startService(i);

			mediaPlayer.stop();
			mediaPlayer.release();
			vibrate.cancel();
			
			finish();
		}
	};
	
	/*
	 * Disable the user from exiting the failsafe screen.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) ||
			(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
			return true;
		}

		return super.onKeyDown(keyCode, event);	
	}
	
	@Override
	public void onBackPressed() {
		//do nothing
	}
	
	@Override
	public void onAttachedToWindow() {
	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	    super.onAttachedToWindow();
	}
}
