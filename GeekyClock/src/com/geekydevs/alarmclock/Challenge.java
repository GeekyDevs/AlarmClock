package com.geekydevs.alarmclock;

import java.util.Calendar;
import java.util.Random;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
	
	private static String[] operators = new String[] {"+", "-", "*"};
	private static int MEDIUM_ADD_DIFFERENCE = 99;
	private static int MEDIUM_MULTIPLY_DIFFERENCE = 10;
	private static final int SNOOZE_INTERVAL = 5;
	
	private int correctAnswer = 0;
	
	private MediaPlayer mediaPlayer;
	private AudioManager amanager;
	private Vibrator vibrate;
	
	private boolean isNativeSnooze = true;
	private boolean soundOn = false;
	private boolean vibrateOn = false;
	
	private int snooze_flag = 0;
	private int snooze_remaining;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.challenge);
		
		findViews();
		
		refreshImage.setOnClickListener(refreshOnClick);
		snoozeButton.setOnClickListener(snoozeOnClick);
		dismissBar.setOnSeekBarChangeListener(bar);
		dismissBar.setProgress(1);
		
		amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		amanager.setStreamVolume(AudioManager.STREAM_ALARM, 20, 0);

		generateSoundVibrate();
		generateMedium();
		
		int snooze_cnt = 0;
		
		if (getIntent().hasExtra("snooze_count"))
			snooze_cnt = getIntent().getExtras().getInt("snooze_count");
		
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
	
	/*
	 * Assigns each item in the UI that requires interaction to the proper 
	 * variable.
	 */
	private void findViews() {
		
		refreshImage = (ImageView)findViewById(R.id.refresh);
		refreshImage.setImageDrawable(getResources().getDrawable(R.drawable.refresh));
		
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
			generateMedium();
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
			i.putExtra("sound", getIntent().getExtras().getString("sound"));
			i.putExtra("challenge_on", 1);
			

			if (vibrateOn) {
				vibrate.cancel();
				i.putExtra("vibrate", 1);
			} else {
				i.putExtra("vibrate", 0);
			}

			if (!isNativeSnooze) {
				i.putExtra("failsafe_on", 1);
				i.putExtra("snooze_count", snooze_remaining - 1);
			}

			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					getBaseContext(), 0, i, snooze_flag);
			
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
				
				Intent i = new Intent(getBaseContext(), AlarmService.class);
				i.setAction(AlarmService.ACTION_STOP_ALARM);
				startService(i);

				Intent j = new Intent(getBaseContext(), AlarmService.class);
				j.setAction(AlarmService.ACTION_SHOW_NOTIF);
				startService(j);
				
				Intent k = new Intent(getBaseContext(), AlarmService.class);
				k.setAction(AlarmService.ACTION_SET_ALARM);;
				startService(k);
				
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
				refreshImage.setVisibility(ImageView.INVISIBLE);
				
				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(answerEdit.getWindowToken(), 0);
				
				if (snooze_remaining > 0) {
					snoozeRemaining.setVisibility(TextView.VISIBLE);
					snoozeRemaining.setText("Snooze Remaining: " + snooze_remaining);
				}
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
		
		String sound = getIntent().getExtras().getString("sound");
		if (!sound.equals("Silent")) {
			soundOn = true;
			if (sound.equals("Default")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.normal);
			} else if (sound.equals("C'mon Man")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.cmon_man);
			} else if (sound.equals("Red Alert")) {
				mediaPlayer = MediaPlayer.create(this, R.raw.red_alert);
			}
		}

		if (soundOn) {
			mediaPlayer.start();
			mediaPlayer.setLooping(true);
		}
		
		if (getIntent().getExtras().getInt("vibrate") > 0) {
			vibrateOn = true;
			vibrate = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			vibrate.vibrate(pattern, 0);
		}
	}
	
	private void generateEasy() {
			
	}
	
	private void generateMedium() {

		Random r = new Random();
		
		String operator = operators[r.nextInt(operators.length)];
		int operandA;
		int operandB = 0;
		
		operandA = r.nextInt(MEDIUM_ADD_DIFFERENCE * 2 + 1) - MEDIUM_ADD_DIFFERENCE;
		
		if (operator == "+") {
			operandB = r.nextInt(MEDIUM_ADD_DIFFERENCE * 2 + 1) - MEDIUM_ADD_DIFFERENCE;
			correctAnswer = operandA + operandB;
		} 
		else if (operator == "-") 
		{
			operandB = r.nextInt(MEDIUM_ADD_DIFFERENCE * 2 + 1) - MEDIUM_ADD_DIFFERENCE;
			correctAnswer = operandA - operandB;
		} 
		else if (operator == "*") 
		{
			operandB = r.nextInt(MEDIUM_MULTIPLY_DIFFERENCE * 2 + 1) - MEDIUM_MULTIPLY_DIFFERENCE;
			correctAnswer = operandA * operandB;
		}
		
		operandAView.setText(operandA + "");
		operandBView.setText(operandB + "");
		operatorView.setText(operator + "");	
	}
	
	private void generateHard() {
	
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