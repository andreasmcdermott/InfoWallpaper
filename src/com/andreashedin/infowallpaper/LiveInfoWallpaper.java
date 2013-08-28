package com.andreashedin.infowallpaper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

public class LiveInfoWallpaper extends WallpaperService {

	public static final String SHARED_PREFERENCES_NAME = "LiveInfoSettings";
	public static int SETTINGS_FILE = R.xml.settings;
	public static final int HTTP_STATUS_OK = 200;
	private static String USER_AGENT = "%1$s/%2$s (Linux; Android)";
	public static String mUserAgent = null;
	public static final String CONFIG_LIST_URL = "http://www.andreashedin.com/configurations/configurationList.xml";
	public static final String CONFIG_FILES_URL = "http://www.andreashedin.com/configurations/";
	
	public static final String UPDATE_WEATHER = "updateWeatherIntent";
	
	public static final String BACKGROUND_IMAGE = "backgroundImage";
	public static final String BACKGROUND_SINGLE_COLOR = "backgroundSingleColor";
	public static final String BACKGROUND_TWO_COLORS = "backgroundTwoColors";
	public static final String REMOVE_BACKGROUND_IMAGE = "removeBackgroundImage";
	public static final String CLEAR_BACKGROUND_COLORS = "clearBackgroundColors";
	public static final String ADD_INFO = "addInfo";
	public static final String EDIT_INFO = "editInfo";
	public static final String REMOVE_INFO = "removeInfo";
	public static final String WEATHER_SETTINGS = "weatherSettings";
	public static final String POSITION_INFOS = "positionInfos";
	public static final String SHOW_HELP = "showHelp";
	public static final String TO_WEBSITE = "toWebsite";
	public static final String LEAVE_FEEDBACK = "leaveFeedback";
	public static final String SAVE_CONFIGURATION = "saveCurrentConfiguration";
	public static final String LOAD_CONFIGURATION = "loadConfiguration";
	public static final String INFO_COUNT = "numInfos";
	public static final String INFO_KEY_FORMAT = "infoItemFormat";
	public static final String INFO_KEY_X = "infoItemX";
	public static final String INFO_KEY_Y = "infoItemY";
	public static final String INFO_KEY_COLOR = "infoItemColor";
	public static final String INFO_KEY_FONT = "infoItemFont";
	public static final String INFO_KEY_SHADOW = "infoItemShadow";
	public static final String INFO_KEY_BACKDROP = "infoItemBackdrop";
	public static final String INFO_KEY_SIZE = "infoItemSize";
	public static final String INFO_KEY_ALIGN = "infoItemAlign";
	public static final String INFO_KEY_ORDER = "infoItemOrder";
	public static final String INFO_KEY_ON_SCREEN = "infoOnScreen";
	public static final String INFO_KEY_ROTATION = "infoItemRotation";
	public static final String INFO_KEY_TEXT_CASE = "infoItemTextCase";
	public static final String INFO_KEY_NUMBERS_AS_TEXT = "infoItemNumbersAsText";
	public static final String INFO_KEY_FIRST_LOAD = "infoItemFirstLoad";
	public static final String WEATHER_KEY_LOCATION = "weatherLocation";
	public static final String WEATHER_KEY_UPDATE = "weatherUpdate";
	public static final String WEATHER_KEY_FAHRENHEIT = "weatherFahrenheit";
	public static final String WEATHER_KEY_ICONS = "weatherIcons";
	public static final String WEATHER_KEY_ICONS_SIZE = "weatherIconsSize";
	public static final String HIDE_WHEN_LOCKED = "hideOnLockSetting";	
	public static final String SHOW_WHEN_ZERO = "showSmsGmailWhenZero";

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}
	
	public static File getAppDirectory() {
		File f = Environment.getExternalStorageDirectory();
		File appDir = new File(f.getAbsolutePath() + "/InfoWallpaper/");
		
		if(appDir.exists() == false) {
			if(!appDir.mkdirs()) {
				Log.i("mytag", "could not create dir");
				return null;
			}
		}
		
		return appDir;
	}
	
	public static boolean externalStorageAvailabe(boolean needToWrite) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// Write and read
		    return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) && !needToWrite) {
		    // Only read
		    return true;
		} 

		return false;
	}
	
	public class WallpaperEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener {
		
		// Battery broadcast receiver
		private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {				
				boolean redraw = false;
				if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED))
					redraw = updateBatteryItems(intent);
				else if(intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
					redraw = updatePhoneInfo();
					redraw = updateDateTimeItems();
				}
				else if (intent.getAction().compareTo(Intent.ACTION_SCREEN_OFF) == 0) {
					mVisible = false;
					Phone.instance().screen().locked(true);
					redraw = true;
				}
				else if(intent.getAction().compareTo(Intent.ACTION_SCREEN_ON) == 0) {
					Phone.instance().screen().locked(true);
					mVisible = true;
					redraw = true;
				}
				else if (intent.getAction().compareTo(Intent.ACTION_USER_PRESENT) == 0) {
					updateWeatherAsync();
					Phone.instance().screen().locked(false);
					mVisible = true;
					redraw = true;
				}
				else if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
					boolean activated = intent.getBooleanExtra("state", true);
					if(activated == false)
						mHandler.postDelayed(mWeatherUpdater, 30000);
				}
				else if (intent.getAction().equals("com.android.music.playbackcomplete") ||
						intent.getAction().equals("com.android.music.playstatechanged") ||
						intent.getAction().equals("com.android.music.metachanged")) {
					redraw = updateCurrentSong(intent);
				}
				
				if (redraw) {
					updateWallpaper();
				}
			}
		};

		private boolean updateBatteryItems(Intent intent) {
			boolean redraw = false;

			for (int i = 0; i < mInfoList.size(); ++i) {
				if (mInfoList.get(i).isType(InfoItem.TYPE_BATTERY)) {
					mInfoList.get(i).update(InfoItem.TYPE_BATTERY, (Object) intent);
					redraw = true;
				}
			}

			return redraw;
		}

		private boolean updateDateTimeItems() {
			for (int i = 0; i < mInfoList.size(); ++i) {
				if (mInfoList.get(i).isType(InfoItem.TYPE_DATETIME)) {
					mInfoList.get(i).update(InfoItem.TYPE_DATETIME, null);
				}
			}

			return true;
		}
		
		// Phone Info
		private boolean updatePhoneInfo() {
			Phone.instance().update(getBaseContext());
			
			boolean redraw = false;
			for (int i = 0; i < mInfoList.size(); ++i) {
				if (mInfoList.get(i).isType(InfoItem.TYPE_PHONESTATUS)) {
					redraw = true;
					mInfoList.get(i).update(InfoItem.TYPE_PHONESTATUS, null);
				}
			}

			return redraw;
		}
		
		private boolean updateCurrentSong(Intent intent) {
			boolean redraw = false;
			
			for(int i = 0; i < mInfoList.size(); ++i) {
				if(mInfoList.get(i).isType(InfoItem.TYPE_CURRENTSONG)) {
					mInfoList.get(i).update(InfoItem.TYPE_CURRENTSONG, intent);
					redraw = true;
				}
			}
			
			return redraw;
		}
		
		private final Runnable mSetBackgroundRunner = new Runnable() {
			@Override
			public void run() {
				retryBackgroundImage();
			}
		};
		
		private final Runnable mWeatherUpdater = new Runnable() {
			@Override
			public void run() {
				updateWeatherAsync();
			}
		};
		
		private void updateWeather() {	
			mHandler.removeCallbacks(mWeatherUpdater);
			
			if(WeatherHandler.instance().updateMinutes() > 0) {
				WeatherHandler.instance().update(mUserAgent);
				
				boolean redraw = false;
				for (int i = 0; i < mInfoList.size(); ++i) {
					if (mInfoList.get(i).isType(InfoItem.TYPE_WEATHER)) {
						redraw = true;
						mInfoList.get(i).update(InfoItem.TYPE_WEATHER, null);
					}
				}
				
				if (redraw) {
					updateWallpaper();
				}
				
				mHandler.postDelayed(mWeatherUpdater, WeatherHandler.instance().updateMinutes() * 60000);
			}
		}
		
		private SharedPreferences mPrefs = null;
		private Handler mHandler = new Handler();
		private boolean mVisible = false;
		private ArrayList<InfoItem> mInfoList = new ArrayList<InfoItem>();
		private Background mBackground = new Background();
		
		public WallpaperEngine() {
			prepareUserAgent();

			mPrefs = LiveInfoWallpaper.this.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
			mPrefs.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(mPrefs, null);

			IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_BATTERY_CHANGED);
			filter.addAction(Intent.ACTION_TIME_TICK);
			filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

			registerReceiver(mReceiver, filter);
			
			TelephonyManager tm = (TelephonyManager) LiveInfoWallpaper.this.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
			tm.listen(Phone.instance(), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		}

		private void prepareUserAgent() {
			PackageManager manager = getPackageManager();
			PackageInfo info = null;
			try {
				info = manager.getPackageInfo(getPackageName(), 0);
				mUserAgent = String.format(USER_AGENT, info.packageName, info.versionName);
			} catch (NameNotFoundException e) {

			}
		}

		private boolean mHide = false;
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {	
			String sVal = "";
			int iVal1 = 0;
			int iVal2 = 0;
			
			mHide = prefs.getBoolean(HIDE_WHEN_LOCKED, false);
	
			// BACKGROUND
			sVal = prefs.getString(BACKGROUND_IMAGE, "");
			if(sVal.length() > 0)
				loadImage(sVal);
			else
				mBackground.clearImage();
			iVal1 = prefs.getInt(BACKGROUND_SINGLE_COLOR, 0x00ffffff);
			iVal2 = prefs.getInt(BACKGROUND_TWO_COLORS, iVal1);
			mBackground.setColor(iVal1, iVal2);
			
			mInfoOnAllScreens = true;
			
			// ITEMS
			iVal1 = prefs.getInt(INFO_COUNT, 0);
			
			if(iVal1 == 0) {
				createDefault();
			}
			else {		
				mInfoList.clear();
				InfoItem item = null;
				for(int i = 0; i < iVal1; ++i) {
					item = InfoItem.loadFromSharedPreference(LiveInfoWallpaper.this, prefs, getAssets(), i);
					
					if(item != null) {
						if(item.mScreen >= 0) {
							mInfoOnAllScreens = false;
						}
						
						mInfoList.add(item);
					}
				}
			}
			
			sVal = prefs.getString(WEATHER_KEY_LOCATION, "");
			
			WeatherHandler.instance().setLocation(sVal);
			iVal1 = prefs.getInt(WEATHER_KEY_UPDATE, 0);
			WeatherHandler.instance().updateMinutes(iVal1);
			iVal1 = prefs.getInt(WEATHER_KEY_ICONS_SIZE, 100);
			WeatherHandler.instance().setIconSize(100);
			sVal = prefs.getString(WEATHER_KEY_ICONS, "");
			WeatherHandler.instance().setIconSet(sVal);
			boolean bVal = prefs.getBoolean(WEATHER_KEY_FAHRENHEIT, false);
			WeatherHandler.instance().useFahrenheit(bVal);
			
			if(WeatherHandler.instance().updateMinutes() > 0) {
				mHandler.post(mWeatherUpdater);
			}
			
			updateBatteryItems(registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)));
			updatePhoneInfo();
			updateDateTimeItems();
		}
		
		private void updateWeatherAsync() {
			new LoadWeatherTask().execute();
		}
		
		private class LoadWeatherTask extends AsyncTask<Void, Void, Void> {

			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					updateWeather();
				}
				catch(Exception e) {
					Log.i("mytag", "EXCEPTION in LoadWeatherTask: " + e.getMessage());
				}
				return null;
			}
		}
		
		private boolean createDefault() {
			mBackground.setColor(0xff000000);
			
			InfoItem item = new InfoItem(LiveInfoWallpaper.this, LiveInfoWallpaper.this.getString(R.string.defaultString1), false);
			item.setColor(0xffffffff);
			item.setAlign(Align.LEFT);
			item.setPosition(50.0f, 150.0f);
			item.setTextSize(22.0f);
			mInfoList.add(item);
			
			item = new InfoItem(LiveInfoWallpaper.this, LiveInfoWallpaper.this.getString(R.string.defaultString2), false);
			item.setColor(0xffffffff);
			item.setAlign(Align.LEFT);
			item.setPosition(50.0f, 200.0f);
			item.setTextSize(22.0f);
			mInfoList.add(item);
			
			item = new InfoItem(LiveInfoWallpaper.this, LiveInfoWallpaper.this.getString(R.string.defaultString3), false);
			item.setColor(0xffffffff);
			item.setAlign(Align.LEFT);
			item.setPosition(50.0f, 250.0f);
			item.setTextSize(22.0f);
			mInfoList.add(item);
			
			item = new InfoItem(LiveInfoWallpaper.this, LiveInfoWallpaper.this.getString(R.string.defaultString4), false);
			item.setColor(0xffffffff);
			item.setAlign(Align.LEFT);
			item.setPosition(50.0f, 300.0f);
			item.setTextSize(22.0f);
			mInfoList.add(item);
			
			return true;
		}
		
		private String mStoredPath = null;
		public void retryBackgroundImage() {
			mHandler.removeCallbacks(mSetBackgroundRunner);
			
			loadImage(mStoredPath);
		}
		
		public boolean loadImage(String string) {
			boolean startRunner = false;
			
			try {
				if(externalStorageAvailabe(false) == false) {
					startRunner = true;
				}
				else {
					if(!mBackground.setImage(string))
						startRunner = true;
				}
			}
			catch(Exception e) {
				Log.i("mytag", "File could not be loaded, " + e.getMessage());
			}
			
			if(startRunner) {
				mStoredPath = string;
				mHandler.postDelayed(mSetBackgroundRunner, 1000);
			}
			else {
				mStoredPath = null;
				return true;
			}
			
			return false;
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
		}

		@Override
		public void onDestroy() {
			mPrefs.unregisterOnSharedPreferenceChangeListener(this);
			mHandler.removeCallbacks(mSetBackgroundRunner);
			mHandler.removeCallbacks(mWeatherUpdater);
			super.onDestroy();
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			mVisible = visible;

			if (visible) {
				updateWallpaper();
			}
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
			super.onSurfaceCreated(surfaceHolder);
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
			super.onSurfaceChanged(surfaceHolder, format, width, height);
			
			Phone.instance().screen().updateWindow(width, height, format);
			draw();
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder surfaceHolder) {
			super.onSurfaceDestroyed(surfaceHolder);
			mVisible = false;
		}

		private boolean mInfoOnAllScreens = true;
		@Override
		public void onOffsetsChanged(float offsetX, float offsetY, float stepX, float stepY, int pixelsX, int pixelsY) {
			Phone.instance().screen().setOffset(offsetX, offsetY, stepX, stepY, pixelsX, pixelsY);
			
			if (mBackground.setOffset(offsetX) || mInfoOnAllScreens == false) {
				updateWallpaper();
			}
		}

		private void updateWallpaper() {
			if(mVisible) {
				mBackground.update();
				for (int i = 0; i < mInfoList.size(); ++i) {
					mInfoList.get(i).update();
				}
	
				draw();
			}
		}

		private void draw() {
			final SurfaceHolder surfaceHolder = getSurfaceHolder();
			Canvas canvas = null;

			try {
				canvas = surfaceHolder.lockCanvas();
				if (canvas != null) {
					canvas.save();
					
					mBackground.draw(canvas);

					if (Phone.instance().screen().isLocked() == false || mHide == false) {
						for (int i = 0; i < mInfoList.size(); ++i) {
							mInfoList.get(i).draw(canvas);
						}
					}

					canvas.restore();
				}
			} finally {
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
					canvas = null;
				}
			}
		}
	}
	
	public static String getUrlContent(String url, String userAgent) {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", userAgent);
        
        try {
            HttpResponse response = client.execute(request);

            // Check if server response is valid
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != LiveInfoWallpaper.HTTP_STATUS_OK) {
                return "";
            }

            // Pull content stream from response
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();

            byte[] sBuffer = new byte[1024];
            ByteArrayOutputStream content = new ByteArrayOutputStream();

            // Read response into a buffered stream
            int readBytes = 0;
            while ((readBytes = inputStream.read(sBuffer, 0, 1024)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }

            // Return result from buffered stream
            return new String(content.toByteArray());
        } catch (Exception e) {
            
        }
        
		return "";
	}	
}