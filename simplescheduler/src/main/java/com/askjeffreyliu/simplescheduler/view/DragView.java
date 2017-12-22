package com.askjeffreyliu.simplescheduler.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.askjeffreyliu.simplescheduler.R;
import com.askjeffreyliu.simplescheduler.ScheduleConstant;
import com.askjeffreyliu.simplescheduler.model.Slot;

/**
 * Created by jeff on 12/21/17.
 */

public class DragView extends FrameLayout {
    private Slot slot;
    private float parentWidth = 0;
    private boolean isDrawingAvailable = true;
    private FrameLayout slotArea;
    private FrameLayout visibleArea;

    private TextView textView;
    private int virtualLeft = 0;
    private int virtualRight = 0;

    public DragView(Context context, Slot slot) {
        super(context);
        init();

        this.slot = slot;

        switch (slot.getType()) {
            case ScheduleConstant.TYPE_AVAILABLE:
                slotArea.setBackgroundColor(Color.GREEN);
                break;
            case ScheduleConstant.TYPE_UNAVAILABLE:
                slotArea.setBackgroundColor(Color.RED);
                break;
            case ScheduleConstant.TYPE_COMMITTED:
                slotArea.setBackgroundColor(Color.BLUE);
                break;
            case ScheduleConstant.TYPE_TIME_OFF:
                slotArea.setBackgroundColor(Color.GRAY);
                break;
        }

        if (slot.getType() == ScheduleConstant.TYPE_EMPTY) {
            textView.setVisibility(GONE);
        } else {
            textView.setVisibility(VISIBLE);

            updateIndexAndText(slot.getStart(), slot.getEnd());
        }
    }

    public DragView(Context context) {
        super(context);
        init();
    }

    public DragView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.day_schedule_drag_view, this);

        this.slotArea = findViewById(R.id.blockBox);
        this.textView = findViewById(R.id.text);
        this.visibleArea = findViewById(R.id.visibleArea);
    }

    public void setParentWidth(float parentWidth) {
        this.parentWidth = parentWidth;
    }

    public void updateIndexAndText(int left, int right) {
        slot.setStartEnd(left, right);

        virtualLeft = slot.getStart();
        virtualRight = slot.getEnd();
        if (virtualLeft < 0) {
            virtualLeft = 0;
        }
        if (virtualRight >= ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY) {
            virtualRight = ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY - 1;
        }

        textView.setText(virtualLeft + " - " + virtualRight);
    }

    public Slot getSlot() {
        return this.slot;
    }

    public void updateDisplayLayout() {
        if (getX() <= -getWidth()) {
            // we can never drag it and make the dragger disappear on the left
            return;
        }
        if (getX() >= parentWidth) {
            // we can never drag it and make the dragger disappear on the right
            return;
        }

//        Logger.d("get x is " + getX() + " and width is " + getWidth());

        if (getX() < 0) {
            LayoutParams params = new LayoutParams(Math.round(getWidth() + getX()), LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.RIGHT;
            visibleArea.setLayoutParams(params);

        } else if (getX() + getWidth() > parentWidth) {
            float rightSideX = getX() + getWidth();
//            Logger.d("right side x" + rightSideX + " " + parentWidth);
            LayoutParams params = new LayoutParams(Math.round(getWidth() - (rightSideX - parentWidth)), LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.LEFT;
            visibleArea.setLayoutParams(params);
        } else {
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            visibleArea.setLayoutParams(params);
        }
        requestLayout();
    }
}
