package com.geekydevs.alarmclock;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class AlarmClock extends ListActivity {
	
	private static final int EDIT_ALARM = Menu.FIRST+1;
	private static final int DELETE_ALARM = Menu.FIRST+2;
	private static final int TURN_ALARM_ON = Menu.FIRST+3;
	
	private static final String AD_UNIT_ID = "a14d7f7d2180609";
	
	private static final int REPEAT_DEFAULT_COLOR = Color.GRAY;
	private static final int REPEAT_SELECTED_COLOR = Color.YELLOW;
	
	private AlarmDBAdapter dbAdapter;
	private CursorAdapter curAdapter;
	public Alarm alarm;
	
	private TextView labelText;
	private TextView timeText;
	private TextView periodText;
	private TextView sunText;
	private TextView monText;
	private TextView tueText;
	private TextView wedText;
	private TextView thuText;
	private TextView friText;
	private TextView satText;
	
	private ImageView alarmOnImage;
	private ImageView failsafeImage;
	private ImageView challengeImage;
	private ImageView soundImage;
	
	private LinearLayout lLAddRow;
	private ImageView addAlarmImage;
	
	private AdView adView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Create an ad.
        adView = new AdView(this, AdSize.BANNER, "a14f8cdc40486f5");
        
        // Add the AdView to the view hierarchy. The view will have no size
        // until the ad is loaded.
        LinearLayout layout = (LinearLayout) findViewById(R.id.ad);
        layout.addView(adView);
        
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device.
        AdRequest request = new AdRequest();
        request.addTestDevice(AdRequest.TEST_EMULATOR);
        
        // Testing on real device
        request.addTestDevice("3334DE9B8EA200EC");
        
        // Start loading the ad in the background.
        adView.loadAd(request);

        dbAdapter = new AlarmDBAdapter(this);
        dbAdapter.open();

        lLAddRow = (LinearLayout)findViewById(R.id.add_alarm_row);
        lLAddRow.setOnClickListener(addOnClick);
        
        addAlarmImage = (ImageView)findViewById(R.id.add_new_alarm);
        addAlarmImage.setImageDrawable(getResources().getDrawable(R.drawable.alarm_add));
		
        Cursor c = dbAdapter.fetchAllAlarms();
        
        startManagingCursor(c);
        curAdapter = new AlarmListAdapter(this, c, true);
        
        assignListeners();
        
        /*
        Intent i = new Intent(getBaseContext(), AlarmService.class);
		i.setAction(AlarmService.ACTION_SHOW_NOTIF);
		startService(i);
		*/
    }
    
    private void assignListeners() {
    	
    	ListView listV = getListView();
    	listV.setAdapter(curAdapter);
    	listV.setOnCreateContextMenuListener(createItemContext);
    }
    
    @Override
    protected void onResume() {
    	
    	curAdapter.getCursor().requery();
    	
    	Intent i = new Intent(getBaseContext(), AlarmService.class);
		i.setAction(AlarmService.ACTION_SHOW_NOTIF);
		startService(i);

    	super.onResume();
    }
    
    @Override
    protected void onDestroy() {

    	// Destroy the AdView.
    	
    	if (adView != null) {
	      adView.destroy();
	    }
    	curAdapter.getCursor().close();
    	dbAdapter.close();
    	super.onDestroy();
    }
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.main);
		
		curAdapter.getCursor().requery();
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
		i.putExtra(Alarm.PACKAGE_PREFIX + "._id", id);
		startActivity(i);
    }
    
    /*
     * Launches the alarm edit screen when the user adds a new alarm.  Default values will
     */
    private View.OnClickListener addOnClick = new View.OnClickListener() {
    	
    	@Override
		public void onClick(View v) {
    		Intent i = new Intent (getBaseContext(), AlarmEdit.class);
    		startActivity(i);
    	}
    };
    
	/*
	 * Helper method to start service for notification. 
	 */
	private void toggleNotif() {

		Intent i = new Intent(getBaseContext(), AlarmService.class);
		i.setAction(AlarmService.ACTION_SHOW_NOTIF);
		startService(i);
	}
	/*
	 * Listens for user's key hold on alarm row to bring up context menu of alarm actions.
	 */
	private final OnCreateContextMenuListener createItemContext = new OnCreateContextMenuListener() {
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			
			menu.add(0, EDIT_ALARM, 0, "Edit Alarm");
			menu.add(0, DELETE_ALARM, 1, "Delete Alarm");
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

			turnOffAlarm((int)selectedId);
			toggleNotif();
			
			break;
		}
		
		return super.onContextItemSelected(item);
	}
	
	/*
	 * Request that the alarm service be started.
	 */
	private void setUpAlarm(int id) {
		
		Intent i = new Intent(this, AlarmService.class);
		i.putExtra(Alarm.PACKAGE_PREFIX + ".id", id);
		i.setAction(AlarmService.ACTION_SET_ALARM);
		startService(i);
		
	}
	
	/*
	 * Removes the alarm with the intent.  Occurs when user disables the 
	 * alarm or deletes it.
	 */
	private void turnOffAlarm(int id) {
		
		Intent i = new Intent(this, AlarmService.class);
		i.putExtra(Alarm.PACKAGE_PREFIX + ".id", id);
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
			
			findViews(view);
			
			//Display the alarm label
			String label = cursor.getString(3);
			// Fix this to work with landscape mode and not use "magic" numbers
			if (label.length() > 16) {
				label = label.substring(0, 13) + "...";
			}
			labelText.setText(label);
			
			// Display the scheduled time
			String time = Alarm.formatTime(cursor.getInt(1), cursor.getInt(2));
			timeText.setText(time.substring(0, time.length()-3));	
			periodText.setText(time.substring(time.length()-2).toUpperCase());
			
			highlightRepeat(cursor);

			// Icon images
			//turnOnOffImage.setImageDrawable(getResources().getDrawable(R.drawable.icon));
			
			Boolean alarmOn = (cursor.getInt(16) > 0);
			Boolean showFailSafe = (cursor.getInt(11) > 0);
			Boolean showChallenge = (cursor.getInt(12) > 0);
			Boolean vibrateOn = (cursor.getInt(13) > 0);
			Boolean soundOff = (cursor.getString(14).equals("Silent"));

			alarmOnImage.setImageDrawable(getResources().getDrawable(R.drawable.alarm_on));
			failsafeImage.setImageDrawable(getResources().getDrawable(R.drawable.failsafe_off));
			challengeImage.setImageDrawable(getResources().getDrawable(R.drawable.challenge_off));
			soundImage.setImageDrawable(getResources().getDrawable(R.drawable.sound_on_vibrate_off));

			if (alarmOn) {
				alarmOnImage.setTag(cursor.getInt(0));
			} else {
				alarmOnImage.setImageDrawable(getResources().getDrawable(R.drawable.alarm_off));
				if (cursor.getInt(0) == 0) {
					alarmOnImage.setTag(-1000);
				} else {
					alarmOnImage.setTag(0 - cursor.getInt(0));
				}
			}

			alarmOnImage.setOnClickListener(enableAlarmOn);
			
			if (showFailSafe) {
				failsafeImage.setImageDrawable(getResources().getDrawable(R.drawable.failsafe_on));
			}
			
			if (showChallenge) {
				challengeImage.setImageDrawable(getResources().getDrawable(R.drawable.challenge_on));
			}
			
			if (soundOff && vibrateOn) {
				soundImage.setImageDrawable(getResources().getDrawable(R.drawable.sound_off_vibrate_on));
			} else if (soundOff && !vibrateOn) {
				soundImage.setImageDrawable(getResources().getDrawable(R.drawable.sound_off_vibrate_off));
			} else if (!soundOff && vibrateOn) {
				soundImage.setImageDrawable(getResources().getDrawable(R.drawable.sound_on_vibrate_on));
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			
			final LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.alarm_row, parent, false);

			bindView(v, context, cursor);

			return v;
		}
	}
	
	private void findViews(View v) {
		
		labelText = (TextView) v.findViewById(R.id.label_row);
		timeText = (TextView) v.findViewById(R.id.time_row);
		periodText = (TextView) v.findViewById(R.id.am_pm);
		
		sunText = (TextView) v.findViewById(R.id.sunday);
		monText = (TextView) v.findViewById(R.id.monday);
		tueText = (TextView) v.findViewById(R.id.tuesday);
		wedText = (TextView) v.findViewById(R.id.wednesday);
		thuText = (TextView) v.findViewById(R.id.thursday);
		friText = (TextView) v.findViewById(R.id.friday);
		satText = (TextView) v.findViewById(R.id.saturday);
		
		alarmOnImage = (ImageView) v.findViewById(R.id.alarm_enabled_row);
		failsafeImage = (ImageView) v.findViewById(R.id.failsafe_icon);
		challengeImage = (ImageView) v.findViewById(R.id.challenge_icon);
		soundImage = (ImageView) v.findViewById(R.id.sound_icon);
	}
	
	private ImageView.OnClickListener enableAlarmOn = new Button.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			int tagId = (Integer) v.getTag();
			boolean alarmOn = tagId >= 0;

			Log.d("Checking", "Alarm " + v.getTag() + " is " + alarmOn);
			
			if (alarmOn) {
				alarmOnImage.setImageDrawable(getResources().getDrawable(R.drawable.alarm_off));
				turnOffAlarm(tagId);
			} else {
				if (tagId == -1000) {
					tagId = 0;
				} else {
					tagId *= -1;
				}
				alarmOnImage.setImageDrawable(getResources().getDrawable(R.drawable.alarm_on));
				setUpAlarm(tagId);
			}
			
			dbAdapter.setAlarmToDB(tagId, !alarmOn);
			
			curAdapter.getCursor().requery();
			toggleNotif();
		}
	};
	
	/*
	 * Highlight (in yellow) all the days in the alarm list screen with respect to the user's
	 * repeat choices.
	 */
	private void highlightRepeat(Cursor cursor) {
		
		if (cursor.getInt(4)>0)
			sunText.setTextColor(REPEAT_SELECTED_COLOR);
		else
			sunText.setTextColor(REPEAT_DEFAULT_COLOR);
		
		if (cursor.getInt(5)>0)
			monText.setTextColor(REPEAT_SELECTED_COLOR);
		else
			monText.setTextColor(REPEAT_DEFAULT_COLOR);
		
		if (cursor.getInt(6)>0)
			tueText.setTextColor(REPEAT_SELECTED_COLOR);
		else
			tueText.setTextColor(REPEAT_DEFAULT_COLOR);
		
		if (cursor.getInt(7)>0)
			wedText.setTextColor(REPEAT_SELECTED_COLOR);
		else
			wedText.setTextColor(REPEAT_DEFAULT_COLOR);
		
		if (cursor.getInt(8)>0)
			thuText.setTextColor(REPEAT_SELECTED_COLOR);
		else
			thuText.setTextColor(REPEAT_DEFAULT_COLOR);
		
		if (cursor.getInt(9)>0)
			friText.setTextColor(REPEAT_SELECTED_COLOR);
		else
			friText.setTextColor(REPEAT_DEFAULT_COLOR);
		
		if (cursor.getInt(10)>0)
			satText.setTextColor(REPEAT_SELECTED_COLOR);
		else
			satText.setTextColor(REPEAT_DEFAULT_COLOR);
	}
}