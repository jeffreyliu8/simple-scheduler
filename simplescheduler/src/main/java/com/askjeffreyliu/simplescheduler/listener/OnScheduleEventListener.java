package com.askjeffreyliu.simplescheduler.listener;


import com.askjeffreyliu.simplescheduler.view.ScheduleView;
import com.askjeffreyliu.simplescheduler.view.SlotView;

/**
 * Created by jeff on 12/21/17.
 */

public interface OnScheduleEventListener {
    void onSlotClicked(SlotView slotView, ScheduleView scheduleView);
}
