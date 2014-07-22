package com.a1w0n.standard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.A1w0n.androidcommonutils.CMDUtils;
import com.A1w0n.androidcommonutils.IOUtils.IOUtils;
import com.A1w0n.androidcommonutils.bitmaputils.BitmapUtils;
import com.A1w0n.androidcommonutils.debugutils.Logger;
import com.a1w0n.standard.Jni.Exec;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		
		ImageView mImageView;

		Button mButton;
		public PlaceholderFragment() {
			
		}

		@SuppressLint("NewApi")
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	        final View view = inflater.inflate(R.layout.fragment_main, null);
	        mImageView = (ImageView) view.findViewById(R.id.Reset);
	        
	        CMDUtils.runWithRoot("chmod 777 /dev/graphics/fb0");
	        
	        mImageView.postDelayed(new Runnable() {
				@Override
				public void run() {
					// Change size on your demand.
			        byte[] aa = new byte[1280 * 720 * 4];
			        long start = System.currentTimeMillis();
			        int bb = Exec.test(aa);
			        
			        if (bb == -1) {
						Logger.e("Error happened in jni!");
						return;
					}
			        
			        Logger.d("Time = " + (System.currentTimeMillis() - start));
			        
			        if (aa != null) {
						Logger.d("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF jpeg size = " + bb);
						Bitmap bm = BitmapFactory.decodeByteArray(aa, 0, bb);
						if (bm != null) {
							mImageView.setBackground(new BitmapDrawable(bm));
							BitmapUtils.saveToFile(new File("/mnt/sdcard/33.jpg"), bm);
						} else {
							Logger.d("bitmap == null");
						}
					}
					
//					CMDUtils.runWithoutRoot("a1w0n -c \"pm install /mnt/sdcard/temp.apk\"");
				}
			}, 2000);
	        
			return view;
		}
		
		private void copyA1w0nToSystemXbin() {
			File dest = new File("/system/xbin/a1w0n");
			if (!dest.exists()) {
				InputStream src = null;
				try {
					src = getActivity().getAssets().open("a1w0n");
					CMDUtils.runWithRoot("mount -o rw,remount /system");
					IOUtils.copy(src, new FileOutputStream(new File("/mnt/sdcard/a1w0n")));
					CMDUtils.runWithRoot("cp /mnt/sdcard/a1w0n /system/xbin/a1w0n");
					CMDUtils.runWithRoot("chmod 06755 /system/xbin/a1w0n");
					CMDUtils.runWithRoot("mount -o ro,remount /system");
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

}
