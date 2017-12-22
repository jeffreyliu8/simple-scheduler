package com.askjeffreyliu.simplescheduler.view;

import android.content.Context;

import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import android.widget.ImageView;
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
    private ImageView leftHandle;
    private ImageView rightHandle;

    private TextView textView;

    public DragView(Context context, Slot slot) {
        super(context);
        init();

        this.slot = slot;

        switch (slot.getType()) {
            default:
            case ScheduleConstant.TYPE_AVAILABLE:
                slotArea.setBackgroundColor(ContextCompat.getColor(context, R.color.available_green));
                visibleArea.setBackgroundResource(R.drawable.drag_view_border_green);
                leftHandle.setImageResource(R.drawable.drag_bar_green);
                rightHandle.setImageResource(R.drawable.drag_bar_green);
                break;
            case ScheduleConstant.TYPE_UNAVAILABLE:
                slotArea.setBackgroundColor(ContextCompat.getColor(context, R.color.unavailable_red));
                visibleArea.setBackgroundResource(R.drawable.drag_view_border_red);
                leftHandle.setImageResource(R.drawable.drag_bar_red);
                rightHandle.setImageResource(R.drawable.drag_bar_red);
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

        slotArea = findViewById(R.id.blockBox);
        textView = findViewById(R.id.text);
        visibleArea = findViewById(R.id.visibleArea);
        leftHandle = findViewById(R.id.leftHandle);
        rightHandle = findViewById(R.id.rightHandle);
    }

    public void setParentWidth(float parentWidth) {
        this.parentWidth = parentWidth;
    }

    public void updateIndexAndText(int left, int right) {
        slot.setStartEnd(left, right);

        int virtualLeft = slot.getStart();
        int virtualRight = slot.getEnd();
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
