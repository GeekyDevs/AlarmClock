package com.geekydevs.alarmclock;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class FailSafe extends Activity {

	TextView timeRemaining;
	private countDown timer;
	private final long lockOutTime = 10000; //600000;
	private final long interval = 1000;
	private MediaPlayer mediaPlayer;
	
	private Vibrator vibrate;
	private static long[] pattern = {200, 500};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.failsafe_layout);
		
		// Assigning views
		ImageView lockImage = (ImageView) findViewById(R.id.failsafe_screen);
		lockImage.setImageDrawable(getResources().getDrawable(R.drawable.lock));
		
		timeRemaining = (TextView) findViewById(R.id.time_remaining);
		
		// Sound settings
		AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		amanager.setStreamVolume(AudioManager.STREAM_ALARM, 20, 0);
		
		mediaPlayer = MediaPlayer.create(this, R.raw.alarm1);

		mediaPlayer.start();
		mediaPlayer.setLooping(true);
		
		if (getIntent().hasExtra("vibrate"))
			vibrate = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			vibrate.vibrate(pattern, 0);
		
		timer = new countDown(lockOutTime, interval);
		timer.start();
	}
	
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

	
	/*
	 * Extends the existing counter class to display count down and turn
	 * off ring on finish. 
	 */
	public class countDown extends CountDownTimer {
		
		public countDown (long startTime, long interval) {
			super(startTime, interval);
		}
		
		@Override
		public void onFinish() {
			timer.cancel();
			mediaPlayer.stop();
			mediaPlayer.release();
			vibrate.cancel();
			finish();
		}
		
		@Override
		public void onTick (long timeTillFinished) {
			
			long minute = timeTillFinished/60000;
			long seconds = (timeTillFinished % 60000)/1000;
			String time;
			
			if (minute > 1) {
				time = minute + ":";	
			} else {
				time = "00:";
			}
			
			if (seconds > 9) {
				time += seconds;
			} else {
				time += "0" + seconds;
			}

			timeRemaining.setText("Time remain:  " + time);
		}
	}

}
