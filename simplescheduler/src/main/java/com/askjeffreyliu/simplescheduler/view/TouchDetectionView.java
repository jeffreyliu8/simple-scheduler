package com.askjeffreyliu.simplescheduler.view;

import android.content.Context;

import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.askjeffreyliu.simplescheduler.ScheduleConstant;
import com.askjeffreyliu.simplescheduler.listener.ClickScrollListener;


/**
 * Created by jeff on 12/21/17.
 */

public class TouchDetectionView extends View implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private GestureDetectorCompat mDetector;
    private ClickScrollListener listener;

    public TouchDetectionView(Context context) {
        super(context);
        init();
    }

    public TouchDetectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchDetectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        mDetector = new GestureDetectorCompat(getContext(), this);
        mDetector.setOnDoubleTapListener(this);
    }

    public void setListener(ClickScrollListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.mDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX,
                            float distanceY) {
        if (listener != null) {
            int startIndex = findIndexByX(event1.getX());
            int endIndex = findIndexByX(event2.getX());
            if (endIndex < 0) {
                endIndex = 0;
            } else if (endIndex >= ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY) {
                endIndex = ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY - 1;
            }
            listener.onIndexScrolled(startIndex, endIndex, false);
        }
        return true;
    }

    private int findIndexByX(float x) {
        float singleSlotWidth = (float) getWidth() / ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY;
        return (int) Math.floor(x / singleSlotWidth);
    }

    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        if (listener != null) {
            int index = findIndexByX(event.getX());

            if (index < 0) {
                index = 0;
            } else if (index >= ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY) {
                index = ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY - 1;
            }
            listener.onIndexClicked(index);
        }
        return true;
    }
}
