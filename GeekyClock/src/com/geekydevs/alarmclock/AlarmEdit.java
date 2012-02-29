package com.geekydevs.alarmclock;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


public class AlarmEdit extends Activity {

	private static final int DIALOG_PICK_TIME = 1;
	
	private static final int ACTION_INPUT_LABEL = 1;
	private static final int ACTION_CHOOSE_REPEAT = 3;
	
	private Button saveButton;
	private Button cancelButton;
	
	private LinearLayout lLRepeat;
	private LinearLayout lLTime;
	private LinearLayout lLLabel;
	
	private CheckBox failSafe;
	private SeekBar seekBar;
	private CheckBox wakeUp;
	
	private TextView timeView;
	private TextView repeatView;
	private TextView labelView;
	private TextView snoozeView;
	
	private AlarmDBAdapter dbAdapter;
	
	private Alarm alarm;
	
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
		
		dbAdapter = new AlarmDBAdapter(this);
		dbAdapter.open();
		
		findViews();
		assignListeners();
		
		loadAlarmFromIntent();
	}
	
	/*
	 * Assigns each item in the UI that requires interaction to the proper 
	 * variable.
	 */
	private void findViews() {
		
		lLLabel = (LinearLayout)findViewById(R.id.ll_label);
		
		lLRepeat = (LinearLayout)findViewById(R.id.ea_ll_alarm_repeat);
		lLTime = (LinearLayout)findViewById(R.id.ea_ll_alarm_time);
		
		timeView = (TextView)findViewById(R.id.time_selection);
		repeatView = (TextView)findViewById(R.id.repeat_selection);
		labelView = (TextView)findViewById(R.id.label_view);
		snoozeView = (TextView)findViewById(R.id.snooze_limit);
		
		failSafe = (CheckBox)findViewById(R.id.chk_failsafe);
		wakeUp = (CheckBox)findViewById(R.id.chk_challenge);
		
		seekBar = (SeekBar)findViewById(R.id.seekbar);
		seekBar.setMax(10);
		seekBar.setVisibility(4);
		
		saveButton = (Button)findViewById(R.id.save_settings);
		cancelButton = (Button)findViewById(R.id.cancel_settings);
	}
	
	/*
	 * Set up each variable that was assigned a view with a listener.
	 */
	private void assignListeners() {
		
		lLLabel.setOnClickListener(labelOnClick);
		
		lLRepeat.setOnClickListener(repeatOnClick);
		lLTime.setOnClickListener(timeOnClick);
		
		failSafe.setOnClickListener(failSafeOnClick);
		wakeUp.setOnClickListener(challengeOnClick);
		
		saveButton.setOnClickListener(saveOnClick);
		cancelButton.setOnClickListener(cancelOnClick);
	}
	
	private void loadAlarmFromIntent() {
		Intent i = getIntent();
		if (i.hasExtra("_id")){
			
			Bundle b = getIntent().getExtras();
			int id = (int) b.getLong("_id");

			alarm = dbAdapter.getAlarmById(id);
			
			ContentValues values = alarm.getAll();
			
			updateTimeView(Alarm.formatTime((Integer) values.get("hour"), (Integer) values.get("minute")));
			updateRepeatView(Alarm.formatRepeat(alarm.getAll()));
			updateLabelView(values.get("name") + "");
			
			if ((Boolean) values.get("failsafe_on")) {
				//snoozeView.setVisibility(0);
				snoozeView.setText("Limit: " + values.get("snooze_value"));
				seekBar.setVisibility(0);
			}
			failSafe.setChecked((Boolean) values.get("failsafe_on"));
			seekBar.setProgress((Integer) values.get("snooze_value"));
			wakeUp.setChecked((Boolean) values.get("wakeup_on"));
			
		} else {
			alarm = new Alarm(dbAdapter.countCursors());
			dbAdapter.initialise(alarm);
		}
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
	
	private void updateLabelView(String label) {
		labelView.setText(label);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ACTION_CHOOSE_REPEAT:
				
				alarm.assign("repeat_mon", data.getBooleanExtra("Monday", false));
				alarm.assign("repeat_tue", data.getBooleanExtra("Tuesday", false));
				alarm.assign("repeat_wed", data.getBooleanExtra("Wednesday", false));
				alarm.assign("repeat_thu", data.getBooleanExtra("Thursday", false));
				alarm.assign("repeat_fri", data.getBooleanExtra("Friday", false));
				alarm.assign("repeat_sat", data.getBooleanExtra("Saturday", false));
				alarm.assign("repeat_sun", data.getBooleanExtra("Sunday", false));
				
				updateRepeatView(Alarm.formatRepeat(alarm.getAll()));
				break;
			case ACTION_INPUT_LABEL:
				
				String label = data.getStringExtra("label");
				
				alarm.assign("name", label);
				updateLabelView(label);
				break;
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		Calendar c = Calendar.getInstance(); 
		int minute;
		int hour;
		
		String timeDisplay = timeView.getText() + "";
		
		if (timeDisplay == "") {
			hour = c.get(Calendar.HOUR_OF_DAY);
			minute = c.get(Calendar.MINUTE);
		} else {
			hour = (Integer) alarm.getAll().get("hour");
			minute = (Integer) alarm.getAll().get("minute");
		}
	
		switch (id)
		{
		case DIALOG_PICK_TIME:
			return new TimePickerDialog(this, mOnTimeSetListener, hour, minute, false);
		}
		
		return super.onCreateDialog(id);
	}
	
	/*
	 * Listens for the 'OK' when user picks the time and update the view in the
	 * alarm edit screen to reflect the change.
	 */
	private TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		
			alarm.assign("hour", hourOfDay);
			alarm.assign("minute", minute);

			updateTimeView(Alarm.formatTime(hourOfDay, minute));
		}
	};
	
	/*
	 * Listens for user's key press to open the time dialog picker to select the alarm time.
	 */
	private View.OnClickListener timeOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			showDialog(DIALOG_PICK_TIME);
		}
	};
	
	/*
	 * Listens for user's key press to open the repeat day picker to select the alarm day.
	 */
	private View.OnClickListener repeatOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent i = new Intent(getBaseContext(), RepeatSelection.class);
			i.putExtra("days", repeatView.getText());
			startActivityForResult(i, ACTION_CHOOSE_REPEAT);
		}
	};

	/*
	 * Listens for user's key press to open the label edit UI.
	 */
	private View.OnClickListener labelOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent i = new Intent(AlarmEdit.this, StringInputDialog.class);
			i.putExtra("label", labelView.getText());
			startActivityForResult(i, ACTION_INPUT_LABEL);
		}
	};
	
	/*
	 * Listens for user's check to enable/disable FailSafe mode and indirectly
	 * enable/disable setting the snooze limit.
	 */
	private View.OnClickListener failSafeOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if (failSafe.isChecked()) {

				snoozeView.setVisibility(0);
				seekBar.setVisibility(0);
				seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
					}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						snoozeView.setText("Limit: " + progress);
						alarm.assign("snooze_value", progress);
					}
				});
			} else {
				seekBar.setVisibility(4);
				snoozeView.setVisibility(4);
			}
			
			alarm.assign("failsafe_on", failSafe.isChecked());
		}
	};
	
	/*
	 * Listens for user's check to enable/disable Wake-Up challenge mode.
	 */
	private View.OnClickListener challengeOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
		
			alarm.assign("wakeup_on", wakeUp.isChecked());
		}
	};
	
	/*
	 * Listens for user's key press to save all changes made and set the alarm.
	 */
	private View.OnClickListener saveOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			dbAdapter.saveAlarm(alarm.getAll());
			
			setResult(Activity.RESULT_OK);
			finish();
			
		}
	};
	
	/*
	 * Listens for user's key press to cancel all changes made in the alarm
	 * edit screen and return to the alarm main menu.
	 */
	private View.OnClickListener cancelOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {

			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	};
}
