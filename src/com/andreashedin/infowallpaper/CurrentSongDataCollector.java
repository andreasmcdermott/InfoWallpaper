package com.andreashedin.infowallpaper;

import java.util.List;

import android.app.ActivityManager;
import android.content.Intent;

public class CurrentSongDataCollector extends DataCollector {

	private static final String ARTIST = "#aa";
	private static final String ALBUM = "#ll";
	private static final String SONG = "#ss";

	private String mCurrentSong = "";
	private String mCurrentAlbum = "";
	private String mCurrentArtist = "";
	
	private boolean mPlaying = false;
	
	CurrentSongDataCollector(LiveInfoWallpaper parent) {
		super(parent);
		
		mPlaying = isMusicServiceRunning();
	}
	
	static String getSampleText(String str) {
		return str.replace(ALBUM, "Current Album").replace(ARTIST, "Current Artist").replace(SONG, "Current Song");
	}

	@Override
	void update(Object object) {
		Intent intent = (Intent)object;
		
		mPlaying = isMusicServiceRunning();
		mCurrentSong = intent.getStringExtra("track");
		mCurrentArtist = intent.getStringExtra("artist");
		mCurrentAlbum = intent.getStringExtra("album");
	}

	@Override
	String updateInfoString(String string, boolean numbersAsText) {
		String str = "";
		
		if(mPlaying) {
			str = string;
			str = str.replace(ARTIST, mCurrentArtist);
			str = str.replace(ALBUM, mCurrentAlbum);
			str = str.replace(SONG, mCurrentSong);
		}
		
		return str;
	}

	public boolean isMusicServiceRunning() {	
		ActivityManager am = (ActivityManager) mParent.getBaseContext().getSystemService("activity");
		if(am == null)
			return false;
		
		List<ActivityManager.RunningAppProcessInfo> runningApps;
		runningApps = am.getRunningAppProcesses();
		
		for(int i = 0; i < runningApps.size(); ++i) {
			if(runningApps.get(i).processName.compareTo("com.google.android.music") == 0) {
				return true;
			}
		}
	
		return false;
	}
}
