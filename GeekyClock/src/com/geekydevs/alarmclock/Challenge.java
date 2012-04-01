package com.geekydevs.alarmclock;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Challenge extends Activity{

	private TextView operandAView;
	private TextView operandBView;
	private TextView operatorView;
	private EditText answerEdit;
	private Button solveButton;
	
	private static String[] operators = new String[] {"+", "-", "*"};
	private static int MEDIUM_ADD_DIFFERENCE = 99;
	private static int MEDIUM_MULTIPLY_DIFFERENCE = 10;
	
	private int correctAnswer = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.challenge);
		
		findViews();
		
		answerEdit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		solveButton.setOnClickListener(solveOnClick);
		
		generateMedium();
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
		
		solveButton = (Button)findViewById(R.id.check_answer);
	}
	

	/*
	 * Checks the user's answer to verify if it is correct. If so, bring up the
	 * snooze/dismiss window, if not, return back to challenge screen.
	 */
	private View.OnClickListener solveOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {

			int snooze_cnt = 0;
			if (getIntent().hasExtra("snooze_count"))
				snooze_cnt = getIntent().getExtras().getInt("snooze_count");
			
			int answer = Integer.parseInt(answerEdit.getText().toString());
			
			if (answer == correctAnswer) {
				Intent i = new Intent(getBaseContext(), Snooze.class);
				i.putExtra("sound", getIntent().getExtras().getString("sound"));
				i.putExtra("snooze_count", snooze_cnt);
				i.putExtra("challenge_on", 1);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);	
			}
		}
	};
	
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