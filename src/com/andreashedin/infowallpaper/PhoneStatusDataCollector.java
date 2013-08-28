package com.andreashedin.infowallpaper;

public class PhoneStatusDataCollector extends DataCollector {

	private int mSmsCount = 0;
	private int mGsm = 0;
	private int mGmailCount = 0;
	private boolean mShowWhenZero = false;
	
	public static final String UNREAD_SMS = "#sms";
	//public static final String UNREAD_GMAIL = "#gmail";
	public static final String GSM_STRENGTH = "#gsm";
	
	public static boolean contains(String str) {
		if(str.contains(UNREAD_SMS))
			return true;
		//if(str.contains(UNREAD_GMAIL))
		//	return true;
		if(str.contains(GSM_STRENGTH))
			return true;
		return false;
	}
	
	static String getSampleText(String str) {
		return str.replace(UNREAD_SMS, "0").replace(GSM_STRENGTH, "00");
	}
	
	PhoneStatusDataCollector(LiveInfoWallpaper parent, boolean showWhenZero) {
		super(parent);
		
		mShowWhenZero = showWhenZero;
	}

	@Override
	void update(Object object) {
		mSmsCount = Phone.instance().getUnreadSmsCount();
		//mGmailCount = Phone.instance().getUnreadGmailCount();
		mGsm = Phone.instance().getSignalStrengthGsm();
	}

	@Override
	String updateInfoString(String string, boolean numbersAsText) {
		String out = "";
		if(mSmsCount > 0 && string.contains(UNREAD_SMS) || 
				mGsm > 0 && string.contains(GSM_STRENGTH) ||
				mShowWhenZero) { 
			
			String number = "";
			
			if(numbersAsText == false)
				number = String.valueOf(mSmsCount);
			else
				number = getNumberAsText(mSmsCount);
			
			out = string.replace(UNREAD_SMS, number);
			
			//if(numbersAsText == false)
			//	number = String.valueOf(mGmailCount);
			//else
			//	number = getNumberAsText(mGmailCount);
			
			//out = out.replace(UNREAD_GMAIL, number);
			
			if(numbersAsText == false)
				number = String.valueOf(mGsm);
			else
				number = getNumberAsText(mGsm);
			
			out = out.replace(GSM_STRENGTH, number);
		}
		
		return out;
	}

	public void setShowWhenZero(boolean show) {
		mShowWhenZero = show;
	}
}
