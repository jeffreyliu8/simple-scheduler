package com.askjeffreyliu.simplescheduler;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;


import com.askjeffreyliu.simplescheduler.model.Slot;
import com.askjeffreyliu.simplescheduler.view.ScheduleView;
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

        ScheduleView mondayView = findViewById(R.id.mondayView);


        ArrayList<Slot> slots = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_30_MINS_PER_DAY; i++) {
            slots.add(new Slot(i));
        }
//
        slots.get(0).setTypeForce(ScheduleConstant.TYPE_COMMITTED);
        slots.get(1).setTypeForce(ScheduleConstant.TYPE_AVAILABLE);
        slots.get(2).setTypeForce(ScheduleConstant.TYPE_UNAVAILABLE);
        slots.get(3).setTypeForce(ScheduleConstant.TYPE_COMMITTED);

        slots.get(4).setTypeForce(ScheduleConstant.TYPE_AVAILABLE);
        slots.get(5).setTypeForce(ScheduleConstant.TYPE_AVAILABLE);
        slots.get(6).setTypeForce(ScheduleConstant.TYPE_AVAILABLE);
        slots.get(7).setTypeForce(ScheduleConstant.TYPE_AVAILABLE);

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
//        dayScheduleView1.setListener(new DayScheduleEventListener() {
//            @Override
//            public void onTimeBlockClicked(final DayScheduleBlockView view) {
//                if (view.getBlock().getType() == DayScheduleBlock.TYPE_COMMITTED) {
//                    AlertDialog.Builder builder;
//                    builder = new AlertDialog.Builder(JeffActivity.this);
//                    builder.setTitle("COmmited hour")
//                            .setIcon(android.R.drawable.ic_dialog_alert)
//                            .show();
//                    return;
//                }
//                AlertDialog.Builder builder;
//                builder = new AlertDialog.Builder(JeffActivity.this);
//                builder.setTitle("Delete entry")
//                        .setMessage("Are you sure you want to delete this entry?")
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                dayScheduleView1.delete(view.getBlock());
//                            }
//                        })
//                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                // do nothing
//                            }
//                        })
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();
//            }
//
//            @Override
//            public void onTimeBlockScrolled(DayScheduleBlockView view) {
////                Logger.d("time block Scrolled ");
//            }
//        });
//
//        dayScheduleView1.setDrawingMode(isGreenOrRedSwitch.isChecked());
//        isGreenOrRedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                dayScheduleView1.setDrawingMode(b);
//            }
//        });
    }


}
