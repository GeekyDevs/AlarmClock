package com.geekydevs.alarmclock;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public abstract class WakeLocker {

	private static WakeLock wakeLock = null;
	private static KeyguardLock keyguardLock;
	private static KeyguardManager keyguardManager;

    public static void acquire(Context ctx) {
        if (wakeLock != null) {
        	wakeLock.release();
        }
        else {
	        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
	        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
	                PowerManager.ACQUIRE_CAUSES_WAKEUP |
	                PowerManager.ON_AFTER_RELEASE, "AlarmClock.class");
	        wakeLock.acquire();
	        
	        keyguardManager = (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE); 
	        keyguardLock = keyguardManager.newKeyguardLock("TAG");
	        keyguardLock.disableKeyguard();
        }
    }

    public static void release() {
        if (wakeLock != null){
        	wakeLock.release(); 
        	wakeLock = null;
        }
    }

    public static void exit() {
    	keyguardLock.reenableKeyguard();
    	keyguardLock = null;
    }
    
    public static void acquire() {
    	
    	if ((wakeLock != null) && !wakeLock.isHeld()) {
    		wakeLock.acquire();
    	}
    }
}
