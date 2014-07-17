package com.a1w0n.standard;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.a1w0n.standard.DebugUtils.Logger;

/**
 * 通过读取文件 /dev/graphics/fb0获取屏幕截图
 * 截图获取bitmap只需要100ms左右
 */
public class ScreenShot {
	
	// Android下FrameBuffer，一般在这里
	final static String FB0FILE1 = "/dev/graphics/fb0";
	// linux下屏幕FrameBuffer，一般在这里
	final static String FB0FILE2 = "/dev/fb0";
	
	static File fbFile;
	static FileInputStream graphics = null;
	static int screenWidth = 480; // 屏幕宽（像素，如：480px）
	static int screenHeight = 800; // 屏幕高（像素，如：800p）
	static byte[] piex;

	/**
	 * 初始化基本信息
	 * 
	 * @param context
	 */
	public static void init(Activity context) {
		fbFile = new File(FB0FILE1);
		if (!fbFile.exists()) {
			File nFile = new File(FB0FILE2);
			if (nFile.exists()) {
				fbFile = nFile;
			}
		}
		// 初始化事件文件的权限
		try {
			Process sh = Runtime.getRuntime().exec("su", null, null);
			OutputStream os = sh.getOutputStream();
			os.write(("chmod 777 " + fbFile.getAbsolutePath()).getBytes());
			os.flush();
			os.close();
			sh.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

		DisplayMetrics dm = new DisplayMetrics();
		Display display = context.getWindowManager().getDefaultDisplay();
		display.getMetrics(dm);
		screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
		screenHeight = dm.heightPixels; // 屏幕高（像素，如：800p）
		Logger.d("Screen width, height : " + screenWidth + " " + screenHeight);

		PixelFormat pixelFormat = new PixelFormat();
		PixelFormat.getPixelFormatInfo(PixelFormat.RGBA_8888, pixelFormat);
		int deepth = pixelFormat.bytesPerPixel; // 位深
		Logger.d("Depth : " +deepth);
		// 根据像素编码方式，计算总共需要读取多少位
		piex = new byte[5257987]; // 像素
	}

	
	/**
	 * 测试截图
	 */
	@SuppressLint("SdCardPath")
	public static void testShot() {
		long start = System.currentTimeMillis();
		try {
			Bitmap bm = getScreenBitmap();
			saveMyBitmap(bm, "/mnt/sdcard/" + System.currentTimeMillis() + ".jpg");
		} catch (IOException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		Log.i("Screenshot", "time cost:" + (end - start));
	}

	/**
	 * 保存bitmap到文件
	 * 
	 * @param bitmap
	 * @param bitName
	 * @throws IOException
	 */
	public static void saveMyBitmap(Bitmap bitmap, String bitName)
			throws IOException {
		File f = new File(bitName);
		f.createNewFile();
		FileOutputStream fOut = new FileOutputStream(f);

		bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		fOut.flush();
		fOut.close();
	}

	/**
	 * 获取当前屏幕截图，一定要先init
	 * 
	 * @return
	 * @throws IOException
	 */
	public synchronized static Bitmap getScreenBitmap() throws IOException {
		try {
			graphics = new FileInputStream(fbFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		DataInputStream dStream = new DataInputStream(graphics);
		
		long stat = System.currentTimeMillis();
		// 把fb0读进字节数组
		dStream.readFully(piex);
		dStream.close();
		Logger.d("Read time =  " + (System.currentTimeMillis() - stat));

		int line_length = 7680;
		int w = 1217;
		int widthBytes = 1217 * 4;
		int h = 685;
		
		int[] pixels = new int[5257987];
		int row;
		int offset = 0;
		for (int i = 0; i < pixels.length; i+=4) {
			row = i / line_length;
			
			if (row >= h) break;
			if ((i - row * line_length) >= widthBytes) continue;
			// fb0里面存储的BGRA中的A都是FF
			pixels[offset] =  (0xFF << 24) | ((piex[i + 2]& 0x0FF) << 16) | ((piex[i + 1]& 0x0FF) << 8) | (piex[i]& 0x0FF);
			offset++;
		}
		
//		Bitmap bitmap =Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//		
//		for (int i = 0; i < h; i++) {
//			for (int j = 0; j < w; j++) {
//				int pos = i*line_length + j*4;
//				
//				//int a = 255;
//				int r = (int)(piex[pos + 2 ] & 0x0FF);
//				int g = (int)(piex[pos + 1 ] & 0x0FF);
//				int b = (int)(piex[pos + 0 ] & 0x0FF);
//				
//				bitmap.setPixel(j, i, Color.rgb(r, g, b));
//			}
//		}
//		
//		return bitmap;

		return Bitmap.createBitmap(pixels, w, h,
				Bitmap.Config.ARGB_8888);
	}
}