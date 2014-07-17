package com.a1w0n.standard;

import java.io.DataOutputStream;
import java.io.InputStream;

import com.a1w0n.standard.DebugUtils.Logger;

public class CMDUtils {

	private CMDUtils() {
	}

	public static boolean runWithRoot(String command) {
		int result = -1;
		
		Process process = null;
		DataOutputStream os = null;
		InputStream is = null;
		InputStream es = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			result = process.waitFor();
			Logger.d("Run command : " +command + ", process return value : " + result);
			is = process.getInputStream();
			es = process.getErrorStream();
			Logger.d(IOUtils.toString(process.getInputStream()));
			Logger.e(IOUtils.toString(process.getErrorStream()));
		} catch (Exception e) {
			return false;
		} finally {
			IOUtils.closeSilently(os);
			IOUtils.closeSilently(is);
			IOUtils.closeSilently(es);
			if (process != null) {
				process.destroy();
			}
		}
		
		return result == 1;
	}
	
	public static boolean runWithoutRoot(String command) {
		int result = -1;
		
		Process process = null;
		DataOutputStream os = null;
		InputStream is = null;
		InputStream es = null;
		try {
			process = Runtime.getRuntime().exec("sh");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			result = process.waitFor();
			Logger.d("Run command : " +command + ", process return value : " + result);
			is = process.getInputStream();
			es = process.getErrorStream();
			Logger.d(IOUtils.toString(process.getInputStream()));
			Logger.e(IOUtils.toString(process.getErrorStream()));
		} catch (Exception e) {
			return false;
		} finally {
			IOUtils.closeSilently(os);
			IOUtils.closeSilently(is);
			IOUtils.closeSilently(es);
			if (process != null) {
				process.destroy();
			}
		}
		
		return result == 1;
	}
}
