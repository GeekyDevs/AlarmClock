package com.geekydevs.alarmclock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class RepeatSelection extends Activity {
	
	private CheckBox chkSun;
	private CheckBox chkMon;
	private CheckBox chkTue;
	private CheckBox chkWed;
	private CheckBox chkThu;
	private CheckBox chkFri;
	private CheckBox chkSat;
	
	/*
	 * Clicking the 
	 */
	private Button.OnClickListener okOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			String daysChecked = "";
			
			if (chkMon.isChecked()) {daysChecked += "Monday, ";}
			if (chkTue.isChecked()) {daysChecked += "Tuesday, ";}
			if (chkWed.isChecked()) {daysChecked += "Wednesday, ";}
			if (chkThu.isChecked()) {daysChecked += "Thursday, ";}
			if (chkFri.isChecked()) {daysChecked += "Friday, ";}
			if (chkSat.isChecked()) {daysChecked += "Saturday, ";}
			if (chkSun.isChecked()) {daysChecked += "Sunday, ";}
			
			if (daysChecked == "") {
				daysChecked = "Never";
			} else {
				daysChecked = daysChecked.substring(0, daysChecked.length()-2);
			}

			Intent i = new Intent();
			i.putExtra("days", daysChecked);

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.repeat_selection);
		setTitle("Alarm Repeats...");
		
		chkMon = (CheckBox)findViewById(R.id.rpt_mon);
		chkTue = (CheckBox)findViewById(R.id.rpt_tues);
		chkWed = (CheckBox)findViewById(R.id.rpt_wed);
		chkThu = (CheckBox)findViewById(R.id.rpt_thur);
		chkFri = (CheckBox)findViewById(R.id.rpt_fri);
		chkSat = (CheckBox)findViewById(R.id.rpt_sat);
		chkSun = (CheckBox)findViewById(R.id.rpt_sun);

		findViewById(R.id.sr_btn_ok).setOnClickListener(okOnClick);
		findViewById(R.id.sr_btn_cancel).setOnClickListener(cancelOnClick);
		
		String oldSetting = "";
		
		Bundle b = getIntent().getExtras();
		oldSetting = b.getString("days");
		
		if (oldSetting.indexOf("Monday") >= 0) {chkMon.setChecked(true);}
		if (oldSetting.indexOf("Tuesday") >= 0) {chkTue.setChecked(true);}
		if (oldSetting.indexOf("Wednesday") >= 0) {chkWed.setChecked(true);}
		if (oldSetting.indexOf("Thursday") >= 0) {chkThu.setChecked(true);}
		if (oldSetting.indexOf("Friday") >= 0) {chkFri.setChecked(true);}
		if (oldSetting.indexOf("Saturday") >= 0) {chkSat.setChecked(true);}
		if (oldSetting.indexOf("Sunday") >= 0) {chkSun.setChecked(true);}
		
	}
	
	
}

