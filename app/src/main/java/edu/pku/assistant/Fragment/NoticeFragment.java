package edu.pku.assistant.Fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardAdapter;
import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardListView;

import java.util.HashMap;

import edu.pku.assistant.HomeActivity;
import edu.pku.assistant.R;
import in.srain.cube.request.JsonData;
import in.srain.cube.request.RequestFinishHandler;
import in.srain.cube.util.LocalDisplay;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;


public class NoticeFragment extends Fragment {

    private static final String FRAGMENT_TITLE_CN = "通知";
    private static final String FRAGMENT_TITLE_EN = "Notification";

    private SharedPreferences mConfig;
    private int userId;

    protected PtrFrameLayout mPtrFrameLayout;
    private CardListView mListView;
    private CardAdapter<Card> adapter;
    private HashMap<Integer, Integer> map;

    public static NoticeFragment newInstance() {
        NoticeFragment fragment = new NoticeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public NoticeFragment() {
    }

    //
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notice, null);

        mPtrFrameLayout = (PtrFrameLayout) view.findViewById(R.id.list_view_with_empty_view_fragment_ptr_frame);
        mListView = (CardListView) view.findViewById(R.id.list_view_with_empty_view_fragment_list_view);

        map = new HashMap<Integer, Integer>();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mConfig = getActivity().getSharedPreferences("account", Context.MODE_PRIVATE);
        userId = mConfig.getInt("userid", -1);

        // header
        final MaterialHeader header = new MaterialHeader(getActivity());
        int[] colors = getResources().getIntArray(R.array.google_colors);
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        header.setPadding(0, LocalDisplay.dp2px(15), 0, LocalDisplay.dp2px(100));
        header.setPtrFrameLayout(mPtrFrameLayout);

        mPtrFrameLayout.setHeaderView(header);
        mPtrFrameLayout.addPtrUIHandler(header);

        adapter = new CardAdapter<Card>(getActivity(), android.R.color.holo_blue_dark);
        mListView.setAdapter(adapter);

        mListView.setOnCardClickListener(new CardListView.CardClickListener() {
            @Override
            public void onCardClick(int index, CardBase card, View view) {
                // TODO
                Intent intent = new Intent(getActivity(), NoticeActivity.class);
                Bundle bundle = new Bundle();
                Log.d("notice_index", String.valueOf(index) + ", " + map.get(index));
                bundle.putInt("groupid", map.get(index));
                intent.putExtras(bundle);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
                adapter.remove(index);
                adapter.notifyDataSetChanged();
            }
        });

        mPtrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {

                // here check $mListView instead of $content
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, mListView, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                updateData();
            }
        });
        // the following are default settings
        mPtrFrameLayout.setResistance(1.7f);
        mPtrFrameLayout.setRatioOfHeaderHeightToRefresh(1.2f);
        mPtrFrameLayout.setDurationToClose(200);
        mPtrFrameLayout.setDurationToCloseHeader(1000);
        // default is false
        mPtrFrameLayout.setPullToRefresh(false);
        // default is true
        mPtrFrameLayout.setKeepHeaderWhenRefresh(true);
        mPtrFrameLayout.autoRefresh(true);
    }


    protected void updateData() {

        DemoRequestData.getImageList(new RequestFinishHandler<JsonData>() {
            @Override
            public void onRequestFinish(final JsonData data) {
                // TODO test data
                JsonData fake_data = JsonData.create("{\"result\":2, list: [{\"msg\":\"魏奎邀请您在4月30日 14:00-16:00参加会议\", \"groupid\":0},{\"msg\":\"魏奎邀请您在4月30日 16:00-18:00参加会议\", \"groupid\":0}]}");

//                displayData(data);
                displayData(fake_data);
            }
        });
    }

    private void displayData(JsonData data) {

        int i = 0;
        adapter.clear();
        map.clear();
        for (JsonData jsondata : data.optJson("list").toArrayList()) {
            Card card = new Card("活动请求", jsondata.optString("msg"));
            adapter.add(card);
            map.put(i, jsondata.optInt("groupid"));
            i++;
        }
        // TODO
        mPtrFrameLayout.refreshComplete();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getResources().getConfiguration().locale.getCountry().equals("CN")) {
            ((HomeActivity) activity).onSectionAttached(FRAGMENT_TITLE_CN);
        } else {
            ((HomeActivity) activity).onSectionAttached(FRAGMENT_TITLE_EN);
        }
    }
}