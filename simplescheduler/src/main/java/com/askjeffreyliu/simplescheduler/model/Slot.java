package com.askjeffreyliu.simplescheduler.model;

import com.askjeffreyliu.simplescheduler.ScheduleConstant;
import com.orhanobut.logger.Logger;

/**
 * Created by jeff on 12/21/17.
 */

public class Slot {
    private int id;
    private int start;
    private int end;
    private int type = ScheduleConstant.TYPE_EMPTY;

    public Slot(int id) {
        this.id = id;
        this.start = id;
        this.end = id;
        this.type = ScheduleConstant.TYPE_EMPTY;
    }

    public Slot(int id, int type) {
        this.id = id;
        this.start = id;
        this.end = id;
        this.type = type;
    }

    public Slot(int start, int end, int type) {
        this.id = -1;
        this.start = start;
        this.end = end;
        this.type = type;
    }

    public Slot(int id, int start, int end, int type) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int size() {
        return end - start + 1;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        if (this.type == ScheduleConstant.TYPE_COMMITTED || this.type == ScheduleConstant.TYPE_TIME_OFF) {
            Logger.d("cannot update type on committed");
            return;
        }
        this.type = type;
    }

    public void setTypeForce(int type) {
        this.type = type;
    }

    public boolean contains(int index) {
        return start <= index && index <= end;
    }
}
