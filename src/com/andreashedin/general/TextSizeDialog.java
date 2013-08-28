package com.andreashedin.general;

import java.util.ArrayList;

import com.andreashedin.infowallpaper.DisplayValuePair;
import com.andreashedin.infowallpaper.R;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class TextSizeDialog extends Dialog implements View.OnClickListener, OnSeekBarChangeListener, OnItemSelectedListener {
	
	public interface OnSizeChangedListener {
		void sizeChanged(int size, String font);
	}

	private OnSizeChangedListener mListener;
	private int mStartSize;
	private SeekBar mSizeBar;
	private TextView mSizeInput;
	private TextView mSample;
	private Spinner mFontSpinner;
	private String mTypefaceFile;
	private Resources mResources;
	private String mText;
	
	TextSizeDialog(Context context, Resources resources, OnSizeChangedListener listener, String font, int startSize, String text) {
		super(context);
		
		mResources = resources;
		mListener = listener;
		mTypefaceFile = font;
		mStartSize = startSize;
		mText = text;
	}
	
	@Override
	public void onClick(View v) {
    	if(v.getId() == R.id.saveSizeDialogButton) {
    		mListener.sizeChanged(mSizeBar.getProgress(), mTypefaceFile);
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.size_slider); 
        
        Button b = (Button)findViewById(R.id.saveSizeDialogButton);
        b.setOnClickListener(this);
        b = (Button)findViewById(R.id.cancelSizeDialogButton);
        b.setOnClickListener(this);
        
        mSizeInput = (TextView)findViewById(R.id.editTextSizeInput);
        mSizeBar = (SeekBar)findViewById(R.id.sizeDialogBar);
        mSample = (TextView)findViewById(R.id.sampleText);
        mSizeBar.setOnSeekBarChangeListener(this);
        mSizeBar.setProgress(mStartSize);
        mFontSpinner = (Spinner)findViewById(R.id.fontSpinner);
        mFontSpinner.setOnItemSelectedListener(this);
        
        int fontSel = 0;
        try {        	
	        String[] fontValues = mResources.getStringArray(R.array.fontValues);	        
	    	String[] fontDisplays = mResources.getStringArray(R.array.fontDisplays);
	        
	        ArrayList<DisplayValuePair<String>> fontList = new ArrayList<DisplayValuePair<String>>();
	        for(int i = 0; i < fontValues.length && i < fontDisplays.length; ++i) {
	        	if(fontValues[i].equals(mTypefaceFile))
	        		fontSel = i;
	        	
	        	fontList.add(new DisplayValuePair<String>(fontDisplays[i], fontValues[i]));
	        }
	        ArrayAdapter<DisplayValuePair<String>> adapter = new ArrayAdapter<DisplayValuePair<String>>(getContext(), R.layout.list_item, fontList);
	        mFontSpinner.setAdapter(adapter);
	        mFontSpinner.setSelection(fontSel);
        }
        catch(Exception ex) {
        	
        }
        
        if(mText.length() > 0 && !mText.equals("#icon#")) {
	    	mText = mText.replace("#n", "\n");
	    	mSample.setText(mText);
        }
        
        setTitle(R.string.setFontAndSizeTitle);
        loadFont();
    }

    private void loadFont() {    	
    	Typeface tf = Typeface.DEFAULT;
		try {
    		AssetManager assets = getContext().getAssets();
    		if(mTypefaceFile.length() > 0)
    			tf = Typeface.createFromAsset(assets, "fonts/" + mTypefaceFile);
		}
		catch(Exception ex) {
			tf = null;
		}
		
		if(tf == null)
			tf = Typeface.DEFAULT;
		
		mSample.setTypeface(tf);
    }
    
	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		mSizeInput.setText(String.valueOf(mSizeBar.getProgress()));
		mSample.setTextSize(mSizeBar.getProgress());
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id) {
		mTypefaceFile = ((DisplayValuePair<String>)mFontSpinner.getItemAtPosition(pos)).getValue();
		loadFont();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		mTypefaceFile = "";
	}
}
