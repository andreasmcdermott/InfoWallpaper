package com.andreashedin.infowallpaper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.andreashedin.general.ColorPickerDialog;
import com.andreashedin.general.CustomizeInfoDialog;
import com.andreashedin.general.CustomizeInfoDialog.CustomizeInfoListener;
import com.andreashedin.general.EnterStringDialog;
import com.andreashedin.general.EnterStringDialog.StringEnteredListener;
import com.andreashedin.general.PickItemDialog;
import com.andreashedin.general.PickItemDialog.OnItemPickedListener;
import com.andreashedin.general.PositionInfosDialog;
import com.andreashedin.general.PositionInfosDialog.OnSavePositionsListener;
import com.andreashedin.general.SelectConfigurationDialog;
import com.andreashedin.general.SelectConfigurationDialog.SelectConfigurationListener;
import com.andreashedin.general.WeatherSettingsDialog;
import com.andreashedin.general.WeatherSettingsDialog.WeatherSettingsListener;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class LiveInfoSettings extends PreferenceActivity
    implements 
    SharedPreferences.OnSharedPreferenceChangeListener,
    ColorPickerDialog.OnColorChangedListener,
    Preference.OnPreferenceClickListener,
    CustomizeInfoListener, 
    OnItemPickedListener, 
    OnSavePositionsListener, 
    StringEnteredListener, 
    SelectConfigurationListener, 
    WeatherSettingsListener, 
    OnPreferenceChangeListener {
	
	
	private static final int REMOVE_ITEM = 0;
	private static final int EDIT_ITEM = 1;
	private static final int LOAD_FROM = 2;
	
	private static final int FROM_WEB = 0;
	private static final int FROM_PHONE = 1;
	
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getPreferenceManager().setSharedPreferencesName(LiveInfoWallpaper.SHARED_PREFERENCES_NAME);
        addPreferencesFromResource(LiveInfoWallpaper.SETTINGS_FILE);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }
	
	private void clearPreference(String key) {
		Editor editor = getPreferenceManager().getSharedPreferences().edit();
		editor.remove(key);
        editor.commit();
        editor = null;
	}
	
	private void chooseBackgroundImage(Context context) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, getString(R.string.imagePickerTitle)), 1);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == RESULT_OK) {
	        if (requestCode == 1) {
	            Uri imageUri = data.getData();
	            
	            String imagePath = getPath(imageUri);

	            Editor editor = getPreferenceManager().getSharedPreferences().edit();
	            editor.putString(LiveInfoWallpaper.BACKGROUND_IMAGE, imagePath);
	            editor.commit();
	            editor = null;
	        }
	    }
	}
	
	public String getPath(Uri uri) {
		String path = "";
		
		if(uri.getScheme().equals("content")) {
			try {
				String[] projection = { MediaStore.Images.Media.DATA };
			    Cursor cursor = managedQuery(uri, projection, null, null, null);
			    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			    cursor.moveToFirst();
			    path = cursor.getString(column_index);
			}
			catch(Exception ex) {
				Log.i("mytag", "getPath failed");
			}
        }
        else if(uri.getScheme().equals("file")) {
        	path = uri.toString().replace("file://", "");
        }
		
		return path;
	}

	private void chooseBackgroundColor(Context context, String key) {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		String getKey = key;
		if(key.equals("color1")) {
			getKey = LiveInfoWallpaper.BACKGROUND_SINGLE_COLOR;
		}
		
		int color = pref.getInt(getKey, 0xffffffff);
		pref = null;
		new ColorPickerDialog(context, this, color, key).show();
	}
	
	@Override
	public void colorChanged(int color, String key) {
		Editor editor = getPreferenceManager().getSharedPreferences().edit();
		
		if(key.equals("color1")) {
			editor.putInt(LiveInfoWallpaper.BACKGROUND_SINGLE_COLOR, color);
			editor.commit();
			chooseBackgroundColor(this, LiveInfoWallpaper.BACKGROUND_TWO_COLORS);
		}
		else {
			editor.putInt(key, color);
			editor.commit();
		}
		
        editor = null;
	}
	
	private void showHelp(Context context) {
		Intent intent = new Intent();
		String url = "http://www.andreashedin.com/";
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		context.startActivity(intent);
	}
	
	private void gotoWebsite(Context context) {
		Intent intent = new Intent();
		String url = "http://www.andreashedin.com";
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		context.startActivity(intent);
	}
	
	private void gotoMarket(Context context) {
		Intent intent = new Intent();
		String url = "market://details?id=com.andreashedin.infowallpaper";
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		context.startActivity(intent);
	}
	
	private void addInfo(Context context) {
		int infos = getInfoCount();
		
		try {
			new CustomizeInfoDialog(context, this, new InfoData(), infos, true).show();
		}
		catch(Exception ex) {
			Log.i("mytag", "addInfo: " + ex.getMessage());
		}
	}
	
	private InfoData getInfoData(int id) {
		if(id < 0) {
			return new InfoData();
		}
		
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();

		String sVal = pref.getString(LiveInfoWallpaper.INFO_KEY_FORMAT + id, "");
		
		if(sVal.length() == 0) {
			pref = null;
			return null;
		}
		
		InfoData infoData = new InfoData();
		
		infoData.text = sVal;
		sVal = pref.getString(LiveInfoWallpaper.INFO_KEY_FONT + id, "");
		infoData.font = sVal;
		sVal = pref.getString(LiveInfoWallpaper.INFO_KEY_ALIGN + id, Align.CENTER.toString());
		infoData.textAlign = Align.valueOf(sVal);
		sVal = pref.getString(LiveInfoWallpaper.INFO_KEY_TEXT_CASE + id, "none");
		infoData.textcase = sVal;
		
		int iVal = pref.getInt(LiveInfoWallpaper.INFO_KEY_COLOR + id, 0xffffffff);
		infoData.color = iVal;
		iVal = pref.getInt(LiveInfoWallpaper.INFO_KEY_SIZE + id, 80);
		infoData.size = iVal;
		iVal = pref.getInt(LiveInfoWallpaper.INFO_KEY_X + id, 50);
		infoData.x = iVal;
		iVal = pref.getInt(LiveInfoWallpaper.INFO_KEY_Y + id, 200);
		infoData.y = iVal;
		iVal = pref.getInt(LiveInfoWallpaper.INFO_KEY_ORDER + id, 0);
		infoData.order = iVal;
		iVal = pref.getInt(LiveInfoWallpaper.INFO_KEY_ROTATION + id, 0);
		infoData.rotation = iVal;
		
		boolean bVal = pref.getBoolean(LiveInfoWallpaper.INFO_KEY_BACKDROP + id, false);
		infoData.backdrop = bVal;
		bVal = pref.getBoolean(LiveInfoWallpaper.INFO_KEY_SHADOW + id, true);
		infoData.shadow = bVal;
		bVal = pref.getBoolean(LiveInfoWallpaper.INFO_KEY_NUMBERS_AS_TEXT + id, false);
		infoData.numbersAsText = bVal;
		infoData.screen = pref.getInt(LiveInfoWallpaper.INFO_KEY_ON_SCREEN + id, -1);
		
		pref = null;
		
		return infoData;
	}
	
	private void removeData(int id) {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		Editor editor = pref.edit();
		
		editor.remove(LiveInfoWallpaper.INFO_KEY_FORMAT + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_FONT + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_ALIGN + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_COLOR + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_SIZE + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_X + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_Y + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_BACKDROP + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_SHADOW + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_ORDER + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_ROTATION + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_TEXT_CASE + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_NUMBERS_AS_TEXT + id);
		editor.remove(LiveInfoWallpaper.INFO_KEY_ON_SCREEN + id);
		
		editor.commit();
		editor = null;
		pref = null;
	}
	
	private void writeData(int id, InfoData settings) {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		Editor editor = pref.edit();
		
		String key = "";
		key = LiveInfoWallpaper.INFO_KEY_FORMAT + id;
		editor.putString(key, settings.text);
		key = LiveInfoWallpaper.INFO_KEY_FONT + id;
		editor.putString(key, settings.font);
		key = LiveInfoWallpaper.INFO_KEY_SHADOW + id;
		editor.putBoolean(key, settings.shadow);
		key = LiveInfoWallpaper.INFO_KEY_BACKDROP + id;
		editor.putBoolean(key, settings.backdrop);
		key = LiveInfoWallpaper.INFO_KEY_COLOR + id;
		editor.putInt(key, settings.color);
		key = LiveInfoWallpaper.INFO_KEY_SIZE + id;
		editor.putInt(key, settings.size);
		key = LiveInfoWallpaper.INFO_KEY_X + id;
		editor.putInt(key, settings.x);
		key = LiveInfoWallpaper.INFO_KEY_Y + id;
		editor.putInt(key, settings.y);
		key = LiveInfoWallpaper.INFO_KEY_ALIGN + id;
		editor.putString(key, settings.textAlign.toString());
		key = LiveInfoWallpaper.INFO_KEY_ORDER + id;
		editor.putInt(key, settings.order);
		key = LiveInfoWallpaper.INFO_KEY_ROTATION + id;
		editor.putInt(key, settings.rotation);
		key = LiveInfoWallpaper.INFO_KEY_TEXT_CASE + id;
		editor.putString(key, settings.textcase);
		key = LiveInfoWallpaper.INFO_KEY_NUMBERS_AS_TEXT + id;
		editor.putBoolean(key, settings.numbersAsText);
		key = LiveInfoWallpaper.INFO_KEY_ON_SCREEN + id;
		editor.putInt(key, settings.screen);
		
		editor.commit();
		editor = null;
		pref = null;
	}
	
	private void editInfo(Context context, int id) {	
		InfoData infoData = getInfoData(id);
		if(infoData == null) {
			return;
		}
		
		try 
		{
			new CustomizeInfoDialog(context, this, infoData, id, false).show();
		}
		catch(Exception ex)
		{
			Log.i("mytag", "editInfo: " + ex.getMessage());
		}
	}
	
	private void removeInfo(Context context, int id) {
		try {
			new RemoveInfoTask().execute(id);
		}
		catch(Exception ex) {
			Toast.makeText(LiveInfoSettings.this, R.string.failedRemoveInfo, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void editInfo(Context context) {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		int infos = getInfoCount();
		
		if(infos > 0) {
			String val = "";
			ArrayList<DisplayValuePair<Integer>> list = new ArrayList<DisplayValuePair<Integer>>();
			for(int i = 0; i < infos; ++i) {
				val = pref.getString(LiveInfoWallpaper.INFO_KEY_FORMAT + i, "");
				
				if(val.length() > 0)
					list.add(new DisplayValuePair<Integer>(val, i));
			}
			
			pref = null;
			
			new PickItemDialog(context, this, list, EDIT_ITEM).show();
		}
	}

	private void removeInfo(Context context) {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		int infos = getInfoCount();
		
		if(infos > 0) {
			String val = "";
			ArrayList<DisplayValuePair<Integer>> list = new ArrayList<DisplayValuePair<Integer>>();
			for(int i = 0; i < infos; ++i) {
				val = pref.getString(LiveInfoWallpaper.INFO_KEY_FORMAT + i, "");
				
				if(val.length() > 0)
					list.add(new DisplayValuePair<Integer>(val, i));
			}
			
			pref = null;
			
			new PickItemDialog(context, this, list, REMOVE_ITEM).show();
		}
	}
	
	public void moveInfo(InfoData settings, int id) {
		if(settings != null)
			infoCustomized(settings, id, false);
	}
	
	@Override
	public void infoCustomized(InfoData settings, int id, boolean flag) {
		if(settings != null) {
			
			settings.id = id;
			settings.increaseInfoCount = flag;
			
			try {
				new UpdateInfosTask().execute(settings);
			}
			catch(Exception ex) {
				Toast.makeText(LiveInfoSettings.this, R.string.failedUpdateInfo, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private void sortData() {
		ArrayList<InfoData> list = new ArrayList<InfoData>();
		
		int infos = getInfoCount();
		
		InfoData infoData = null;
		for(int i = 0; i < infos; ++i) {
			infoData = getInfoData(i);
			if(infoData != null)
				list.add(infoData);
		}
		
		if(infos > list.size()) {
			setInfoCount(list.size());
		}

		int id = 0;
		while(list.isEmpty() == false) {
			int pos = 0;
			int min = list.get(0).order;
			for(int i = 1; i < list.size(); ++i) {
				if(list.get(i).order < min) {
					pos = i;
					min = list.get(i).order;
				}
			}
			
			if(pos < list.size()) {
				InfoData data = list.remove(pos);
				writeData(id++, data);
				data = null;
			}
			else
				break;
		}
	}
	
	private class UpdateInfosTask extends AsyncTask<InfoData, Void, Boolean> {
		ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(LiveInfoSettings.this);
			dialog.setIndeterminate(true);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMessage(LiveInfoSettings.this.getString(R.string.loadingMessage));
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(InfoData... params) {
			InfoData settings = params[0];
			
			writeData(settings.id, settings);
			
			if(settings.increaseInfoCount)
				increaseInfoCount();
			
			sortData();
			
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Void... param) {
	         
	     }
		
		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
		}
	}
	
	private class RemoveInfoTask extends AsyncTask<Integer, Void, Boolean> {
		ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(LiveInfoSettings.this);
			dialog.setIndeterminate(true);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMessage(LiveInfoSettings.this.getString(R.string.loadingMessage));
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Integer... params) {
			int id = params[0];
			int infos = getInfoCount();
			
			removeData(id);
			
			decreaseInfoCount();
			
			if(id < (infos - 1)) {
				for(int i = id + 1; i < infos; ++i) {
					InfoData infoData = getInfoData(i);
					moveInfo(infoData, i - 1);
				}
				
				removeData(infos - 1);
			}
			
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Void... param) {
	         
	     }
		
		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
		}
	}
	
	private void setInfoCount(int count) {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		Editor editor = pref.edit();
		editor.putInt(LiveInfoWallpaper.INFO_COUNT, count);
		editor.commit();
		editor = null;
		pref = null;
	}
	
	private void increaseInfoCount() {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		int infos = getInfoCount();
		Editor editor = pref.edit();
		editor.putInt(LiveInfoWallpaper.INFO_COUNT, ++infos);
		editor.commit();
		editor = null;
		pref = null;
	}
	
	private void decreaseInfoCount() {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		int infos = getInfoCount();
		Editor editor = pref.edit();
		editor.putInt(LiveInfoWallpaper.INFO_COUNT, --infos);
		editor.commit();
		editor = null;
		pref = null;
	}
	
	private int getInfoCount() {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		int infos = pref.getInt(LiveInfoWallpaper.INFO_COUNT, 0);
		pref = null;
		return infos;
	}
	
	private void positionInfos(Context context) {
		ArrayList<InfoData> list = new ArrayList<InfoData>();
		
		int infos = getInfoCount();
		
		InfoData infoData = null;
		for(int i = 0; i < infos; ++i) {
			infoData = getInfoData(i);
			if(infoData != null) {
				infoData.text = InfoItem.getSampleText(infoData.text);
				list.add(infoData);
			}
		}
		
		if(list.size() < infos) {
			setInfoCount(list.size());
		}
		
		if(list.size() > 0)
			new PositionInfosDialog(context, this, list).show();
	}
	
	private void addItems(ArrayList<InfoData> list) {
		for(int i = 0; i < list.size(); ++i) {
			writeData(i, list.get(i));
		}
		
		setInfoCount(list.size());
	}
	
	private void clearAllItems() {
		int infos = getInfoCount();
		
		for(int i = 0; i < infos; ++i) {
			removeData(i);
		}
		
		setInfoCount(0);
	}
	
	@Override
	public void itemPicked(int id, int flag) {
		if(flag == REMOVE_ITEM) {
			removeInfo(this, id);
		}
		else if(flag == EDIT_ITEM) {
			editInfo(this, id);
		}
		//else if(flag == LOAD_FROM) {
		//	loadFrom(id);
		//}
	}

	@Override
	public void savePositions(ArrayList<InfoData> items) {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		String key = "";
		Editor editor = pref.edit();

		for(int i = 0; i < items.size(); ++i) {
			key = LiveInfoWallpaper.INFO_KEY_X + i;
			editor.putInt(key, items.get(i).x);
			key = LiveInfoWallpaper.INFO_KEY_Y + i;
			editor.putInt(key, items.get(i).y);
		}
		
		editor.commit();
		editor = null;
		pref = null;
		
	}
	
	@Override
	public void enteredString(String string) {
		if(string.length() == 0)
			string = "myConfiguration";
		
		string = string.trim();
		
		SaveLoadData saveData = new SaveLoadData();
		saveData.infoDataList = new ArrayList<InfoData>();
		
		int infos = getInfoCount();
		for(int i = 0; i < infos; ++i) {
			InfoData data = getInfoData(i);
			if(data == null)
				continue;
			else if(data.text == null)
				continue;
			else if(data.text.length() > 0)
				saveData.infoDataList.add(data);
		}
		
		saveData.temperatureType = (WeatherHandler.instance().useFahrenheit()) ? "F" : "C";
		saveData.useWeather = (WeatherHandler.instance().updateMinutes() == 0) ? false : true;
		saveData.iconSet = WeatherHandler.instance().getIconSet();
		saveData.iconSize = WeatherHandler.instance().getIconSize();
		saveData.updateFrequence = WeatherHandler.instance().updateMinutes();
		saveData.weatherLocation = WeatherHandler.instance().getLocation();
		
		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		
		saveData.backgroundImageSrc = prefs.getString(LiveInfoWallpaper.BACKGROUND_IMAGE, "");
		saveData.backgroundColor1 = prefs.getInt(LiveInfoWallpaper.BACKGROUND_SINGLE_COLOR, 0x00ffffff);
		saveData.backgroundColor2 = prefs.getInt(LiveInfoWallpaper.BACKGROUND_TWO_COLORS, saveData.backgroundColor1);
		
		prefs = null;
		
		boolean error = false;
		File f = LiveInfoWallpaper.getAppDirectory();
		
		if(f != null) {
			if(!ConfigHandler.writeConfig(f.getAbsolutePath() + "/" + string + ".xml", saveData)) {
				error = true;
			}
		}
		else
			error = true;
		
		if(error) {
			
		}
	}
	
	public void loadFrom(int from) {
		try {
			new LoadConfigurationListTask().execute(from);
		}
		catch(Exception ex) {
			Toast.makeText(LiveInfoSettings.this, R.string.saveFailed, Toast.LENGTH_SHORT).show();
		}
	}
	
	private class LoadConfigurationListTask extends AsyncTask<Integer, Void, Boolean> {
		ArrayList<String> list;
		int from;
		ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(LiveInfoSettings.this);
			dialog.setIndeterminate(true);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMessage(LiveInfoSettings.this.getString(R.string.loadingMessage));
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Integer... params) {
			from = params[0].intValue();
			
			if(from == FROM_PHONE)
				list = ConfigHandler.getListFromPhone();
			else {
				list = ConfigHandler.getListFromWeb();
			}
			
			if(list.size() > 0)
				return true;
			else 
				return false;
		}
		
		@Override
		protected void onProgressUpdate(Void... param) {
	         
	     }
		
		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			
			if(result.booleanValue() == false)
				Toast.makeText(LiveInfoSettings.this, R.string.saveFailed, Toast.LENGTH_SHORT).show();
			else
				new SelectConfigurationDialog(LiveInfoSettings.this, LiveInfoSettings.this, list, from).show();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void configurationSelected(String str, int from) {
		try {
			new LoadConfigurationTask().execute(new DisplayValuePair<Integer>(str, from));
		}
		catch(Exception ex) {
			Toast.makeText(LiveInfoSettings.this, R.string.failedReadConfig, Toast.LENGTH_SHORT);
		}
	}
	
	private class LoadConfigurationTask extends AsyncTask<DisplayValuePair<Integer>, Void, Boolean> {
		ProgressDialog dialog;
	
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(LiveInfoSettings.this);
			dialog.setIndeterminate(true);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setMessage(LiveInfoSettings.this.getString(R.string.loadingMessage));
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(DisplayValuePair<Integer>... arg0) {
			SaveLoadData loadedData;	
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			String xmlData = "";
			if(((Integer)arg0[0].getValue()).intValue() == FROM_PHONE) {
				try {
					FileInputStream f = new FileInputStream(LiveInfoWallpaper.getAppDirectory().getAbsolutePath() + "/" + arg0[0].getDisplay());
					
					int bytesRead = 0;
					while((bytesRead = f.read(buffer, 0, 1024)) != -1) {
						content.write(buffer, 0, bytesRead);
					}
				} catch (FileNotFoundException e) {	
					Log.i("mytag", "configSelected: " + e.getMessage());
				} 
				catch (IOException e) {
					Log.i("mytag", "configSelected: " + e.getMessage());
				}
				
				xmlData = new String(content.toByteArray());
			}
			else {
				xmlData = LiveInfoWallpaper.getUrlContent(LiveInfoWallpaper.CONFIG_FILES_URL + arg0[0].getDisplay(), LiveInfoWallpaper.mUserAgent);
			}
			
			loadedData = ConfigHandler.readConfig(xmlData);
			
			if(loadedData.infoDataList.size() > 0) {
				clearAllItems();
				addItems(loadedData.infoDataList);
				
				Editor editor = getPreferenceManager().getSharedPreferences().edit();
				
				if(loadedData.useWeather) {
					WeatherHandler.instance().useFahrenheit((loadedData.temperatureType == "F"));
					WeatherHandler.instance().setLocation(loadedData.weatherLocation);
					WeatherHandler.instance().setIconSet(loadedData.iconSet);
					WeatherHandler.instance().setIconSize(loadedData.iconSize);
					WeatherHandler.instance().updateMinutes(loadedData.updateFrequence);
					
					editor.putBoolean(LiveInfoWallpaper.WEATHER_KEY_FAHRENHEIT, WeatherHandler.instance().useFahrenheit());
					editor.putString(LiveInfoWallpaper.WEATHER_KEY_LOCATION, WeatherHandler.instance().getLocation());
					editor.putInt(LiveInfoWallpaper.WEATHER_KEY_UPDATE, WeatherHandler.instance().updateMinutes());
					editor.putString(LiveInfoWallpaper.WEATHER_KEY_ICONS, WeatherHandler.instance().getIconSet());
					editor.putInt(LiveInfoWallpaper.WEATHER_KEY_ICONS_SIZE, WeatherHandler.instance().getIconSize());
				}
				else {
					WeatherHandler.instance().setLocation("");
					WeatherHandler.instance().setIconSet("");
					WeatherHandler.instance().setIconSize(100);
					WeatherHandler.instance().updateMinutes(0);
				}
				
				if(loadedData.backgroundImageSrc.length() > 0) {
					editor.putString(LiveInfoWallpaper.BACKGROUND_IMAGE, loadedData.backgroundImageSrc);
				}
				else {
					editor.remove(LiveInfoWallpaper.BACKGROUND_IMAGE);
				}
				
				editor.putInt(LiveInfoWallpaper.BACKGROUND_SINGLE_COLOR, loadedData.backgroundColor1);
				editor.putInt(LiveInfoWallpaper.BACKGROUND_TWO_COLORS, loadedData.backgroundColor2);
				
				editor.commit();
		        editor = null;
			}
			else 
				return false;
				
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if(result.booleanValue() == false)
				Toast.makeText(LiveInfoSettings.this, R.string.failedReadConfig, Toast.LENGTH_SHORT);
		}
	}
	
	private void loadConfig() {
		loadFrom(FROM_PHONE);
		//ArrayList<DisplayValuePair<Integer>> list = new ArrayList<DisplayValuePair<Integer>>();
		//list.add(new DisplayValuePair<Integer>(getString(R.string.loadFromWeb), FROM_WEB));
		//list.add(new DisplayValuePair<Integer>(getString(R.string.loadFromPhone), FROM_PHONE));
		//new PickItemDialog(this, this, list, LOAD_FROM).show();
	}
	
	private void saveConfig() {
		new EnterStringDialog(this, this, getString(R.string.enterNameTitle), "").show();
	}
	
	private void showWeatherDialog(Context context) {
		new WeatherSettingsDialog(this, this).show();
	}
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equals(LiveInfoWallpaper.BACKGROUND_IMAGE)) {
			chooseBackgroundImage(preference.getContext());
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.BACKGROUND_SINGLE_COLOR)) {
			clearPreference(LiveInfoWallpaper.BACKGROUND_TWO_COLORS);
			chooseBackgroundColor(preference.getContext(), LiveInfoWallpaper.BACKGROUND_SINGLE_COLOR);
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.BACKGROUND_TWO_COLORS)) {
			chooseBackgroundColor(preference.getContext(), "color1");
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.REMOVE_BACKGROUND_IMAGE)) {
			clearPreference(LiveInfoWallpaper.BACKGROUND_IMAGE);
			Toast.makeText(this, R.string.removedBackgroundImageMsg, Toast.LENGTH_SHORT);
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.CLEAR_BACKGROUND_COLORS)) {
			clearPreference(LiveInfoWallpaper.BACKGROUND_SINGLE_COLOR);
			clearPreference(LiveInfoWallpaper.BACKGROUND_TWO_COLORS);
			Toast.makeText(this, R.string.removedBackgroundColorsMsg, Toast.LENGTH_SHORT);
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.ADD_INFO)) {
			addInfo(preference.getContext());
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.EDIT_INFO)) {
			editInfo(preference.getContext());
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.POSITION_INFOS)) {
			positionInfos(preference.getContext());
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.WEATHER_SETTINGS)) {
			showWeatherDialog(preference.getContext());
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.REMOVE_INFO)) {
			removeInfo(preference.getContext());
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.SHOW_HELP)) {
			showHelp(preference.getContext());
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.TO_WEBSITE)) {
			gotoWebsite(preference.getContext());
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.LEAVE_FEEDBACK)) {
			gotoMarket(preference.getContext());
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.SAVE_CONFIGURATION)) {
			saveConfig();
		}
		else if(preference.getKey().equals(LiveInfoWallpaper.LOAD_CONFIGURATION)) {
			loadConfig();
		}
		
		return true;
	}
	
	protected void setupListeners() {
    	Preference pref = getPreferenceManager().findPreference(LiveInfoWallpaper.BACKGROUND_IMAGE);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.BACKGROUND_SINGLE_COLOR);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.BACKGROUND_TWO_COLORS);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.REMOVE_BACKGROUND_IMAGE);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.CLEAR_BACKGROUND_COLORS);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.ADD_INFO);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.EDIT_INFO);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.REMOVE_INFO);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.POSITION_INFOS);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.WEATHER_SETTINGS);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.SHOW_HELP);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.LEAVE_FEEDBACK);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.TO_WEBSITE);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.SAVE_CONFIGURATION);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.LOAD_CONFIGURATION);
        if(pref != null)
        	pref.setOnPreferenceClickListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.HIDE_WHEN_LOCKED);
        if(pref != null)
        	pref.setOnPreferenceChangeListener(this);
        pref = getPreferenceManager().findPreference(LiveInfoWallpaper.SHOW_WHEN_ZERO);
        if(pref != null)
        	pref.setOnPreferenceChangeListener(this);
	}

	@Override
	public void weatherSettingsSaved() {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		Editor editor = pref.edit();
		
		editor.putBoolean(LiveInfoWallpaper.WEATHER_KEY_FAHRENHEIT, WeatherHandler.instance().useFahrenheit());
		editor.putString(LiveInfoWallpaper.WEATHER_KEY_LOCATION, WeatherHandler.instance().getLocation());
		editor.putInt(LiveInfoWallpaper.WEATHER_KEY_UPDATE, WeatherHandler.instance().updateMinutes());
		editor.putString(LiveInfoWallpaper.WEATHER_KEY_ICONS, WeatherHandler.instance().getIconSet());
		editor.putInt(LiveInfoWallpaper.WEATHER_KEY_ICONS_SIZE, WeatherHandler.instance().getIconSize());
		
		editor.commit();
		editor = null;
		pref = null;
	}

	@Override
	public boolean onPreferenceChange(Preference arg0, Object value) {
		SharedPreferences pref = getPreferenceManager().getSharedPreferences();
		Editor editor = pref.edit();
		
		if(arg0.getKey().equals(LiveInfoWallpaper.HIDE_WHEN_LOCKED)) {
			editor.putBoolean(LiveInfoWallpaper.HIDE_WHEN_LOCKED, ((CheckBoxPreference)arg0).isChecked());
		}
		//else if(arg0.getKey().equals(LiveInfoWallpaper.SHOW_WHEN_ZERO)) {
	//		editor.putBoolean(LiveInfoWallpaper.SHOW_WHEN_ZERO, ((CheckBoxPreference)arg0).isChecked());
		//}
		
		editor.commit();
		editor = null;
		pref = null;
		
		return true;
	}
}
