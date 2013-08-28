package com.andreashedin.infowallpaper;

import java.util.Calendar;

import android.content.res.Resources;

public class DateTimeDataCollector extends DataCollector {

	private static final String FULL_YEAR = "#yyyy";
	private static final String SHORT_YEAR = "#yy";
	private static final String MONTH_NO = "#MM";
	private static final String MONTH_NAME = "#MMMM";
	private static final String MONTH_NAME_SHORT = "#MMM";
	private static final String WEEK = "#ww";
	private static final String DAY_OF_MONTH = "#dd";
	private static final String DAY_OF_MONTH_TH = "#dd+";
	private static final String DAY_OF_WEEK = "#dddd";
	private static final String DAY_OF_WEEK_SHORT = "#ddd";
	private static final String HOUR_OF_DAY = "#HH";
	private static final String HOUR = "#hh";
	private static final String MINUTE = "#mm";
	private static final String AMPM = "#ampm";
	
	DateTimeDataCollector(LiveInfoWallpaper parent) {
		super(parent);
		
		getNumberAsText(26);
		getNumberAsText(39);
		getNumberAsText(95);
		getNumberAsText(84);
		getNumberAsText(125);
		getNumberAsText(46);
		getNumberAsText(594);
		getNumberAsText(2304);
		getNumberAsText(4034);
		getNumberAsText(102);
		getNumberAsText(545);
		getNumberAsText(1234);
		getNumberAsText(349);
	}
	
	static String getSampleText(String str) {
		String ret = str;
		ret = ret.replace(FULL_YEAR, "0000");
		ret = ret.replace(SHORT_YEAR, "00");
		ret = ret.replace(MONTH_NAME, "December");
		ret = ret.replace(MONTH_NAME_SHORT, "Dec");
		ret = ret.replace(MONTH_NO, "00");
		ret = ret.replace(WEEK, "00");
		ret = ret.replace(DAY_OF_WEEK, "Thursday");
		ret = ret.replace(DAY_OF_WEEK_SHORT, "Thu");
		ret = ret.replace(DAY_OF_MONTH_TH, "00th");
		ret = ret.replace(DAY_OF_MONTH, "00");
		ret = ret.replace(HOUR_OF_DAY, "00");
		ret = ret.replace(HOUR, "00");
		ret = ret.replace(MINUTE, "00");
		ret = ret.replace(AMPM, "PM");
		return ret;
	}
	
	@Override
	void update(Object object) {

	}

	@Override
	String updateInfoString(String string, boolean numbersAsText) {
		Calendar calendar = Calendar.getInstance();
		
		String year = "" + calendar.get(Calendar.YEAR);
		int monthNo = calendar.get(Calendar.MONTH);
		String month = getWithLeadingZero(monthNo + 1);
		String dayOfMonth = getWithLeadingZero(calendar.get(Calendar.DAY_OF_MONTH));
		String dayOfMonthNoLeadingZero = "" + calendar.get(Calendar.DAY_OF_MONTH);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		String hourOfDay = getWithLeadingZero(calendar.get(Calendar.HOUR_OF_DAY));
		String hourAMPM = getWithLeadingZero(calendar.get(Calendar.HOUR));
		if(hourAMPM.equals("00"))
			hourAMPM = "12";
		String ampm = (calendar.get(Calendar.AM_PM) == Calendar.AM) ? "AM" : "PM";
		String minutes = getWithLeadingZero(calendar.get(Calendar.MINUTE));
		
		String week = "" + (calendar.get(Calendar.WEEK_OF_YEAR));
		
		String[] monthNames = null;
		String[] weekdays = null;
		String[] numberEndings = null;
		
		Resources resources = mParent.getResources();
		if(resources != null) {
			monthNames = resources.getStringArray(R.array.month);
			weekdays = resources.getStringArray(R.array.weekdays);
			numberEndings = resources.getStringArray(R.array.number_endings);
		}
		
		string = string.replace(FULL_YEAR, year);
		string = string.replace(SHORT_YEAR, year.substring(2));
		if(resources != null) {
			string = string.replace(MONTH_NAME, monthNames[monthNo]);
			string = string.replace(MONTH_NAME_SHORT, monthNames[monthNo].substring(0, 3));
			string = string.replace(DAY_OF_WEEK, weekdays[dayOfWeek]);
			string = string.replace(DAY_OF_WEEK_SHORT, weekdays[dayOfWeek].substring(0, 3));
			string = string.replace(DAY_OF_MONTH_TH, getWithNumberEnding(dayOfMonthNoLeadingZero, numberEndings));
		}
		
		if(numbersAsText) {
			month = getNumberAsText(calendar.get(Calendar.MONTH + 1));
			dayOfMonth = getNumberAsText(calendar.get(Calendar.DAY_OF_MONTH));
			hourOfDay = getNumberAsText(calendar.get(Calendar.HOUR_OF_DAY));
			int h = calendar.get(Calendar.HOUR);
			if(h == 0) h = 12;
			hourAMPM = getNumberAsText(h);
			minutes = getNumberAsText(calendar.get(Calendar.MINUTE));
			week = getNumberAsText(calendar.get(Calendar.WEEK_OF_YEAR));
		}
		
		string = string.replace(MONTH_NO, month);
		string = string.replace(DAY_OF_MONTH, dayOfMonth);
		string = string.replace(HOUR_OF_DAY, hourOfDay);
		string = string.replace(HOUR, hourAMPM);
		string = string.replace(AMPM, ampm);
		string = string.replace(MINUTE, minutes);
		string = string.replace(WEEK, week);
		
		return string;
	}
	
	protected String getWithLeadingZero(int val) {
		if(val < 10) {
			return "0" + val;
		}
		
		return "" + val;
	}
	
	protected String getWithNumberEnding(String str, String[] endings) {
		String lastLetter = str.substring(str.length() - 1);
		String nextToLastLetter = "";
		
		if(str.length() >= 2) {
			nextToLastLetter = str.substring(str.length() - 2, str.length() - 1);
		}
		
		if(lastLetter.equals("1") && endings.length > 0 && nextToLastLetter.equals("1") == false) {
			str += endings[0];
		}
		else if(lastLetter.equals("2") && endings.length > 1 && nextToLastLetter.equals("1") == false) {
			str += endings[1];
		}
		else if(lastLetter.equals("3") && endings.length > 2 && nextToLastLetter.equals("1") == false) {
			str += endings[2];
		}
		else {
			if(endings.length > 3)
				str += endings[3];
		}
		
		return str;
	}
}
