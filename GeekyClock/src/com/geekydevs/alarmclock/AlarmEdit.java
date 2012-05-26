package com.geekydevs.alarmclock;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AlarmEdit extends Activity {

	private static final int DIALOG_PICK_TIME = 1;
	
	private static final int ACTION_INPUT_LABEL = 1;
	private static final int ACTION_CHOOSE_SOUND = 2;
	private static final int ACTION_CHOOSE_REPEAT = 3;
	private static final int ACTION_CHOOSE_LEVEL = 4;
	
	private static final int OFF_COLOR = Color.DKGRAY;
	private static final int ON_COLOR = Color.WHITE;
	
	private Button saveButton;
	private Button cancelButton;
	
	private LinearLayout lLRepeat;
	private LinearLayout lLTime;
	private LinearLayout lLLabel;
	private LinearLayout lLSound;
	
	private LinearLayout lLFailSafe;
	private LinearLayout lLChallenge;
	private LinearLayout lLDifficulty;
	private LinearLayout lLVibrate;
	
	private CheckBox failSafe;
	private SeekBar seekBar;
	private CheckBox wakeUp;
	private CheckBox vibrate;
	
	private TextView timeView;
	private TextView repeatView;
	private TextView labelView;
	private TextView snoozeView;
	private TextView snoozeLabel;
	private TextView difficultyLabel;
	private TextView difficultyView;
	private TextView soundView;
	
	private AlarmDBAdapter dbAdapter;
	
	private Alarm alarm;
	private Context ctx;
	
	private Boolean newAlarm = true;
	//private Boolean notifOn = false;
	
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
	 * Cancels any changes made if back key has been pressed.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    
		long id;
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	
	    	if (newAlarm) {
	    		id = (Integer) alarm.getAll().get("_id");
	    		dbAdapter.deleteAlarm((Long) id);
	    	}
	    	
	    	dbAdapter.close();
	    	finish();
			
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}

	
	/*
	 * Assigns each item in the UI that requires interaction to the proper 
	 * variable.
	 */
	private void findViews() {
		
		lLLabel = (LinearLayout)findViewById(R.id.ll_label);
		
		lLRepeat = (LinearLayout)findViewById(R.id.ea_ll_alarm_repeat);
		lLTime = (LinearLayout)findViewById(R.id.ea_ll_alarm_time);
	
		lLFailSafe = (LinearLayout)findViewById(R.id.failsafe_section);
		lLChallenge = (LinearLayout)findViewById(R.id.challenge_section);
		lLVibrate = (LinearLayout)findViewById(R.id.vibrate_section);
		
		timeView = (TextView)findViewById(R.id.time_selection);
		repeatView = (TextView)findViewById(R.id.repeat_selection);
		labelView = (TextView)findViewById(R.id.label_view);
		
		snoozeLabel = (TextView)findViewById(R.id.snooze_label);
		snoozeView = (TextView)findViewById(R.id.snooze_limit);
		snoozeView.setVisibility(TextView.GONE);
		
		failSafe = (CheckBox)findViewById(R.id.chk_failsafe);
		wakeUp = (CheckBox)findViewById(R.id.chk_challenge);
		
		seekBar = (SeekBar)findViewById(R.id.seekbar);
		seekBar.setMax(10);
		seekBar.setVisibility(SeekBar.GONE);
		
		lLDifficulty = (LinearLayout)findViewById(R.id.difficulty_section);
		difficultyLabel = (TextView)findViewById(R.id.difficulty_label);
		difficultyView = (TextView)findViewById(R.id.difficulty_level);
		
		vibrate = (CheckBox)findViewById(R.id.chk_alarm_vibrate);
		lLSound = (LinearLayout)findViewById(R.id.ll_alarm_sound);
		soundView = (TextView)findViewById(R.id.sound_pick);
		
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
		lLFailSafe.setOnClickListener(failSafeScreenOnClick);
		
		seekBar.setOnSeekBarChangeListener(bar);
		wakeUp.setOnClickListener(challengeOnClick);
		lLChallenge.setOnClickListener(challengeScreenOnClick);
		lLDifficulty.setOnClickListener(difficultyScreenOnClick);
		
		lLSound.setOnClickListener(soundOnClick);
		
		vibrate.setOnClickListener(vibrateOnClick);
		lLVibrate.setOnClickListener(vibrateScreenOnClick);
		
		saveButton.setOnClickListener(saveOnClick);
		cancelButton.setOnClickListener(cancelOnClick);
	}
	
	private void loadAlarmFromIntent() {
		Intent i = getIntent();
		if (i.hasExtra(Alarm.PACKAGE_PREFIX + "._id")){
			
			newAlarm = false;
			
			Bundle b = getIntent().getExtras();
			int id = (int) b.getLong(Alarm.PACKAGE_PREFIX + "._id");

			alarm = dbAdapter.getAlarmById(id);
			
			ContentValues values = alarm.getAll();
			
			updateTimeView(Alarm.formatTime((Integer) values.get("hour"), (Integer) values.get("minute")));
			updateRepeatView(Alarm.formatRepeat(alarm.getAll()));
			updateLabelView(values.get("name") + "");
			
			updateSoundView(values.get("sound") + "");
			
			if ((Boolean) values.get("failsafe_on")) {
				snoozeLabel.setTextColor(ON_COLOR);
				snoozeView.setVisibility(TextView.VISIBLE);
				snoozeView.setText("Limit: " + values.get("snooze_value"));
				seekBar.setVisibility(SeekBar.VISIBLE);
			} else {
				seekBar.setProgress(5);
			}
			failSafe.setChecked((Boolean) values.get("failsafe_on"));
			seekBar.setProgress((Integer) values.get("snooze_value"));
			
			if ((Boolean) values.get("wakeup_on")) {
				updateDifficultyView(values.get("challenge_level") + "");
				difficultyView.setVisibility(TextView.VISIBLE);
				difficultyLabel.setTextColor(ON_COLOR);
			}

			wakeUp.setChecked((Boolean) values.get("wakeup_on"));
			vibrate.setChecked((Boolean) values.get("vibrate_on"));
			
		} else {
			Bundle b = getIntent().getExtras();
			
			alarm = new Alarm(dbAdapter.getNewId());
			dbAdapter.initialise(alarm);
			
			Calendar c = Calendar.getInstance();
			timeView.setText(Alarm.formatTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
			updateSoundView(alarm.getAll().get("sound") + "");
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
	
	private void updateDifficultyView (String level) {
		difficultyView.setText(level);
	}
	
	private void updateSoundView(String sound) {
		soundView.setText(sound);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ACTION_INPUT_LABEL:
				
				String label = data.getStringExtra(Alarm.PACKAGE_PREFIX + ".label");
				
				alarm.assign("name", label);
				updateLabelView(label);
				break;
			case ACTION_CHOOSE_REPEAT:
				
				alarm.assign("repeat_sun", data.getBooleanExtra(Alarm.PACKAGE_PREFIX + ".Sunday", false));
				alarm.assign("repeat_mon", data.getBooleanExtra(Alarm.PACKAGE_PREFIX + ".Monday", false));
				alarm.assign("repeat_tue", data.getBooleanExtra(Alarm.PACKAGE_PREFIX + ".Tuesday", false));
				alarm.assign("repeat_wed", data.getBooleanExtra(Alarm.PACKAGE_PREFIX + ".Wednesday", false));
				alarm.assign("repeat_thu", data.getBooleanExtra(Alarm.PACKAGE_PREFIX + ".Thursday", false));
				alarm.assign("repeat_fri", data.getBooleanExtra(Alarm.PACKAGE_PREFIX + ".Friday", false));
				alarm.assign("repeat_sat", data.getBooleanExtra(Alarm.PACKAGE_PREFIX + ".Saturday", false));

				updateRepeatView(Alarm.formatRepeat(alarm.getAll()));
				break;
			case ACTION_CHOOSE_LEVEL:
				
				String difficulty = data.getStringExtra(Alarm.PACKAGE_PREFIX + ".level");
				alarm.assign("challenge_level", difficulty);
				updateDifficultyView(difficulty);
				break;
			case ACTION_CHOOSE_SOUND:
				
				String sound = data.getStringExtra(Alarm.PACKAGE_PREFIX + ".sound");
				alarm.assign("sound", sound);
				updateSoundView(sound);
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
			i.putExtra(Alarm.PACKAGE_PREFIX + ".days", repeatView.getText());
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
			i.putExtra(Alarm.PACKAGE_PREFIX + ".label", labelView.getText());
			startActivityForResult(i, ACTION_INPUT_LABEL);
		}
	};
	
	/*
	 * Listens for user's key press to open the label edit UI.
	 */
	private View.OnClickListener soundOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent i = new Intent(AlarmEdit.this, SoundSelection.class);
			i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", soundView.getText());
			startActivityForResult(i, ACTION_CHOOSE_SOUND);
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
				snoozeLabel.setTextColor(ON_COLOR);
				snoozeView.setVisibility(TextView.VISIBLE);
				seekBar.setVisibility(SeekBar.VISIBLE);
				if (newAlarm) {
					seekBar.setProgress(5);
				}
			} else {
				snoozeLabel.setTextColor(OFF_COLOR);
				seekBar.setVisibility(SeekBar.GONE);
				snoozeView.setVisibility(TextView.GONE);
			}
			
			alarm.assign("failsafe_on", failSafe.isChecked());
		}
	};
	
	private View.OnClickListener failSafeScreenOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if (failSafe.isChecked()) {
				failSafe.setChecked(false);
				seekBar.setVisibility(SeekBar.GONE);
				snoozeLabel.setTextColor(OFF_COLOR);
				snoozeView.setVisibility(TextView.GONE);
			} else {
				failSafe.setChecked(true);
				snoozeLabel.setTextColor(ON_COLOR);
				snoozeView.setVisibility(TextView.VISIBLE);
				seekBar.setVisibility(SeekBar.VISIBLE);
				if (newAlarm) {
					seekBar.setProgress(5);
				}
			}
			
			alarm.assign("failsafe_on", failSafe.isChecked());
		}
	};
	
	/*
	 * Listens for user's bar slide or key press on the snooze bar to get retrieve the
	 * updated value.
	 */
	private SeekBar.OnSeekBarChangeListener bar = new SeekBar.OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			snoozeView.setText("Limit: " + progress);
			alarm.assign("snooze_value", progress);
		}
	};
	
	/*
	 * Listens for user's check to enable/disable Wake-Up challenge mode.
	 */
	private View.OnClickListener challengeOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
		
			alarm.assign("wakeup_on", wakeUp.isChecked());
			if (wakeUp.isChecked()) {
				difficultyView.setVisibility(View.VISIBLE);
				difficultyLabel.setTextColor(ON_COLOR);
			} else {
				difficultyView.setVisibility(View.GONE);
				difficultyLabel.setTextColor(OFF_COLOR);
			}
		}
	};
	
	private View.OnClickListener challengeScreenOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
		
			
			wakeUp.setChecked(!wakeUp.isChecked());
			if (wakeUp.isChecked()) {
				difficultyView.setVisibility(View.VISIBLE);
				difficultyLabel.setTextColor(ON_COLOR);
			} else {
				difficultyView.setVisibility(View.GONE);
				difficultyLabel.setTextColor(OFF_COLOR);
			}
			alarm.assign("wakeup_on", wakeUp.isChecked());
		}
	};
	
	private View.OnClickListener difficultyScreenOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (wakeUp.isChecked()) {
				Intent i = new Intent(AlarmEdit.this, DifficultySelection.class);
				i.putExtra(Alarm.PACKAGE_PREFIX + ".level", difficultyView.getText());
				startActivityForResult(i, ACTION_CHOOSE_LEVEL);	
			}
		}
	};
	
	/*
	 * Listens for user's check to enable/disable Wake-Up challenge mode.
	 */
	private View.OnClickListener vibrateOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
		
			alarm.assign("vibrate_on", vibrate.isChecked());
		}
	};
	
	private View.OnClickListener vibrateScreenOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
		
			vibrate.setChecked(!vibrate.isChecked());
			alarm.assign("vibrate_on", vibrate.isChecked());
		}
	};
	
	/*
	 * Listens for user's key press to save all changes made and set the alarm.
	 */
	private View.OnClickListener saveOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			dbAdapter.saveAlarm(alarm.getAll());
			
			int id = (Integer) alarm.getAll().get("_id");
			dbAdapter.setAlarmToDB(id, true);
			
			if (newAlarm) {
				setUpAlarm(id);	
			} else {
				if (dbAdapter.fetchEnabledById(id)) {
					setUpAlarm(id);
				}
			}
			
			dbAdapter.close();
			
			/*
			Intent i = new Intent(getBaseContext(), AlarmService.class);
			i.setAction(AlarmService.ACTION_SHOW_NOTIF);
			startService(i);
			*/
	
			setResult(Activity.RESULT_OK);
			finish();
			
		}
	};
	
	private void setUpAlarm(int id) {
		
		Intent i = new Intent(this, AlarmService.class);
		i.setAction(AlarmService.ACTION_SET_ALARM);
		i.putExtra(Alarm.PACKAGE_PREFIX + "._id", id);
		startService(i);
	}
	
	/*
	 * Listens for user's key press to cancel all changes made in the alarm
	 * edit screen and return to the alarm main menu.
	 */
	private View.OnClickListener cancelOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if (newAlarm) {
				long id = (Integer) alarm.getAll().get("_id");
				dbAdapter.deleteAlarm((Long) id);
			}
			
			dbAdapter.close();
			
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	};
}
