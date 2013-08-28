package com.andreashedin.infowallpaper;

import android.util.DisplayMetrics;
import android.util.Log;

public class Screen {
	private int mWidth = 0;
	private int mHeight = 0;
	private int mDensity = 0;
	private int mFormat = 0;
	private float mOffsetX = -1.0f;
	private float mOffsetY = -1.0f;
	private float mStepX = -1.0f;
	private float mStepY = -1.0f;
	private int mPixelsX = -1;
	private int mPixelsY = -1;
	private boolean mLocked = false;
	
	public Screen() {
		DisplayMetrics metrics = new DisplayMetrics();
		mDensity = metrics.densityDpi;
		metrics = null;
	}
	
	public int numberOfScreens() {
		if(mStepX <= 0.0f)
			return 0;
		
		return ((int)(1.0f / mStepX) + 1);
	}
	
	public float getPosOfScreen(int screen) {
		if(mPixelsX < 0 || mStepX < 0.0f || screen < 0) {
			return -1.0f;
		}
		
		int screens = (int)(1.0f / mStepX) + 1;
		
		float pos = mPixelsX / screens; 
		
		return pos;
	}
	
	public void updateWindow(int width, int height, int format) {
		mWidth = width;
		mHeight = height;
		mFormat = format;
	}
	
	public void setOffset(float offsetX, float offsetY, float stepX, float stepY, int pixelsX, int pixelsY) {
		if(stepX == 0.0f)
			return;
		
		mOffsetX = offsetX;
		mOffsetY = offsetY;
		mStepX = stepX;
		mStepY = stepY;
		mPixelsX = pixelsX;
		mPixelsY = pixelsY;
	}
	
	public int getOffsetXInPixels() {
		return mPixelsX;
	}
	
	public float getOffsetX() {
		return mOffsetX;
	}
	
	public float getStepX() { 
		return mStepX;
	}
	
	public boolean isLocked() {
		return mLocked;
	}
	
	public void locked(boolean locked) {
		mLocked = locked;
	}
	
	public float current() {
		return (mOffsetX / mStepX);
	}
	
	public int getWidth() {
		return mWidth;
	}
	
	public int getHeight() {
		return mHeight;
	}
	
	public int getDensity() {
		return mDensity;
	}
}
