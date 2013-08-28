package com.andreashedin.general;

import java.util.ArrayList;

import com.andreashedin.infowallpaper.InfoData;
import com.andreashedin.infowallpaper.R;
import com.andreashedin.infowallpaper.WeatherHandler;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class PositionInfosDialog extends Dialog {
	
	private class PositionView extends View implements 
	View.OnClickListener {

		private ArrayList<InfoData> mList;
		private Dialog mParent;
		private OnSavePositionsListener mListener;
		private Paint mPaint;
		private ArrayList<Typeface> mFonts;
		
		public PositionView(Context context, Dialog parent, OnSavePositionsListener listener, ArrayList<InfoData> items) {
			super(context);
			
			mListener = listener;
			mParent = parent;
			mList = items;		
			
			Typeface tf = null;
			mFonts = new ArrayList<Typeface>();
			for(int i = 0; i < mList.size(); ++i) {
				try {
					tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + mList.get(i).font);
				}
				catch(Exception ex) 
				{
					tf = null;
				}
				
				if(tf != null)
					mFonts.add(tf);
				else
					mFonts.add(Typeface.DEFAULT);
			}
			
			mPaint = new Paint();
		}
		
		@Override 
        protected void onDraw(Canvas canvas) {
            canvas.save();
            
            
            for(int i = 0; i < mList.size(); ++i) {
            	if(mList.get(i) != null) {
            		canvas.save();
	            	canvas.translate(mList.get(i).x, mList.get(i).y);
	            	canvas.rotate((float)mList.get(i).rotation);
	            	
	            	mPaint.setTextSize(mList.get(i).size);
	            	mPaint.setAntiAlias(true);
	            	mPaint.setTypeface(mFonts.get(i));
	            	mPaint.setTextAlign(mList.get(i).textAlign);
	            	mPaint.setColor(0xff000000);
	            	
            		if(mList.get(i).text.equals("#icon#")) {
            			canvas.drawRect(0.0f, 0.0f, WeatherHandler.instance().getIconSize(), WeatherHandler.instance().getIconSize(), mPaint);
            		}
            		else {
	            		
		            	Paint.FontMetrics metrics = new Paint.FontMetrics();
		        		mPaint.getFontMetrics(metrics);
		        	
		        		float height = Math.abs(metrics.bottom) + Math.abs(metrics.top);
		            	
		            	String[] str = mList.get(i).text.split("#n");
		            	for(int s = 0; s < str.length; ++s) {
		            		canvas.drawText(str[s], 0.0f, 0.0f + (height * s + 4.0f), mPaint);
		            	}
            		}
            		
		            canvas.restore();
            	}
            }
            
            canvas.restore();
        }
		
		private int mClickedItem = -1;
		private float mOffsetX = 0.0f;
		private float mOffsetY = 0.0f;
		@Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                	mClickedItem = clickedAt(event.getRawX(), event.getRawY());
                	if(mClickedItem >= 0) {
                		mOffsetX = (float)mList.get(mClickedItem).x - event.getRawX();
                		mOffsetY = (float)mList.get(mClickedItem).y - event.getRawY();
                	}
                    break;
                case MotionEvent.ACTION_MOVE:
                	if(mClickedItem >= 0) {
                		mList.get(mClickedItem).x = (int)(event.getRawX() + 0.5f + mOffsetX);
                		mList.get(mClickedItem).y = (int)(event.getRawY() + 0.5f + mOffsetY);
                		invalidate();
                	}
                    break;
                case MotionEvent.ACTION_UP:
                	if(mClickedItem >= 0) {
                		mList.get(mClickedItem).x = (int)(event.getRawX() + 0.5f + mOffsetX);
                		mList.get(mClickedItem).y = (int)(event.getRawY() + 0.5f + mOffsetY);
                		mClickedItem = -1;
                		invalidate();
                	}
                    break;
            }
            
            return true;
        }
		
		private int clickedAt(float x, float y) {
			int id = -1;
			
			Rect rect = new Rect();
			boolean vertical = false;
			for(int i = 0; i < mList.size(); ++i) {
				
				if(mList.get(i).text.equals("#icon#")) {
					rect.left = 0;
					rect.top = 0;
					rect.right = WeatherHandler.instance().getIconSize();
					rect.bottom = WeatherHandler.instance().getIconSize();
				}
				else {
					String[] str = mList.get(i).text.split("#n");
					
					mPaint.setTextSize(mList.get(i).size);
	            	mPaint.setColor(mList.get(i).color);	        	            	
	            	mPaint.setTypeface(mFonts.get(i));
	            	mPaint.setTextAlign(mList.get(i).textAlign);
	            	if(mList.get(i).rotation == 0)
	            		vertical = false;
	            	else
	            		vertical = true;
	            	
	            	
	            	mPaint.getTextBounds(mList.get(i).text, 0, mList.get(i).text.length(), rect);      	
	            	
	            	if(mList.get(i).textAlign.equals(Align.CENTER)) {
	            		rect.right /= 2;
	            		rect.left -= rect.right;
	            	}
	            	else if(mList.get(i).textAlign.equals(Align.RIGHT)) {
	            		rect.left -= rect.right;
	            		rect.right = 0;
	            	}
	            	
	            	if(vertical) {
	            		int height = Math.abs(rect.left) + Math.abs(rect.right);
	            		if(height < 25)
	            			height = 25;
	            		
	            		rect.left = rect.top;
	            		rect.right = rect.bottom;
	            		
	            		int width = rect.right - rect.left;
	            		if(width < 25) {
	            			rect.right += (25 - width) / 2;
	            			rect.left -= (25 - width) / 2;
	            		}
	            		
	            		rect.bottom = height;
	            		rect.top = 0;
	            	}
				}
				
            	if(x >= (float)(mList.get(i).x + rect.left) && x <= (float)(mList.get(i).x + rect.right)) {
            		if(y >= (float)(mList.get(i).y + rect.top) && y <= (float)(mList.get(i).y + rect.bottom)) {
            			id = i;
            		}
            	}
			}

			return id;
		}
		
		public void save() {
			mListener.savePositions(mList);
		}

		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.savePositionsButton) {
				save();
			}
			else 
				mParent.cancel();
		}
	}
	
	public interface OnSavePositionsListener {
		public void savePositions(ArrayList<InfoData> items);
	}
	
	private OnSavePositionsListener mListener;
	private ArrayList<InfoData> mList;
	
	public PositionInfosDialog(Context context, OnSavePositionsListener listener, ArrayList<InfoData> items) {
		super(context, R.style.Dialog_Fullscreen);
		
		mListener = listener;
		mList = items;
	}
	
	@Override
	public void onBackPressed() {
		cancel();
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnSavePositionsListener l = new OnSavePositionsListener() {

			@Override
			public void savePositions(ArrayList<InfoData> items) {
				mListener.savePositions(items);
                dismiss();
			}
        };

        setContentView(R.layout.position_infos);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.RelativeLayout01);
        
        PositionView pv = new PositionView(getContext(), this, l, mList);
        
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, 
        		RelativeLayout.LayoutParams.FILL_PARENT);
        pv.setLayoutParams(lp);
        pv.setBackgroundColor(0xff888888);
        
        Button b = (Button) findViewById(R.id.savePositionsButton);
        b.setOnClickListener(pv);
        b = (Button) findViewById(R.id.cancelPositionsButtons);
        b.setOnClickListener(pv);

        rl.addView(pv, 0);
    }
}
