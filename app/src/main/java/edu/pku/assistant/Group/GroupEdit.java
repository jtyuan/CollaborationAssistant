package edu.pku.assistant.Group;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardAdapter;
import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardHeader;
import com.afollestad.cardsui.CardListView;
import com.beardedhen.androidbootstrap.BootstrapButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import edu.pku.assistant.ContactInfoActivity;
import edu.pku.assistant.Group.member.MemberFromContacts;
import edu.pku.assistant.Group.member.MemberFromSearch;
import edu.pku.assistant.Group.util.Group;
import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Contact;

@SuppressWarnings("unchecked")
public class GroupEdit extends FragmentActivity {

    private static final int REQUEST_MEM = 4;


    private static final int INDEX_MEMBER = 5;
    private static final int INDEX_GROUP_NAME = 1;

    private CardAdapter adapter;

    private Group groupInfo = null;
    private Calendar createDate = Calendar.getInstance();

    private ArrayList<Contact> members;

    private HashMap<Contact, Integer> memberChange; // second == false: removed, second == true: added

    private boolean edit_mode = false;

    //private HashMap<Integer, Card> userIdToCard;
    private SparseArray<Card> userIdToCard;
    private Card gmemnum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list_with_two_buttons);

        //userIdToCard = new HashMap<Integer, Card>();
        userIdToCard = new SparseArray<Card>();
        memberChange = new HashMap<Contact, Integer>();
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        edit_mode = bundle.getBoolean("edit_mode");
        groupInfo = (Group) bundle.getSerializable("group");
        members = (ArrayList<Contact>) bundle.getSerializable("members");
        initView();
    }

    private void initView() {

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        BootstrapButton button = (BootstrapButton) findViewById(R.id.button_confirm);
        button.setText(getResources().getString(R.string.button_confirm));
        BootstrapButton button_delete = (BootstrapButton) findViewById(R.id.button_delete);

        CardListView list = (CardListView) findViewById(R.id.card_list);
        adapter = new GroupNameAdapter(this, android.R.color.holo_blue_dark);
        adapter.registerLayout(R.layout.member_list_header);
        list.setAdapter(adapter);

        CardHeader chGroup = new CardHeader(GroupEdit.this.getResources().getString(R.string.basic_info));
        Card chMember = new Card(GroupEdit.this.getResources().getString(R.string.group_member));

        chMember.setLayout(R.layout.member_list_header);
        chMember.setClickable(false);

        chMember.setPopupMenu(R.menu.member_card, new Card.CardMenuListener<Card>() {
            @Override
            public void onMenuItemClick(Card card, MenuItem item) {
                Intent intent;
                Bundle bundle;
                switch (item.getItemId()) {
                    case R.id.add_member_from_contacts:
                        intent = new Intent(GroupEdit.this, MemberFromContacts.class);
                        bundle = new Bundle();
                        bundle.putSerializable("members", members);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, REQUEST_MEM);
                        overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                        break;
                    case R.id.add_member_from_search:
                        intent = new Intent(GroupEdit.this, MemberFromSearch.class);
                        bundle = new Bundle();
                        bundle.putSerializable("members", members);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, REQUEST_MEM);
                        overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                        break;
                }
            }
        });
//        chMember
//        chMember.setAction(GroupEdit.this.getResources().getString(R.string.add_new), new CardHeader.ActionListener() {
//            @Override
//            public void onHeaderActionClick(CardHeader header) {
////                PopupMenu pop = new PopupMenu(GroupEdit.this, );
//                Intent intent = new Intent(GroupEdit.this, MemberFromSearch.class);
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("members", members);
//                intent.putExtras(bundle);
//                startActivityForResult(intent, REQUEST_MEM);
//                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
//            }
//        });

        Card gname = new GroupNameCard(this, R.id.title, R.id.et_group_name);

        gmemnum = new Card(GroupEdit.this.getResources().getString(R.string.memnum), String.valueOf(members.size()+1));
        Card gcreator = new Card(GroupEdit.this.getResources().getString(R.string.creator), groupInfo.creatorName);
        Card gtime = new Card(GroupEdit.this.getResources().getString(R.string.create_date), GroupActivity.sdf.format(createDate.getTime()));

        adapter.add(chGroup);
        adapter.add(gname);
        adapter.add(gmemnum);
        adapter.add(gcreator);
        adapter.add(gtime);

        adapter.add(chMember);
        if (members != null) {
            for (Contact m : members) {
                Card mc = new Card(m.getName());
                adapter.add(mc);
                userIdToCard.put(m.getId(), mc);
            }
        }


        list.setOnCardClickListener(new CardListView.CardClickListener() {
            @Override
            public void onCardClick(int index, CardBase card, View view) {
                if (index > INDEX_MEMBER) { // member info
                    Intent intent = new Intent(GroupEdit.this, ContactInfoActivity.class);
                    intent.putExtra("contact", members.get(index-INDEX_MEMBER-1));
                    startActivityForResult(intent, 0);
                    overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                }
            }
        });

        if (!edit_mode) {
            button_delete.setVisibility(View.GONE);
        } else {
            button_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(GroupEdit.this).setTitle(GroupEdit.this.getResources().getString(R.string.alert))
                            .setMessage(GroupEdit.this.getResources().getString(R.string.remove_group_confirm))
                            .setPositiveButton(GroupEdit.this.getResources().getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent();
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean("removed", true);
                                    intent.putExtras(bundle);
                                    setResult(RESULT_CANCELED, intent);
                                    finish();
                                    overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
                                }
                            })
                            .setNegativeButton(GroupEdit.this.getResources().getString(R.string.button_cancel), null)
                            .show();

                }
            });
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupInfo.groupName.length() > 0) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("group", groupInfo);
                    bundle.putSerializable("members", members);
                    bundle.putSerializable("member_change", memberChange);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
                } else {
                    Toast.makeText(GroupEdit.this, GroupEdit.this.getResources().getString(R.string.please_name), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
//                recoverOriginalMembers();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putBoolean("removed", !edit_mode);
                intent.putExtras(bundle);
                setResult(RESULT_CANCELED, intent);
                finish();
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
//        recoverOriginalMembers();
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putBoolean("removed", !edit_mode);
        intent.putExtras(bundle);
        setResult(RESULT_CANCELED, intent);
        super.onBackPressed();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Boolean removed = data.getExtras().getBoolean("removed");
            if (!removed) {
                ArrayList<Contact> localNewMembers = (ArrayList<Contact>) data.getExtras().getSerializable("new_members");
                if (localNewMembers != null) {
                    for (Contact m : localNewMembers) {
                        members.add(m);
                        if (memberChange.containsKey(m)) {
                            memberChange.put(m, memberChange.get(m)+1);
                        } else {
                            memberChange.put(m, 1);
                        }
                        Card c = new Card(m.getName());
                        adapter.add(c);
                        userIdToCard.put(m.getId(), c);
                    }
                }
            } else {
                Contact member = (Contact) data.getExtras().getSerializable("removed_member");
                members.remove(member);
                if (memberChange.containsKey(member)) {
                    memberChange.put(member, memberChange.get(member)-1);
                } else {
                    memberChange.put(member, -1);
                }
                adapter.remove(userIdToCard.get(member.getId()));
            }
            gmemnum.setContent(String.valueOf(members.size()));
            adapter.notifyDataSetChanged();
        }
    }

    public class GroupNameCard extends Card {
        private static final long serialVersionUID = -4085099599157152843L;

        public GroupNameCard(Context context, int title, int content) {
            super(context, title, content);
        }

        public int getLayout() {
            // Replace with your layout
            return R.layout.card_group_name;
        }
    }

    public class GroupNameAdapter extends CardAdapter {

        public GroupNameAdapter(Context context, int accentColorRes) {
            super(context, accentColorRes);
            registerLayout(R.layout.card_group_name);
        }

        @Override
        public View onViewCreated(int index, View recycled, CardBase item) {
            View view = super.onViewCreated(index, recycled, item);

            if (index == INDEX_GROUP_NAME) {
                TextView title = (TextView) view.findViewById(android.R.id.title);
                final EditText editText = (EditText) view.findViewById(R.id.et_group_name);

                if (title != null) {
                    title.setText(GroupEdit.this.getResources().getString(R.string.group_title));
                }
                if (editText != null) {
                    if (!groupInfo.groupName.equals("")) {
                        editText.setText(groupInfo.groupName);
                    }
                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            groupInfo.groupName = editText.getText().toString();
                        }
                    });
                }
            }
            return view;
        }
    }
}
