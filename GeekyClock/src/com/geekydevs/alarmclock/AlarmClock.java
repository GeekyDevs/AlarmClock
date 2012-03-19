package com.geekydevs.alarmclock;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmClock extends ListActivity {
	
	private static final int EDIT_ALARM = Menu.FIRST+1;
	private static final int DELETE_ALARM = Menu.FIRST+2;
	private static final int TURN_ALARM_ON = Menu.FIRST+3;
	
	private boolean notifOn = false;
	
	private AlarmDBAdapter dbAdapter;
	private CursorAdapter curAdapter;
	public Alarm alarm;
	
	private Context ctx;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        dbAdapter = new AlarmDBAdapter(this);
        dbAdapter.open();
        
        Cursor c = dbAdapter.fetchAllAlarms();
        
        startManagingCursor(c);
        curAdapter = new AlarmListAdapter(this, c, true);

        assignListeners();
        
    }
    
    private void assignListeners() {
    	
    	ListView listV = getListView();
    	listV.setAdapter(curAdapter);
    	listV.setOnCreateContextMenuListener(createItemContext);
    	
    	((Button)findViewById(R.id.m_btn_add_new)).setOnClickListener(onAddNewAlarmClick);
    	((Button)findViewById(R.id.notif_on)).setOnClickListener(onToggleNotifOn);
    	((Button)findViewById(R.id.notif_off)).setOnClickListener(onToggleNotifOff);
    }
    
    @Override
    protected void onDestroy() {
    	dbAdapter.close();
    	super.onDestroy();
    }
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.main);
		
		assignListeners();
	}
    
    /*
     * Listens for user's key press on any alarm on the list to launch the edit screen. 
     */
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		launchAlarmEdit(id);
		super.onListItemClick(l, v, position, id);
	}
 
    /*
     * Sends an intent to alarm edit screen with the alarm's id attached. 
     */
    private void launchAlarmEdit(long id) {
    	Intent i = new Intent(this, AlarmEdit.class);
		i.putExtra("_id", id);
		i.putExtra("notifOn", notifOn);
		startActivity(i);
    }
    
    /*
     * Launches the alarm edit screen when the user adds a new alarm.  Default values will
     */
    private final View.OnClickListener onAddNewAlarmClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent i = new Intent (getBaseContext(), AlarmEdit.class);
			i.putExtra("notifOn", notifOn);
			startActivity(i);
		}
		
	};
	
	/*
     * Turn on the notification to alert the user an alarm has been enabled.
     */
    private final View.OnClickListener onToggleNotifOn = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			notifOn = true;
			toggleNotif();
		}
		
	};
	
	/*
     * Turn off the notification which alerts the user an alarm has been enabled.
     */
    private final View.OnClickListener onToggleNotifOff = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			notifOn = false;
			Intent i = new Intent(getBaseContext(), AlarmService.class);
			i.setAction(AlarmService.ACTION_CANCEL_NOTIF);
			startService(i);
		}
		
	};
	
	/*
	 * Helper method to start service for notification. 
	 */
	private void toggleNotif () {
		
		if (notifOn) {
			Intent i = new Intent(getBaseContext(), AlarmService.class);
			i.setAction(AlarmService.ACTION_SHOW_NOTIF);
			startService(i);
		}
	}
	
	/*
	 * Listens for user's key hold on alarm row to bring up context menu of alarm actions.
	 */
	private final OnCreateContextMenuListener createItemContext = new OnCreateContextMenuListener() {
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			
			menu.add(0, EDIT_ALARM, 0, "Edit Alarm");
			menu.add(0, DELETE_ALARM, 1, "Delete Alarm");
			//menu.add(0, TURN_ALARM_ON, 2, "Turn Alarm On");
		}
	};
	
	/*
	 * Checks which context menu action was selected and perform that responsibility.
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
		long selectedId = acmi.id;
		
		switch (item.getItemId()) {
		case EDIT_ALARM:
			launchAlarmEdit(selectedId);
			break;
		case DELETE_ALARM:
			dbAdapter.deleteAlarm(selectedId);
			curAdapter.getCursor().requery();

			turnOffAlarm();
			toggleNotif();
			
			break;
		//case TURN_ALARM_ON:
		}
		
		return super.onContextItemSelected(item);
	}
	
	/*
	 * Request that the alarm service be started.
	 */
	private void setUpAlarm(int id) {
		
		Intent i = new Intent(this, AlarmService.class);
		i.setAction(AlarmService.ACTION_SET_ALARM);
		i.putExtra("_id", id);
		startService(i);
		
	}
	
	/*
	 * Removes the alarm with the intent.  Occurs when user disables the 
	 * alarm or deletes it.
	 */
	private void turnOffAlarm() {
		
		Intent i = new Intent(this, AlarmService.class);
		i.setAction(AlarmService.ACTION_STOP_ALARM);
		startService(i);
	}
	
	
	/*
	 * Adapter that exposes data from a Cursor to a ListView widget.
	 */
	private class AlarmListAdapter extends CursorAdapter {
		
		public AlarmListAdapter(Context context, Cursor c, Boolean autoRequery){
			super(context, c, autoRequery);
		}
		
		/*
		 * Bind an existing view to the data pointed to by cursor
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			//ImageView turnOnOffImage = (ImageView) view.findViewById(R.id.turn_onoff_icon);
			
			TextView labelText = (TextView) view.findViewById(R.id.label_row);
			TextView timeText = (TextView) view.findViewById(R.id.time_row);
			TextView repeatText = (TextView) view.findViewById(R.id.repeat_row);

			ImageView alarmImage = (ImageView) view.findViewById(R.id.alarm_icon);
			ImageView failsafeImage = (ImageView) view.findViewById(R.id.failsafe_icon);
			ImageView challengeImage = (ImageView) view.findViewById(R.id.challenge_icon);
			
			CheckBox chkAlarmOn = (CheckBox) view.findViewById(R.id.alarm_enabled_row);
			
			//Display the alarm label
			String label = cursor.getString(3);
			// Fix this to work with landscape mode and not use "magic" numbers
			if (label.length() > 16) {
				label = label.substring(0, 13) + "...";
			}
			labelText.setText(label);
			
			// Display the scheduled time
			String time = Alarm.formatTime(cursor.getInt(1), cursor.getInt(2));
			timeText.setText(time);
			
			// Display the repeat info
			ContentValues values = new ContentValues();
			
			values.put("repeat_sun", cursor.getInt(4) > 0);
			values.put("repeat_mon", cursor.getInt(5) > 0);
			values.put("repeat_tue", cursor.getInt(6) > 0);
			values.put("repeat_wed", cursor.getInt(7) > 0);
			values.put("repeat_thu", cursor.getInt(8) > 0);
			values.put("repeat_fri", cursor.getInt(9) > 0);
			values.put("repeat_sat", cursor.getInt(10) > 0);
			
			repeatText.setText(Alarm.formatRepeat(values) + "");
			
			// Icon images
			//turnOnOffImage.setImageDrawable(getResources().getDrawable(R.drawable.icon));
			
			Boolean showFailSafe = (cursor.getInt(11) > 0);
			Boolean showChallenge = (cursor.getInt(12) > 0);
			
			alarmImage.setImageDrawable(getResources().getDrawable(R.drawable.alarm_icon));
			failsafeImage.setImageDrawable(getResources().getDrawable(R.drawable.failsafe_icon));
			challengeImage.setImageDrawable(getResources().getDrawable(R.drawable.challenge_icon));
			
			failsafeImage.setVisibility(ImageView.GONE);
			challengeImage.setVisibility(ImageView.GONE);
			
			if (!showFailSafe && !showChallenge) {
				alarmImage.setVisibility(ImageView.VISIBLE);
				failsafeImage.setVisibility(ImageView.GONE);
				challengeImage.setVisibility(ImageView.GONE);
			} else {
				alarmImage.setVisibility(ImageView.GONE);
			}
			
			if (showFailSafe) {
				failsafeImage.setVisibility(ImageView.VISIBLE);
			}
			
			if (showChallenge) {
				challengeImage.setVisibility(ImageView.VISIBLE);
			}
			
			chkAlarmOn.setOnCheckedChangeListener(null);
			chkAlarmOn.setTag(cursor.getInt(0));
			chkAlarmOn.setChecked(cursor.getInt(16) > 0);
			chkAlarmOn.setOnCheckedChangeListener(checkAlarm);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			
			final LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.alarm_row, parent, false);

			bindView(v, context, cursor);

			return v;
		}
		
		private CheckBox.OnCheckedChangeListener checkAlarm = new CheckBox.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				
				int alarmId = Integer.parseInt(arg0.getTag().toString());
				dbAdapter.setAlarmToDB(alarmId, arg1);
				curAdapter.getCursor().requery();

				if (arg1) { 
					setUpAlarm(alarmId);	
				} else {
					turnOffAlarm();
				}
				
				toggleNotif();
			}
			
		};
	}
}