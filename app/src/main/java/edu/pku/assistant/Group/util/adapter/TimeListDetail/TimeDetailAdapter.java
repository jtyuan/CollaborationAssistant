package edu.pku.assistant.Group.util.adapter.TimeListDetail;

import android.content.Context;
import android.view.View;
import android.widget.SectionIndexer;

import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardAdapter;
import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardHeader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeSet;

import edu.pku.assistant.Group.util.adapter.TimeList.TimeItem;
import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Contact;

@SuppressWarnings("unchecked")
public class TimeDetailAdapter extends CardAdapter implements SectionIndexer {

    long originalSilkId;
    Calendar currentDate;
    Calendar currentDateEnd;

    Context context;

    private TreeSet<DetailItem> items;
    private TreeSet<DetailItem> sections;


    public TimeDetailAdapter(Context context, int accentColorRes, ArrayList<Contact> list, long silkid, Calendar currentDate, Calendar currentDateEnd) {
        super(context, accentColorRes);

        setOriginalSilkId(silkid);
        setCurrentDate(currentDate, currentDateEnd);

        this.context = context;

        items = new TreeSet<DetailItem>();
        sections = new TreeSet<DetailItem>();

        for (Contact c : list) {
            add(c);
        }
        mNotifyDataSetChanged();
    }

    private void mNotifyDataSetChanged() {
        int position = 0, section = -1;
        for (DetailItem item : items) {
            if (item.type == TimeItem.SECTION) {
                CardHeader header = new CardHeader(item.toString());
                this.add(header);
                item.silkid = header.getSilkId();
                item.listPosition = position++;
                item.sectionPosition = ++section;
            } else {
                // TODO
                Card card = new Card(item.toString());
                this.add(card);
                item.silkid = card.getSilkId();
                item.listPosition = position++;
                item.sectionPosition = section;
            }
        }
    }

    @Override
    public View onViewCreated(int index, View recycled, CardBase item) {
        return super.onViewCreated(index, recycled, item);
    }

    public void setCurrentDate(Calendar calendar, Calendar calendar_end) {
        this.currentDate = calendar;
        this.currentDateEnd = calendar_end;
    }

    public void setOriginalSilkId(long originalSilkId) {
        this.originalSilkId = originalSilkId;
    }

    public void add(Contact member) {
        DetailItem section;
        if (member.isAvailable()) {
            Contact tmp = new Contact(-1, this.context.getResources().getString(R.string.available));
            section = new DetailItem(DetailItem.SECTION, tmp);
            section.isOk = true;
        } else {
            Contact tmp = new Contact(-1, this.context.getResources().getString(R.string.unavailable));
            section = new DetailItem(DetailItem.SECTION, tmp);
            section.isOk = false;
        }
        if (!items.contains(section)) {
            sections.add(section);
            items.add(section);
        }
        DetailItem item = new DetailItem(DetailItem.ITEM, member);
        item.isOk = member.isAvailable();
//        item.isOk = true;
        items.add(item);
    }

    public DetailItem getPrivateItem(int position) {
        if (position >= items.size())
            return items.last();
        Iterator<DetailItem> itr = items.iterator();
        DetailItem item = null;
        while (position >= 0) {
            item = itr.next();
            --position;
        }
        return item;
    }

    @Override
    public DetailItem[] getSections() {
        DetailItem[] sec = new DetailItem[sections.size()];
        Iterator<DetailItem> itr = sections.iterator();
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

        Iterator<DetailItem> itr = sections.iterator();
        DetailItem item = null;
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
}