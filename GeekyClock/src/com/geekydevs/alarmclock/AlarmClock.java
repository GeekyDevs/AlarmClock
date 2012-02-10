package com.geekydevs.alarmclock;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AlarmClock extends ListActivity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        assignListeners();
        
    }
    
    private void assignListeners() {
    	
    	((Button)findViewById(R.id.m_btn_add_new)).setOnClickListener(onAddNewAlarmClick);
    }
    
    private final View.OnClickListener onAddNewAlarmClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			startActivity(new Intent(getBaseContext(), AlarmEdit.class));
		}
		
	};
}