package com.andreashedin.infowallpaper;

import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

public class ColorHandler {
	
	protected int mColor0 = 0xff000000;
	protected int mColor1 = 0xff000000;
	protected LinearGradient mGradient = null;
	protected float mShadowRadius = 0.0f;
	protected int mShadowOffsetX = 0;
	protected int mShadowOffsetY = 0;
	
	public ColorHandler() {
		
	}
	
	public ColorHandler(int color) {
		mColor0 = color;
	}
	
	public ColorHandler(int topColor, int bottomColor) {
		mColor0 = topColor;
		mColor1 = bottomColor;
		mGradient = new LinearGradient(0, 0, 0, Phone.instance().screen().getHeight() + 1, topColor, bottomColor, Shader.TileMode.REPEAT);
	}
	
	public void setColor(int color) {
		mColor0 = color;
		mGradient = null;
	}
	
	public void setColor(int topColor, int bottomColor) {
		mColor0 = topColor;
		mColor1 = bottomColor;
		mGradient = new LinearGradient(0, 0, 0, Phone.instance().screen().getHeight() + 1, topColor, bottomColor, Shader.TileMode.REPEAT);
	}
	
	public void setShadow(float radius, int offsetX, int offsetY) {
		mShadowRadius = radius;
		mShadowOffsetX = offsetX;
		mShadowOffsetY = offsetY;
	}
	
	public Paint getPaint(Paint paint) {
		paint.setShader(mGradient);
		if(mShadowRadius > 0.0f)
			paint.setShadowLayer(mShadowRadius, mShadowOffsetX, mShadowOffsetY, 0xff000000);
		else
			paint.clearShadowLayer();
		
		if(mGradient == null)
			paint.setColor(mColor0);
		
		return paint;
	}

	public void update() {
		if(mGradient != null) {
			setColor(mColor0, mColor1);
		}
	}
}
