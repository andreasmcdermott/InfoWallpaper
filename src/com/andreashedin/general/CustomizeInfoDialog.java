package com.andreashedin.general;

import java.util.ArrayList;

import com.andreashedin.general.ColorPickerDialog.OnColorChangedListener;
import com.andreashedin.general.TextSizeDialog.OnSizeChangedListener;
import com.andreashedin.infowallpaper.DisplayValuePair;
import com.andreashedin.infowallpaper.InfoData;
import com.andreashedin.infowallpaper.InfoItem;
import com.andreashedin.infowallpaper.Phone;
import com.andreashedin.infowallpaper.R;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CustomizeInfoDialog extends Dialog 
	implements View.OnClickListener, 
	OnItemSelectedListener, OnColorChangedListener, OnSizeChangedListener {

	public interface CustomizeInfoListener {
		void infoCustomized(InfoData settings, int id, boolean flag);
	}
	
	private int mId;
	private CustomizeInfoListener mListener;
	private InfoData mInitialSettings;
	private EditText mFormatInput;
	private Spinner mPresetSpinner;
	private Button mSizeButton;
	private RadioGroup mAlignGroup;
	private RadioButton mAlignLeft;
	private RadioButton mAlignCenter;
	private RadioButton mAlignRight;
	private RadioGroup mCaseGroup;
	private RadioButton mNoCase;
	private RadioButton mUpperCase;
	private RadioButton mLowerCase;
	private CheckBox mShadow;
	private CheckBox mNumbersAsText;
	private Button mAddPresetButton;
	private Button mColorButton;
	private TextView mColorPreview;
	private EditText mTextX;
	private EditText mTextY;
	private EditText mOrderInput;
	private int mColor;
	private Resources mResources;
	private boolean mFlag = false;
	private String mLastPresetSelected = "#MM/#dd/#yyyy";
	private int mSize = 0;
	private String mFont = "";
	private Spinner mRotationSpinner;
	private int mRotation = 0;
	private Spinner mOnScreenSpinner;
	private int mScreen = -1;

	public CustomizeInfoDialog(Context context, CustomizeInfoListener listener, InfoData currentSettings, int id, boolean flag) {
		super(context);
		
		mResources = context.getResources();
		mListener = listener;
		mInitialSettings = currentSettings;
		mId = id;
		mFlag = flag;
	}

	@Override
	public void onClick(View v) {
    	if(v.getId() == R.id.infoButtonDone) {
    		if(sendSettings())
    			dismiss();
    		else
    			Toast.makeText(this.getContext(), R.string.customizeErrorMessage, Toast.LENGTH_SHORT).show();
    	}
    	else if(v.getId() == R.id.addPreset) {
    		mFormatInput.append(mLastPresetSelected);
    	}
    	else if(v.getId() == R.id.setFontPropButton) {    		
    		new TextSizeDialog(getContext(), mResources, this, mFont, mSize, InfoItem.getSampleText(mFormatInput.getText().toString())).show();
    	}
    	else if(v.getId() == R.id.setTextColor) {
    		new ColorPickerDialog(getContext(), this, mColor, "").show();
    	}
    	else {
    		cancel();
    	}
	}
    
    @Override
	public void onBackPressed() {
		cancel();
	}

	private boolean sendSettings() {
    	InfoData settings = new InfoData();
    	
    	settings.backdrop = false;
    	settings.shadow = mShadow.isChecked();
    	settings.numbersAsText = mNumbersAsText.isChecked();
    	settings.color = mColor;
    	settings.font = mFont;
    	settings.size = mSize;
    	settings.text = mFormatInput.getText().toString();
    	if(mTextX.getText().toString().length() > 0 && isValid(mTextX.getText().toString()))
    		settings.x = Integer.parseInt(mTextX.getText().toString());
    	if(mTextY.getText().toString().length() > 0 && isValid(mTextX.getText().toString()))
    		settings.y = Integer.parseInt(mTextY.getText().toString());
    	int alignId = mAlignGroup.getCheckedRadioButtonId();
    	if(alignId == R.id.alignTextCenter)
    		settings.textAlign = Align.CENTER;
    	else if(alignId == R.id.alignTextRight)
    		settings.textAlign = Align.RIGHT;
    	settings.textcase = "none";
    	int caseId = mCaseGroup.getCheckedRadioButtonId();
    	if(caseId == R.id.upperCase)
    		settings.textcase = "upper";
    	else if(caseId == R.id.lowerCase)
    		settings.textcase = "lower";
    	String str = mOrderInput.getText().toString();
    	if(str.length() > 0 && isValid(str))
    		settings.order = Integer.parseInt(str); 
    	settings.rotation = mRotation;
    	if(settings.text.length() == 0) { 
    		settings = null;
    		return false;
    	}
    	settings.screen = mScreen;
    	
    	mListener.infoCustomized(settings, mId, mFlag);
    	
    	return true;
    }
    
    private boolean isValid(String str) {
    	for(int i = 0; i < str.length(); ++i) {
    		if(str.charAt(i) < '0' || str.charAt(i) > '9')
    			return false;
    	}
    	
    	return true;
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.customize_info); 

        Button b = (Button)findViewById(R.id.infoButtonDone);
        b.setOnClickListener(this);
        b = (Button)findViewById(R.id.infoButtonCancel);
        b.setOnClickListener(this);

        mFormatInput = (EditText) findViewById(R.id.infoFormat);
        mPresetSpinner = (Spinner) findViewById(R.id.presetSpinner);
        mAddPresetButton = (Button) findViewById(R.id.addPreset);
        mSizeButton = (Button) findViewById(R.id.setFontPropButton);
        mColorButton = (Button) findViewById(R.id.setTextColor);
        mAlignGroup = (RadioGroup) findViewById(R.id.alignText);
        mAlignLeft = (RadioButton) findViewById(R.id.alignTextLeft);
        mAlignCenter = (RadioButton) findViewById(R.id.alignTextCenter);
        mAlignRight = (RadioButton) findViewById(R.id.alignTextRight);
        mShadow = (CheckBox) findViewById(R.id.useShadow);
        mNumbersAsText = (CheckBox) findViewById(R.id.numbersAsText);
        mColorPreview = (TextView) findViewById(R.id.textColorPreview);
        mTextX = (EditText) findViewById(R.id.textXInput);
        mTextY = (EditText) findViewById(R.id.textYInput);
        mOrderInput = (EditText) findViewById(R.id.orderInput);
        mRotationSpinner = (Spinner) findViewById(R.id.textRotationSpinner);
        mCaseGroup = (RadioGroup) findViewById(R.id.textCaseGroup);
        mNoCase = (RadioButton) findViewById(R.id.noTextCase);
        mUpperCase = (RadioButton) findViewById(R.id.upperCase);
        mLowerCase = (RadioButton) findViewById(R.id.lowerCase);
        mFont = mInitialSettings.font;
        mOnScreenSpinner = (Spinner) findViewById(R.id.onScreenSpinner);

        try {
        	// Preset spinner
        	String[] presetValues = mResources.getStringArray(R.array.presetValues);
        	String[] presetDisplays = mResources.getStringArray(R.array.presetDisplays);
	        
	        ArrayList<DisplayValuePair<String>> presetList = new ArrayList<DisplayValuePair<String>>();
	        for(int i = 0; i < presetValues.length && i < presetDisplays.length; ++i) {
	        	presetList.add(new DisplayValuePair<String>(presetDisplays[i], presetValues[i]));
	        }
	        ArrayAdapter<DisplayValuePair<String>> adapter = new ArrayAdapter<DisplayValuePair<String>>(getContext(), R.layout.list_item, presetList);
	        mPresetSpinner.setAdapter(adapter);
	        
	        // Rotation spinner
	        String[] rotationValues = mResources.getStringArray(R.array.rotationValues);
        	String[] rotationDisplays = mResources.getStringArray(R.array.rotationDisplays);
	        
	        ArrayList<DisplayValuePair<Integer>> rotationList = new ArrayList<DisplayValuePair<Integer>>();
	        for(int i = 0; i < rotationValues.length && i < rotationDisplays.length; ++i) {
	        	rotationList.add(new DisplayValuePair<Integer>(rotationDisplays[i], Integer.parseInt(rotationValues[i])));
	        }
	        ArrayAdapter<DisplayValuePair<Integer>> adapterInt = new ArrayAdapter<DisplayValuePair<Integer>>(getContext(), R.layout.list_item, rotationList);
	        mRotationSpinner.setAdapter(adapterInt);
	        
	        // Screen spinner
	        int listSize = Phone.instance().screen().numberOfScreens();
	        listSize++;
	        
	        String[] screenValues = new String[listSize];
        	String[] screenDisplays = new String[listSize];
        	screenDisplays[0] = mResources.getString(R.string.allScreens);
        	screenValues[0] = "-1";
        	
        	for(int i = 1; i < listSize; ++i) {
        		screenDisplays[i] = "" + i;
        		screenValues[i] = "" + (i-1);
        	}
	        
	        ArrayList<DisplayValuePair<Integer>> screenList = new ArrayList<DisplayValuePair<Integer>>();
	        for(int i = 0; i < listSize; ++i) {
	        	screenList.add(new DisplayValuePair<Integer>(screenDisplays[i], Integer.parseInt(screenValues[i])));
	        }
	        adapterInt = new ArrayAdapter<DisplayValuePair<Integer>>(getContext(), R.layout.list_item, screenList);
	        mOnScreenSpinner.setAdapter(adapterInt);
        }
        catch(Exception ex) {
        	Log.d("mytag", "Setting up presets failed: " + ex.getMessage());
        }
        
        mPresetSpinner.setOnItemSelectedListener(this);
        mRotationSpinner.setOnItemSelectedListener(this);
        mOnScreenSpinner.setOnItemSelectedListener(this);
        
        if(mInitialSettings.rotation == 0)
        	mRotationSpinner.setSelection(0);
        else if(mInitialSettings.rotation == 90)
        	mRotationSpinner.setSelection(2);
        else if(mInitialSettings.rotation == 270)
        	mRotationSpinner.setSelection(1);
        
       	mOnScreenSpinner.setSelection(mInitialSettings.screen + 1);
        
        mFormatInput.setText(mInitialSettings.text);
        mSize = mInitialSettings.size;
        mSizeButton.setOnClickListener(this);
        mColorButton.setOnClickListener(this);
        mAddPresetButton.setOnClickListener(this);
        mShadow.setChecked(mInitialSettings.shadow);
        mNumbersAsText.setChecked(mInitialSettings.numbersAsText);
        mAlignLeft.setChecked(mInitialSettings.textAlign == Align.LEFT);
        mAlignCenter.setChecked(mInitialSettings.textAlign == Align.CENTER);
        mAlignRight.setChecked(mInitialSettings.textAlign == Align.RIGHT);
        mNoCase.setChecked(mInitialSettings.textcase.equals("none"));
        mUpperCase.setChecked(mInitialSettings.textcase.equals("upper"));
        mLowerCase.setChecked(mInitialSettings.textcase.equals("lower"));
        mColor = mInitialSettings.color;
        mColorPreview.setBackgroundColor(mColor);
        mTextX.setText(String.valueOf(mInitialSettings.x));
        mTextY.setText(String.valueOf(mInitialSettings.y));
        mOrderInput.setText(String.valueOf(mInitialSettings.order));
        setTitle(R.string.customizeInfoTitle);
    }

	@SuppressWarnings("unchecked")
	@Override
	public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
		if(spinner.getId() == R.id.presetSpinner)
			mLastPresetSelected = ((DisplayValuePair<String>)mPresetSpinner.getItemAtPosition(position)).getValue();
		else if(spinner.getId() == R.id.textRotationSpinner)
			mRotation = ((DisplayValuePair<Integer>)mRotationSpinner.getItemAtPosition(position)).getValue().intValue();
		else 
			mScreen = ((DisplayValuePair<Integer>)mOnScreenSpinner.getItemAtPosition(position)).getValue().intValue();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		mLastPresetSelected = "";
	}

	@Override
	public void colorChanged(int color, String key) {
		mColor = color;		
		mColorPreview.setBackgroundColor(mColor);
	}

	@Override
	public void sizeChanged(int size, String font) {
		mSize = size;
		mFont = font;
	}
}
