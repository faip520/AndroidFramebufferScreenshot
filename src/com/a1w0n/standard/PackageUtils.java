package com.a1w0n.standard;

import android.content.Context;
import android.text.TextUtils;

import com.a1w0n.standard.DebugUtils.Logger;


public class PackageUtils {

	private PackageUtils() {
	}
	
	/**
	 * 给出包名，启动包名对应的应用软件的主Activity
	 * @param context
	 * @param packageName
	 */
	public static void startMainActivity(Context context, String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			Logger.e("Start main activity with empty package name!");
			return;
		}
		
		context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.x4enjoy.mathisfun"));
	}

}
