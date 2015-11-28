package edu.pku.assistant.Group.util.adapter.TimeList;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.SectionIndexer;

import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardAdapter;
import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardCompressed;
import com.afollestad.cardsui.CardHeader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeSet;

import edu.pku.assistant.Group.recommendation.Recommendation;
import edu.pku.assistant.Group.util.TimeSeg;
import edu.pku.assistant.R;

@SuppressWarnings("unchecked")
public class TimeListAdapter extends CardAdapter implements SectionIndexer {

    Context context;

    private TreeSet<TimeItem> items;
    private TreeSet<TimeItem> sections;

    public TimeListAdapter(Context context, int accentColorRes,
                           ArrayList<TimeSeg> list) {
        super(context, accentColorRes);

        this.context = context;

        items = new TreeSet<TimeItem>();
        sections = new TreeSet<TimeItem>();

        for (TimeSeg p : list) {
            add(p);
        }
        mNotifyDataSetChanged();
    }

    private void mNotifyDataSetChanged() {
        int position = 0, section = -1;
        for (TimeItem item : items) {
            if (item.type == TimeItem.SECTION) {
                CardHeader header = new CardHeader(item.toString());
                this.add(header);
                item.silkid = header.getSilkId();
                item.listPosition = position++;
                item.sectionPosition = ++section;
            } else {
                Card card = new Card(item.toString()/*, String.valueOf(item.ts.availUserIds.size())+"人可行"*/);
//                CardCompressed card = new CardCompressed(item.toString(), "");
                this.add(card);
                item.silkid = card.getSilkId();
                item.listPosition = position++;
                item.sectionPosition = section;
            }
        }
    }

    public ArrayList<TimeItem> getSectionItems(int section) {
        ArrayList<TimeItem> list = new ArrayList<TimeItem>();
        for (TimeItem item : items) {
            if (item.sectionPosition == section) {
                list.add(item);
            }
        }
        return list;
    }

    public ArrayList<TimeItem> getAllItems() {
        ArrayList<TimeItem> list = new ArrayList<TimeItem>();
        for (TimeItem item : items) {
            list.add(item);
        }
        return list;
    }

    public int getSectionForSilkId(long silkId) {
        for (TimeItem item : sections) {
            if (item.silkid == silkId) {
                return item.sectionPosition;
            }
        }
        return -1;
    }

    @Override
    public View onViewCreated(int index, View recycled, CardBase item) {
        return super.onViewCreated(index, recycled, item);
    }

    public void add(TimeSeg ts) {
        Calendar t = Calendar.getInstance();
        t.set(Calendar.YEAR, ts.beg.get(Calendar.YEAR));
        t.set(Calendar.MONTH, ts.beg.get(Calendar.MONTH));
        t.set(Calendar.DAY_OF_MONTH, ts.beg.get(Calendar.DAY_OF_MONTH));
        t.set(Calendar.HOUR, 0);
        t.set(Calendar.MINUTE, 0);
        t.set(Calendar.SECOND, 0);
        t.set(Calendar.MILLISECOND, 0);
        t.set(Calendar.AM_PM, Calendar.AM);
        final TimeItem section = new TimeItem(TimeItem.SECTION, new TimeSeg(t, context.getResources().getString(R.string.ts_description)));
        if (!items.contains(section)) {
            sections.add(section);
            items.add(section);
        }
        TimeItem item = new TimeItem(TimeItem.ITEM, ts);
        items.add(item);
    }

    public TimeItem getPrivateItem(int position) {
        if (position >= items.size())
            return items.last();
        Iterator<TimeItem> itr = items.iterator();
        TimeItem item = null;
        while (position >= 0) {
            item = itr.next();
            --position;
        }
        return item;
    }

    @Override
    public TimeItem[] getSections() {
        TimeItem[] sec = new TimeItem[sections.size()];
        Iterator<TimeItem> itr = sections.iterator();
        int pos = 0;
        while (itr.hasNext()) {
            sec[pos++] = itr.next();
        }
        return sec;
    }

    @Override
    public int getPositionForSection(int section) {
        if (section >= sections.size()) {
            section = sections.size() - 1;
        }

        Iterator<TimeItem> itr = sections.iterator();
        TimeItem item = null;
        while (section >= 0) {
            item = itr.next();
            --section;
        }
        if (item != null)
            return item.listPosition;
        else
            return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return getPrivateItem(position).sectionPosition;
    }

    // return true if a section is removed in process
    public boolean removeItem(long id) {
//        Log.d("checkSection", "comeon "+String.valueOf(id)+" "+String.valueOf(items.size()));
        for (TimeItem item : items) {
            if (item.silkid == id) {
                items.remove(item);
//                Log.d("checkSection---", String.valueOf(item.listPosition));
                updateItemList(item.listPosition, false);
                //updateSectionList(item.listPosition, false);
                return checkSection(item.sectionPosition);
            }
        }
        return false;
    }

    // check if a section is empty
    public boolean checkSection(int section) {
        int position = getPositionForSection(section);
        TimeItem sect = getPrivateItem(position);
        TimeItem next = getPrivateItem(position + 1);
        if (next.type == TimeItem.SECTION) {
            removeSection(sect);
            return true;
        }
        return false;
    }

    public void removeSection(TimeItem sect) {
        items.remove(sect);
        sections.remove(sect);

        // remove if based on silk id
        Card dummy = new Card("dummy");
        dummy.setSilkId(sect.silkid);
        TimeListAdapter.this.remove(dummy);

        updateItemList(sect.listPosition, true);
    }

    public void removeWholeSection(long id) {
        TimeItem next = null;
        int position = 0;
        for (TimeItem item : sections) {
            if (item.silkid == id) {
                next = getPrivateItem(item.listPosition + 1);
                position = item.listPosition;
                break;
            }
        }

        if (next != null) {
            while (next.type == TimeItem.ITEM) {
                Card dummy = new Card("dummy");
                dummy.setSilkId(next.silkid);
                TimeListAdapter.this.remove(dummy);
                if (removeItem(next.silkid)) {
                    break;
                }
                next = getPrivateItem(position + 1);
            }
        }
    }

    public void updateItemList(int position, boolean isSection) {
        for (TimeItem item : items) {
            if (item.listPosition > position) {
                --item.listPosition;
                if (isSection) --item.sectionPosition;
            }
        }
    }

    public int getItemCount() {
        return items.size();
    }
}