package com.a1w0n.standard;

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * 完全自由的ImageView，可以双指缩放旋转，单指移动
 */
public class FreeMovableImageView extends ImageView {
	// 边框画笔
	private static Paint mFramePaint;
	
	private Path[] mGridPath = new Path[4];

	private float mPaintHalfWidth = 1.5f;

	private int mWidth, mHeight;
	
	// 最小能缩小到这个值
	private int mScaledSmallestSize = 200;
	
	// 是否要画边框和网格
	private boolean mNeedToDrawFramAndGrid = false;
	// 是否响应手势
	public boolean mIsTouchable = true;
	// 双指按屏幕的时候，记下两点间直线的斜率角度
	private float mStartAngle;
	
	// 当前View的位置相对于原始位置，在X轴上的偏移量
	private float mXOffset = 0;
	// 当前View的位置相对于原始位置，在Y轴上的偏移量
	private float mYOffset = 0;
	
	// 缩放大小累积量
	private float mXScaleAccumulation = 1.0f;
	private float mYScaleAccumulation = 1.0f;
	// 旋转系数累积量
	private float mRotateAccumulation = 1.0f;
	
    // 三种内部交互模式
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    // 当前的模式
    private int mMode = 0;
    
    private PointF mLastPoint = new PointF();
    // 两指中点，用来计算双指移动的位移量
    private PointF mTwoFingerMidPoint = new PointF();
    // 两指的间距
    private float mLastDistanceOfTwoFinger = 1.0f;
    
    // ===========For test==============
    private Rect mTempRect = new Rect();
    // ==============================
    

	public FreeMovableImageView(Context context) {
		super(context);
		init(context);
	}

	public FreeMovableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FreeMovableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		// 初始化画笔
		if (mFramePaint == null) {
			mFramePaint = new Paint();
			mFramePaint.setStrokeWidth(mPaintHalfWidth * 2);
			mFramePaint.setColor(Color.LTGRAY);
			mFramePaint.setStyle(Paint.Style.STROKE);
			// 设置为虚线模式 3 像素 实线 3 像素 虚线 不断重复
			mFramePaint.setPathEffect(new DashPathEffect(new float[] { 3, 3 }, 0));
		}

		//mAnchorDrawable = context.getResources().getDrawable(R.drawable.resize);
	}

	// 中间的网格四条线 存储在Path数组中
	private void updatePathes() {
		Path temp = new Path();
		temp.moveTo(mWidth / 3, 0);
		temp.lineTo(mWidth / 3, mHeight);
		mGridPath[0] = temp;

		temp = new Path();
		temp.moveTo(mWidth * 2 / 3, 0);
		temp.lineTo(mWidth * 2 / 3, mHeight);
		mGridPath[1] = temp;

		temp = new Path();
		temp.moveTo(0, mHeight / 3);
		temp.lineTo(mWidth, mHeight / 3);
		mGridPath[2] = temp;

		temp = new Path();
		temp.moveTo(0, mHeight * 2 / 3);
		temp.lineTo(mWidth, mHeight * 2 / 3);
		mGridPath[3] = temp;
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		getLocalVisibleRect(mTempRect);
		Log.d("===", mTempRect.left + "   " + mTempRect.top + "   " +mTempRect.right + "   " + mTempRect.bottom );

		// 画虚线边框和中间的网格
		if (mNeedToDrawFramAndGrid) {
			canvas.drawRect(mPaintHalfWidth, mPaintHalfWidth, mWidth - mPaintHalfWidth, mHeight - mPaintHalfWidth, mFramePaint);
			canvas.drawPath(mGridPath[0], mFramePaint);
			canvas.drawPath(mGridPath[1], mFramePaint);
			canvas.drawPath(mGridPath[2], mFramePaint);
			canvas.drawPath(mGridPath[3], mFramePaint);
			
//			canvas.getClipBounds(mTempRect);
//			mTempRect.inset(-mAnchorHalfWidth, -mAnchorHalfWidth);
//
//			canvas.clipRect(mTempRect, Op.REPLACE);
//
//			mAnchorDrawable.setBounds(mWidth - mAnchorHalfWidth, mHeight - mAnchorHalfWidth, mWidth + mAnchorHalfWidth, mHeight + mAnchorHalfWidth);
//			mAnchorDrawable.draw(canvas);
		}

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
		
		updatePathes();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 设置成可触控的时候才，响应手势
		if (!mIsTouchable) {
			return super.onTouchEvent(event);
		}
		
		int action = MotionEventCompat.getActionMasked(event);
		switch (action) {
		case (MotionEvent.ACTION_DOWN) :
			// 移到最上面的图层
			bringToFront();
			onDown();
			mMode = DRAG;
		    mLastPoint.set(event.getRawX(), event.getRawY());
			return true;
		case MotionEvent.ACTION_POINTER_DOWN:
            mLastDistanceOfTwoFinger = distance(event);
            
            midPoint(mTwoFingerMidPoint, event);
            mMode = ZOOM;
            
            mStartAngle = calculateRotationAngle(event);
            return true;
        case MotionEvent.ACTION_UP:
            mMode = NONE;
            onUp();
            return true;
        case MotionEvent.ACTION_POINTER_UP:
            mMode = NONE;
            return true;
        case MotionEvent.ACTION_MOVE:
            if (mMode == DRAG) {
                float dx = event.getRawX() - mLastPoint.x;
                float dy = event.getRawY() - mLastPoint.y;
                mLastPoint.set(event.getRawX(), event.getRawY());
                onMove(dx, dy);
            } 
            else if (mMode == ZOOM) {
                float newDist = distance(event);
                if (newDist > 10 && mLastDistanceOfTwoFinger != 0) {
                	float scale = (newDist / mLastDistanceOfTwoFinger);
                    onScale(scale);
				}
                
                // 旋转的时候 暂时不给移动
//                PointF newMid = new PointF();
//                midPoint(newMid, event);
//                float dx = newMid.x - mid.x;
//                float dy = newMid.y - mid.y;
//                mid = newMid;
//                onMove(dx, dy);
                
                float newAngle = calculateRotationAngle(event);
                float result = newAngle - mStartAngle;
                onRotate(result);
            }
            return true;
		}
		
		return super.onTouchEvent(event);
	}
	
    /**
     * 计算旋转的角度，默认是中心旋转
     */
    private float calculateRotationAngle(MotionEvent event) {
        double delta_x = getRawX(event, 0) - getRawX(event, 1);
        double delta_y = getRawY(event, 0) - getRawY(event, 1);
        double radians = Math.atan2(delta_y, delta_x);
        float degree =(float)Math.toDegrees(radians);
        // 这个角度如果超过 180，例如 181度会变成 -179度 
        // 如果是负的 就加上一个360，让它变成正的角度
        if (degree < 0) {
			degree += 360.0;
		}
        return degree;
    }
    
    /**
     * 两个手指之间的距离
     */
    private float distance(MotionEvent event) {
        float x = getRawX(event, 0) - getRawX(event, 1);
        float y = getRawY(event, 0) - getRawY(event, 1);
        return (float)Math.sqrt(x * x + y * y);
    }
    
    /**
     * 计算两个手指的中点
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = getRawX(event, 0) + getRawX(event, 1);
        float y = getRawY(event, 0) + getRawY(event, 1);
        point.set(x / 2, y / 2);
    }
	
    // ===============边框和网格的显示与消除=====================
	private void onUp() {
		mNeedToDrawFramAndGrid = false;
		invalidate();
		
//		Log.d("===", "left == " + getCurrentLeft() + "\n"
//				+ "top == " + getCurrentTop() + "\n"
//				+ "width == " + getCurrentWidth() + "\n"
//				+ "height == " + getCurrentHeight() + "\n"
//				+ "degree == " + getCurrentDegree() + "\n");
	}
	
	private void onDown() {
		mNeedToDrawFramAndGrid = true;
		invalidate();
	}
	// =====================================
	
	// ========================移动，旋转，缩放=============================
	/**
	 * 移动，传入x, y坐标上的位移
	 */
	private void onMove(float dx, float dy) {
		setTranslationX(mXOffset += dx);
		setTranslationY(mYOffset += dy);
	}
	
	/**
	 * 缩放，传入一个系数，1就是不缩也不放大
	 * 缩小到一定程度，就会不让缩小了
	 */
	private void onScale(float factor) {
		// 如果少于最少值了，并且还想缩小，忽略这个操作
		if (getWidth() * getScaleX() <= mScaledSmallestSize || getHeight() * getScaleX() <= mScaledSmallestSize) {
			if (factor < 1.0f) {
				return;
			}
		}
		
		setScaleX(mXScaleAccumulation *= factor);
		setScaleY(mYScaleAccumulation *= factor);
		
		// 不加的话，缩小时会有残影
		invalidate();
	}
	
	/**
	 * 旋转，传入旋转的角度
	 */
	private void onRotate(float angle) {
		setRotation(mRotateAccumulation += angle);
	}
	
	// =================================================================
	
    /**
     * MotionEvent getX(int)方法 而没有 getRawX(int) 这样的方法的;
     * 现在只能用模拟的方法做到同样的功能
     */
	public static float getRawX(MotionEvent event, int pointerIndex) {
        if (pointerIndex < 0) return Float.MIN_VALUE;
        if (pointerIndex == 0) return event.getRawX();
        float offset = event.getRawX() - event.getX();
        return event.getX(pointerIndex) + offset;
    }
	
    /**
     * MotionEvent getY(int)方法 而没有 getRawY(int) 这样的方法的;
     * 现在只能用模拟的方法做到同样的功能
     */
	public static float getRawY(MotionEvent event, int pointerIndex) {
        if (pointerIndex < 0) return Float.MIN_VALUE;
        if (pointerIndex == 0) return event.getRawY();
        float offset = event.getRawY() - event.getY();
        return event.getY(pointerIndex) + offset;
    }
	
	// ==================获取被编辑后的位置大小信息的接口=====================
	
	protected int getXOffset() {
		return (int)mXOffset;
	}
	
	protected int getYOffset() {
		return (int)mYOffset;
	}
	
	protected int getCurrentWidth() {
		return (int)(getScaleX() * getWidth());
	}
	
	protected int getCurrentHeight() {
		return (int)(getScaleY() * getHeight());
	}
	
	protected int getCurrentDegree() {
		//getCurrentDegree();
		return ((int)mRotateAccumulation) % 360;
	}
	
	// =============================================================
}
