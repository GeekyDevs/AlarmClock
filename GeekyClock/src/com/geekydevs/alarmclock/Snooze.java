package com.geekydevs.alarmclock;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


public class Snooze extends Activity {

	private static long[] pattern = {200, 500};
	private static final int SNOOZE_INTERVAL = 540;
	
	private MediaPlayer mediaPlayer;
	private AudioManager amanager;
	
	private Vibrator vibrate; 
	private TextView snooze;
	private SeekBar dismissBar;
	
	private boolean isNativeSnooze = true;
	private boolean soundOn = false;
	private boolean vibrateOn = false;
	private boolean emptyIntent = false;
	
	private int snooze_flag = 0;
	private int snooze_remaining;
	private int id = -1;
	
	private int oldVolumeIndex = 0;

	private NotificationManager notifManager;
	
	private PowerManager.WakeLock wakeLock;
	private PowerManager pm;
	private KeyguardLock keyguardLock;
	
	private AlarmDBAdapter dbAdapter;
	private boolean repeatFlag = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.snooze);
		
		dbAdapter = new AlarmDBAdapter(getBaseContext());
		dbAdapter.open();
		
		//notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		WakeLocker.acquire(this);

		int snooze_cnt = 0;
		
		if (getIntent().hasExtra(Alarm.PACKAGE_PREFIX + ".snooze_count"))
			snooze_cnt = getIntent().getExtras().getInt(Alarm.PACKAGE_PREFIX + ".snooze_count"); 
		
		findViewById(R.id.snooze_button).setOnClickListener(snoozeOnClick);
		dismissBar = (SeekBar) findViewById(R.id.dismiss_bar);
		dismissBar.setOnSeekBarChangeListener(bar);
		dismissBar.setProgress(3);
		
		snooze = (TextView)findViewById(R.id.snooze_remaining);
		
		if (snooze_cnt > 0) {
			isNativeSnooze = false;
			snooze_flag = PendingIntent.FLAG_UPDATE_CURRENT;
			snooze.setVisibility(TextView.VISIBLE);
			snooze.setText("Snooze Remaining: " + snooze_cnt);
			snooze_remaining = snooze_cnt;
		}

		if (getIntent().getExtras() == null) {
			emptyIntent = true;
		} else {
			emptyIntent = getIntent().getExtras().isEmpty();
		}
		
		amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		oldVolumeIndex = amanager.getStreamVolume(AudioManager.STREAM_MUSIC);
		amanager.setStreamVolume(AudioManager.STREAM_MUSIC, amanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
		
		if (!emptyIntent) {
			String sound = getIntent().getExtras().getString(Alarm.PACKAGE_PREFIX + ".sound");
			id = getIntent().getExtras().getInt(Alarm.PACKAGE_PREFIX + ".id");
			repeatFlag = getIntent().getExtras().getBoolean(Alarm.PACKAGE_PREFIX + ".has_repeat");
			
			if (!sound.equals("Silent")) {
				soundOn = true;
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
			}
			if (soundOn) {
				mediaPlayer.start();
				mediaPlayer.setLooping(true);
			}
			
			if (getIntent().getExtras().getInt(Alarm.PACKAGE_PREFIX + ".vibrate") > 0) {
				vibrateOn = true;
				snooze_flag = PendingIntent.FLAG_UPDATE_CURRENT;
				vibrate = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
				vibrate.vibrate(pattern, 0);
			}
		}
			
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
	
	private Button.OnClickListener snoozeOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {

			if (!emptyIntent) {
				if (soundOn) {
					mediaPlayer.stop();
					mediaPlayer.release();
				}
				
				Intent i = new Intent(getBaseContext(), AlarmReceiver.class);
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", getIntent().getExtras().getString(Alarm.PACKAGE_PREFIX + ".sound"));
				i.putExtra(Alarm.PACKAGE_PREFIX + ".has_repeat", getIntent().getExtras().getBoolean(Alarm.PACKAGE_PREFIX + ".has_repeat"));
				i.putExtra(Alarm.PACKAGE_PREFIX + ".id", id);
				
				if (vibrateOn) {
					vibrate.cancel();
					i.putExtra(Alarm.PACKAGE_PREFIX + ".vibrate", getIntent().getExtras().getInt(Alarm.PACKAGE_PREFIX + ".vibrate"));
				}

				if (!isNativeSnooze) {
					i.putExtra(Alarm.PACKAGE_PREFIX + ".failsafe_on", 1);
					i.putExtra(Alarm.PACKAGE_PREFIX + ".snooze_count", snooze_remaining - 1);
				}
	
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						getBaseContext(), id, i, snooze_flag);
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.add(Calendar.SECOND, SNOOZE_INTERVAL);
				
		        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);	
				
				finish();
				
				//setSnoozeNotification(calendar.toString());
			}
		}
	};
	
	/*
	 * Cancel the alarm entirely and release all resources tied to the alarm.
	 */
	private SeekBar.OnSeekBarChangeListener bar = new SeekBar.OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			
			if (seekBar.getProgress() < seekBar.getMax()) {
				seekBar.setProgress(3);
			}
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

			if (progress > seekBar.getMax() - 5) {

				if (!emptyIntent) {
					Intent i = new Intent(getBaseContext(), AlarmService.class);
					i.putExtra(Alarm.PACKAGE_PREFIX + ".id", id);
					i.setAction(AlarmService.ACTION_STOP_ALARM);
					startService(i);
	
					if (soundOn) {
						mediaPlayer.stop();
						mediaPlayer.release();
					}
					if (vibrateOn) {
						vibrate.cancel();
					}
					
					amanager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolumeIndex, 0);
					
					if (!repeatFlag) {
						dbAdapter.setAlarmToDB(id, false);
					}
					
					//notifManager.cancel(0);
					finish();
				}
			}
		}
	};
	
	/*
	 * Set the snooze notification in the widget bar
	 */
	private void setSnoozeNotification(String timeString) {
		
		String message = "Alarm (snoozed)";

		Intent intent = new Intent();
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
		
		Notification notif = new Notification(R.drawable.alarm_icon, message, System.currentTimeMillis());
		notif.setLatestEventInfo(this, message, timeString, contentIntent);
		notif.flags = Notification.FLAG_AUTO_CANCEL;
		notifManager.notify(0, notif);	
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
	    Rect dialogBounds = new Rect();
	    getWindow().getDecorView().getHitRect(dialogBounds);

	    if (!dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
	        return false;
	    }
	    return super.dispatchTouchEvent(ev);
	}

	
	/*
	 * Disable the user from exiting the failsafe screen.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) ||
			(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || 
			(keyCode == KeyEvent.KEYCODE_HOME)) {
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
	    super.onAttachedToWindow();
	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	}
}
