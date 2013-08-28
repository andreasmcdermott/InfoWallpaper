package com.andreashedin.infowallpaper;

import java.util.ArrayList;

import com.andreashedin.infowallpaper.CurrentSongDataCollector;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

public class InfoItem {
	public static final int TYPE_BATTERY = 0;
	public static final int TYPE_DATETIME = 1;
	public static final int TYPE_WEATHER = 2;
	public static final int TYPE_CURRENTSONG = 3;
	public static final int TYPE_PHONESTATUS = 4;
	public static final int TYPE_COUNT = 5;
	
	protected Paint mPaint = new Paint();
	protected String mFormat = "";
	protected String mText = "";
	protected float mX = 0.0f;
	protected float mY = 0.0f;
	protected ColorHandler mColor = new ColorHandler(0xffffffff);
	protected DataCollector[] mDataCollectors = new DataCollector[TYPE_COUNT];
	protected boolean mDrawBackdrop = false;
	protected float mBackdropPaddingX = -0.1f;
	protected float mBackdropPaddingY = -0.1f;
	protected int mBackdropColor = 0xAA000000;
	protected Rect mTextBounds = new Rect();
	protected float mBackdropCornerRadius = 0.0f;
	protected float mRotation = 0.0f;
	protected float mHeight = 0.0f;
	protected Paint mIconPaint = new Paint();
	protected boolean mNumbersAsText = false;
	protected String mTextCase = "none";
	protected int mScreen = -1;
	protected float mScreenOffset = -1.0f;	
	
	public static String getSampleText(String text) {
		String str = text;
		
		str = BatteryDataCollector.getSampleText(str);
		str = DateTimeDataCollector.getSampleText(str);
		str = CurrentSongDataCollector.getSampleText(str);
		str = WeatherDataCollector.getSampleText(str);
		str = PhoneStatusDataCollector.getSampleText(str);
		
		return str;
	}
	
	public static InfoItem loadFromSharedPreference(LiveInfoWallpaper parent, SharedPreferences prefs, AssetManager assets, int id) {
		String key = LiveInfoWallpaper.INFO_KEY_FORMAT + id;
		String sVal = prefs.getString(key, "");
		
		if(sVal.length() == 0)
			return null;
		
		boolean showWhenZero = prefs.getBoolean(LiveInfoWallpaper.SHOW_WHEN_ZERO, false);
		
		InfoItem item = new InfoItem(parent, sVal, showWhenZero);
		key = LiveInfoWallpaper.INFO_KEY_FONT + id;
		sVal = prefs.getString(key, "");
		item.loadFont(assets, sVal);
		key = LiveInfoWallpaper.INFO_KEY_ALIGN + id;
		sVal = prefs.getString(key, Align.CENTER.toString());
		item.setAlign(Align.valueOf(sVal));
		key = LiveInfoWallpaper.INFO_KEY_COLOR + id;
		int iVal = prefs.getInt(key, 0xffffffff);
		item.setColor(iVal);
		key = LiveInfoWallpaper.INFO_KEY_SIZE + id;
		iVal = prefs.getInt(key, 80);
		item.setTextSize(iVal);
		key = LiveInfoWallpaper.INFO_KEY_ROTATION + id;
		iVal = prefs.getInt(key, 0);
		item.setRotation((float)iVal);
		key = LiveInfoWallpaper.INFO_KEY_TEXT_CASE + id;
		sVal = prefs.getString(key, "none");
		item.mTextCase = sVal;
		//key = LiveInfoWallpaper.INFO_KEY_BACKDROP + id;
		//boolean bVal = prefs.getBoolean(key, false);
		//if(bVal)
		//	item.setBackdrop(10.0f, 10.0f, iVal / 10, 0x77000000);
		key = LiveInfoWallpaper.INFO_KEY_SHADOW + id;
		boolean bVal = prefs.getBoolean(key, true);
		if(bVal)
			item.setShadow(2.0f, 1, 1);
		key = LiveInfoWallpaper.INFO_KEY_NUMBERS_AS_TEXT + id;
		bVal = prefs.getBoolean(key, false);
		item.mNumbersAsText = bVal;
		key = LiveInfoWallpaper.INFO_KEY_X + id;
		int x = prefs.getInt(key, 200);
		key = LiveInfoWallpaper.INFO_KEY_Y + id;
		int y = prefs.getInt(key, 50);
		item.setPosition((float)x, (float)y);
		item.mScreen = prefs.getInt(LiveInfoWallpaper.INFO_KEY_ON_SCREEN + id, -1);
		
		return item;
	}
	
	public InfoItem(LiveInfoWallpaper parent, String format, boolean showWhenZero) {
		mShowWhenZero = showWhenZero;
		readInfoFormat(parent, format);
		setColor(0xffffffff);
		mPaint.setAntiAlias(true);
		mPaint.setTypeface(Typeface.DEFAULT);
		mIconPaint.setAntiAlias(true);
		mIconPaint.setColor(0xffffffff);
		mIconPaint.setFilterBitmap(true);
		mIconPaint.setDither(true);
	}
	
	public InfoItem(LiveInfoWallpaper parent, String format, int color, boolean showWhenZero) {
		mShowWhenZero = showWhenZero;
		readInfoFormat(parent, format);
		setColor(color);
		mPaint.setAntiAlias(true);
		mPaint.setTypeface(Typeface.DEFAULT);
		mIconPaint.setAntiAlias(true);
		mIconPaint.setColor(0xffffffff);
		mIconPaint.setFilterBitmap(true);
	}
	
	private boolean mShowWhenZero = false;
	public void setShowWhenZero(boolean val) {
		mShowWhenZero = val;
		if(mDataCollectors[TYPE_PHONESTATUS] != null) {
			((PhoneStatusDataCollector)mDataCollectors[TYPE_PHONESTATUS]).setShowWhenZero(val);
		}
	}
	
	public void draw(Canvas canvas) {
		//drawBackdrop(canvas);
		
		canvas.save();
		if(mScreen < 0.0f) {
			canvas.translate(mX, mY);
		}
		else {
			float width = (float)Phone.instance().screen().getWidth();
			float current = (float)Phone.instance().screen().current();
			float offset = current * width;
			mScreenOffset = mScreen * width;

			canvas.translate(mX + mScreenOffset - offset, mY);
		}
		canvas.rotate(mRotation);
		
		if(WeatherDataCollector.useIcon(mFormat)) {
			if(drawIcon(canvas))
				drawText(canvas);
		}
		else {
			drawText(canvas);
		}
		
		canvas.restore();
	}
	
	public boolean drawIcon(Canvas canvas) {		
		if(mDataCollectors[TYPE_WEATHER] == null)
			return false;
		
		Bitmap bitmap = ((WeatherDataCollector)mDataCollectors[TYPE_WEATHER]).getIcon(mFormat);
		
		if(bitmap != null) {		
			Rect srcRect = new Rect();
			srcRect.left = 0;
			srcRect.top = 0;
			srcRect.bottom = bitmap.getHeight();
			srcRect.right = bitmap.getWidth();
			
			Rect destRect = new Rect();
			destRect.left = 0;
			destRect.top = 0;
			destRect.bottom = WeatherHandler.instance().getIconSize();
			destRect.right = destRect.bottom;
			
			canvas.drawBitmap(bitmap, srcRect, destRect, mIconPaint);
		}
		
		return true;
	}
	
	public void drawText(Canvas canvas) {
		String[] str = mText.split("#n");
		for(int i = 0; i < str.length; ++i) {
			String s = str[i];
			
			if(mTextCase.equals("upper"))
				s = s.toUpperCase();
			else if(mTextCase.equals("lower"))
				s = s.toLowerCase();
				
			canvas.drawText(s, 0.0f, 0.0f + (mHeight * i + 4.0f), mPaint);
		}
	}
	
	public void drawBackdrop(Canvas canvas) {
		if(mDrawBackdrop) {
			RectF backdrop = new RectF();
					
			backdrop.left = mX + (mTextBounds.left - mBackdropPaddingX);
			backdrop.top = mY + (mTextBounds.top - mBackdropPaddingY);
			backdrop.right = mX + (mTextBounds.right + mBackdropPaddingX);
			backdrop.bottom = mY + (mTextBounds.bottom + mBackdropPaddingY);
			
			Paint backdropPaint = new Paint(mPaint);
			backdropPaint.setShader(null);
			backdropPaint.setColor(mBackdropColor);
			canvas.drawRoundRect(backdrop, mBackdropCornerRadius, mBackdropCornerRadius, backdropPaint);
			backdropPaint = null;
		}

	}
	
	public void update() {
		mText = mFormat;
		
		for(int i = 0; i < mDataCollectors.length; ++i) {
			if(mDataCollectors[i] != null) {
				mText = mDataCollectors[i].updateInfoString(mText, mNumbersAsText);
			}
		}
		
		mPaint = mColor.getPaint(mPaint);
		
		mPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
		
		if(mPaint.getTextAlign() == Align.RIGHT) {
			mTextBounds.left -= mTextBounds.right;
			mTextBounds.right = 0;
		}
		else if(mPaint.getTextAlign() == Align.CENTER) {
			mTextBounds.right /= 2;
			mTextBounds.left -= mTextBounds.right;
		}

		Paint.FontMetrics metrics = new Paint.FontMetrics();
		mPaint.getFontMetrics(metrics);
		mHeight = Math.abs(metrics.bottom) + Math.abs(metrics.top);
		
	}
	
	public void update(int type, Object object) {
		if(mDataCollectors[type] != null) {
			mDataCollectors[type].update(object);
		}
	}
	
	public void loadFont(AssetManager assets, String font) {
		Typeface tf = null;
		try {
			tf = Typeface.createFromAsset(assets, "fonts/" + font);
			if(tf == null)
				tf = Typeface.DEFAULT;
		}
		catch(Exception ex) {
			tf = Typeface.DEFAULT;
		}
		
		mPaint.setTypeface(tf);
	}
	
	public void setAlign(Align align) {
		mPaint.setTextAlign(align);
	}
	
	public void setRotation(float rot) {
		mRotation = rot;
	}
	
	public void setPosition(float x, float y) {
		mX = x;
		mY = y;
	}
	
	public void setTextSize(float size) {
		mPaint.setTextSize(size);
	}
	
	public void setColor(int color) {
		mColor.setColor(color);
	}
	
	public void setColor(int colorTop, int colorBottom) {
		mColor.setColor(colorTop, colorBottom);
	}
	
	public void setShadow(float radius, int offsetX, int offsetY) {
		mColor.setShadow(radius, offsetX, offsetY);
	}
	
	public void removeShadow() {
		mColor.setShadow(0.0f, 0, 0);
	}
	
	public void setBackdrop(float verticalPadding, float horizontalPadding, float cornerRadius, int color) {
		mBackdropPaddingY = verticalPadding;
		mBackdropPaddingX = horizontalPadding;
		mBackdropColor = color;
		mDrawBackdrop = true;
		mBackdropCornerRadius = cornerRadius;
	}
	
	public void removeBackdrop() {
		mDrawBackdrop = false;
	}
	
	protected void readInfoFormat(LiveInfoWallpaper parent, String str) {
		mFormat = str;
		
		if(BatteryDataCollector.contains(str)) {
			mDataCollectors[TYPE_BATTERY] = new BatteryDataCollector(parent);
		}
		if(showDateTime(str)) {
			mDataCollectors[TYPE_DATETIME] = new DateTimeDataCollector(parent);
		}
		if(showCurrentSong(str)) {
			mDataCollectors[TYPE_CURRENTSONG] = new CurrentSongDataCollector(parent);
		}
		if(WeatherDataCollector.contains(str)) {
			mDataCollectors[TYPE_WEATHER] = new WeatherDataCollector(parent);
		}
		if(PhoneStatusDataCollector.contains(str)) {
			mDataCollectors[TYPE_PHONESTATUS] = new PhoneStatusDataCollector(parent, mShowWhenZero);
		}
	}
	
	protected boolean showDateTime(String str) {
		if(str.contains("#yy"))
			return true;
		else if(str.contains("#MM"))
			return true;
		else if(str.contains("#dd"))
			return true;
		else if(str.contains("#ww"))
			return true;
		else if(str.contains("#mm"))
			return true;
		else if(str.contains("#hh"))
			return true;
		else if(str.contains("#HH"))
			return true;
		else if(str.contains("#ampm"))
			return true;
		
		return false;
	}
	
	protected boolean showCurrentSong(String str) {
		if(str.contains("#aa"))
			return true;
		else if(str.contains("#ll"))
			return true;
		else if(str.contains("#ss"))
			return true;
		
		return false;
	}
	
	protected boolean isType(int type) {
		if(mDataCollectors[type] != null)
			return true;
		
		return false;
	}
}
