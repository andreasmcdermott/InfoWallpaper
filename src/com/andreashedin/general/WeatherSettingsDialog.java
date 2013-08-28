package com.andreashedin.general;

import java.util.ArrayList;

import com.andreashedin.infowallpaper.DisplayValuePair;
import com.andreashedin.infowallpaper.R;
import com.andreashedin.infowallpaper.WeatherHandler;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class WeatherSettingsDialog extends Dialog implements android.view.View.OnClickListener, OnItemSelectedListener, OnSeekBarChangeListener {

	private RadioGroup mTempRadios;
	private RadioButton mTempC;
	private RadioButton mTempF;
	private EditText mLocationInput;
	private EditText mUpdateInput;
	private CheckBox mUpdateWeather;
	private Button mSave;
	private Button mCancel;
	private Spinner mIconSet;
	private String mIconSetFilename = "";
	private SeekBar mIconSize;
	private TextView mIconSizeText;
	
	WeatherSettingsListener mListener;
	
	public interface WeatherSettingsListener {
		void weatherSettingsSaved();
	}
	
	public WeatherSettingsDialog(Context context, WeatherSettingsListener listener) {
		super(context);
		mListener = listener;
	}
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        setContentView(R.layout.weather_settings_dialog);
        
        mTempRadios = (RadioGroup)findViewById(R.id.tempRadios);
        mTempC = (RadioButton)findViewById(R.id.tempC);
        mTempF = (RadioButton)findViewById(R.id.tempF);
        mLocationInput = (EditText)findViewById(R.id.weatherLocationInput);
        mUpdateInput = (EditText)findViewById(R.id.weatherUpdateInput);
        mUpdateWeather = (CheckBox)findViewById(R.id.updateWeather);
        mSave = (Button)findViewById(R.id.weatherSave);
        mCancel = (Button)findViewById(R.id.weatherCancel);
        mIconSet = (Spinner) findViewById(R.id.iconSetList);
        mIconSize = (SeekBar) findViewById(R.id.iconSizeBar);
        mIconSizeText = (TextView) findViewById(R.id.iconSizeText);
        
        mUpdateWeather.setChecked(WeatherHandler.instance().updateMinutes() > 0);
        mLocationInput.setText(WeatherHandler.instance().getLocation());
        mTempC.setChecked(WeatherHandler.instance().useFahrenheit() == false);
        mTempF.setChecked(WeatherHandler.instance().useFahrenheit() == true);
        if(WeatherHandler.instance().updateMinutes() > 0)
        	mUpdateInput.setText(String.valueOf(WeatherHandler.instance().updateMinutes()));
        else
        	mUpdateInput.setText(String.valueOf(60));
        mSave.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mIconSize.setProgress(WeatherHandler.instance().getIconSize());
        mIconSize.setOnSeekBarChangeListener(this);
        mIconSizeText.setText(String.valueOf(WeatherHandler.instance().getIconSize()));
        
        try {
        	String[] iconSetValues = getContext().getResources().getStringArray(R.array.iconsetValues);
        	String[] iconSetDisplays = getContext().getResources().getStringArray(R.array.iconsetDisplays);
	        
	        ArrayList<DisplayValuePair<String>> iconSetList = new ArrayList<DisplayValuePair<String>>();
	        int selected = 0;
	        for(int i = 0; i < iconSetValues.length && i < iconSetDisplays.length; ++i) {
	        	if(iconSetValues[i].equals(WeatherHandler.instance().getIconSet()))
	        		selected = i;
	        	
	        	iconSetList.add(new DisplayValuePair<String>(iconSetDisplays[i], iconSetValues[i]));
	        }
	        ArrayAdapter<DisplayValuePair<String>> adapter = new ArrayAdapter<DisplayValuePair<String>>(getContext(), R.layout.list_item, iconSetList);
	        mIconSet.setAdapter(adapter);
	        mIconSet.setSelection(selected);
        }
        catch(Exception ex) {
        }
        
        mIconSet.setOnItemSelectedListener(this);
        
        setTitle(getContext().getString(R.string.weatherSettingsTitle));
	}
	
	@Override
	public void onBackPressed() {
		cancel();
	}
	
	private void saveSettings() {
		int min = 0;
		try {
			min = Integer.parseInt(mUpdateInput.getText().toString());
		}
		catch(NumberFormatException e) {
			min = 0;
		}
		
		if(mUpdateWeather.isChecked())
			WeatherHandler.instance().updateMinutes(min);
		else
			WeatherHandler.instance().updateMinutes(0);
			
		WeatherHandler.instance().setLocation(mLocationInput.getText().toString());
		WeatherHandler.instance().useFahrenheit(mTempF.isChecked());
		WeatherHandler.instance().setIconSet(mIconSetFilename);
		WeatherHandler.instance().setIconSize(mIconSize.getProgress());
		
		mListener.weatherSettingsSaved();
		
		dismiss();
	}

	@Override
	public void onClick(View arg0) {
		if(arg0.getId() == R.id.weatherSave) {
			saveSettings();
		}
		else
			cancel();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		mIconSetFilename = ((DisplayValuePair<String>)mIconSet.getItemAtPosition(arg2)).getValue();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		mIconSetFilename = "";
	}

	@Override
	public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
		mIconSizeText.setText(String.valueOf(progress));
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}
}
