package com.andreashedin.infowallpaper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;
import android.util.Log;

public class Phone extends PhoneStateListener {
	private Screen mScreen = new Screen();
	private final static Phone mInstance = new Phone();
	private int mUnreadSMSCount;
	//private int mUnreadGmailCount;
	private int mSignalStrengthGsm = 0;
	
	private Phone() { 
		mUnreadSMSCount = 0;
		//mUnreadGmailCount = 0;
	}
	
	public static Phone instance() {
		return mInstance;
	}
	
	public Screen screen() {
		return mScreen;
	}
	
	public void update(Context context) {
		updateUnreadSmsCount(context);
		//updateUnreadGmailCount(context);
	}
	
	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		mSignalStrengthGsm = signalStrength.getGsmSignalStrength();
		
		if(mSignalStrengthGsm == 99)
			mSignalStrengthGsm = 0;
		
		mSignalStrengthGsm = (int)(((float)mSignalStrengthGsm / 31.0f) * 100.0f);
	}
	
	public int getSignalStrengthGsm() {
		return mSignalStrengthGsm;
	}
	
	public int updateUnreadSmsCount(Context context) {
		final Uri SMS_INBOX = Uri.parse("content://sms/inbox");

		Cursor c = context.getContentResolver().query(SMS_INBOX, null, "read = 0", null, null);
		if(c != null) {
			try {
				mUnreadSMSCount = c.getCount();
			}
			finally {
				c.close();
			}
		}
		
		return mUnreadSMSCount;
	}
	
	public int getUnreadSmsCount() {
		return mUnreadSMSCount;
	}
	

//	public int updateUnreadGmailCount(Context context) {
//		
//		AccountManager am = AccountManager.get(context);
//		Account[] a = am.getAccountsByType("com.google");
//		
//		if(a.length == 0) {
//			mUnreadGmailCount = 0;
//			return 0;
//		}
//		
//	    String account = a[0].name;
//	    Uri LABELS_URI = Uri.parse("content://gmail-ls/labels/");
//	    Uri ACCOUNT_URI = Uri.withAppendedPath(LABELS_URI, account);
//	    Cursor cursor = context.getContentResolver().query(ACCOUNT_URI, null, null, null, null);
//
//	    mUnreadGmailCount = 0;
//	    if(cursor != null) {
//		    try {
//			    if (cursor.moveToFirst()) {
//			    	int nameColumn = cursor.getColumnIndex("name");
//			        int unreadColumn = cursor.getColumnIndex("numUnreadConversations");
//			        do {
//			            String unread = cursor.getString(unreadColumn);
//			            String name = cursor.getString(nameColumn);
//			            int val = 0;
//			            try
//			            {
//			            	if(name.equals("^i")) // Inbox
//			            		val = Integer.parseInt(unread);
//			            }
//			            catch(Exception ex) {
//			            	
//			            }
//			            
//			            mUnreadGmailCount += val;
//			        } while (cursor.moveToNext());
//			    }
//		    }
//		    catch(Exception ex) {
//		    	
//		    }
//		    finally {
//		    	cursor.close();
//		    }
//	    }
//	    
//		return mUnreadGmailCount;
//	}
	
//	public int getUnreadGmailCount() {
//		return mUnreadGmailCount;
//	}
}
