package com.jianjian.gestureplane.gestureLogin.gesturePasswordView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.jianjian.gestureplane.R;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author jian
 * @version 1.0
 * @date 18/8/7 14:30
 */
public class GesturePlane extends RelativeLayout {

    private PointView[] mPointViewArray;
    private float mWidth;
    private float mHeight;
    private int mPointViewWidth;
    private int mPointViewMargin;
    private int mCount;
    private int mCountSum;
    private int mNoFingerColor = 0xFFD8D8D8;
    private int mFingerOnCenterColor = 0xFF6aa0ff;
    private int mFingerOnBackgroundColor = 0x896aa0ff;
    private int mIncorrectCenterColor = 0xFFFF794C;
    private int mIncorrectBackgroundColor = 0x89FDD7CA;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<Integer> mChoose = new ArrayList<>();
    private float mTargetTempX;
    private float mTargetTempY;
    private float mLastX;
    private float mLastY;
    private boolean mDrawTempFlag = true;
    private boolean mCleared;
    private ResultListener mResultListener;
    private int mRetryTimes = 5;
    private int mRetryTemp = mRetryTimes;
    private int mMinLength;
    private float mDrawWidth = 0f;
    private float mDrawHeight = 0f;
    private RelativeLayout mDrawPlane;

    ScheduledExecutorService mScheduledExecutorService = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());

    public GesturePlane(Context context) {
        super(context);
        mDrawPlane = new RelativeLayout(context);
    }

    public GesturePlane(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDrawPlane = new RelativeLayout(context);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GesturePlane);
        mCount = array.getInteger(R.styleable.GesturePlane_count, 3);
        mNoFingerColor = array.getColor(R.styleable.GesturePlane_color_no_finger, mNoFingerColor);
        mFingerOnCenterColor = array.getColor(R.styleable.GesturePlane_color_finger_on_center, mFingerOnCenterColor);
        mFingerOnBackgroundColor = array.getColor(R.styleable.GesturePlane_color_finger_on_background, mFingerOnBackgroundColor);
        mIncorrectCenterColor = array.getColor(R.styleable.GesturePlane_color_incorrect_center, mIncorrectCenterColor);
        mIncorrectBackgroundColor = array.getColor(R.styleable.GesturePlane_color_incorrect_background, mIncorrectBackgroundColor);
        mDrawWidth = array.getDimension(R.styleable.GesturePlane_point_area_width, mDrawWidth);
        mDrawHeight = array.getDimension(R.styleable.GesturePlane_point_area_height, mDrawHeight);
        array.recycle();
        mPath = new Path();
        //画笔初始化
        mPaint = new Paint();
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        if(mWidth < mHeight){
            mHeight = mWidth;
        }else{
            mWidth = mHeight;
        }
        mCountSum = mCount * 9 - 4;
        int width = (int)(mDrawWidth == 0f ? mWidth : mDrawWidth);
        int height = (int)(mDrawHeight == 0f ? mHeight : mDrawHeight);
        mPointViewWidth = (int) (width / mCountSum * 5f);
        mPointViewMargin = (int) (height / mCountSum * 4f);
        mPaint.setStrokeWidth(mPointViewWidth / 20f);
        if (mPointViewArray == null) {
            mPointViewArray = new PointView[mCount * mCount];
            for (int i = 0; i < mPointViewArray.length; i++) {
                mPointViewArray[i] = new PointView(getContext(), mNoFingerColor,
                        mFingerOnCenterColor, mFingerOnBackgroundColor,
                        mIncorrectCenterColor, mIncorrectBackgroundColor);
                mPointViewArray[i].setId(i + 1);
                LayoutParams layoutParams = new LayoutParams(mPointViewWidth, mPointViewWidth);
                //不是第一列就设置为前面的右边
                if (i % mCount != 0) {
                    layoutParams.addRule(RelativeLayout.RIGHT_OF, mPointViewArray[i - 1].getId());
                }
                //不是第一行就设置为上面的下边
                if (i >= mCount) {
                    layoutParams.addRule(RelativeLayout.BELOW, mPointViewArray[i - mCount].getId());
                }
                int marginTop = 0;
                int marginRight = 0;
                int marginDown = 0;
                int marginLeft = 0;
                //不是最后一列则设置右间距
                if ((i + 1) % mCount != 0) {
                    marginRight = mPointViewMargin;
                }
                //不是最后一行则设置下间距
                if (i < mCount * (mCount - 1)) {
                    marginDown = mPointViewMargin;
                }
                layoutParams.setMargins(marginLeft, marginTop, marginRight, marginDown);
                mPointViewArray[i].setMode(PointView.Mode.STATUS_NO_FINGER);
                mDrawPlane.addView(mPointViewArray[i], layoutParams);
            }
            LayoutParams layoutParams = new LayoutParams(width, height);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, TRUE);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, TRUE);
            this.addView(mDrawPlane, layoutParams);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!mPath.isEmpty()) {
            canvas.drawPath(mPath, mPaint);
        }
        if (mChoose.size() != 0 && mLastX != 0 && mLastY != 0 && mDrawTempFlag) {
            canvas.drawLine(mLastX, mLastY, mTargetTempX, mTargetTempY, mPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                reset();
                mCleared = true;
                break;
            case MotionEvent.ACTION_MOVE:
                PointView cv = getChildByPos(x, y);
                if (cv != null) {
                    int id = cv.getId();
                    if (!mChoose.contains(id)) {
                        mChoose.add(id);
                        cv.setMode(PointView.Mode.STATUS_FINGER_ON);
                        mLastX = (cv.getRight() + cv.getLeft()) / 2 + ((ViewGroup)cv.getParent()).getLeft();
                        mLastY = (cv.getBottom() + cv.getTop()) / 2 + ((ViewGroup)cv.getParent()).getTop();
                        if (mChoose.size() == 1) {
                            mPath.moveTo(mLastX, mLastY);
                        } else {
                            mPath.lineTo(mLastX, mLastY);
                        }
                    }
                }
                mTargetTempX = x;
                mTargetTempY = y;
                break;
            case MotionEvent.ACTION_UP:
                mDrawTempFlag = false;
                mCleared = false;
                mScheduledExecutorService.schedule(() -> {
                    GesturePlane.this.post(() -> {
                        GesturePlane.this.reset();
                        GesturePlane.this.invalidate();
                    });
                }, 500, TimeUnit.MILLISECONDS);
                if(mResultListener!=null){
                    mResultListener.onResult(integerArray2String(mChoose.toArray(new Integer[mChoose.size()])));
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.d("AAA", "onTouchEvent: ");
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    private String integerArray2String(Integer[] arr){
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer i : arr) {
            stringBuilder.append(i);
        }
        return stringBuilder.toString();
    }


    private PointView getChildByPos(float x, float y) {
        for (PointView pv : mPointViewArray) {
            int extraX = ((ViewGroup)pv.getParent()).getLeft();
            int extraY = ((ViewGroup)pv.getParent()).getTop();
            if (x < pv.getRight() + extraX && x > pv.getLeft() + extraX && y > pv.getTop() + extraY && y < pv.getBottom() + extraY) {
                return pv;
            }
        }
        return null;
    }

    private void reset() {
        if (!mCleared) {
            mDrawTempFlag = true;
            mPath.reset();
            mPaint.setColor(mFingerOnCenterColor);
            mChoose.clear();
            for (PointView pv : mPointViewArray) {
                pv.setMode(PointView.Mode.STATUS_NO_FINGER);
            }
        }
    }

    public void setResultListener(ResultListener resultListener) {
        mResultListener = resultListener;
    }

    public interface ResultListener {
        void onResult(String result);
    }

    public void setError(){
        mPaint.setColor(mIncorrectCenterColor);
        for (int i = 0;i<mChoose.size();i++){
            mPointViewArray[mChoose.get(i)-1].setMode(PointView.Mode.STATUS_INCORRECT);
        }
        invalidate();
    }
}
