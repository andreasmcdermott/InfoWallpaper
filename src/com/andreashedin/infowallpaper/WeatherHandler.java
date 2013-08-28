package com.andreashedin.infowallpaper;

import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class WeatherHandler {

	private final static WeatherHandler sInstance = new WeatherHandler();
	private static final String WEATHER_URL = "http://www.google.com/ig/api?weather=%s";
	public static final int MAX_DAYS = 4;

	private String mLastSuccessfulLocation = "";
	private String mLocation = "";
	private WeatherData[] mWeatherData = new WeatherData[MAX_DAYS];
	private boolean mUseFahrenheit = true;
	private String mIconSet = "light_frame_%s.png";
	private int mIconSize = 100;
	private int mUpdateMinutes = -1;
	private Date mLastUpdate = null;
	private boolean mForce = false;
	
	private WeatherHandler() {
		for(int i = 0; i < MAX_DAYS; ++i)
			mWeatherData[i] = null;
	}

	public static WeatherHandler instance() {
		return sInstance;
	}
	
	public boolean useIcons() {
		return (mIconSize > 0);
	}
	
	public boolean useFahrenheit() {
		return mUseFahrenheit;
	}
	
	public void useFahrenheit(boolean use) {
		mUseFahrenheit = use;
	}
	
	public void updateMinutes(int minutes) {
		mUpdateMinutes = minutes;
	}
	
	public void setLocation(String loc) {
		mLocation = loc;
	}
	
	public String getIconSet() {
		return mIconSet;
	}
	
	public void setIconSet(String set) {
		mIconSet = set;
	}
	
	public int getIconSize() {
		return mIconSize;
	}
	
	public void setIconSize(int iconSize) {
		mIconSize = iconSize;
	}
	
	public int updateMinutes() {
		return mUpdateMinutes;
	}

	public WeatherData getData(int day) {
		if (day >= MAX_DAYS || day < 0)
			return null;

		return mWeatherData[day];
	}
	
	public boolean shouldUpdate() {
		boolean update = (mForce || (Calendar.getInstance().getTime().getTime() - mLastUpdate.getTime()) > updateMinutes());
		mForce = false;
		
		return update;
	}
	
	public void forceUpdate(boolean force) {
		mForce = force;
	}
	
	public boolean forceUpdate() {
		return mForce;
	}

	public void update(String userAgent) {
		if (userAgent == null || userAgent.length() == 0 || mLocation.length() == 0)
			return;

		String loc = mLocation.replace(" ", "%20");
		
		String content = LiveInfoWallpaper.getUrlContent(String.format(WEATHER_URL, loc), userAgent);

		if (content.length() > 0) {
			mLastUpdate = Calendar.getInstance().getTime();
			parseWeatherInformation(content);
		}
		
		for (int i = 0; i < MAX_DAYS; ++i) {
			if(mWeatherData[i] == null)
				continue;
			
			if (mUseFahrenheit == true)
				mWeatherData[i].fahrenheit = true;
			else
				mWeatherData[i].fahrenheit = false;
		}
	}

	private void parseWeatherInformation(String weather) {

		int day = 0;
		boolean foundData = false;
		
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();

			xpp.setInput(new StringReader(weather));
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					if (xpp.getName().compareTo("city") == 0) {
						mLocation = xpp.getAttributeValue(0);
						mLastSuccessfulLocation = mLocation;
					} else if (xpp.getName().compareTo("current_conditions") == 0) {
						foundData = true;
						parseCurrentConditions(xpp);
					} else if (xpp.getName().compareTo("forecast_conditions") == 0) {
						foundData = true;
						parseForecastConditions(xpp, day);
						++day;
					}
				}

				eventType = xpp.next();
			}
		} catch (Exception ex) {

		}
		
		if(foundData == false)
			mLocation = mLastSuccessfulLocation;
	}

	private void parseForecastConditions(XmlPullParser xpp, int day)
			throws XmlPullParserException, IOException {
		if(mWeatherData[day] == null)
			mWeatherData[day] = new WeatherData();

		while (true) {
			if (xpp.getEventType() == XmlPullParser.START_TAG) {
				if (xpp.getName().compareTo("low") == 0) {
					mWeatherData[day].lowF = Integer.parseInt(xpp
							.getAttributeValue(0));
				} else if (xpp.getName().compareTo("high") == 0) {
					mWeatherData[day].highF = Integer.parseInt(xpp
							.getAttributeValue(0));
				} else if (xpp.getName().compareTo("condition") == 0) {
					if(day > 0)
						mWeatherData[day].condition = xpp.getAttributeValue(0);
				}
			} else if (xpp.getEventType() == XmlPullParser.END_TAG) {
				if (xpp.getName().compareTo("forecast_conditions") == 0)
					return;
			}

			xpp.next();
		}
	}

	private void parseCurrentConditions(XmlPullParser xpp)
			throws XmlPullParserException, IOException {
		if(mWeatherData[0] == null)
			mWeatherData[0] = new WeatherData();

		while (true) {
			if (xpp.getEventType() == XmlPullParser.START_TAG) {
				if (xpp.getName().compareTo("temp_f") == 0) {
					mWeatherData[0].tempF = Integer.parseInt(xpp
							.getAttributeValue(0));
				} else if (xpp.getName().compareTo("condition") == 0) {
					mWeatherData[0].condition = xpp.getAttributeValue(0);
				}
			} else if (xpp.getEventType() == XmlPullParser.END_TAG) {
				if (xpp.getName().compareTo("current_conditions") == 0)
					return;
			}

			xpp.next();
		}
	}
	
	public String getLocation() {
		return mLocation;
	}

	public class WeatherData {
		public String condition = "";
		public String originalCondition = "";
		public int tempF = 0;
		public int highF = 0;
		public int lowF = 0;
		public boolean fahrenheit = true;

		public String getTemp() {
			String temp = "";
			if (fahrenheit) {
				temp = "" + tempF;
			} else {
				float t = ((float)(tempF - 32)) * 5.0f / 9.0f;
				if(t < 0)
					t -= 0.5f;
				else
					t += 0.5f;
				
				int tempC = (int)t;
				temp = "" + tempC;
			}

			return temp;
		}

		public String getCondition() {
			return condition;
		}

		public String getHighTemp() {
			String temp = "";
			if (fahrenheit) {
				temp = "" + highF;
			} else {
				float t = ((float)(highF - 32)) * 5.0f / 9.0f;
				if(t < 0)
					t -= 0.5f;
				else
					t += 0.5f;
				
				int tempC = (int)t;
				temp = "" + tempC;
			}

			return temp;
		}

		public String getLowTemp() {
			String temp = "";
			if (fahrenheit) {
				temp = "" + lowF;
			} else {
				float t = ((float)(lowF - 32)) * 5.0f / 9.0f;
				if(t < 0)
					t -= 0.5f;
				else
					t += 0.5f;
				
				int tempC = (int)t;
				temp = "" + tempC;
			}

			return temp;
		}
	}
}
