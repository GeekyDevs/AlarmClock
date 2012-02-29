package com.geekydevs.alarmclock;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmClock extends ListActivity {
	
	private AlarmDBAdapter dbAdapter;
	private CursorAdapter curAdapter;
	Alarm alarm;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        dbAdapter = new AlarmDBAdapter(this);
        dbAdapter.open();
        
        Cursor c = dbAdapter.fetchAllAlarms();
        
        startManagingCursor(c);
        curAdapter = new AlarmListAdapter(this, c);
        
        assignListeners();
        
    }
    
    private void assignListeners() {
    	
    	ListView listV = getListView();
    	listV.setAdapter(curAdapter);
    	
    	((Button)findViewById(R.id.m_btn_add_new)).setOnClickListener(onAddNewAlarmClick);
    }
    
    /*
     * Opens the edit screen 
     */
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		launchAlarmEdit(id);
		super.onListItemClick(l, v, position, id);
	}
 
    private void launchAlarmEdit(long id) {
    	Intent i = new Intent(this, AlarmEdit.class);
		i.putExtra("_id", id);
		startActivity(i);
    }
    
    /*
     * Launches the alarm edit screen when the user adds a new alarm.  Default values will
     */
    private final View.OnClickListener onAddNewAlarmClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			startActivity(new Intent(getBaseContext(), AlarmEdit.class));
		}
		
	};
	
	/*
	 * Adapter that exposes data from a Cursor to a ListView widget.
	 */
	private class AlarmListAdapter extends CursorAdapter {
		
		public AlarmListAdapter(Context context, Cursor c){
			super(context, c);
		}
		
		/*
		 * Bind an existing view to the data pointed to by cursor
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			ImageView alarmImage = (ImageView) view.findViewById(R.id.alarm_icon);
			TextView timeText = (TextView) view.findViewById(R.id.time_row);
			TextView repeatText = (TextView) view.findViewById(R.id.repeat_row);
			TextView labelText = (TextView) view.findViewById(R.id.label_row);
			
			TextView failSafeText = (TextView) view.findViewById(R.id.failsafe_row);
			TextView snoozeText = (TextView) view.findViewById(R.id.snooze_row);
			TextView wakeUpText = (TextView) view.findViewById(R.id.wakeup_row);

			String time = Alarm.formatTime(cursor.getInt(1), cursor.getInt(2));
			ContentValues values = new ContentValues();
			
			values.put("repeat_mon", cursor.getInt(4) > 0);
			values.put("repeat_tue", cursor.getInt(5) > 0);
			values.put("repeat_wed", cursor.getInt(6) > 0);
			values.put("repeat_thu", cursor.getInt(7) > 0);
			values.put("repeat_fri", cursor.getInt(8) > 0);
			values.put("repeat_sat", cursor.getInt(9) > 0);
			values.put("repeat_sun", cursor.getInt(10) > 0);

			alarmImage.setImageDrawable(getResources().getDrawable(R.drawable.icon));
			timeText.setText(time);
			repeatText.setText(Alarm.formatRepeat(values) + "");
			
			if (cursor.getInt(11) > 0) {
				failSafeText.setText("FailSafe Mode (Snooze Limit: " + cursor.getInt(15) + ")");
			}
			if (cursor.getInt(12) > 0) { wakeUpText.setText("Wake-Up Mode"); }
			
			
			String label = cursor.getString(3);
			// Fix this to work with landscape mode and not use "magic" numbers
			if (label.length() > 16) {
				label = label.substring(0, 13) + "...";
			}
			labelText.setText(label);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			
			final LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.alarm_row, parent, false);
			
			return v;
		}
	}
}