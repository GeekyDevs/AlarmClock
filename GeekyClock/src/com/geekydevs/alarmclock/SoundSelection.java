package com.geekydevs.alarmclock;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;

public class SoundSelection extends Activity{

	private RadioButton silent;
	private RadioButton defaultRing;
	private RadioButton digital;
	private RadioButton rooster;
	private RadioButton trumpet;
	private RadioButton awaken;
	private RadioButton redAlert;
	private RadioButton buzzAlert;
	
	MediaPlayer player;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ring_selection);
		setTitle("Alarm Sound");
		
		silent = (RadioButton)findViewById(R.id.silent);
		defaultRing = (RadioButton)findViewById(R.id.default_ring);
		digital = (RadioButton)findViewById(R.id.digital);
		rooster = (RadioButton)findViewById(R.id.rooster);
		trumpet = (RadioButton)findViewById(R.id.trumpet);
		awaken = (RadioButton)findViewById(R.id.awaken);
		redAlert = (RadioButton)findViewById(R.id.red_alert);
		buzzAlert = (RadioButton)findViewById(R.id.buzz_alert);
		
		findViewById(R.id.ring_ok).setOnClickListener(okOnClick);
		findViewById(R.id.ring_cancel).setOnClickListener(cancelOnClick);
		
		String sound = getIntent().getExtras().getString(Alarm.PACKAGE_PREFIX + ".sound");
		
		if (sound.equals("Silent")) {
			silent.setChecked(true);
		} else if (sound.equals("Default")) {
			defaultRing.setChecked(true);
		} else if (sound.equals("Digital")) {
			digital.setChecked(true);
		} else if (sound.equals("Rooster")) {
			rooster.setChecked(true);
		} else if (sound.equals("Trumpet")) {
			trumpet.setChecked(true);
		} else if (sound.equals("Awaken")) {
			awaken.setChecked(true);
		} else if (sound.equals("Red Alert")) {
			redAlert.setChecked(true);
		} else if (sound.equals("Buzz")) {
			buzzAlert.setChecked(true);
		}
		
		silent.setOnCheckedChangeListener(checkedClick);
		defaultRing.setOnCheckedChangeListener(checkedClick);
		digital.setOnCheckedChangeListener(checkedClick);
		rooster.setOnCheckedChangeListener(checkedClick);
		trumpet.setOnCheckedChangeListener(checkedClick);
		awaken.setOnCheckedChangeListener(checkedClick);
		redAlert.setOnCheckedChangeListener(checkedClick);
		buzzAlert.setOnCheckedChangeListener(checkedClick);
	}
	
	private CompoundButton.OnCheckedChangeListener checkedClick = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(!isChecked) {
				return;
			}
			
			if(player!=null) {
				player.stop();
				player.release();
				player = null;
			}		
			
			if(buttonView==silent) {
				return;
			} else if(buttonView==defaultRing) {
				player = MediaPlayer.create(SoundSelection.this, R.raw.default_ring); 
			} else if(buttonView==digital) {
				player = MediaPlayer.create(SoundSelection.this, R.raw.digital_alarm);
			} else if(buttonView==rooster) {
				player = MediaPlayer.create(SoundSelection.this, R.raw.rooster);
			} else if(buttonView==trumpet) {
				player = MediaPlayer.create(SoundSelection.this, R.raw.trumpet_alarm);
			} else if(buttonView==awaken) {
				player = MediaPlayer.create(SoundSelection.this, R.raw.awaken);
			} else if(buttonView==redAlert) {
				player = MediaPlayer.create(SoundSelection.this, R.raw.red_alert);
			} else if(buttonView==buzzAlert) {
				player = MediaPlayer.create(SoundSelection.this, R.raw.buzz_alert);
			}
			
			if(player!=null) {
				player.start();
			}
		}
	};
	
	private Button.OnClickListener okOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Intent i = new Intent();
			
			if (silent.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", "Silent");
			} else if (defaultRing.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", "Default");
			} else if (digital.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", "Digital");
			} else if (rooster.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", "Rooster");
			} else if (trumpet.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", "Trumpet");
			} else if (awaken.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", "Awaken");
			} else if (redAlert.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", "Red Alert");
			} else if (buzzAlert.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", "Buzz");
			}
			
			if(player!=null) {
				player.stop();
				player.release();
				player = null;
			}

			setResult(Activity.RESULT_OK, i);
			finish();
		}
	};
	
	private Button.OnClickListener cancelOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(player!=null) {
				player.stop();
				player.release();
				player = null;
			}
			
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	};
}
