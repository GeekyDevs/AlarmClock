package com.geekydevs.alarmclock;

import java.util.Calendar;
import java.util.Random;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class Challenge extends Activity{

	private TextView operandAView;
	private TextView operandBView;
	private TextView operatorView;
	private EditText answerEdit;
	
	private ImageView refreshImage;
	
	private Button snoozeButton;
	private TextView snoozeRemaining;
	private SeekBar dismissBar;
	
	private static long[] pattern = {200, 500};
	
	private static String[] operators = new String[] {"+", "-", "*", "/"};
	private static int EASY_ADD_DIFFERENCE = 20;
	private static int MEDIUM_ADD_DIFFERENCE = 99;
	private static int MEDIUM_MULTIPLY_DIFFERENCE = 10;
	private static int DIVISOR_DIFFERENCE = 9;
	private static int QUOTIENT_DIFFERENCE = 12;
	private static final int SNOOZE_INTERVAL = 5;
	
	private int correctAnswer = 0;
	private int correctOperandAAnswer = 0;
	private int correctOperandBAnswer = 0;
	
	private MediaPlayer mediaPlayer;
	private AudioManager amanager;
	private Vibrator vibrate;
	
	private boolean isNativeSnooze = true;
	private boolean soundOn = false;
	private boolean vibrateOn = false;
	private boolean isHard = false;
	private boolean canRefresh = true;
	
	private int snooze_flag = 0;
	private int snooze_remaining;
	
	private int oldVolumeIndex = 0;
	private int id = -1;
	
	private String difficultyLevel;
	
	private WakeLock wakeLock;
	private KeyguardLock keyguardLock;
	
	private AlarmDBAdapter dbAdapter;
	private boolean repeatFlag = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.challenge);
		
		dbAdapter = new AlarmDBAdapter(getBaseContext());
		dbAdapter.open();
		
		WakeLocker.acquire(getBaseContext());
		
		findViews();
		
		refreshImage.setOnClickListener(refreshOnClick);
		snoozeButton.setOnClickListener(snoozeOnClick);
		dismissBar.setOnSeekBarChangeListener(bar);
		dismissBar.setProgress(1);
		
		amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		oldVolumeIndex = amanager.getStreamVolume(AudioManager.STREAM_MUSIC);
		amanager.setStreamVolume(AudioManager.STREAM_MUSIC, amanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);

		generateSoundVibrate();

		id = getIntent().getExtras().getInt(Alarm.PACKAGE_PREFIX + ".id");
		repeatFlag = getIntent().getExtras().getBoolean(Alarm.PACKAGE_PREFIX + ".has_repeat");
		difficultyLevel = getIntent().getExtras().getString(Alarm.PACKAGE_PREFIX + ".challenge_level");
		
		if (difficultyLevel.equals("Easy")) {
			generateEasy();
		} else if (difficultyLevel.equals("Medium")) {
			generateMedium();
		} else if (difficultyLevel.equals("Hard")) {
			isHard = true;
			generateHard();
		}
			
		int snooze_cnt = 0;
		
		if (getIntent().hasExtra(Alarm.PACKAGE_PREFIX + ".snooze_count"))
			snooze_cnt = getIntent().getExtras().getInt(Alarm.PACKAGE_PREFIX + ".snooze_count");
		
		if (snooze_cnt > 0) {
			isNativeSnooze = false;
			snooze_flag = PendingIntent.FLAG_UPDATE_CURRENT;
			snooze_remaining = snooze_cnt;
		}
		
		answerEdit.addTextChangedListener (new TextWatcher(){
			
			public void afterTextChanged(Editable txt) {
				checkAnswer();
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        
			public void onTextChanged(CharSequence s, int start, int before, int count){}

		});
	}
	

	@Override
	public void onResume() {
		try {
			Log.v("On resume called","------ WakeLocker aquire next!");
			WakeLocker.acquire();
		}catch(Exception ex){

		}

		super.onResume();
	}
	
	@Override
	protected void onPause() {

		try {
			Log.v("on pause called", "on pause called");
			WakeLocker.release();
		}catch(Exception ex){
			Log.e("Exception in on menu", "exception on menu");
		}
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		try {
			Log.v("on stopped called", "on stopped called");
			WakeLocker.release();
		}catch(Exception ex){
			Log.e("Exception in on menu", "exception on menu");
		}
		super.onStop();
	}
	
	@Override
	public void onDestroy() {

		try {
			Log.v("on destroy called", "on destroy called");
			WakeLocker.release();
			WakeLocker.exit();
		}catch(Exception ex){
			Log.e("Exception in on menu", "exception on menu");
		}
		
		dbAdapter.close();
		super.onDestroy();
	}
	
	/*
	 * Assigns each item in the UI that requires interaction to the proper 
	 * variable.
	 */
	private void findViews() {
		
		refreshImage = (ImageView)findViewById(R.id.refresh);
		refreshImage.setImageDrawable(getResources().getDrawable(R.drawable.refresh_on));
		
		operandAView = (TextView)findViewById(R.id.operand_a);
		operandBView = (TextView)findViewById(R.id.operand_b);
		operatorView = (TextView)findViewById(R.id.operator);
		answerEdit = (EditText)findViewById(R.id.answer);
		
		snoozeButton = (Button) findViewById(R.id.snooze_challenge_button);
		snoozeRemaining = (TextView)findViewById(R.id.snooze_challenge_remaining); 
		dismissBar = (SeekBar) findViewById(R.id.dismiss_challenge_bar);
	}

	private ImageView.OnClickListener refreshOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if (canRefresh) {
				if (difficultyLevel.equals("Easy")) {
					generateEasy();
				} else if (difficultyLevel.equals("Medium")) {
					generateMedium();
				} else if (difficultyLevel.equals("Hard")) {
					isHard = true;
					generateHard();
				}
			}
		}
	};

	private Button.OnClickListener snoozeOnClick = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {

			if (soundOn) {
				mediaPlayer.stop();
				mediaPlayer.release();
			}

			Intent i = new Intent(getBaseContext(), AlarmReceiver.class);
			i.putExtra(Alarm.PACKAGE_PREFIX + ".sound", getIntent().getExtras().getString(Alarm.PACKAGE_PREFIX + ".sound"));
			i.putExtra(Alarm.PACKAGE_PREFIX + ".challenge_on", 1);
			i.putExtra(Alarm.PACKAGE_PREFIX + ".challenge_level", difficultyLevel);
			i.putExtra(Alarm.PACKAGE_PREFIX + ".id", id);
			

			if (vibrateOn) {
				vibrate.cancel();
				i.putExtra(Alarm.PACKAGE_PREFIX + ".vibrate", 1);
			} else {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".vibrate", 0);
			}

			if (!isNativeSnooze) {
				i.putExtra(Alarm.PACKAGE_PREFIX + ".failsafe_on", 1);
				i.putExtra(Alarm.PACKAGE_PREFIX + ".snooze_count", snooze_remaining - 1);
			}

			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					getBaseContext(), id, i, snooze_flag);
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.SECOND, SNOOZE_INTERVAL);
			
	        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
	        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);	
			
			finish();
		}
	};
	
	/*
	 * Cancel the alarm entirely and release all resources tied to the alarm.
	 */
	private SeekBar.OnSeekBarChangeListener bar = new SeekBar.OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			
			if (seekBar.getProgress() < seekBar.getMax()) {
				seekBar.setProgress(1);
			}
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

			if (progress == seekBar.getMax()) {
				
				if (soundOn) {
					mediaPlayer.stop();
					mediaPlayer.release();
				}
				if (vibrateOn) {
					vibrate.cancel();
				}
				
				amanager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolumeIndex, 0);
				
				Intent i = new Intent(getBaseContext(), AlarmService.class);
				i.putExtra(Alarm.PACKAGE_PREFIX + ".id", id);
				i.setAction(AlarmService.ACTION_STOP_ALARM);
				startService(i);
	
				if (!repeatFlag) {
					dbAdapter.setAlarmToDB(id, false);
				}
				
				finish();
			}
		}
	};
	
	/*
	 * Checks the user's answer to verify if it is correct. If so, bring up the
	 * snooze/dismiss window, if not, return back to challenge screen.
	 */
	public void checkAnswer() {

		if (isInteger(answerEdit.getText().toString())) {
			if (Integer.parseInt(answerEdit.getText().toString()) == correctAnswer) {
				
				snoozeButton.setVisibility(Button.VISIBLE);
				dismissBar.setVisibility(SeekBar.VISIBLE);
				refreshImage.setImageDrawable(getResources().getDrawable(R.drawable.refresh_off));
				canRefresh = false;
				
				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(answerEdit.getWindowToken(), 0);
				
				if (snooze_remaining > 0) {
					snoozeRemaining.setVisibility(TextView.VISIBLE);
					snoozeRemaining.setText("Snooze Remaining: " + snooze_remaining);
				}
			} else {
				
				snoozeButton.setVisibility(Button.GONE);
				dismissBar.setVisibility(SeekBar.GONE);
				snoozeRemaining.setVisibility(TextView.GONE);
				refreshImage.setImageDrawable(getResources().getDrawable(R.drawable.refresh_on));
				canRefresh = true;
			}
		} 
	}
	
	private boolean isInteger(String value){
		
		try {
	        Integer.parseInt( value );
	        return true;
	    }
	    catch( Exception e ) {
	        return false;
	    }
	}
	
	private void generateSoundVibrate() {
		
		String sound = getIntent().getExtras().getString(Alarm.PACKAGE_PREFIX + ".sound");
		if (!sound.equals("Silent")) {
			soundOn = true;
			if (sound.equals("Default")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.default_ring);
			} else if (sound.equals("Digital")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.digital_alarm);
			} else if (sound.equals("Rooster")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.rooster);
			} else if (sound.equals("Trumpet")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.trumpet_alarm);
			} else if (sound.equals("Awaken")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.awaken);
			} else if (sound.equals("Red Alert")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.red_alert);
			} else if (sound.equals("Buzz")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.buzz_alert);
			}
		}

		if (soundOn) {
			mediaPlayer.start();
			mediaPlayer.setLooping(true);
		}
		
		if (getIntent().getExtras().getInt(Alarm.PACKAGE_PREFIX + ".vibrate") > 0) {
			vibrateOn = true;
			vibrate = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			vibrate.vibrate(pattern, 0);
		}
	}
	
	private void generateEasy() {
			
		Random r = new Random();
		
		String operator = operators[r.nextInt(operators.length - 2)];

		int operandA = r.nextInt(EASY_ADD_DIFFERENCE * 2 + 1) - EASY_ADD_DIFFERENCE;
		while (operandA == 0) {
			operandA = r.nextInt(EASY_ADD_DIFFERENCE * 2 + 1) - EASY_ADD_DIFFERENCE;
		}
		
		int operandB = r.nextInt(EASY_ADD_DIFFERENCE * 2 + 1) - EASY_ADD_DIFFERENCE;
		while (operandB == 0) {
			operandB = r.nextInt(EASY_ADD_DIFFERENCE * 2 + 1) - EASY_ADD_DIFFERENCE;
		}
		
		if (operator == "+") {
			if (isHard) {
				correctOperandBAnswer = operandA + operandB;
			} else {
				correctAnswer = operandA + operandB;
			}
		} 
		else if (operator == "-") 
		{
			if (isHard) {
				correctOperandBAnswer = operandA - operandB;
			} else {
				correctAnswer = operandA - operandB;
			}
		}
		
		if (isHard) {
			operandBView.setText("(" + operandA + " " + operator + " " + operandB + ")");
		} else {
			operandAView.setText(operandA + "");
			operandBView.setText(operandB + "");
			operatorView.setText(operator + "");
		}
	}
	
	private void generateMedium() {

		Random r = new Random();
		
		String operator = operators[r.nextInt(operators.length - 1)];
		
		int operandA = r.nextInt(MEDIUM_ADD_DIFFERENCE * 2 + 1) - MEDIUM_ADD_DIFFERENCE;
		int operandB = 0;
		
		if (operator == "+") {
			operandB = r.nextInt(MEDIUM_ADD_DIFFERENCE * 2 + 1) - MEDIUM_ADD_DIFFERENCE;
			if (isHard) {
				correctOperandAAnswer = operandA + operandB;
			} else {
				correctAnswer = operandA + operandB;
			}
		} 
		else if (operator == "-") 
		{
			operandB = r.nextInt(MEDIUM_ADD_DIFFERENCE * 2 + 1) - MEDIUM_ADD_DIFFERENCE;
			if (isHard) {
				correctOperandAAnswer = operandA - operandB;
			} else {
				correctAnswer = operandA - operandB;
			}
		} 
		else if (operator == "*") 
		{
			operandB = r.nextInt(MEDIUM_MULTIPLY_DIFFERENCE * 2 + 1) - MEDIUM_MULTIPLY_DIFFERENCE;
			if (isHard) {
				correctOperandAAnswer = operandA * operandB;
			} else {
				correctAnswer = operandA * operandB;
			}
		}
		
		if (isHard) {
			operandAView.setText("(" + operandA + " " + operator + " " + operandB + ")");
		} else {
			operandAView.setText(operandA + "");
			operandBView.setText(operandB + "");
			operatorView.setText(operator + "");
		}
	}
	
	private void generateHard() {
	
		Random r = new Random();
		
		String operator = operators[r.nextInt(operators.length)];
		while (operator == "*") {
			operator = operators[r.nextInt(operators.length)];
		}
		int operandA = 0;
		int operandB = 0;

		if ((operator == "+") || (operator == "-")) {
			generateMedium();
			operatorView.setText(operator + "");
			generateEasy();
			
			if (operator == "+") {
				correctAnswer = correctOperandAAnswer + correctOperandBAnswer;
			} else if (operator == "-") {
				correctAnswer = correctOperandAAnswer - correctOperandBAnswer;
			}
			
		} else if (operator == "/") {
			
			int quotient = r.nextInt(QUOTIENT_DIFFERENCE * 2 + 1) - QUOTIENT_DIFFERENCE;
			while (quotient == 0) {
				quotient = r.nextInt(QUOTIENT_DIFFERENCE * 2 + 1) - QUOTIENT_DIFFERENCE;
			}
			while (operandB == 0) {
				operandB = r.nextInt(DIVISOR_DIFFERENCE * 2 + 1) - DIVISOR_DIFFERENCE;
			}
			operandA = operandB * quotient;
	
			correctAnswer = quotient;
			
			operandAView.setText(operandA + "");
			operatorView.setText(operator);
			operandBView.setText(operandB + "");
		}
	}
	
	/*
	 * Disable the user from exiting the failsafe screen.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) ||
			(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
			return true;
		}

		return super.onKeyDown(keyCode, event);	
	}
	
	@Override
	public void onBackPressed() {
		//do nothing
	}
	
	@Override
	public void onAttachedToWindow() {
	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	    super.onAttachedToWindow();
	}
}