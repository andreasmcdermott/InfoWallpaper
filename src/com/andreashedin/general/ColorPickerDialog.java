package com.andreashedin.general;

import com.andreashedin.infowallpaper.R;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class ColorPickerDialog extends Dialog 
	implements View.OnClickListener,
	SeekBar.OnSeekBarChangeListener {
	
    public interface OnColorChangedListener {
        void colorChanged(int color, String key);
    }

    private OnColorChangedListener mListener;
    private int mInitialColor;
    private String mKey = "";
    
    private SeekBar mRedBar;
    private SeekBar mBlueBar;
    private SeekBar mGreenBar;
    private SeekBar mTransparencyBar;
    private TextView mColorInput;
    private TextView mRedInput;
    private TextView mGreenInput;
    private TextView mBlueInput;
    private TextView mTransparencyInput;
    private TextView mColorPreview;

    public ColorPickerDialog(Context context, OnColorChangedListener listener, int initialColor, String key) {
        super(context);

        mKey = key;
        mListener = listener;
        mInitialColor = initialColor;
        
    }
    
    @Override
	public void onClick(View v) {
    	if(v.getId() == R.id.buttonSetColor) {
    		mListener.colorChanged(getColor(), mKey);
    		dismiss();
    	}
    	else {
    		cancel();
    	}
	}
    
    @Override
	public void onBackPressed() {
		cancel();
	}
    
    private int getColor() {
    	int alpha = mTransparencyBar.getProgress();
    	int red = mRedBar.getProgress();
    	int green = mGreenBar.getProgress();
    	int blue = mBlueBar.getProgress();
    	
    	return Color.argb(alpha, red, green, blue);
    }
    
    private void updateColor() {
    	int red = mRedBar.getProgress();
    	int green = mGreenBar.getProgress();
    	int blue = mBlueBar.getProgress();
    	int alpha = mTransparencyBar.getProgress();
    	
    	int color = Color.argb(alpha, red, green, blue);
    	
    	setColor(color);
    }
    
    private void setColor(int color) {
    	int red = Color.red(color);
    	int green = Color.green(color);
    	int blue = Color.blue(color);
    	int alpha = Color.alpha(color);
    	
    	mRedBar.setProgress(red);
    	mGreenBar.setProgress(green);
    	mBlueBar.setProgress(blue);
    	mTransparencyBar.setProgress(alpha);
    	
    	mRedInput.setText(String.valueOf(red));
    	mGreenInput.setText(String.valueOf(green));
    	mBlueInput.setText(String.valueOf(blue));
    	mTransparencyInput.setText(String.valueOf(alpha));
    	
    	mColorPreview.setBackgroundColor(Color.rgb(red, green, blue));
    	
    	String hexRed = Integer.toHexString(red);
    	String hexGreen = Integer.toHexString(green);
    	String hexBlue = Integer.toHexString(blue);
    	
    	String hex = leadingZero(hexRed) + leadingZero(hexGreen) + leadingZero(hexBlue);
    	mColorInput.setText(hex);
    }
    
    private String leadingZero(String in) {
    	String out = in;
    	
    	if (in.length() < 2) {
    		out = "0" + in;
    	}
    	
    	return out;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.color_picker); 
        
        Button b = (Button)findViewById(R.id.buttonSetColor);
        b.setOnClickListener(this);
        b = (Button)findViewById(R.id.buttonCancel);
        b.setOnClickListener(this);
        
        mRedBar = (SeekBar)findViewById(R.id.seekBarRed);
        mRedBar.setOnSeekBarChangeListener(this);
        mGreenBar = (SeekBar)findViewById(R.id.seekBarGreen);
        mGreenBar.setOnSeekBarChangeListener(this);
        mBlueBar = (SeekBar)findViewById(R.id.seekBarBlue);
        mBlueBar.setOnSeekBarChangeListener(this);
        mTransparencyBar = (SeekBar)findViewById(R.id.seekBarTransparency);
        mTransparencyBar.setOnSeekBarChangeListener(this);
        
        mRedInput = (TextView)findViewById(R.id.redInput);
        //mRedInput.addTextChangedListener(this);
        //mRedInput.setEnabled(false);
        mGreenInput = (TextView)findViewById(R.id.greenInput);
        //mGreenInput.setEnabled(false);
        //mGreenInput.addTextChangedListener(this);
        mBlueInput = (TextView)findViewById(R.id.blueInput);
        //mBlueInput.setEnabled(false);
        //mBlueInput.addTextChangedListener(this);
        mTransparencyInput = (TextView)findViewById(R.id.transparencyInput);
        //mTransparencyInput.setEnabled(false);
        //mTransparencyInput.addTextChangedListener(this);
        mColorInput = (TextView)findViewById(R.id.htmlColorInput);
        //mColorInput.setEnabled(false);
        
        mColorPreview = (TextView)findViewById(R.id.colorPreview);
        
        setColor(mInitialColor);
        setTitle(R.string.colorPickerTitle);
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser) {
			if(seekBar.getId() == R.id.seekBarRed) {
				mRedInput.setText(String.valueOf(progress));
			}
			else if(seekBar.getId() == R.id.seekBarGreen) {
				mGreenInput.setText(String.valueOf(progress));
			}
			else if(seekBar.getId() == R.id.seekBarBlue) {
				mBlueInput.setText(String.valueOf(progress));
			}
			else if(seekBar.getId() == R.id.seekBarTransparency) {
				mTransparencyInput.setText(String.valueOf(progress));
			}
			
			updateColor();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { }

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { }
}