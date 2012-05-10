package com.geekydevs.alarmclock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

public class DifficultySelection extends Activity {

	private RadioButton easy;
	private RadioButton medium;
	private RadioButton hard;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.level_selection);
		setTitle("Difficulty Level");
		
		easy = (RadioButton)findViewById(R.id.easy);
		medium = (RadioButton)findViewById(R.id.medium);
		hard = (RadioButton)findViewById(R.id.hard);
		
		findViewById(R.id.level_ok).setOnClickListener(okOnClick);
		findViewById(R.id.level_cancel).setOnClickListener(cancelOnClick);
		
		String sound = getIntent().getExtras().getString(Alarm.PACKAGE_PREFIX + ".level");
		
		if (sound.equals("Easy")) {
			easy.setChecked(true);
		} else if (sound.equals("Medium")) {
			medium.setChecked(true);
		} else if (sound.equals("Hard")) {
			hard.setChecked(true);
		}
	}

	private Button.OnClickListener okOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Intent i = new Intent();
			
			if (easy.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".level", "Easy");
			} else if (medium.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".level", "Medium");
			} else if (hard.isChecked()) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".level", "Hard");
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
