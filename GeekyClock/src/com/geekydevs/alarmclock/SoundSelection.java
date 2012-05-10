package com.geekydevs.alarmclock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

public class SoundSelection extends Activity{

	private RadioButton dfault;
	private RadioButton cmon;
	private RadioButton alert;
	private RadioButton silent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ring_selection);
		setTitle("Alarm Sound");
		
		silent = (RadioButton)findViewById(R.id.silent);
		dfault = (RadioButton)findViewById(R.id.default_ring);
		cmon = (RadioButton)findViewById(R.id.cmon_man_ring);
		alert = (RadioButton)findViewById(R.id.red_alert_ring);
		
		findViewById(R.id.ring_ok).setOnClickListener(okOnClick);
		findViewById(R.id.ring_cancel).setOnClickListener(cancelOnClick);
		
		String sound = getIntent().getExtras().getString(Alarm.PACKAGE_PREFIX + ".sound");
		
		if (sound.equals("Silent")) {
			silent.setChecked(true);
		} else if (sound.equals("Default")) {
			dfault.setChecked(true);
		} else if (sound.equals("C'mon Man")) {
			cmon.setChecked(true);
		} else if (sound.equals("Red Alert")) {
			alert.setChecked(true);
		}
	}

	private Button.OnClickListener okOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Intent i = new Intent();
			
			if (silent.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", "Silent");
			} else if (dfault.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", "Default");
			} else if (cmon.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", "C'mon Man");
			} else if (alert.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", "Red Alert");
			}

			setResult(Activity.RESULT_OK, i);
			finish();
		}
	};
	
	private Button.OnClickListener cancelOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	};
}
