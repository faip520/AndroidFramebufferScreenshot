package com.a1w0n.standard;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;

public class MockMotion {
	
	// 命令定义 切记不要忘记最后的换行符
	public static final String CMD_DOWN = "sendevent/dev/input/event3 1 330 1 \n";
	public static final String CMD_TOUCH_MAJOR = "sendevent /dev/input/event3 3 4820 \n";
	public static final String CMD_X = "sendevent/dev/input/event3 3 53 ";
	public static final String CMD_Y = "sendevent/dev/input/event3 3 54 ";
	public static final String CMD_TRACK_ID = "sendevent /dev/input/event3 357 0 \n";
	public static final String CMD_SYN_MT_REPORT = "sendevent/dev/input/event3 0 2 0 \n";
	public static final String CMD_SYN_REPORT = "sendevent /dev/input/event3 00 0 \n";
	public static final String CMD_UP = "sendevent/dev/input/event3 1 330 0 \n";

	private MockMotion() {
	}
	
	public static void mockClick(float x, float y) {
		Instrumentation inst = new Instrumentation();
		inst.sendPointerSync(MotionEvent.obtain(
				SystemClock.uptimeMillis(),
				SystemClock.uptimeMillis(), 
				MotionEvent.ACTION_DOWN, x, y, 0));
		inst.sendPointerSync(MotionEvent.obtain(
				SystemClock.uptimeMillis(),
				SystemClock.uptimeMillis(), 
				MotionEvent.ACTION_UP, x, y, 0));
	}

}
