package edu.pku.assistant.Group.util.adapter.TimeList;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import edu.pku.assistant.Group.util.TimeSeg;

public class TimeItem implements Comparable<TimeItem>, Serializable {

    public static final int ITEM = 0;
    public static final int SECTION = 1;
    private static final long serialVersionUID = -8911300085965654552L;

    public final int type;
    public TimeSeg ts;
    public long silkid;

    public int sectionPosition;
    public int listPosition;

    private static final SimpleDateFormat format_section = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat format_item = new SimpleDateFormat("HH:mm");

    public TimeItem(int type, TimeSeg ts) {
        this.type = type;
        this.ts = ts;
    }

    public TimeSeg getTimeSeg() {
        return this.ts;
    }

    @Override
    public String toString() {
        if (this.type == ITEM) {
            return format_item.format(this.ts.beg.getTime()) + " - " + format_item.format(this.ts.end.getTime());
        } else {
            return format_section.format(this.ts.beg.getTime());
        }
    }

    @Override
    public int compareTo(@NonNull TimeItem item) {
        int flag = this.ts.beg.compareTo(item.ts.beg);
        int flag1 = this.ts.end.compareTo(item.ts.end);
        if (flag != 0) {
            return flag;
        } else {
            if (flag1 != 0) {
                return flag1;
            } else {
                return this.type - item.type;
            }
        }
    }
}