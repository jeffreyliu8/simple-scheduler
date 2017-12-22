package com.askjeffreyliu.simplescheduler;

import android.content.DialogInterface;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;


import com.askjeffreyliu.simplescheduler.listener.OnScheduleEventListener;
import com.askjeffreyliu.simplescheduler.model.Slot;
import com.askjeffreyliu.simplescheduler.view.ScheduleView;
import com.askjeffreyliu.simplescheduler.view.SlotView;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import static com.askjeffreyliu.simplescheduler.ScheduleConstant.NUMBER_OF_30_MINS_PER_DAY;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.addLogAdapter(new AndroidLogAdapter());

        final ScheduleView mondayView = findViewById(R.id.mondayView);


        ArrayList<Slot> slots = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_30_MINS_PER_DAY; i++) {
            slots.add(new Slot(i));
        }
//
        slots.get(0).setTypeForce(ScheduleConstant.TYPE_COMMITTED);
        slots.get(1).setTypeForce(ScheduleConstant.TYPE_AVAILABLE);
        slots.get(2).setTypeForce(ScheduleConstant.TYPE_UNAVAILABLE);
        slots.get(3).setTypeForce(ScheduleConstant.TYPE_COMMITTED);

        slots.get(4).setTypeForce(ScheduleConstant.TYPE_TIME_OFF);
        slots.get(5).setTypeForce(ScheduleConstant.TYPE_TIME_OFF);
        slots.get(6).setTypeForce(ScheduleConstant.TYPE_TIME_OFF);
        slots.get(7).setTypeForce(ScheduleConstant.TYPE_TIME_OFF);

        slots.get(16).setTypeForce(ScheduleConstant.TYPE_UNAVAILABLE);
        slots.get(17).setTypeForce(ScheduleConstant.TYPE_UNAVAILABLE);
        slots.get(18).setTypeForce(ScheduleConstant.TYPE_UNAVAILABLE);


        slots.get(26).setTypeForce(ScheduleConstant.TYPE_COMMITTED);
        slots.get(27).setTypeForce(ScheduleConstant.TYPE_COMMITTED);
        slots.get(28).setTypeForce(ScheduleConstant.TYPE_COMMITTED);

        slots.get(36).setTypeForce(ScheduleConstant.TYPE_AVAILABLE);
        slots.get(37).setTypeForce(ScheduleConstant.TYPE_AVAILABLE);
        slots.get(38).setTypeForce(ScheduleConstant.TYPE_AVAILABLE);

        mondayView.setSlots(slots);
        mondayView.setEventListener(new OnScheduleEventListener() {
            @Override
            public void onSlotClicked(final SlotView view) {
                Logger.d(" slot clicked view " + view.getSlot().getStart() + " - " + view.getSlot().getEnd() + " id " + view.getSlot().getId());
                if (view.getSlot().getType() == ScheduleConstant.TYPE_COMMITTED) {
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Committed hour")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return;
                }
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mondayView.delete(view.getSlot());
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });


        SwitchCompat isGreenOrRedSwitch = findViewById(R.id.isGreenSwitch);
        mondayView.setDrawingMode(isGreenOrRedSwitch.isChecked());
        isGreenOrRedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mondayView.setDrawingMode(b);
            }
        });
    }
}
