package com.andreashedin.infowallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;

public class Background {

	private Bitmap mImage = null;
	private ColorHandler mColor = null;
	private Paint mPaint = new Paint();
	private float mOffset = 0.0f;
	
	public Background() {
		
	}
	
	public boolean hasDifferentScreens() {
		return false;
	}
	
	public void setColor(int color) {
		if(mColor == null) {
			mColor = new ColorHandler(color);
		}
		else
			mColor.setColor(color);
	}
	
	public void setColor(int colorTop, int colorBottom) {
		if(mColor == null) {
			mColor = new ColorHandler(colorTop, colorBottom);
		}
		else {
			mColor.setColor(colorTop, colorBottom);
		}
	}
	
	public boolean setOffset(float offsetX) {
		mOffset = offsetX;
		
		if(imgExtraWidth > 0.0f)
			return true;
		
		return false;
	}
	
	public void clearColor() {
		mColor = null;
		mPaint.setColor(0xffffffff);
		mPaint.setShader(null);
	}
	
	private String mCurrentFile = "";
	public boolean setImage(String file) throws Exception {
		if(file.equals(mCurrentFile) == false)
			clearImage();
		else
			return true;
		
		if(file.equals("") == false) {
			//Options options = new Options();
			//options.inDensity = Phone.instance().screen().getDensity();
			
			try {
				mImage = BitmapFactory.decodeFile(file);
			}
			catch(Exception ex) {
				try {
					Options options = new Options();
					options.inSampleSize = 2;
					System.gc();
					mImage = BitmapFactory.decodeFile(file, options);
				}
				catch(Exception ex2) {
					mImage = null;
					throw ex2;
				}
			}
		}
		
		if(mImage != null) {
			mNewImage = true;
			mCurrentFile = file;
		}
		
		return (mImage != null);
	}
	
	public void clearImage() {
		if(mImage != null)
			mImage.recycle();
		mImage = null;
	}

	private int imgExtraWidth = 0;
	public void decideImageScaling() {
		if(mImage != null) {
			int screenHeight =Phone.instance().screen().getHeight();
			int screenWidth = Phone.instance().screen().getWidth();
			int imgHeight = mImage.getHeight();
			int imgWidth = mImage.getWidth();
			
			int newImgWidth = (int)(((float)imgWidth / (float)imgHeight) * screenHeight);
			if(newImgWidth < screenWidth)
				newImgWidth = screenWidth;
			
			mImage = Bitmap.createScaledBitmap(mImage, newImgWidth, screenHeight, true);
			
			imgExtraWidth = newImgWidth - screenWidth;
		}
	}
	
	private boolean mNewImage = false;
	public void draw(Canvas canvas) { 
		canvas.drawColor(0xff000000, PorterDuff.Mode.CLEAR);
		
		if(mImage != null) {
			if(mNewImage == true) {
				decideImageScaling();
				mNewImage = false;
			}
			mPaint.setColor(0xffffffff);
			mPaint.setFilterBitmap(true);
			mPaint.setDither(true);
			canvas.drawBitmap(mImage, -(imgExtraWidth * mOffset), 0.0f, mPaint);
		}
		
		mPaint = mColor.getPaint(mPaint);
		canvas.drawRect(0, 0, Phone.instance().screen().getWidth(), Phone.instance().screen().getHeight(), mPaint);
	}
	
	public void update() {
		mColor.update();
	}
}
