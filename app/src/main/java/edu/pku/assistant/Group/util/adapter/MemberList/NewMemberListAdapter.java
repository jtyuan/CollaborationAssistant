package edu.pku.assistant.Group.util.adapter.MemberList;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.SectionIndexer;

import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardAdapter;
import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardHeader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Contact;

@SuppressWarnings("unchecked")
public class NewMemberListAdapter extends CardAdapter implements SectionIndexer {

    Context context;

    private TreeSet<MemberItem> items;
    private TreeSet<MemberItem> sections;

    public NewMemberListAdapter(Context context, int accentColorRes, ArrayList<Contact> list) {
        super(context, accentColorRes);

        this.context = context;

        items = new TreeSet<MemberItem>();
        sections = new TreeSet<MemberItem>();

        for (Contact c : list) {
            add(c);
        }
        mNotifyDataSetChanged();
    }

    private void mNotifyDataSetChanged() {
        int position = 0, section = -1;
        for (MemberItem item : items) {
            if (item.type == MemberItem.SECTION) {
                CardHeader header = new CardHeader(item.toString());
                this.add(header);
                item.silkid = header.getSilkId();
                item.listPosition = position++;
                item.sectionPosition = ++section;
            } else {
                // TODO
                Card card = new Card(item.toString(), "tags:");
                card.setPopupMenu(R.menu.member_card, new Card.CardMenuListener<Card>() {
                    @Override
                    public void onMenuItemClick(Card card, MenuItem item) {
                        if (item.getItemId() == R.id.action_add) {
                            // TODO
                        }
                    }
                });
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

    public void add(Contact member) {
        Contact tmp = new Contact(-1, String.valueOf(member.getName().charAt(0)));
        MemberItem section = new MemberItem(MemberItem.SECTION, tmp);
        if (!items.contains(section)) {
            sections.add(section);
            items.add(section);
        }
        MemberItem item = new MemberItem(MemberItem.ITEM, member);
        items.add(item);
    }

    public MemberItem getPrivateItem(int position) {
        if (position >= items.size())
            return items.last();
        Iterator<MemberItem> itr = items.iterator();
        MemberItem item = null;
        while (position >= 0) {
            item = itr.next();
            --position;
        }
        return item;
    }

    @Override
    public MemberItem[] getSections() {
        MemberItem[] sec = new MemberItem[sections.size()];
        Iterator<MemberItem> itr = sections.iterator();
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

        Iterator<MemberItem> itr = sections.iterator();
        MemberItem item = null;
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