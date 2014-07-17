package com.a1w0n.standard.FileUtils;

import java.io.File;

import com.a1w0n.standard.GlobalApplication;
import com.a1w0n.standard.DebugUtils.Logger;

import android.text.TextUtils;

public class A1w0nFileManager {
	
	public static final String PATH_A1W0N = "A1w0n";
	public static final String PATH_APK_ICON = PATH_A1W0N + File.separator + "apkIcons";
	public static final String FILENAME_ALL_APPS_INFO = "app.info";
	public static final String PATH_ALL_APPS_INFO_FILE = PATH_A1W0N + File.separator + FILENAME_ALL_APPS_INFO;
	

	public A1w0nFileManager() {
	}
	
	public static File getIconFileForWriting(String iconFileName) {
		String relativePath = PATH_A1W0N + File.separator + iconFileName + ".png";
		return getFile(relativePath);
	}
	
	public static File getAllAppsInfoFileForWriting() {
		String relativePath = PATH_ALL_APPS_INFO_FILE;
		return getFile(relativePath);
	}
	
	private static File getFile(String relativePath) {
		File result = null;
		
		if (TextUtils.isEmpty(relativePath)) {
			Logger.e("Try to access a file with empty file name!");
			return result;
		}
		
		if (AndroidFileUtils.isExternalStorageWritable()) {
			result = AndroidFileUtils.getOrCreateFileOnExternalStorageRelative(relativePath);
		} else {
			result = AndroidFileUtils.getOrCreateFileOnInternalStorage(GlobalApplication.getInstance(), relativePath);
		}
		
		return result;
	}
	
	
	
	

}
