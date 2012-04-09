package com.geekydevs.alarmclock;

import java.util.Random;

import android.app.Activity;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Challenge extends Activity{

	private TextView operandAView;
	private TextView operandBView;
	private TextView operatorView;
	private EditText answerEdit;
	
	private static long[] pattern = {200, 500};
	
	private static String[] operators = new String[] {"+", "-", "*"};
	private static int MEDIUM_ADD_DIFFERENCE = 99;
	private static int MEDIUM_MULTIPLY_DIFFERENCE = 10;
	
	private int correctAnswer = 0;
	
	private MediaPlayer mediaPlayer;
	private AudioManager amanager;
	private Vibrator vibrate;
	
	private boolean soundOn = false;
	private boolean vibrateOn = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.challenge);
		
		findViews();
		
		amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		amanager.setStreamVolume(AudioManager.STREAM_ALARM, 20, 0);

		generateSoundVibrate();
		generateMedium();
		
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
		
		operandAView = (TextView)findViewById(R.id.operand_a);
		operandBView = (TextView)findViewById(R.id.operand_b);
		operatorView = (TextView)findViewById(R.id.operator);
		answerEdit = (EditText)findViewById(R.id.answer);
	}
	

	/*
	 * Checks the user's answer to verify if it is correct. If so, bring up the
	 * snooze/dismiss window, if not, return back to challenge screen.
	 */
	public void checkAnswer() {

		if (isInteger(answerEdit.getText().toString())) {
			if (Integer.parseInt(answerEdit.getText().toString()) == correctAnswer) {
				if (soundOn) {
					mediaPlayer.stop();
					mediaPlayer.release();
				}
				Intent i = new Intent(getBaseContext(), Snooze.class);
				if (vibrateOn) {
					vibrate.cancel();
					i.putExtra("vibrate", 1);
				} else {
					i.putExtra("vibrate", 0);
				}
				i.putExtra("sound", getIntent().getExtras().getString("sound"));
				if (getIntent().hasExtra("snooze_count"))
					i.putExtra("snooze_count", getIntent().getExtras().getInt("snooze_count"));
					i.putExtra("failsafe_on", 1);
				i.putExtra("challenge_on", 1);
				i.putExtra("notifOn", getIntent().getExtras().getBoolean("notifOn"));
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);	
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
}