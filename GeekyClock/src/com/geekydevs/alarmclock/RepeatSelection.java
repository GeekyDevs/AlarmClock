package com.geekydevs.alarmclock;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class RepeatSelection extends Activity {
	
	private CheckBox chkSun;
	private CheckBox chkMon;
	private CheckBox chkTue;
	private CheckBox chkWed;
	private CheckBox chkThu;
	private CheckBox chkFri;
	private CheckBox chkSat;
	
	private LinearLayout llSun;
	private LinearLayout llMon;
	private LinearLayout llTue;
	private LinearLayout llWed;
	private LinearLayout llThu;
	private LinearLayout llFri;
	private LinearLayout llSat;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.repeat_selection);
		setTitle("Repeat");
		
		findViews();

		String oldSetting = "";
		
		Bundle b = getIntent().getExtras();
		oldSetting = b.getString(Alarm.PACKAGE_PREFIX + ".days");
		
		if (oldSetting.indexOf("Sunday") >= 0) {chkSun.setChecked(true);}
		if (oldSetting.indexOf("Monday") >= 0) {chkMon.setChecked(true);}
		if (oldSetting.indexOf("Tuesday") >= 0) {chkTue.setChecked(true);}
		if (oldSetting.indexOf("Wednesday") >= 0) {chkWed.setChecked(true);}
		if (oldSetting.indexOf("Thursday") >= 0) {chkThu.setChecked(true);}
		if (oldSetting.indexOf("Friday") >= 0) {chkFri.setChecked(true);}
		if (oldSetting.indexOf("Saturday") >= 0) {chkSat.setChecked(true);}
		
	}
	
	private void findViews() {
		
		chkSun = (CheckBox)findViewById(R.id.rpt_sun);
		chkMon = (CheckBox)findViewById(R.id.rpt_mon);
		chkTue = (CheckBox)findViewById(R.id.rpt_tues);
		chkWed = (CheckBox)findViewById(R.id.rpt_wed);
		chkThu = (CheckBox)findViewById(R.id.rpt_thur);
		chkFri = (CheckBox)findViewById(R.id.rpt_fri);
		chkSat = (CheckBox)findViewById(R.id.rpt_sat);
		
		findViewById(R.id.ll_repeat_sun).setOnClickListener(sunRowOnClick);
		findViewById(R.id.ll_repeat_mon).setOnClickListener(monRowOnClick);
		findViewById(R.id.ll_repeat_tues).setOnClickListener(tueRowOnClick);
		findViewById(R.id.ll_repeat_wed).setOnClickListener(wedRowOnClick);
		findViewById(R.id.ll_repeat_thur).setOnClickListener(thuRowOnClick);
		findViewById(R.id.ll_repeat_fri).setOnClickListener(friRowOnClick);
		findViewById(R.id.ll_repeat_sat).setOnClickListener(satRowOnClick);
		
		findViewById(R.id.sr_btn_ok).setOnClickListener(okOnClick);
		findViewById(R.id.sr_btn_cancel).setOnClickListener(cancelOnClick);
	}
	
	private View.OnClickListener sunRowOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			chkSun.setChecked(!chkSun.isChecked());
		}	
	};
	
	private View.OnClickListener monRowOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			chkMon.setChecked(!chkMon.isChecked());
		}	
	};
	
		private View.OnClickListener tueRowOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			chkTue.setChecked(!chkTue.isChecked());
		}	
	};
	
	private View.OnClickListener wedRowOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			chkWed.setChecked(!chkWed.isChecked());
		}	
	};
	
	private View.OnClickListener thuRowOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			chkThu.setChecked(!chkThu.isChecked());
		}	
	};
	
	private View.OnClickListener friRowOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			chkFri.setChecked(!chkFri.isChecked());
		}	
	};
	
	private View.OnClickListener satRowOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			chkSat.setChecked(!chkSat.isChecked());
		}	
	};
	
	/*
	 * Clicking the 
	 */
	private Button.OnClickListener okOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Intent i = new Intent();
			
			i.putExtra(Alarm.PACKAGE_PREFIX + ".Sunday", chkSun.isChecked());
			i.putExtra(Alarm.PACKAGE_PREFIX + ".Monday", chkMon.isChecked());
			i.putExtra(Alarm.PACKAGE_PREFIX + ".Tuesday", chkTue.isChecked());
			i.putExtra(Alarm.PACKAGE_PREFIX + ".Wednesday", chkWed.isChecked());
			i.putExtra(Alarm.PACKAGE_PREFIX + ".Thursday", chkThu.isChecked());
			i.putExtra(Alarm.PACKAGE_PREFIX + ".Friday", chkFri.isChecked());
			i.putExtra(Alarm.PACKAGE_PREFIX + ".Saturday", chkSat.isChecked());
			
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

