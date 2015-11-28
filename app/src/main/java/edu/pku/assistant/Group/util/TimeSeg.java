package edu.pku.assistant.Group.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import edu.pku.assistant.Group.recommendation.RecommendCondition;

public class TimeSeg implements Serializable {
    private static final long serialVersionUID = -5375999523169193381L;
    public Calendar beg;
    public Calendar end;
    public int mode;
    public double length;
    public int count;
    public String description;
    public ArrayList<Integer> availUserIds = new ArrayList<Integer>();

    public TimeSeg(String description) {
        this.beg = Calendar.getInstance();
        this.end = Calendar.getInstance();
        this.end.add(Calendar.HOUR_OF_DAY, 2);
        this.mode = RecommendCondition.MODE_CUSTOMED;
        this.length = 0;
        this.count = 0;
        this.description = description;
    }
    public TimeSeg(TimeSeg ts) {
        this.beg = ts.beg;
        this.end = ts.end;
        this.mode = ts.mode;
        this.length = ts.length;
        this.count = ts.count;
        this.description = ts.description;
    }
    public TimeSeg(Calendar beg, String description) {
        this.beg = beg;
        this.end = beg;
        this.mode = RecommendCondition.MODE_CUSTOMED;
        this.length = 0;
        this.count = 0;
        this.description = description;
    }
    public TimeSeg(Calendar beg, Calendar end, String description) {
        this.beg = beg;
        this.end = end;
        this.mode = RecommendCondition.MODE_CUSTOMED;
        this.length = 0;
        this.count = 0;
        this.description = description;
    }
    public TimeSeg(Calendar beg, int mode, double length, String description) {
        this.beg = beg;
        this.end = beg;
        this.mode = mode;
        this.length = length;
        this.count = 0;
        this.description = "请填写活动描述";
    }
    public TimeSeg(Calendar beg, Calendar end, int mode, double length) {
        this.beg = beg;
        this.end = end;
        this.mode = mode;
        this.length = length;
        this.count = 0;
        this.description = description;
    }
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat(" - HH:mm");
        return sdf.format(this.beg.getTime());
    }
    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(this.beg.getTime());
    }
    public String getSeg() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(this.beg.getTime()) + " - " +  sdf.format(this.end.getTime());
    }
    public String getDes() {
        return this.description;
    }
}
