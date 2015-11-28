package edu.pku.assistant.Group.util;

import java.util.ArrayList;

import edu.pku.assistant.Tool.Contact;

public class TimeFilter {

    private ArrayList<Contact> members;

    public TimeFilter(ArrayList<Contact> members) {
        this.members = members;
    }


    // TODO improve algorithm
    public ArrayList<TimeSeg> filter(ArrayList<TimeSeg> l) {
        ArrayList<TimeSeg> tmp = new ArrayList<TimeSeg>();
        for (TimeSeg ts : l) {
            for (Contact m : members) {
                if (m.isAvailable()) {
                    ++ts.count;
                }
            }
            if (ts.count > 0) {
                tmp.add(ts);
            }
        }
        return tmp;
    }

    //public TimeFilter()
}
