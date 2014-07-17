package com.a1w0n.standard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.a1w0n.standard.DebugUtils.Logger;

public class MyLinearLayout extends LinearLayout {

	public MyLinearLayout(Context context) {
		super(context);
	}

	public MyLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Logger.d("a！You hurt me~~~~" + event.getRawX() + "  " + event.getRawY());
		return super.onTouchEvent(event);
	}
	
	
	
	

}
