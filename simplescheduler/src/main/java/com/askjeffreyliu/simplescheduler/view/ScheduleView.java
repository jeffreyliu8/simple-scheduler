package com.askjeffreyliu.simplescheduler.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

import android.widget.LinearLayout;

import com.askjeffreyliu.simplescheduler.R;
import com.askjeffreyliu.simplescheduler.listener.ClickScrollListener;
import com.askjeffreyliu.simplescheduler.listener.OnSlotViewClickListener;
import com.askjeffreyliu.simplescheduler.model.Slot;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import static com.askjeffreyliu.simplescheduler.ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY;

/**
 * Created by jeff on 12/21/17.
 */

public class ScheduleView extends CardView implements ClickScrollListener, OnSlotViewClickListener {

    private LinearLayout slotsArea;
    private TouchDetectionView detectionView;
    private ArrayList<Slot> blocks = new ArrayList<>();

    public ScheduleView(Context context) {
        super(context);
        init();
    }

    public ScheduleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScheduleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.schedule_view, this);
        this.slotsArea = findViewById(R.id.slotsArea);
        this.detectionView = findViewById(R.id.detection);
        detectionView.setListener(this);

        this.setRadius(getContext().getResources().getDimensionPixelSize(R.dimen.corner_radius));
//        this.dragAndSlideArea = findViewById(R.id.dragAndSlideArea);
    }

    @Override
    public void onEmptyIndexClicked(int index) {
        Logger.d("onEmptyIndexClicked on" + index);
    }

    @Override
    public void onIndexScrolled(int startIndex, int endIndex, boolean isStartScrolling) {
        Logger.d("onIndexScrolled on" + startIndex + " " + endIndex);
    }

    @Override
    public void onIndexScrollEnd(int endIndex) {
        Logger.d("onIndexScrollEnd " + endIndex);
    }

    @Override
    public void onSlotViewClicked(SlotView view) {
        Logger.d(" slot clicked view " + view.getSlot().getStart() + " - " + view.getSlot().getEnd() + " id " + view.getSlot().getId());
    }

    public void setSlots(ArrayList<Slot> blocks) {
        this.blocks = blocks;
        updateModelAndUI();
    }

    private ArrayList<Slot> createBreakDown() {
        // break down the original list to 48 pieces
        ArrayList<Slot> temp = new ArrayList<>();

        for (int i = 0; i < blocks.size(); i++) {
            Slot slot = blocks.get(i);


            for (int j = 0; j < slot.size(); j++) {
                temp.add(new Slot(i + j, slot.getType()));
            }
        }


        return temp;
    }

    private void updateModelAndUI() {
        // find the number of Schedule block

        // break down the original list to 48 pieces
        ArrayList<Slot> temp = createBreakDown();


        int index = 0;
        ArrayList<Slot> result = new ArrayList<>();
        while (index < NUMBER_OF_30_MINS_PER_DAY) {
            // find a block, could be type
            Slot slot = getBlockStartEnd(temp, index);

            result.add(slot);

            if (slot.getEnd() == NUMBER_OF_30_MINS_PER_DAY - 1) {
                break;
            }
            index = slot.getEnd() + 1;
        }

        blocks = result;
        for (int i = 0; i < blocks.size(); i++) {
            blocks.get(i).setId(i);
        }

        updateUiAccordingToModel();
    }

    private void updateUiAccordingToModel() {

        // remove all the view listener
        for (int i = 0; i < slotsArea.getChildCount(); i++) {
            SlotView slotView = (SlotView) slotsArea.getChildAt(i);
            slotView.setOnClickListener(null);
        }

        // remove the view doesn't remove the listeners
        slotsArea.removeAllViews();

        for (int i = 0; i < blocks.size(); i++) {
            Slot slot = blocks.get(i);
            SlotView blockView = new SlotView(getContext(), slot);
            blockView.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, slot.size()));
            blockView.setId(slot.getStart());
            blockView.setSlotClickListener(this);
            slotsArea.addView(blockView);
        }
    }

    private Slot getBlockStartEnd(ArrayList<Slot> list, int index) {
        int start = index;
        int end = index;
        int type = list.get(index).getType();
        while (start - 1 >= 0 && list.get(start - 1).getType() == type) {
            start--;
        }
        while (end + 1 < NUMBER_OF_30_MINS_PER_DAY && list.get(end + 1).getType() == type) {
            end++;
        }
        return new Slot(start, end, type);
    }
}
