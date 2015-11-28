package edu.pku.assistant.Group.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import edu.pku.assistant.R;

public class Group implements Serializable {
    private static final long serialVersionUID = 7780190556263893645L;
    public int groupId;
    public String groupName;
    public int creatorId;
    public String creatorName;
    public Calendar createDate;
    public ArrayList<TimeSeg> rendezvous = new ArrayList<TimeSeg>();

    public Group(){}

    public Group(String name, int creator) {
        this.groupName = name;
        this.creatorId = creator;
        this.creatorName = "";
    }

    public Group(Group group) {
        this.groupId = group.groupId;
        this.groupName = group.groupName;
        this.creatorId = group.creatorId;
        this.creatorName = group.creatorName;
        this.createDate = group.createDate;
        for (TimeSeg ts : group.rendezvous)
            this.rendezvous.add(new TimeSeg(ts));
    }

    public Group(int id, int creator, Calendar createDate) {
        this.groupId = id;
        this.groupName = "";
        this.creatorId = creator;
        this.createDate = createDate;
        this.creatorName = "";
    }

    public Group(int id, String name, int creator, Calendar createDate) {
        this.groupId = id;
        this.groupName = name;
        this.creatorId = creator;
        this.createDate = createDate;
        this.creatorName = "";
    }


    public Group(int id, String name, int creator, Calendar createDate, ArrayList<TimeSeg> rendezvous) {
        this.groupId = id;
        this.groupName = name;
        this.creatorId = creator;
        this.createDate = createDate;
        this.rendezvous = rendezvous;
        this.creatorName = "";
    }

    @Override
    public String toString() {
        return "["+this.groupId+"]"+this.groupName+':'+this.creatorId+'&'+this.createDate.getTime().toString();
    }

    public static String getBeginTimes(ArrayList<TimeSeg> rendezvous) {
        StringBuilder sb = new StringBuilder(16 * rendezvous.size()); // 13 is the length of Java Timestamp
        for (TimeSeg timeSeg:rendezvous) {
            sb.append(timeSeg.beg.getTimeInMillis()/1000).append(',');
        }
        sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }
    public static String getEndTimes(ArrayList<TimeSeg> rendezvous) {
        StringBuilder sb = new StringBuilder(16 * rendezvous.size()); // 13 is the length of Java Timestamp
        for (TimeSeg timeSeg:rendezvous) {
            sb.append(timeSeg.end.getTimeInMillis()/1000).append(',');
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public String formatGroupName() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(groupName).append(',');
        for (TimeSeg timeSeg:rendezvous) {
            if (!timeSeg.description.equals("请输入活动描述"))
                sb.append(timeSeg.description).append(',');
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
}
