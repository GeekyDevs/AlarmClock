package com.geekydevs.alarmclock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class StringInputDialog extends Activity {

	private EditText labelText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.string_input);
		setTitle("Label");
		
		labelText = (EditText) findViewById(R.id.edit_input);
		findViewById(R.id.ok_label).setOnClickListener(okOnClick);
		findViewById(R.id.cancel_label).setOnClickListener(cancelOnClick);
		
		Bundle b = getIntent().getExtras();
		
		if (b.getString(Alarm.PACKAGE_PREFIX + ".label").contains("Enter a custom label for your alarm")) {
			labelText.setText("");
		} else {
			labelText.setText(b.getString(Alarm.PACKAGE_PREFIX + ".label"));
		}
	}
	
	private Button.OnClickListener okOnClick = new Button.OnClickListener() {
	
		@Override
		public void onClick(View v) {
			
			Intent i = new Intent();
			i.putExtra(Alarm.PACKAGE_PREFIX + ".label", labelText.getText() + "");
			
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
