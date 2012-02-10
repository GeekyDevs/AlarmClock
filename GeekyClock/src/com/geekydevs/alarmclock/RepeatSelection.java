package com.geekydevs.alarmclock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class RepeatSelection extends Activity {
	
	private CheckBox sun_check;
	private CheckBox mon_check;
	private CheckBox tue_check;
	private CheckBox wed_check;
	private CheckBox thu_check;
	private CheckBox fri_check;
	private CheckBox sat_check;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.repeat_selection);
		setTitle("Alarm Repeats...");
	}
}

