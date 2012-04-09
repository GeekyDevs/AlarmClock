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
import android.widget.SeekBar;
import android.widget.TextView;

public class Snooze extends Activity {

	private static long[] pattern = {200, 500};
	private static final int SNOOZE_INTERVAL = 5;
	
	private MediaPlayer mediaPlayer;
	private AudioManager amanager;
	
	private Vibrator vibrate; 
	private TextView snooze;
	private SeekBar dismissBar;
	
	private boolean isNativeSnooze = true;
	private boolean notificationOn = false;
	private boolean soundOn = false;
	private boolean challengeOn = false;
	private boolean vibrateOn = false;
	
	private int snooze_flag = 0;
	private int snooze_remaining;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.snooze);
		
		int snooze_cnt = 0;
		
		notificationOn = getIntent().getExtras().getBoolean("notifOn");
		
		if (getIntent().hasExtra("snooze_count"))
			snooze_cnt = getIntent().getExtras().getInt("snooze_count"); 
		
		findViewById(R.id.snooze_button).setOnClickListener(snoozeOnClick);
		dismissBar = (SeekBar) findViewById(R.id.dismiss_bar);
		dismissBar.setOnSeekBarChangeListener(bar);
		dismissBar.setMax(100);
		dismissBar.setProgress(1);
		
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
		if (!sound.equals("Silent")) {
			soundOn = true;
			if (sound.equals("Default")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.normal);
			} else if (sound.equals("C'mon Man")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.cmon_man);
			} else if (sound.equals("Red Alert")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.red_alert);
			}
		}

		if (soundOn) {
			mediaPlayer.start();
			mediaPlayer.setLooping(true);
		}
		
		if (getIntent().getExtras().getInt("vibrate") > 0) {
			vibrateOn = true;
			snooze_flag = PendingIntent.FLAG_UPDATE_CURRENT;
			vibrate = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			vibrate.vibrate(pattern, 0);
		}
			
	}
	
	private Button.OnClickListener snoozeOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {

			if (soundOn) {
				mediaPlayer.stop();
				mediaPlayer.release();
			}
			
			Intent i = new Intent(getBaseContext(), AlarmReceiver.class);
			i.putExtra("sound", getIntent().getExtras().getString("sound"));
			
			if (vibrateOn) {
				vibrate.cancel();
				i.putExtra("vibrate", getIntent().getExtras().getInt("vibrate"));
			}

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
			calendar.add(Calendar.SECOND, SNOOZE_INTERVAL);
			
	        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
	        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);	
			
			finish();
		}
	};
	
	/*
	 * Cancel the alarm entirely and release all resources tied to the alarm.
	 */
	private SeekBar.OnSeekBarChangeListener bar = new SeekBar.OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			
			if (seekBar.getProgress() < seekBar.getMax()) {
				seekBar.setProgress(1);
			}
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

			if (progress == seekBar.getMax()) {
				
				Intent i = new Intent(getBaseContext(), AlarmService.class);
				i.setAction(AlarmService.ACTION_STOP_ALARM);
				startService(i);

				if (soundOn) {
					mediaPlayer.stop();
					mediaPlayer.release();
				}
				if (vibrateOn) {
					vibrate.cancel();
				}
				
				if (notificationOn) {
					Intent in = new Intent(getBaseContext(), AlarmService.class);
					in.setAction(AlarmService.ACTION_SHOW_NOTIF);
					startService(in);
				}
				finish();
			}
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
