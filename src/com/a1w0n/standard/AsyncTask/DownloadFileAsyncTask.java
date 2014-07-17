package com.a1w0n.standard.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import com.A1w0n.androidcommonutils.EventBus.BusProvider;
import com.A1w0n.androidcommonutils.EventBus.Subscribe;
import com.a1w0n.standard.GlobalApplication;
import com.a1w0n.standard.IOUtils;
import com.a1w0n.standard.TestEvent;
import com.a1w0n.standard.DebugUtils.Logger;
import com.a1w0n.standard.FileUtils.AndroidFileUtils;

import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.Toast;

public class DownloadFileAsyncTask extends BaseAsyncTask<String, Void, Boolean> {
	
	private static final int DOWNLOAD_CONNECT_TIMEOUT = 15 * 1000;
	private static final int DOWNLOAD_READ_TIMEOUT = 60 * 1000;

	private String mUrl;
	private String mPath;
	private DFATDownloadListener mDownloadListener;
	
	private boolean mOK = false;

	public DownloadFileAsyncTask(String url, String pathOnExternal, DFATDownloadListener listener) {
		mUrl = url;
		mPath = pathOnExternal;
		mDownloadListener = listener;
		
		BusProvider.getInstance().register(this);
	}

	@Override
	protected Boolean doInBackground(String... params) {
//		return doGetSaveFile(mUrl, mPath, mDownloadListener);
		SystemClock.sleep(10000);
		return false;
	}
	
	@Subscribe
	public void onReceived(TestEvent event) {
		Logger.d("Fuck ===========================");
	}

	/**
	 * @param urlStr
	 * @param path
	 * @param downloadListener
	 * @return
	 */
	public boolean doGetSaveFile(String urlStr, String path, DFATDownloadListener downloadListener) {
		File file = AndroidFileUtils.getOrCreateFileOnExternalStorageFull(path);
		if (file == null) {
			return false;
		}

		BufferedOutputStream out = null;
		InputStream in = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(urlStr);
			Logger.d("download request=" + urlStr + "  path = " + path);
			Proxy proxy = getProxy();
			if (proxy != null)
				urlConnection = (HttpURLConnection) url.openConnection(proxy);
			else
				urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(false);
			urlConnection.setConnectTimeout(DOWNLOAD_CONNECT_TIMEOUT);
			urlConnection.setReadTimeout(DOWNLOAD_READ_TIMEOUT);
			urlConnection.setRequestProperty("Connection", "Keep-Alive");
			urlConnection.setRequestProperty("Charset", "UTF-8");
			urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

			urlConnection.connect();

			int status = urlConnection.getResponseCode();

			if (status != HttpURLConnection.HTTP_OK) {
				return false;
			}

			int bytetotal = (int) urlConnection.getContentLength();
			int bytesum = 0;
			int byteread = 0;
			out = new BufferedOutputStream(new FileOutputStream(file));
			in = new BufferedInputStream(urlConnection.getInputStream());

			final Thread thread = Thread.currentThread();
			byte[] buffer = new byte[1444];
			while ((byteread = in.read(buffer)) != -1) {
				if (thread.isInterrupted()) {
					file.delete();
					IOUtils.closeSilently(out);
					throw new InterruptedIOException();
				}

				bytesum += byteread;
				out.write(buffer, 0, byteread);
				if (downloadListener != null && bytetotal > 0) {
					downloadListener.pushProgress(bytesum, bytetotal);
				}
			}
			if (downloadListener != null) {
				downloadListener.completed();
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			if (downloadListener != null) {
				downloadListener.error();
			}
		} finally {
			IOUtils.closeSilently(in);
			IOUtils.closeSilently(out);
			if (urlConnection != null)
				urlConnection.disconnect();
		}

		return false;
	}
	
	/**
	 * Return a Proxy instance of system's proxy setting.
	 */
	private Proxy getProxy() {
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");
		if (!TextUtils.isEmpty(proxyHost) && !TextUtils.isEmpty(proxyPort))
			return new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.valueOf(proxyPort)));
		else
			return null;
	}
	
	// ===================DownloadListener=================
	public interface DFATDownloadListener {
		
		public void pushProgress(int progress, int max);

		public void completed();

		public void cancel();
		
		public void error();
	}
	// =================================================

}
