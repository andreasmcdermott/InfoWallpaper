package com.andreashedin.infowallpaper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class WeatherDataCollector extends DataCollector {
	
	private static final String WEATHER_CURRENT_CONDITION = "#wcc";
	private static final String WEATHER_CURRENT_CONDITION_ICON = "#wcci";
	private static final String WEATHER_CURRENT_TEMPERATURE = "#wct";
	private static final String WEATHER_HIGH = "#wh+";
	private static final String WEATHER_LOW = "#wl+";
	private static final String WEATHER_CONDITION = "#wc+";
	private static final String WEATHER_CONDITION_ICON = "#wci+";
	private static final String WEATHER_LOCATION = "#wloc";
	
	String mLocation = "";
	
	WeatherDataCollector(LiveInfoWallpaper parent) {
		super(parent);
	}
	
	private WeatherHandler.WeatherData[] mDays = new WeatherHandler.WeatherData[WeatherHandler.MAX_DAYS];

	public static boolean contains(String str) {
		if(str.contains(WEATHER_CURRENT_CONDITION))
			return true;
		if(str.contains(WEATHER_CURRENT_CONDITION_ICON))
			return true;
		if(str.contains(WEATHER_CURRENT_TEMPERATURE))
			return true;
		if(str.contains(WEATHER_LOCATION))
			return true;
		if(str.contains(WEATHER_HIGH))
			return true;
		if(str.contains(WEATHER_LOW))
			return true;
		if(str.contains(WEATHER_CONDITION))
			return true;
		if(str.contains(WEATHER_CONDITION_ICON))
			return true;
		
		return false;
	}
	
	public static boolean useIcon(String str) {
		if(str.contains(WEATHER_CONDITION_ICON) || str.contains(WEATHER_CURRENT_CONDITION_ICON))
			return true;
		
		return false;
	}
	
	
	public static String getSampleText(String in) {
		String out = in;
		
		if(in.contains(WEATHER_CURRENT_CONDITION_ICON) || in.contains(WEATHER_CONDITION_ICON)) {
			out = "#icon#";
		}
		else {
			out = out.replace(WEATHER_CURRENT_CONDITION, "Condition");
			out = out.replace(WEATHER_CURRENT_TEMPERATURE, "00");
			out = out.replace(WEATHER_HIGH, "0");
			out = out.replace(WEATHER_LOW, "0"); 
			out = out.replace(WEATHER_CONDITION, "Condition");
			out = out.replace(WEATHER_LOCATION, "City, Country");
		}
		
		return out;
	}	

	@Override
	String updateInfoString(String str, boolean numbersAsText) {
		String out = "";
		String number = "";
		
		if(useIcon(str) == false) {	
			if(mDays[0] != null) {				
				out = str.replace(WEATHER_CURRENT_CONDITION, mDays[0].condition);
				
				if(numbersAsText == true) {
					int i = Integer.parseInt(mDays[0].getTemp());
					number = getNumberAsText(i);
				}
				else number = mDays[0].getTemp();
				
				out = out.replace(WEATHER_CURRENT_TEMPERATURE, number);
				
				out = out.replace(WEATHER_LOCATION, mLocation);
			}
			
			out = doTodayPlusN(0, out, numbersAsText);
			out = doTodayPlusN(1, out, numbersAsText);
			out = doTodayPlusN(2, out, numbersAsText);
			out = doTodayPlusN(3, out, numbersAsText);
		}
		
		return out;
	}
	
	private String mLastIcon = "";
	private Bitmap mWeatherIcon = null;
	public Bitmap getIcon(String str) {
		String file = "";
		
		if(str.contains(WEATHER_CURRENT_CONDITION_ICON) && mDays[0] != null) {
			file = getIconFilename(mDays[0].condition);
		}
		else {
			int index = str.indexOf(WEATHER_CONDITION_ICON);
			
			if(index >= 0) {
				index += WEATHER_CONDITION_ICON.length();
				
				int day = -1;
				
				try {
					Integer.parseInt(str.substring(index, index + 1));
				}
				catch(NumberFormatException e) {
					
				}
				
				if(day >= 0 && day < WeatherHandler.MAX_DAYS) {
					if(mDays[day] != null) {
						file = getIconFilename(mDays[day].condition);
					}
				}
			}
		}
		
		if(file.length() > 0) {
			if(file != mLastIcon && mParent != null) {
				mLastIcon = file;
				try {
					AssetManager assets = mParent.getAssets();
					InputStream is = assets.open("icons/" + file);
					mWeatherIcon = BitmapFactory.decodeStream(is);
				}
				catch(Exception e) {
					mLastIcon = "";
					mWeatherIcon = null;
				}
			}
		}
		
		return mWeatherIcon;
	}

	String doTodayPlusN(int n, String str, boolean numbersAsText) {
		String out = str;
		String number = "";
		
		if(n >= WeatherHandler.MAX_DAYS || mDays[n] == null)
			return out;
		
		String toReplace = WEATHER_CONDITION + n;
		out = out.replace(toReplace, mDays[n].condition);
		toReplace = WEATHER_HIGH + n;
		
		if(numbersAsText == true) {
			int i = Integer.parseInt(mDays[n].getHighTemp());
			number = getNumberAsText(i);
		}
		else number = mDays[n].getHighTemp();
		
		out = out.replace(toReplace, number);
		toReplace = WEATHER_LOW + n;
		
		if(numbersAsText == true) {
			int i = Integer.parseInt( mDays[n].getLowTemp());
			number = getNumberAsText(i);
		}
		else number =  mDays[n].getLowTemp();
		
		out = out.replace(toReplace, number);
		
		return out;
	}
	
	@Override
	void update(Object object) {
		mLocation = WeatherHandler.instance().getLocation();
		for(int i = 0; i < WeatherHandler.MAX_DAYS; ++i) {
			mDays[i] = WeatherHandler.instance().getData(i);
			mDays[i].originalCondition = mDays[i].condition;
			mDays[i].condition = getLocalizedString(mDays[i].originalCondition);
		}
	}
	
	String getLocalizedString(String str) {
		String out = str;
		if(str.equals("Clear"))
			out = mParent.getString(R.string.Clear_title);
		else if(str.equals("Cloudy"))
			out = mParent.getString(R.string.Cloudy_title);
		else if(str.equals("Fog"))
			out = mParent.getString(R.string.Fog_title);
		else if(str.equals("Haze"))
			out = mParent.getString(R.string.Haze_title);
		else if(str.equals("Light Rain"))
			out = mParent.getString(R.string.LightRain_title);
		else if(str.equals("Mostly Cloudy"))
			out = mParent.getString(R.string.MostlyCloudy_title);
		else if(str.equals("Mostly Sunny"))
			out = mParent.getString(R.string.MostlySunny_title);
		else if(str.equals("Overcast"))
			out = mParent.getString(R.string.Overcast_title);
		else if(str.equals("Partly Cloudy"))
			out = mParent.getString(R.string.PartlyCloudy_title);
		else if(str.equals("Rain"))
			out = mParent.getString(R.string.Rain_title);
		else if(str.equals("Rain Showers"))
			out = mParent.getString(R.string.RainShowers_title);
		else if(str.equals("Showers"))
			out = mParent.getString(R.string.Showers_title);
		else if(str.equals("Thunderstorm"))
			out = mParent.getString(R.string.Thunderstorm_title);
		else if(str.equals("Chance of Showers"))
			out = mParent.getString(R.string.ChanceofShowers_title);
		else if(str.equals("Chance of Storm"))
			out = mParent.getString(R.string.ChanceofStorm_title);
		else if(str.equals("Chance of Snow"))
			out = mParent.getString(R.string.ChanceofSnow_title);
		else if(str.equals("Chance of Rain"))
			out = mParent.getString(R.string.ChanceofRain_title);
		else if(str.equals("Partly Sunny"))
			out = mParent.getString(R.string.PartlySunny_title);
		else if(str.equals("Scattered Showers"))
			out = mParent.getString(R.string.ScatteredShowers_title);
		else if(str.equals("Sunny"))
			out = mParent.getString(R.string.Sunny_title);
		return out;
	}
	
	public String getIconFilename(String str) {
		String file = "";
		
		str = str.toLowerCase().replace(" ", "_");
		
		file = String.format(WeatherHandler.instance().getIconSet(), str);
		
		return file;
	}
}
