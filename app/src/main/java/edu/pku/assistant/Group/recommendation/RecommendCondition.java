package edu.pku.assistant.Group.recommendation;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.cardsui.Card;
import com.afollestad.cardsui.CardAdapter;
import com.afollestad.cardsui.CardBase;
import com.afollestad.cardsui.CardHeader;
import com.afollestad.cardsui.CardListView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import edu.pku.assistant.Group.util.ModePickerDialogFragment;
import edu.pku.assistant.Group.util.TimeSeg;
import edu.pku.assistant.R;
import edu.pku.assistant.Tool.Contact;

@SuppressWarnings("unchecked")
public class RecommendCondition extends FragmentActivity implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener, NumberPickerDialogFragment.NumberPickerDialogHandler, NumberPicker.OnValueChangeListener {

    private static final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");

    private static final int INDEX_START = 1;
    private static final int INDEX_DATE = 2;
    private static final int INDEX_TIME = 3;
    private static final int INDEX_LENGTH = 4;
    private static final int INDEX_TIME_START = 4;
    private static final int INDEX_TIME_END = 5;
    private static final int INDEX_TIME_DES = 5;
    private static final int INDEX_TIME_DES_CUS = 6;

    private static final int LEN_DEFAULT_HOUR = 2;
    private static final int LEN_DEFAULT_MINUTE = 0;

    public static final int RESULT_CHANGE = 15;

    public static final int MODE_MORNING = 0;
    public static final int MODE_AFTERNOON = 1;
    public static final int MODE_ANYTIME = 2;
    public static final int MODE_CUSTOMED = 3;

    public static final String FRAG_TAG_DATE_PICKER = "DatePicker";
    public static final String FRAG_TAG_TIME_PICKER = "TimePicker";
    public static final String FRAG_TAG_MODE_PICKER = "ModePicker";

    private static final int MAX_LENGTH = 8;
    private static final int MIN_LENGTH = 1;
    private static final int MAX_DAYS = 15;
    private static final int MIN_DAYS = 1;

    private static final int MAX_LEN_HOUR = 9;
    private static final int MIN_LEN_HOUR = 0;

    private static final int MAX_LEN_MINUTE = 59;
    private static final int MIN_LEN_MINUTE = 0;


    private static final int DATE_PICKER = 0;
    private static final int LENG_PICKER = 1;
    private static final int LENG_PICKER_MIN = 2;

    private Calendar calendar;
    private Calendar calendar_end;
    private double length;
    private int mode;
    private int days;

    private boolean isCustom = false;
    private boolean isFirst = true;

    private CardAdapter adapter;

    private Card start, date, time, len, time1, time2, desc;

    private ArrayList<Contact> members;
    private int groupId;
    private int timesegId;
    private boolean newseg;
    private String description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            members = (ArrayList<Contact>) bundle.getSerializable("members");
            groupId = bundle.getInt("groupId");
            timesegId = bundle.getInt("timesegId");
            newseg = bundle.getBoolean("newseg");
            description = bundle.getString("description");
        }

        if (newseg)
            setContentView(R.layout.activity_card_list_with_button);
        else
            setContentView(R.layout.activity_card_list_with_two_ok_buttons);
        initView();
    }

    private void initView() {

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        BootstrapButton button_confirm = (BootstrapButton) findViewById(R.id.button_confirm);
        button_confirm.setText(RecommendCondition.this.getResources().getString(R.string.rec_confirm));
        BootstrapButton button_ok = null;
        if (!newseg) {
            button_ok = (BootstrapButton) findViewById(R.id.button_ok);
            button_ok.setText(RecommendCondition.this.getResources().getString(R.string.rec_confirm_return));
        }

        calendar = Calendar.getInstance();
        calendar_end = Calendar.getInstance();
        days = 3;
        length = 2;
        mode = MODE_MORNING;

        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar_end.set(Calendar.MINUTE, 0);
        calendar_end.set(Calendar.SECOND, 0);
        calendar_end.set(Calendar.MILLISECOND, 0);
        calendar_end.set(Calendar.DATE, calendar.get(Calendar.DATE));
        calendar_end.add(Calendar.DATE, days-1);

        CardListView list = (CardListView) findViewById(R.id.card_list);
        adapter = new CardAdapter(this, android.R.color.holo_blue_dark);
        list.setAdapter(adapter);

        CardHeader header = new CardHeader(RecommendCondition.this.getResources().getString(R.string.cond_set));

        start = new Card(RecommendCondition.this.getResources().getString(R.string.start_date), sdf1.format(calendar.getTime()));
        date = new Card(RecommendCondition.this.getResources().getString(R.string.date_range), "3" + RecommendCondition.this.getResources().getString(R.string.days));
        time = new Card(RecommendCondition.this.getResources().getString(R.string.time_quantum), RecommendCondition.this.getResources().getString(R.string.morning));
        len = new Card(RecommendCondition.this.getResources().getString(R.string.duration), "2" + RecommendCondition.this.getResources().getString(R.string.hours));

        time1 = new Card(RecommendCondition.this.getResources().getString(R.string.start_time), sdf2.format(calendar.getTime()));
        time2 = new Card(RecommendCondition.this.getResources().getString(R.string.end_time), sdf2.format(calendar_end.getTime()));

        desc = new Card(RecommendCondition.this.getResources().getString(R.string.ts_description_title), description);

        adapter.add(header);
        adapter.add(start);
        adapter.add(date);
        adapter.add(time);
        adapter.add(len);
        adapter.add(desc);

        list.setOnCardClickListener(new CardListView.CardClickListener() {
            @Override
            public void onCardClick(int index, CardBase card, View view) {
                if (mode != MODE_CUSTOMED) {
                    switch (index) {
                        case INDEX_START:
                            showStartDialog();
                            break;
                        case INDEX_DATE:
                            showDateDialog();
                            break;
                        case INDEX_TIME:
                            showModeDialog();
                            break;
                        case INDEX_LENGTH:
                            showNumberDialog();
                            break;
                        case INDEX_TIME_DES:
                            showDescDialog();
                            break;
                    }
                } else {
                    switch (index) {
                        case INDEX_START:
                            showStartDialog();
                            break;
                        case INDEX_DATE:
                            showDateDialog();
                            break;
                        case INDEX_TIME:
                            showModeDialog();
                            break;
                        case INDEX_TIME_START:
                            isFirst = true;
                            showTimeDialog();
                            break;
                        case INDEX_TIME_END:
                            isFirst = false;
                            showTimeDialog();
                            break;
                        case INDEX_TIME_DES_CUS:
                            showDescDialog();
                            break;
                    }
                }
            }
        });

        button_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecommendCondition.this, Recommendation.class);
                Bundle bundle = new Bundle();
                bundle.putInt("groupId", groupId);
                bundle.putSerializable("members", members);
                bundle.putSerializable("condition", new TimeSeg(RecommendCondition.this.calendar,
                        RecommendCondition.this.calendar_end, RecommendCondition.this.mode,
                        RecommendCondition.this.length));
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
            }
        });

        if (button_ok != null) {
            button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent result = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt("timesegId", timesegId);
                    bundle.putString("description", desc.getContent().toString());
                    result.putExtras(bundle);
                    Log.d("recommend_condition", "_timesegId: " + result.getExtras().getInt("timesegId"));
                    setResult(RESULT_CHANGE, result);
                    finish();
                    overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
                }
            });
        }
    }

    private void showModeDialog() {
        FragmentManager fm = RecommendCondition.this.getSupportFragmentManager();
        ModePickerDialogFragment tf = ModePickerDialogFragment.newInstance();
        tf.show(fm, FRAG_TAG_MODE_PICKER);
    }

    private void showNumberDialog() {
//        NumberPickerBuilder npb = new NumberPickerBuilder()
//                .setFragmentManager(getSupportFragmentManager())
//                .setStyleResId(R.style.BetterPickersDialogFragment_Light)
//                .setMaxNumber(MAX_LEN_HOUR)
//                .setMinNumber(MIN_LEN_HOUR)
//                .setDecimalVisibility(View.INVISIBLE)
//                .setPlusMinusVisibility(View.INVISIBLE)
//                .setLabelText("hours")
//                .setReference(LENG_PICKER);
//        npb.show();
//        TimePickerDialog dialog = new TimePickerDialog(this, this, LEN_DEFAULT_HOUR, LEN_DEFAULT_MINUTE, true);
//        dialog.show();
        final AlertDialog.Builder builder = new AlertDialog.Builder(RecommendCondition.this);

        builder.setTitle(RecommendCondition.this.getResources().getString(R.string.len_title));
        LinearLayout LL = new LinearLayout(RecommendCondition.this);
        LL.setOrientation(LinearLayout.HORIZONTAL);
        LL.setPadding(64, 64, 64, 64);

        final NumberPicker np_hour = new NumberPicker(RecommendCondition.this);
        np_hour.setMaxValue(MAX_LEN_HOUR);
        np_hour.setMinValue(MIN_LEN_HOUR);
        np_hour.setValue(LEN_DEFAULT_HOUR);
        String[] hours = new String[MAX_LEN_HOUR-MIN_LEN_HOUR+1];
        for (int i = MIN_LEN_HOUR; i <= MAX_LEN_HOUR; ++i) {
            hours[i] = String.valueOf(i) + RecommendCondition.this.getResources().getString(R.string.hours);
        }
        np_hour.setDisplayedValues(hours);
        np_hour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        final NumberPicker np_minute = new NumberPicker(RecommendCondition.this);
        np_minute.setMaxValue(MAX_LEN_MINUTE/10);
        np_minute.setMinValue(MIN_LEN_MINUTE/10);
        np_minute.setValue(LEN_DEFAULT_MINUTE/10);
        String[] minutes = new String[(MAX_LEN_MINUTE-MIN_LEN_MINUTE+1)/10];
        for (int i = MIN_LEN_MINUTE; i <= MAX_LEN_MINUTE; i += 10) {
            minutes[i/10] = String.valueOf(i) + RecommendCondition.this.getResources().getString(R.string.minutes);
        }
        np_minute.setDisplayedValues(minutes);
        np_minute.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50);
        params.gravity = Gravity.CENTER;

        LinearLayout.LayoutParams param_hour = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param_hour.weight = 1;

        LinearLayout.LayoutParams param_minute = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        param_minute.weight = 1;

        LL.setLayoutParams(params);
        LL.addView(np_hour, param_hour);
        LL.addView(np_minute, param_minute);

        builder.setView(LL);

        builder.setPositiveButton(RecommendCondition.this.getResources().getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                Toast.makeText(RecommendCondition.this, np_hour.getValue(), Toast.LENGTH_SHORT).show();
                length = np_hour.getValue() + (double) np_minute.getValue() / 6.0;
                len.setContent(String.valueOf(np_hour.getValue()) + RecommendCondition.this.getResources().getString(R.string.hours)
                        + String.valueOf(np_minute.getValue()*10) + RecommendCondition.this.getResources().getString(R.string.minutes));
                adapter.notifyDataSetChanged();
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton(RecommendCondition.this.getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    private void showTimeDialog() {
        TimePickerDialog dialog = new TimePickerDialog(this, this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        dialog.show();
    }

    private void showStartDialog() {
        FragmentManager fm = this.getSupportFragmentManager();
        DatePickerDialog datePickerDialog = (DatePickerDialog) fm.findFragmentByTag(FRAG_TAG_DATE_PICKER);
        if (datePickerDialog != null) {
            datePickerDialog.dismiss();
        }
        datePickerDialog =
                DatePickerDialog.newInstance(RecommendCondition.this, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        datePickerDialog.setYearRange(calendar.get(Calendar.YEAR), datePickerDialog.getMaxYear());
        datePickerDialog.show(fm, FRAG_TAG_DATE_PICKER);
    }

    private void showDateDialog() {
        NumberPickerBuilder npb = new NumberPickerBuilder()
                .setFragmentManager(getSupportFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                .setMaxNumber(MAX_DAYS)
                .setMinNumber(MIN_DAYS)
                .setDecimalVisibility(View.INVISIBLE)
                .setPlusMinusVisibility(View.INVISIBLE)
                .setLabelText("days")
                .setReference(DATE_PICKER);
        npb.show();
    }

    private void showDescDialog() {
        final EditText editText = new EditText(this);
        AlertDialog.Builder ad = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.ts_description_title))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(editText).setPositiveButton(getResources().getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        desc.setContent(editText.getText());
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.button_cancel), null);
        ad.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//        calendar_end.set(Calendar.DATE, calendar.get(Calendar.DATE));
        int t = calendar.get(Calendar.DATE);
        calendar_end.setTimeInMillis(calendar.getTimeInMillis());
        calendar_end.add(Calendar.DATE, days-1);
        start.setContent(sdf1.format(calendar.getTime()));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
        switch (reference) {
//            case LENG_PICKER:
//                length = number;
//                len.setContent(String.valueOf(number)+RecommendCondition.this.getResources().getString(R.string.hours));
//                showNumberDialogOfMinute();
//                break;
//            case LENG_PICKER_MIN:
//                length += (double)number / 60;
//                len.setContent(len.getContent()+String.valueOf(number)+RecommendCondition.this.getResources().getString(R.string.minutes));
//                break;
            case DATE_PICKER:
                days = number;
                date.setContent(String.valueOf(days) + RecommendCondition.this.getResources().getString(R.string.days));
                calendar_end.set(Calendar.DATE, calendar.get(Calendar.DATE));
                calendar_end.add(Calendar.DATE, days-1);
                break;
        }
        adapter.notifyDataSetChanged();
    }

    public void onModeChanged(int index) {
        String[] modes = {getResources().getString(R.string.morning), RecommendCondition.this.getResources().getString(R.string.afternoon), RecommendCondition.this.getResources().getString(R.string.anytime), RecommendCondition.this.getResources().getString(R.string.custom)};

        time.setContent(modes[index]);
        mode = index;

//        String desc_bak = desc.getContent().toString();

        if (mode == MODE_CUSTOMED) {
            if (!isCustom) {
                adapter.remove(len);
                adapter.remove(desc);
                adapter.add(time1);
                adapter.add(time2);
                adapter.add(desc);
                isCustom = true;
            }
        } else {
            if (isCustom) {
                adapter.remove(time1);
                adapter.remove(time2);
                adapter.remove(desc);
                adapter.add(len);
                adapter.add(desc);
                isCustom = false;
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d("result", data.getExtras().getSerializable("result").toString());
            Log.d("recommend_condition", "timesegId: " + timesegId);
            Intent result = new Intent();
            Bundle bundle = data.getExtras();
            bundle.putInt("timesegId", timesegId);
            bundle.putString("description", desc.getContent().toString());
            result.putExtras(bundle);
            Log.d("recommend_condition", "_timesegId: " + result.getExtras().getInt("timesegId"));
            setResult(RESULT_OK, result);
            finish();
            overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        if (isFirst) {
            calendar.set(Calendar.HOUR_OF_DAY, i);
            calendar.set(Calendar.MINUTE, i1);
            time1.setContent(sdf2.format(calendar.getTime()));
        } else {
            calendar_end.set(Calendar.HOUR_OF_DAY, i);
            calendar_end.set(Calendar.MINUTE, i1);
            time2.setContent(sdf2.format(calendar_end.getTime()));
        }
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {

    }
}
