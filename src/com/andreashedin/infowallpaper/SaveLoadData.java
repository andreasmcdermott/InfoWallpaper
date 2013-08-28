package com.andreashedin.infowallpaper;

import java.util.ArrayList;

public class SaveLoadData {
	public ArrayList<InfoData> infoDataList;
	
	public String backgroundImageSrc = "";
	public int backgroundColor1 = 0xffffffff;
	public int backgroundColor2 = 0xffffffff;
	
	public boolean useWeather = false;
	public String weatherLocation = "";
	public int updateFrequence = 0;
	public int iconSize = 0;
	public String iconSet = "";
	public String temperatureType = "";
}
