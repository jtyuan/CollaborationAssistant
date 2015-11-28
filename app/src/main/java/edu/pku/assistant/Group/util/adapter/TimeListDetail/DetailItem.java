package edu.pku.assistant.Group.util.adapter.TimeListDetail;


import edu.pku.assistant.Tool.Contact;

public class DetailItem implements Comparable<DetailItem> {

    public static final int ITEM = 0;
    public static final int SECTION = 1;

    public final int type;
    public final Contact member;
    public int sectionPosition;
    public int listPosition;

    public long silkid;

    public boolean isOk;

    public DetailItem(int type, Contact member) {
        this.type = type;
        this.member = member;
        this.isOk = false;
    }

    @Override public String toString() {
        return this.member.getName();
    }

    @Override
    public int compareTo(DetailItem item) {
        int result = this.member.getName().compareTo(item.member.getName());
        if (this.isOk && !item.isOk) {
            return 1;
        } else if (!this.isOk && item.isOk) {
            return -1;
        }
        if (result == 0) {
            long flag = this.member.getId() - item.member.getId();
            if (flag > 0)
                return 1;
            else if (flag < 0)
                return -1;
            else
                return 0;
        } else {
            return result;
        }
    }
}
