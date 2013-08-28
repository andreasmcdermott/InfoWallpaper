package com.andreashedin.infowallpaper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.graphics.Paint.Align;
import android.util.Log;
import android.util.Xml;

public class ConfigHandler {
	
	public static ArrayList<String> getListFromPhone() {		
		ArrayList<String> list = new ArrayList<String>();
		
		if(LiveInfoWallpaper.externalStorageAvailabe(false) == false) {
			return list;
		}
		
		File dir = LiveInfoWallpaper.getAppDirectory();
		
		String[] files = dir.list();
		
		if(files != null) {
			for(int i = 0; i < files.length; ++i) {
				if(files[i].endsWith(".xml")) {
					list.add(files[i]);
				}
			}
		}
		
		return list;
	}
	
	public static ArrayList<String> getListFromWeb() {
		ArrayList<String> list = new ArrayList<String>();
		
		String content = LiveInfoWallpaper.getUrlContent(LiveInfoWallpaper.CONFIG_LIST_URL, LiveInfoWallpaper.mUserAgent);
		
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        XmlPullParser xpp = factory.newPullParser();
	
	        xpp.setInput(new StringReader (content));
	        int eventType = xpp.getEventType();
	        String file = "";
	        while (eventType != XmlPullParser.END_DOCUMENT) {
		         if(eventType == XmlPullParser.START_TAG) {        	 
		        	 if(xpp.getName().compareTo("item") == 0) {
		        		 file = xpp.getAttributeValue("", "file");
		        		 if(file.length() > 0 && file.endsWith(".xml")) {
		        			 list.add(file);
		        		 }
		        	 }
		         }
		         
		         eventType = xpp.next();
	        }
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
		
		return list;
	}
	
	public static boolean writeConfig(String fileName, SaveLoadData saveData) {
		boolean failed = false;
		if(LiveInfoWallpaper.externalStorageAvailabe(true)) {
			XmlSerializer xml = Xml.newSerializer();
			FileOutputStream foutput;
			try {
				foutput = new FileOutputStream(fileName, false);
				xml.setOutput(foutput, Xml.Encoding.UTF_8.toString());
				xml.startDocument(Xml.Encoding.UTF_8.toString(), null);
				xml.startTag("", "info_wallpaper");
				
				xml.startTag("", "screen");
					xml.attribute("", "x", String.valueOf(Phone.instance().screen().getWidth()));
					xml.attribute("", "y", String.valueOf(Phone.instance().screen().getHeight()));
				xml.endTag("", "screen");
				
				xml.startTag("", "background");
					xml.attribute("", "src", saveData.backgroundImageSrc);
					xml.attribute("", "color1", String.valueOf(saveData.backgroundColor1));
					xml.attribute("", "color2", String.valueOf(saveData.backgroundColor2));
				xml.endTag("", "background");
				
				for(int i = 0; i < saveData.infoDataList.size(); ++i) {
					xml.startTag("", "item");
						xml.attribute("", "data", saveData.infoDataList.get(i).text);
						xml.attribute("", "order", String.valueOf(saveData.infoDataList.get(i).order));
						xml.startTag("", "position");
							xml.attribute("", "x", String.valueOf(saveData.infoDataList.get(i).x));
							xml.attribute("", "y", String.valueOf(saveData.infoDataList.get(i).y));
							xml.endTag("", "position");
						xml.startTag("", "text");
							xml.attribute("", "font", saveData.infoDataList.get(i).font);
							xml.attribute("", "size", String.valueOf(saveData.infoDataList.get(i).size));
							xml.attribute("", "color", String.valueOf(saveData.infoDataList.get(i).color));
							xml.attribute("", "shadow", String.valueOf(saveData.infoDataList.get(i).shadow));
							xml.attribute("", "align", saveData.infoDataList.get(i).textAlign.toString());
							xml.attribute("", "rotation", String.valueOf(((int)saveData.infoDataList.get(i).rotation)));
							xml.attribute("", "screen", String.valueOf(saveData.infoDataList.get(i).screen));
							xml.attribute("", "numbersAsText", String.valueOf(saveData.infoDataList.get(i).numbersAsText));
							xml.attribute("", "case", saveData.infoDataList.get(i).textcase);
						xml.endTag("", "text");
					xml.endTag("", "item");
				}
				
				xml.startTag("", "weather");
					xml.attribute("", "active", String.valueOf(saveData.useWeather));
					xml.attribute("", "location", saveData.weatherLocation);
					xml.attribute("", "freq", String.valueOf(saveData.updateFrequence));
					xml.attribute("", "tempType", String.valueOf(saveData.temperatureType));
					xml.attribute("", "icons", saveData.iconSet);
					xml.attribute("", "iconSize", String.valueOf(saveData.iconSize));
				xml.endTag("", "weather");
				
				xml.endTag("", "info_wallpaper");
				xml.endDocument();
				xml.flush();
				foutput.close();
			} catch (FileNotFoundException e) {
				failed = true;
			} catch (IllegalArgumentException e) {
				failed = true;
			} catch (IllegalStateException e) {
				failed = true;
			} catch (IOException e) {
				failed = true;
			} catch (Exception e) {
				failed = true;
			}
		}
		else
			return false;
		
		return (failed == false);
	}
	
	public static SaveLoadData readConfig(String data) {
		SaveLoadData returnData = new SaveLoadData();
		ArrayList<InfoData> list = new ArrayList<InfoData>();
		
		XmlPullParserFactory factory;
		int screenX = 480;
        int screenY = 800;
        
		try {
			factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        XmlPullParser xpp = factory.newPullParser();
	
	        xpp.setInput(new StringReader (data));
	        int eventType = xpp.getEventType();
	        InfoData infoData = null;
	        
	        while (eventType != XmlPullParser.END_DOCUMENT) {
		         if(eventType == XmlPullParser.START_TAG) {        	 
		        	 if(xpp.getName().compareTo("item") == 0) {
		        		 infoData = new InfoData();
		        		 infoData.text = xpp.getAttributeValue("", "data");
		        		 try {
		        			 infoData.order = Integer.parseInt(xpp.getAttributeValue("", "order"));
		        		 }
		        		 catch(NumberFormatException nfe) {
		        			 infoData.order = 0;
		        		 }
		        	 }
		        	 else if(xpp.getName().compareTo("position") == 0) {
		        		 if(infoData != null) {
			        		 try {
			        			 infoData.x = Integer.parseInt(xpp.getAttributeValue("", "x"));
			        			 infoData.y = Integer.parseInt(xpp.getAttributeValue("", "y"));
			        		 }
			        		 catch(NumberFormatException nfe) {
			        			 infoData.x = 50;
			        			 infoData.y = 200;
			        		 }
		        		 }
		        	 }
		        	 else if(xpp.getName().compareTo("text") == 0) {
		        		 if(infoData != null) {
		        			 try {
		        				 infoData.font = xpp.getAttributeValue("", "font");
		        			 } catch(Exception ex) {
		        				 infoData.font = "";
		        			 }
		        			 try {
		        				 infoData.size = Integer.parseInt(xpp.getAttributeValue("", "size"));
	        			 	 } catch(Exception ex) {
	        			 		infoData.size = 80;
		        			 }
	        				 try {
		        				 infoData.color = Integer.parseInt(xpp.getAttributeValue("", "color"));
	        				 } catch(Exception ex) {
	        					 infoData.color = 0xffffffff;
		        			 }
	        				 try {
	        					 infoData.textAlign = Align.valueOf(xpp.getAttributeValue("", "align"));
	        				 } catch(Exception ex) {
	        					 infoData.textAlign = Align.LEFT;
		        			 }
	        				 try {
	        					 infoData.shadow = Boolean.parseBoolean(xpp.getAttributeValue("", "shadow"));
	        				 } catch(Exception ex) {
	        					 infoData.shadow = false;
		        			 }
	        				 try {
	        					 infoData.rotation = Integer.parseInt(xpp.getAttributeValue("", "rotation"));
	        				 } catch(Exception ex) {
		        				 infoData.rotation = 0;
		        			 }
	        				 try {
	        					 infoData.numbersAsText = Boolean.parseBoolean(xpp.getAttributeValue("", "numbersAsText"));
	        				 } catch(Exception ex) {
	        					 infoData.screen = -1;
		        			 }
	        				 try {
	        					 infoData.screen = Integer.parseInt(xpp.getAttributeValue("", "screen"));
	        				 } catch(Exception ex) {
	        					 infoData.numbersAsText = false;
	        				 }
	        				 try {
	        					 infoData.textcase = xpp.getAttributeValue("", "case");
	        				 } catch(Exception ex) {
	        					 infoData.textcase = "none";
		        			 }
		        		 }
		        	 }
		        	 else if(xpp.getName().compareTo("screen") == 0) {
		        		 try {
		        			 screenX = Integer.parseInt(xpp.getAttributeValue("", "x"));
		        			 screenY = Integer.parseInt(xpp.getAttributeValue("", "y"));
		        		 }
		        		 catch(NumberFormatException nfe) {
		        			 screenX = 480;
		        			 screenY = 800;
		        		 }
		        	 }
		        	 else if(xpp.getName().compareTo("background") == 0) {
		        		 returnData.backgroundImageSrc = xpp.getAttributeValue("", "src");
		        		 try {
		        			 returnData.backgroundColor1 = Integer.parseInt(xpp.getAttributeValue("", "color1"));
		        			 returnData.backgroundColor2 = Integer.parseInt(xpp.getAttributeValue("", "color2"));
		        		 }
		        		 catch(NumberFormatException nfe) {
		        			 returnData.backgroundColor1 = 0xffffffff;
		        			 returnData.backgroundColor2 = 0xffffffff;		        			 
		        		 }
		        	 }
		        	 else if(xpp.getName().compareTo("weather") == 0) {
		        		 returnData.useWeather = Boolean.parseBoolean(xpp.getAttributeValue("", "active"));
						 returnData.weatherLocation = xpp.getAttributeValue("", "location");
						 returnData.iconSet = xpp.getAttributeValue("", "icons");
						 returnData.temperatureType = xpp.getAttributeValue("", "tempType");
		        		 try {
		        			 returnData.updateFrequence = Integer.parseInt(xpp.getAttributeValue("", "freq"));
		        			 returnData.iconSize = Integer.parseInt(xpp.getAttributeValue("", "iconSize"));
		        		 }
		        		 catch(NumberFormatException nfe) {
		        			 returnData.updateFrequence = 60;
		        			 returnData.iconSize = 100;
		        		 }
		        	 }
		         }
		         else if(eventType == XmlPullParser.END_TAG && xpp.getName().equals("item")) {
		        	 list.add(infoData);
		        	 infoData = null;
		         }
		         
		         eventType = xpp.next();
	        }
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
	
		for(int i = 0; i < list.size(); ++i) {
			
			float x = (float)list.get(i).x / (float)screenX;
			float y = (float)list.get(i).y / (float)screenY;
			
			list.get(i).x = (int)(x * Phone.instance().screen().getWidth() + 0.5f);
			list.get(i).y = (int)(y * Phone.instance().screen().getHeight() + 0.5f);
			
			// TODO: fix
			//list.get(i).size = (int)(y * list.get(i).size + 0.5f);
		}
		
		returnData.infoDataList = list;
		return returnData;
	}
}
