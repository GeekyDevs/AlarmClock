package com.geekydevs.alarmclock;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.DatePicker;

public class AlarmEdit extends Activity {

	private static final int DIALOG_PICK_TIME = 1;
	
	private static final int ACTION_INPUT_LABEL = 1;
	private static final int ACTION_CHOOSE_REPEAT = 3;
	
	private LinearLayout lLRepeat;
	private LinearLayout lLTime;
	private LinearLayout lLLabel;
	
	private TextView timeView;
	private TextView repeatView;
	
	
	
	/*
	 * Automatically called when AlarmEdit was created and launches the UI to
	 * the alarm edit screen. 
	 * Sets up the view and listener.
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_edit);
		
		findViews();
		assignListeners();
	}
	
	/*
	 * Assigns each item in the UI that requires interaction to the proper 
	 * variable.
	 */
	private void findViews() {
		
		lLLabel = (LinearLayout)findViewById(R.id.ea_ll_label);
		
		lLRepeat = (LinearLayout)findViewById(R.id.ea_ll_alarm_repeat);
		lLTime = (LinearLayout)findViewById(R.id.ea_ll_alarm_time);
		
		timeView = (TextView)findViewById(R.id.time_selection);
		repeatView = (TextView)findViewById(R.id.repeat_selection);
	}
	
	/*
	 * Set up each variable that was assigned a view with a listener.
	 */
	private void assignListeners() {
		
		lLLabel.setOnClickListener(labelOnClick);
		
		lLRepeat.setOnClickListener(repeatOnClick);
		lLTime.setOnClickListener(timeOnClick);
	}
	
	/*
	 * Updates the time to reflect the change made by the user.
	 */
	private void updateTimeView (String time) {
		timeView.setText(time);
	}
	
	private void updateRepeatView (String choices) {
		repeatView.setText(choices);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ACTION_CHOOSE_REPEAT:
				updateRepeatView(data.getStringExtra("days"));
				break;
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/*
	 * 
	 */
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

		switch (id)
		{
		case DIALOG_PICK_TIME:
			return new TimePickerDialog(this, mOnTimeSetListener, hour, minute, false);
		}
		
		return super.onCreateDialog(id);
	}
	
	private TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		
			String timeFormat = "";
			
			if (hourOfDay > 12) {
				timeFormat += (hourOfDay-12) + ":" ;
			} else if (hourOfDay == 0) {
				timeFormat += (hourOfDay + 12) + ":";
			} else {
				timeFormat += hourOfDay + ":";
			}
			
			if (minute < 10) {
				timeFormat += "0" + minute;
			} else {
				timeFormat += minute;
			}
			
			if (hourOfDay >= 12) {
				timeFormat += " pm";
			} else {
				timeFormat += " am";
			}
		
			updateTimeView(timeFormat);
		}
	};
	
	private View.OnClickListener timeOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			showDialog(DIALOG_PICK_TIME);
		}
	};
	
	
	private View.OnClickListener repeatOnClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent i = new Intent(getBaseContext(), RepeatSelection.class);
			i.putExtra("days", repeatView.getText());
			startActivityForResult(i, ACTION_CHOOSE_REPEAT);
		}
	};

}
