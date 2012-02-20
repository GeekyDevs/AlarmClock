package com.geekydevs.alarmclock;

import java.util.Calendar;
import java.util.Arrays;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
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
	
	/*
	 * Called when the user clicks the 'Save' button in the alarm edit screen.
	 * Save any change
	 */
	private void setAlarm() {
		
		String seqDays = (String) repeatView.getText();
		
	    ArrayList<String> days = new ArrayList<String>(Arrays.asList(seqDays.split(", ")));
		
		//Alarm.ALARM_DEFAULTS.put("repeat_mon", days.contains("Monday"));
		
		//Intent i = new Intent(this, AlarmService.class);
		//i.setAction(AlarmService.ACTION_SET_ALARM);
		//startService(i);
		
		finish();
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
				updateRepeatView(data.getStringExtra("days"));
				break;
			case ACTION_INPUT_LABEL:
				updateLabelView(data.getStringExtra("label"));
				break;
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
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
	
	/*
	 * Listens for the 'OK' when user picks the time and update the view in the
	 * alarm edit screen to reflect the change.
	 */
	private TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		
			Alarm.assign("hour", hourOfDay);
			Alarm.assign("minute", minute);

			updateTimeView(Alarm.formatTime());
			//updateTimeView(Alarm.createTableStatement("alarm"));
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
						Alarm.ALARM_DEFAULTS_INT.put("snooze_limit", progress);
					}
				});
			} else {
				seekBar.setVisibility(4);
				snoozeView.setVisibility(4);
			}
			
			
		}
	};
	
	/*
	 * Listens for user's check to enable/disable Wake-Up challenge mode.
	 */
	private View.OnClickListener challengeOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
		
		}
	};
	
	/*
	 * Listens for user's key press to save all changes made and set the alarm.
	 */
	private View.OnClickListener saveOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			//setAlarm();
			
			ContentValues values = new ContentValues();
			/*
			for (String key : Alarm.ALARM_DEFAULTS_INT.keySet()) {
				if (!key.equals("ID")) {
					values.put(key, Alarm.ALARM_DEFAULTS_INT.get(key));
				}
			}
			values.put("name", (String)labelView.getText());
			
			String days = (String) repeatView.getText();
			
			values.put("repeat_mon", days.contains("Monday"));
			values.put("repeat_tue", days.contains("Tuesday"));
			values.put("repeat_wed", days.contains("Wednesday"));
			values.put("repeat_thu", days.contains("Thursday"));
			values.put("repeat_fri", days.contains("Friday"));
			values.put("repeat_sat", days.contains("Saturday"));
			values.put("repeat_sun", days.contains("Sunday"));
			values.put("failsafe_on", failSafe.isChecked());
			*/
			//values.put("wakeup_on", wakeUp.isChecked());
			
			//int i = dbAdapter.updateSettings(values, "ID=" + Alarm.ALARM_DEFAULTS_INT.get("ID"));
		}
	};
	
	/*
	 * Listens for user's key press to cancel all changes made in the alarm
	 * edit screen and return to the alarm main menu.
	 */
	private View.OnClickListener cancelOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			setResult(Activity.RESULT_OK);
			finish();
		}
	};
}
