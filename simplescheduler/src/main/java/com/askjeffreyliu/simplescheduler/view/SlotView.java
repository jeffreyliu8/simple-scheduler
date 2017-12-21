package com.askjeffreyliu.simplescheduler.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.askjeffreyliu.simplescheduler.R;
import com.askjeffreyliu.simplescheduler.ScheduleConstant;
import com.askjeffreyliu.simplescheduler.model.Slot;

/**
 * Created by jeff on 12/21/17.
 */

public class SlotView extends FrameLayout {
    private Slot slot;

    private boolean isDrawingAvailable = true;
    private FrameLayout slotArea;
    private TextView textView;


    public SlotView(Context context, Slot slot) {
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
            textView.setText(slot.getStart() + " - " + slot.getEnd());
        }
    }

    public SlotView(Context context, Slot slot, boolean isDraggerMode) {
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
            textView.setText(slot.getStart() + " - " + slot.getEnd());

            if (isDraggerMode) {
                slotArea.setBackgroundColor(Color.BLACK);
            }
        }
    }


    public SlotView(Context context) {
        super(context);
        init();
    }

    public SlotView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.day_schedule_block_view, this);

        this.slotArea = findViewById(R.id.blockBox);
        this.textView = findViewById(R.id.text);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
