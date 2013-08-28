package com.andreashedin.infowallpaper;

public class DisplayValuePair<T> {
	private String mDisplay;
	private T mValue;
	
	public DisplayValuePair(String display, T value) {
		mDisplay = display;
		mValue = value;
	}
	
	public String getDisplay() {
		return mDisplay;
	}
	
	@Override
	public String toString() {
		return getDisplay();
	}
	
	public T getValue() {
		return mValue;
	}
}
