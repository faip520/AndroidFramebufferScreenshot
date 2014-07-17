package com.a1w0n.standard.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.a1w0n.standard.DebugUtils.Logger;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

public class AndroidFileUtils {
	
	private static final String ERROR_EXTERNAL_STORAGE_NOT_WRITABLE = "External storage not writable!";
	
	private AndroidFileUtils() {
	}
    
    // ************************************************Internal Storage****************************************
    /**
     * 在手机内部存储的/data/data/包名/目录下新建一个文件夹
     */
    public static File createOrGetDirectoryInInternalStorage(Context context, String directoryName) {
    	if (TextUtils.isEmpty(directoryName) || context == null) return null;
    	return context.getDir(directoryName, Context.MODE_PRIVATE);
    }
    
    /**
     * 在手机内部存储的/data/data/包名/目录下删除一个文件夹
     */
    public static void deleteDirectoryInInternalStorage(Context context, String directoryName) {
    	if (TextUtils.isEmpty(directoryName) || context == null) return;
    	File temp = createOrGetDirectoryInInternalStorage(context, directoryName);
    	if (temp != null && temp.exists()) {
			try {
				FileUtils.deleteDirectory(temp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**
     * 在App专属的内部存储的中新建一个文件，有可能返回null
     */
    public static File getOrCreateFileOnInternalStorage(Context context, String relativePath) {
    	File result = null;
    	
    	if (context == null || TextUtils.isEmpty(relativePath)) {
			Logger.d("Calling createFileOnInternalStorage with illegal arguments!");
			return result;
		}
    	
    	String fullPath = getFullPathOnInternalStorage(context, relativePath);
    	result = new File(fullPath);
    	
    	if (!result.exists()) {
			File parentDir = result.getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}
			
			try {
				result.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				result = null;
			}
		}
    	
    	return result;
    }
    
    /**
     * Read a file on internal storage. May return null if exception happens.
     */
    public static FileInputStream getFileOnInternalStorage(Context context, String name) {
    	FileInputStream result = null;
    	
    	try {
    		result = context.openFileInput(name);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
    	return result;
    }
    
    public static String getFullPathOnInternalStorage(Context context, String relativePath) {
    	if (context == null || TextUtils.isEmpty(relativePath)) {
			Logger.e("Illegal arguments!");
			return null;
		}
    	
    	return context.getFilesDir().getAbsolutePath() + File.separator + relativePath;
    }
    
    public static boolean deleteFileOnInternalStorage(Context context, String name) {
    	return context.deleteFile(name);
    }
    
    
    public static void getInternalStorageFreeSpace() {
		
	}
    
    // ************************************************External Storage****************************************
    /**
     * ExternalStorage是否可写
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * ExternalStorage是否可读
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    
    /**
     * 获取外部存储的根目录的绝对路径，如果外部存储不可读，就会返回null.
     * @return
     */
    public static String getExternalStorageRootAbsolutePath() {
		if (!isExternalStorageReadable()) return null;
		else {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}
	}
    
    /**
     * Provide a relative path on external storage, return a full path.
     */
    public static String getFullPathOnExternalStorage(String relativePath) {
    	if (TextUtils.isEmpty(relativePath)) {
			Logger.e("Illegal argument!");
			return null;
		}
    	
    	String path = getExternalStorageRootAbsolutePath();
    	path = path + File.separator + relativePath;
    	return path;
    }
    
    /**
     * 在外部存储根目录创建或者获取一个文件夹，如果失败了会返回null
     * 这个API如果不是返回null，则目标文件夹是确定存在的
     */
    public static File getOrCreateDirectoryInExternalStorage(String relativeDirectoryName) {
    	File target = null;
    	
    	if (TextUtils.isEmpty(relativeDirectoryName)) return target;
    	
    	if (!isExternalStorageWritable()) return target;
    	
    	target = new File(getFullPathOnExternalStorage(relativeDirectoryName));
    	
    	if (target.exists()) {
			return target;
		}
    	
    	// 如果创建文件夹失败了，就返回null
    	if (!target.mkdirs()) {
			target = null;
		}
    	
    	return target;
    }
    
    /**
     * Try to delete a directory on external storage, return the result.
     */
    public static boolean deleteDirectoryOnExternalStorage(String relativePath) {
    	if (TextUtils.isEmpty(relativePath)) return false;
    	
    	if (!isExternalStorageWritable()) return false;
    	
    	File tarFile = new File(getFullPathOnExternalStorage(relativePath));
    	try {
			FileUtils.deleteDirectory(tarFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    	
    	return true;
    }
    
    /**
     * Try to create a file on external strorage, return null if any failure happens.
     */
    public static File getOrCreateFileOnExternalStorageRelative(String relativePath) {
    	String fullPath = getFullPathOnExternalStorage(relativePath);
    	
    	return getOrCreateFileOnExternalStorageFull(fullPath);
    }
    
    /**
     * 传入一个Sdcard上的完整的绝对路径，创建相应的文件，有可能返回null
     * @param fullPath
     * @return
     */
    public static File getOrCreateFileOnExternalStorageFull(String fullPath) {
    	File result = null;
    	
    	if (TextUtils.isEmpty(fullPath)) {
    		Logger.e("Empty full path!");
    		return result;
    	}
    	
    	if (!isExternalStorageWritable()) {
    		Logger.e(ERROR_EXTERNAL_STORAGE_NOT_WRITABLE);
    		return result;
    	}
    	
    	result = new File(fullPath);
    	
    	if (!result.exists()) {
			File parentDir = result.getParentFile();
			if (parentDir != null && !parentDir.exists()) {
				parentDir.mkdirs();
			}
			
			try {
				result.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				result = null;
			}
		}
    	
    	return result;
    }
    
    public static boolean deleteFileOnExternalStorage(String relativePath) {
    	if (TextUtils.isEmpty(relativePath)) return false;
    	
    	if (!isExternalStorageWritable()) return false;
    	
    	File target = new File(getFullPathOnExternalStorage(relativePath));
    	
    	return target.delete();
    }
}
