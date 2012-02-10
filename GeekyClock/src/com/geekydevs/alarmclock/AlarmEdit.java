package com.geekydevs.alarmclock;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.DatePicker;


public class AlarmEdit extends Activity {

	private static final int DIALOG_PICK_TIME = 1;
	private static final int DIALOG_PICK_DATE = 2;
	
	private static final int ACTION_INPUT_LABEL = 1;
	private static final int ACTION_CHOOSE_REPEAT = 3;
	
	private LinearLayout lLRepeat;
	private LinearLayout lLTime;
	private LinearLayout lLDate;
	private LinearLayout lLLabel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_edit);
		
		findViews();
		assignListeners();
	}
	
	private void findViews() {
		
		lLLabel = (LinearLayout)findViewById(R.id.ea_ll_label);
		
		lLRepeat = (LinearLayout)findViewById(R.id.ea_ll_alarm_repeat);
		lLTime = (LinearLayout)findViewById(R.id.ea_ll_alarm_time);
		lLDate = (LinearLayout)findViewById(R.id.ea_ll_alarm_date);
	}
	
	private void assignListeners() {
		
		lLLabel.setOnClickListener(labelOnClick);
		
		lLRepeat.setOnClickListener(repeatOnClick);
		lLTime.setOnClickListener(timeOnClick);
		lLDate.setOnClickListener(dateOnClick);
	}
	
	private View.OnClickListener labelOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent i = new Intent(AlarmEdit.this, StringInputDialog.class);
			startActivityForResult(i, ACTION_INPUT_LABEL);
		}
	};
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		Calendar c = Calendar.getInstance(); 
		int minute = c.get(Calendar.MINUTE);
		int hour = c.get(Calendar.HOUR);
		
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		switch (id)
		{
		case DIALOG_PICK_TIME:
			return new TimePickerDialog(this, mOnTimeSetListener, hour, minute, false);
		case DIALOG_PICK_DATE:
			return new DatePickerDialog(this, mOnDateSetListener, year, month, day);
		}
		
		return super.onCreateDialog(id);
	}
	
	private TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			//do nothing
		}
	};
	
	private View.OnClickListener timeOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			showDialog(DIALOG_PICK_TIME);
		}
	};
	
	private DatePickerDialog.OnDateSetListener mOnDateSetListener =
        new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				//do nothing
			}
	};
	
	private View.OnClickListener dateOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick (View view) {
			showDialog(DIALOG_PICK_DATE);
		}
	};
	
	private View.OnClickListener repeatOnClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent i = new Intent(getBaseContext(), RepeatSelection.class);
			startActivityForResult(i, ACTION_CHOOSE_REPEAT);
		}
	};

}
