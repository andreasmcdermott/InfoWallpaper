package com.andreashedin.infowallpaper;

import android.content.Intent;
import android.os.BatteryManager;

public class BatteryDataCollector extends DataCollector {

	private static final String BATTERY_STATUS = "#bb";
	private static final String BATTERY_TEMPERATURE_C = "#btc";
	private static final String BATTERY_TEMPERATURE_F = "#btf";
	private float mBatteryStatus = 0.0f;
	private int mTemperatureC = 0;
	private int mTemperatureF = 0;
	//private boolean mCharging = false;
	
	BatteryDataCollector(LiveInfoWallpaper parent) {
		super(parent);
	}
	
	static String getSampleText(String str) {
		return str.replace(BATTERY_STATUS, "00").replace(BATTERY_TEMPERATURE_C, "00").replace(BATTERY_TEMPERATURE_F, "00");
	}
	
	@Override
	void update(Object object) {
		Intent intent = (Intent)object;
		
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		mTemperatureC = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
		mTemperatureC /= 10;
		mTemperatureF = mTemperatureC * 9 / 5 + 32;
		//int charging = intent.getIntExtra("plugged", -1);
		
		if(level >= 0 && scale >= 0) {
			mBatteryStatus = (level * 100) / scale;
		}
//		if(charging == 0) {
//			mCharging = false;
//		}
//		else {
//			mCharging = true;
//		}
	}

	@Override
	String updateInfoString(String string, boolean numbersAsText) {
		String number = "";
		
		if(numbersAsText == false)
			number = String.valueOf((int)(mBatteryStatus + 0.5f));
		else
			number = getNumberAsText((int)(mBatteryStatus + 0.5f));
		
		String str = string.replace(BATTERY_STATUS, number);
		
		if(numbersAsText == false)
			number = String.valueOf(mTemperatureC);
		else
			number = getNumberAsText(mTemperatureC);
		
		str = str.replace(BATTERY_TEMPERATURE_C, number);
		
		if(numbersAsText == false)
			number = String.valueOf(mTemperatureF);
		else
			number = getNumberAsText(mTemperatureF);
		
		str = str.replace(BATTERY_TEMPERATURE_F, number);
		
		return str;
	}
	
	public static boolean contains(String str) {
		if(str.contains(BATTERY_STATUS))
			return true;
		else if(str.contains(BATTERY_TEMPERATURE_C))
			return true;
		else if(str.contains(BATTERY_TEMPERATURE_F))
			return true;
		
		return false;
	}
}
