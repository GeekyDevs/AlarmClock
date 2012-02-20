package com.geekydevs.alarmclock;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class AlarmClock extends ListActivity {
	
	private AlarmDBAdapter dbAdapter;
	private CursorAdapter mCurAdapter;
	
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
	
	private void doAlarmSet() {
    	Intent i = new Intent(this, AlarmService.class);
    	i.setAction(AlarmService.ACTION_SET_ALARM);
    	startService(i);
    }
	
	private class AlarmListAdapter extends CursorAdapter {
		
		public AlarmListAdapter(Context context, Cursor c){
			super(context, c);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			fillView(cursor, view);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			
			final LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.alarm_row, parent, false);
			return v;
		}
		
		private CheckBox.OnCheckedChangeListener checkList = new CheckBox.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				doAlarmSet();
			}
		};
		
		private void fillView(Cursor cur, View row) {
			
			((TextView)row.findViewById(R.id.ar_tv_alarm_time)).setText(Alarm.formatTime());

		}
	}
}