package com.geekydevs.alarmclock;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FailSafe extends Activity {

	TextView timeRemaining;
	private CountDown timer;
	private final long lockOutTime = 120000;
	private final long interval = 1000;
	
	private AudioManager amanager;
	private MediaPlayer mediaPlayer;
	
	private Vibrator vibrate;
	private static long[] pattern = {200, 500};
	
	private boolean vibrateOn = false;
	
	private int oldVolumeIndex = 0;
	private int id = -1;
	
	private WakeLock wakeLock;
	private KeyguardLock keyguardLock;
	
	private AlarmDBAdapter dbAdapter;
	private boolean repeatFlag = false;
	
	private AdView adView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.failsafe_layout);
		
		dbAdapter = new AlarmDBAdapter(getBaseContext());
		dbAdapter.open();
		
		WakeLocker.acquire(getBaseContext());

		// Assigning views
		ImageView lockImage = (ImageView) findViewById(R.id.failsafe_screen);
		lockImage.setImageDrawable(getResources().getDrawable(R.drawable.failsafe_lock));
		
        // Add the AdView to the view hierarchy. The view will have no size
        // until the ad is loaded.
        // Create an ad.
        adView = new AdView(this, AdSize.BANNER, "a14f8cdc40486f5");
        LinearLayout layout = (LinearLayout) findViewById(R.id.ad);
        layout.addView(adView);
        
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device.
        AdRequest request = new AdRequest();
        request.addTestDevice(AdRequest.TEST_EMULATOR);
        
        // Testing on real device
        request.addTestDevice("3334DE9B8EA200EC");
        
        // Start loading the ad in the background.
        adView.loadAd(request);
        
		timeRemaining = (TextView) findViewById(R.id.time_remaining);
		
		// Sound settings
		amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		oldVolumeIndex = amanager.getStreamVolume(AudioManager.STREAM_MUSIC);
		amanager.setStreamVolume(AudioManager.STREAM_MUSIC, amanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
		
		repeatFlag = getIntent().getExtras().getBoolean(Alarm.PACKAGE_PREFIX + ".has_repeat");
		id = getIntent().getExtras().getInt(Alarm.PACKAGE_PREFIX + ".id"); 
		String sound = getIntent().getExtras().getString(Alarm.PACKAGE_PREFIX + ".sound");
		
		if (sound.equals("Default")) {
			mediaPlayer = MediaPlayer.create(this, R.raw.default_ring);
		} else if (sound.equals("Digital")) {
			mediaPlayer = MediaPlayer.create(this, R.raw.digital_alarm);
		} else if (sound.equals("Rooster")) {
			mediaPlayer = MediaPlayer.create(this, R.raw.rooster);
		} else if (sound.equals("Trumpet")) {
			mediaPlayer = MediaPlayer.create(this, R.raw.trumpet_alarm);
		} else if (sound.equals("Awaken")) {
			mediaPlayer = MediaPlayer.create(this, R.raw.awaken);
		} else if (sound.equals("Red Alert")) {
			mediaPlayer = MediaPlayer.create(this, R.raw.red_alert);
		} else if (sound.equals("Buzz")) {
			mediaPlayer = MediaPlayer.create(this, R.raw.buzz_alert);
		}

		if(mediaPlayer!=null) {
			mediaPlayer.start();
			mediaPlayer.setLooping(true);
		}
		
		if (getIntent().getExtras().getInt(Alarm.PACKAGE_PREFIX + ".vibrate") == 1) {
			vibrateOn = true;
			vibrate = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			vibrate.vibrate(pattern, 0);
		}
		
		timer = new CountDown(lockOutTime, interval);
		timer.start();
	}
	

	@Override
	public void onResume() {
		try {
			Log.v("On resume called","------ WakeLocker aquire next!");
			WakeLocker.acquire();
		}catch(Exception ex){

		}

		super.onResume();
	}
	
	@Override
	protected void onPause() {

		try {
			Log.v("on pause called", "on pause called");
			WakeLocker.release();
		}catch(Exception ex){
			Log.e("Exception in on menu", "exception on menu");
		}
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		try {
			Log.v("on stopped called", "on stopped called");
			WakeLocker.release();
		}catch(Exception ex){
			Log.e("Exception in on menu", "exception on menu");
		}
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
    	// Destroy the AdView.
    	if (adView != null) {
	      adView.destroy();
	    }
		
		try {
			Log.v("on destroy called", "on destroy called");
			WakeLocker.release();
			WakeLocker.exit();
		}catch(Exception ex){
			Log.e("Exception in on menu", "exception on menu");
		}

		dbAdapter.close();
		super.onDestroy();
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
	public class CountDown extends CountDownTimer {
		
		public CountDown (long startTime, long interval) {
			super(startTime, interval);
		}
		
		@Override
		public void onFinish() {
			timer.cancel();
			if(mediaPlayer!=null){
				mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
			}
			if (vibrateOn) {
				vibrate.cancel();
			}
			
			amanager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolumeIndex, 0);
			
			Intent i = new Intent(getBaseContext(), AlarmService.class);
			i.setAction(AlarmService.ACTION_STOP_ALARM);
			i.putExtra(Alarm.PACKAGE_PREFIX + ".id", id);
			startService(i);
			
			if (!repeatFlag) {
				dbAdapter.setAlarmToDB(id, false);
			}
			
			finish();
		}
		
		@Override
		public void onTick (long timeTillFinished) {
			long minute = timeTillFinished/60000;
			long seconds = (timeTillFinished % 60000)/1000;
			
			StringBuffer time = new StringBuffer();
			if (minute >= 1) {
				time.append(minute);
				time.append(":");	
			} else {				
				time.append("00:");
			}
			
			if (seconds > 9) {
				time.append(seconds);
			} else {
				time.append("0");
				time.append(seconds);
			}

			timeRemaining.setText(time.toString());
		}
	}

}
