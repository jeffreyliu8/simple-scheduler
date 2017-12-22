package com.askjeffreyliu.simplescheduler.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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

    private boolean isDrawingAvailable = true;
    private FrameLayout slotArea;
    private FrameLayout visibleArea;

    private TextView textView;
    private boolean isMoving = true;
    private float oldX = 0;

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
            textView.setText(slot.getStart() + " - " + slot.getEnd());
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

    public Slot getSlot() {
        return this.slot;
    }
}
