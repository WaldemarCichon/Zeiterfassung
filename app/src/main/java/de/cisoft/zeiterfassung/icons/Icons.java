package de.cisoft.zeiterfassung.icons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kobjects.base64.Base64;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import de.cisoft.framework.webservice.Webservice;
import de.cisoft.utility.log.MyLog;
import de.cisoft.zeiterfassung.MainActivity;
import de.cisoft.zeiterfassung.R;

public class Icons {
	
	private static final int REPLACEMENT_ICON = R.drawable.question_mark;
	private static Icons instance;
	Set<String> icons;
	String[] remoteIcons;
	private File iconDir;
	private Webservice webservice;
	private Bitmap replacementIconBitmap;
	private HashMap <String, Bitmap> loadedIcons;
	
	private Icons() {
		icons = getLocalIcons();
		webservice = Webservice.getInstance();
		loadedIcons = new HashMap<String, Bitmap>();
	}
	
	public static Icons getInstance() {
		if (instance == null) {
			instance = new Icons();
		} 
		return instance;
	}
	
	public void clear() {
		instance = new Icons();
	}
	
	public String[] getRemoteIcons() {
		String sRemoteIcons;
		try {
			sRemoteIcons = webservice.call("getIconList");
		} catch (Exception e) {
			MyLog.i("getIconList","Webservice-Call failed",e);
			return null;
		}
		return sRemoteIcons.split("\\|");
	}
	
	public void fillRemoteIcons() {
		remoteIcons = getRemoteIcons();
	}
	
	private HashSet<String> getLocalIcons() {
		File localDir = MainActivity.getLastInstance().getFilesDir();
		iconDir = new File (localDir, "Icons");
		if (!iconDir.exists()) {
			boolean success = iconDir.mkdir();
			if (!success) {
				return null;
			}
		}
		String[] files = iconDir.list();
		if (files == null) {
			return null;
		}
		HashSet<String> localFiles = new HashSet<String>();
		for (String fileName : files) {
			localFiles.add(fileName);
		}
		return localFiles;
	}

	public byte[] loadRemote(String iconName) {
		String iconString;
		try {
			iconString = webservice.call("getIconAsString","iconName", iconName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return Base64.decode(iconString);
	}
	
	private void save(String fileName, byte[] content) {
		
		File iconFile = new File(iconDir, fileName); 
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(iconFile);
			fos.write(content);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void loadMissingIcons() {
		for (String iconName : remoteIcons) {
			if (!icons.contains(iconName)) {
				byte[] content = loadRemote(iconName);
				save(iconName, content);
				icons.add(iconName);
			}
		}
	}
	
	public Bitmap getReplacementIcon() {
		if (replacementIconBitmap == null) {
			replacementIconBitmap = BitmapFactory.decodeResource(MainActivity.getLastInstance().getResources(), REPLACEMENT_ICON);
		}
		return replacementIconBitmap;
	}
	
	private Bitmap getBitmap(String iconName) {
		Bitmap bitmap = loadedIcons.get(iconName);
		if (bitmap == null) {
			bitmap = BitmapFactory.decodeFile(iconDir+"/"+iconName);
			loadedIcons.put(iconName, bitmap);
		}
		return bitmap;
	}
	
	public Bitmap getIcon(String iconName) {
		if (iconName == null || iconName.length()==0) {
			return null;
		}
		if (!icons.contains(iconName)) {
			return getReplacementIcon();
		}
		return getBitmap(iconName);
	}
}

