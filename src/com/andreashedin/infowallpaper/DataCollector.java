package com.andreashedin.infowallpaper;

import android.content.res.Resources;
import android.util.Log;

public abstract class DataCollector {
	protected LiveInfoWallpaper mParent = null;
	DataCollector(LiveInfoWallpaper parent) {
		mParent = parent;
	}
	
	protected String getNumberAsText(int num) {
		String[] numbers1 = new String[20];
		String[] numbers2 = new String[8];
		String[] numbers3 = new String[2];
		
		Resources resources = mParent.getResources();
		if(resources != null) {
			numbers1 = resources.getStringArray(R.array.numbers1);
			numbers2 = resources.getStringArray(R.array.numbers2);
			numbers3 = resources.getStringArray(R.array.numbers3);
		}
		
		String str = String.valueOf(num);
		
		if(num < 20 && num >= 0) {
			str = numbers1[num];
		}
		else {
			int thousand = num / 1000;
			num %= 1000;
			int hundred = num / 100;
			num %= 100;
			int tens = num / 10;
			num %= 10;
			int ones = num;
			
			str = "";
			if(thousand > 0 && thousand < 20) {
				str += numbers1[thousand];
				str += numbers3[1];
			}
			if(hundred > 0 && hundred < 10) {
				String temp = numbers1[hundred];
				temp += numbers3[0];
				
				if(str.length() > 0)
					temp = temp.toLowerCase();
				str += temp;
			}
			if(tens >= 2 && tens < 10) {
				String temp = numbers2[tens - 2];
				
				if(str.length() > 0)
					temp = temp.toLowerCase();
				str += temp;
			}
			if(tens == 1) {
				String temp = numbers1[ones + 10];
				
				if(str.length() > 0)
					temp = temp.toLowerCase();
				str += temp;
			}
			if(ones > 0 && ones < 10 && tens != 1) {
				String temp = numbers1[ones];
				
				if(str.length() > 0)
					temp = temp.toLowerCase();
				str += temp;
			}
		}
		
		return str;
	}
	
	abstract void update(Object object);
	abstract String updateInfoString(String string, boolean numbersAsText);
}
