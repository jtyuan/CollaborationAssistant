package edu.pku.assistant.Group.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import edu.pku.assistant.Group.recommendation.RecommendCondition;
import edu.pku.assistant.R;

public class ModePickerDialogFragment extends DialogFragment {

    private static final String ARG_CUR_ITEM = "currentItem";

    private int currentItem;

    private AbsListView mListView;

    private ListAdapter mAdapter;

    private FragmentActivity activity;

    public ModePickerDialogFragment() {

    }

    public static ModePickerDialogFragment newInstance() {
        return new ModePickerDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("modedialog", "new instance");
        activity = getActivity();

        String[] items = {ModePickerDialogFragment.this.getActivity().getResources().getString(R.string.morning),
                ModePickerDialogFragment.this.getActivity().getResources().getString(R.string.afternoon),
                ModePickerDialogFragment.this.getActivity().getResources().getString(R.string.anytime),
                ModePickerDialogFragment.this.getActivity().getResources().getString(R.string.custom)};

        mAdapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_list_item_1, android.R.id.text1, items);
        Log.d("modedialog", mAdapter.toString());
    }

    public Dialog onCreateDialog(Bundle saveInstanceState) {
        super.onCreateDialog(saveInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();

        View view = inflater.inflate(R.layout.fragment_mode, null);

        builder.setTitle(ModePickerDialogFragment.this.getActivity().getResources().getString(R.string.select_time))
               .setView(view);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(R.id.list);
        mListView.setAdapter(mAdapter);
        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                TimeLog.mAdapter.itemSelectInteraction(currentItem, i);
                ((RecommendCondition)getActivity()).onModeChanged(i);
                dismiss();
            }
        });
        return builder.create();
    }
}
