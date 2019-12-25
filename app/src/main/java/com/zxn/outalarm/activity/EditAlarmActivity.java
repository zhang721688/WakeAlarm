package com.zxn.outalarm.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.zcommon.lib.UIUtils;
import com.zxn.iconitemview.IconItemView;
import com.zxn.outalarm.AlarmService;
import com.zxn.outalarm.Model.AlarmModel;
import com.zxn.outalarm.data.MyAlarmDataBase;
import com.zxn.outalarm.R;
import com.zxn.outalarm.dialog.ClockNameEditDgFrag;
import com.zxn.titleview.TitleView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *
 * Created by zyascend .
 */
public class EditAlarmActivity extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener, ClockNameEditDgFrag.OnDialogClickListener {

//    private EditText mTitleText;
    private FloatingActionButton mFAB1;
    private FloatingActionButton mFAB2;
    private int  mHour, mMinute;
    private String mTitle;
    private String mTime;
    private String mRepeatType,mRepeatCode;
    private String mActive,mWake,mRing;
    private List<Integer> repeatCode = new ArrayList<>() ;
    private int mAlarmID;
    private MyAlarmDataBase db;
    private AlarmModel mCheckedAlarm;
    private AlarmService.MyBinder binder;
    private ServiceConnection connection = null;

    private static final String KEY_TITLE = "title_key";
    private static final String KEY_TIME = "time_key";
    private static final String KEY_REPEAT = "repeat_key";
    private static final String KEY_WAKE= "wake_key";
    private static final String KEY_RING = "ring_key";
    private static final String KEY_ACTIVE = "active_key";
    public static final String ALARM_ID = "Alarm_ID";

    @BindView(R.id.title_common)
    TitleView titleCommon;
    @BindView(R.id.iiv_clock_name)
    IconItemView iivClockName;
    @BindView(R.id.iiv_clock_ring)
    IconItemView iivClockRing;
    @BindView(R.id.iiv_clock_wake)
    IconItemView iivClockWake;
    @BindView(R.id.iiv_clock_repeat)
    IconItemView iivClockRepeat;
    @BindView(R.id.iiv_clock_time)
    IconItemView iivClockTime;

    private String finalDefine;
    private MediaPlayer player;

    private void onInitTitle() {
        TextView view = new TextView(this);
        view.setText("确定");
        view.setTextColor(UIUtils.getColor(R.color.c_ffffff));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveAlarm();
            }
        });
        titleCommon.setTitleText("闹钟编辑");
        titleCommon.addRightView(view);
    }

    private void onSaveAlarm() {
        if (iivClockName.getRightText().length() == 0) {
            UIUtils.toast("闹钟名不能为空");
        } else {
            mTitle = iivClockName.getRightText();
            updateAlarm();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        ButterKnife.bind(this);
        mAlarmID = Integer.parseInt(getIntent().getStringExtra(ALARM_ID));


        onInitTitle();

        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);

        db = new MyAlarmDataBase(this);
        mCheckedAlarm = db.getAlarm(mAlarmID);

        Log.d("ed id", String.valueOf(mAlarmID));

        mTitle = mCheckedAlarm.getTitle();
        mRepeatType = mCheckedAlarm.getRepeatType();
        mTime = mCheckedAlarm.getTime();
        mActive = mCheckedAlarm.getActive();
        mWake = mCheckedAlarm.getWakeType();
        mRing = mCheckedAlarm.getRing();

        iivClockName.setRightText(mTitle);
        
        iivClockTime.setRightText((mTime));
        iivClockTime.setRightText(mTime);
        iivClockRepeat.setRightText(mRepeatType);
        iivClockRing.setRightText(mRing);
        iivClockWake.setRightText(mWake);


        // 得到上次设置状态
        if (savedInstanceState != null) {
            String savedTitle = savedInstanceState.getString(KEY_TITLE);
            iivClockName.setRightText(savedTitle);
            mTitle = savedTitle;

            String savedTime = savedInstanceState.getString(KEY_TIME);
            iivClockTime.setRightText((savedTime));
            mTime = savedTime;
            String savedRepeat= savedInstanceState.getString(KEY_REPEAT);
            iivClockRepeat.setRightText(savedRepeat);
            mRepeatType = savedRepeat;

            String savedRing = savedInstanceState.getString(KEY_RING);
            iivClockRing.setRightText(savedRing);
            mRing = savedRing;

            String savedWake = savedInstanceState.getString(KEY_WAKE);
            iivClockWake.setRightText(savedWake);
            mWake = savedWake;

            mActive = savedInstanceState.getString(KEY_ACTIVE);
        }

        if (mActive.equals("false")) {
            mFAB1.setVisibility(View.VISIBLE);
            mFAB2.setVisibility(View.GONE);
        } else if (mActive.equals("true")) {
            mFAB1.setVisibility(View.GONE);
            mFAB2.setVisibility(View.VISIBLE);
        }


    }

    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(KEY_TITLE, iivClockName.getRightText());
        outState.putCharSequence(KEY_TIME, iivClockTime.getRightText());
        outState.putCharSequence(KEY_REPEAT, iivClockRepeat.getRightText());
        outState.putCharSequence(KEY_RING, iivClockRing.getRightText());
        outState.putCharSequence(KEY_WAKE, iivClockWake.getRightText());
        outState.putCharSequence(KEY_ACTIVE, mActive);

    }

    public void selectFab1(View v){
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB1.setVisibility(View.GONE);
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mFAB2.setVisibility(View.VISIBLE);
        mActive = "true";
    }

    public void selectFab2(View v){
        mFAB2 = (FloatingActionButton) findViewById(R.id.starred2);
        mFAB2.setVisibility(View.GONE);
        mFAB1 = (FloatingActionButton) findViewById(R.id.starred1);
        mFAB1.setVisibility(View.VISIBLE);
        mActive = "false";
    }


    public void selectTime(View v){
        Calendar now = Calendar.getInstance();
        TimePickerDialog timeDialog = TimePickerDialog.newInstance(
                this, now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE),false);
        timeDialog.setThemeDark(false);
        timeDialog.show(getFragmentManager(), "选择时间");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinute = minute;
        if (minute < 10) {
            mTime = hourOfDay + ":" + "0" + minute;
        } else {
            mTime = hourOfDay + ":" + minute;
        }
        iivClockTime.setRightText((mTime));
    }

    public void selectRepeat(View v){

        final String[] items = {"只响一次","每天","周一至周五","周六周日","自定义"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_view_day_grey600_24dp);
        builder.setTitle("选择重复");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String repeatType = items[which];
                if (which == 4) {
                    showDefineDialog(dialog);
                } else {
                    mRepeatType = repeatType;
                    iivClockRepeat.setRightText(mRepeatType);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void selectWake(View v){
        final String[] items = {"常规","回答问题"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("停闹方式");
        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mWake = items[which];
                iivClockWake.setRightText( mWake);

                if (which == 1){
                    Intent i = new Intent(EditAlarmActivity.this, QuestionActivity.class);
                    i.putExtra(ALARM_ID, Integer.toString(mAlarmID));
                    startActivity(i);
                }

                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void showDefineDialog(DialogInterface lastDialog) {

        lastDialog.dismiss();

        final String[] myDefine = {"周一","周二","周三","周四","周五","周六","周日"};
        final List<String> choosedDefine = new ArrayList<>();
        finalDefine="";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("自定义");
        builder.setMultiChoiceItems(myDefine, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                if (isChecked) {
                    choosedDefine.add(myDefine[which]);
                    repeatCode.add(which + 2);

                    StringBuilder sb = new StringBuilder();
                    if (repeatCode != null && repeatCode.size() > 0) {
                        for (int i = 0; i < repeatCode.size(); i++) {
                            if (i < repeatCode.size() - 1) {
                                sb.append(repeatCode.get(i) + ",");
                            } else {
                                sb.append(repeatCode.get(i));
                            }
                        }
                    }
                    mRepeatCode = sb.toString();
                }
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < choosedDefine.size(); i++) {
                    finalDefine = finalDefine + " " + choosedDefine.get(i);
                    if (choosedDefine.size() == 7) {
                        mRepeatType = "每天";
                        iivClockRepeat.setRightText(mRepeatType);
                    } else {
                        mRepeatType = finalDefine;
                        iivClockRepeat.setRightText(mRepeatType);
                    }
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void showRingDialog(DialogInterface lastDialog) {

        lastDialog.dismiss();


        String[] ringList = new String[]{"Morning","卡农","空灵","天籁森林","唯美","温暖早晨"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(ringList, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = getSharedPreferences("ringCode", MODE_PRIVATE).edit();
                editor.putInt("key_ring", which+1);
                System.out.println("已添加ringCode 是：  "+(which+1));
                editor.apply();
                playRing(which + 1);

            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (player != null && player.isPlaying()) {
                    player.stop();
                    player.release();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (player.isPlaying()) {
                    player.stop();
                    player.release();
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void selectRing(View v){
        final String[] options = new String[]{"震动","响铃","震动并响铃"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择方式");
        builder.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRing = options[which];
                iivClockRing.setRightText(mRing);
                if (which == 1 || which == 2) {
                    showRingDialog(dialog);
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void playRing(int i){
        switch (i){
            case 1:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this, R.raw.ring01);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring01);

                }
                break;
            case 2:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring02);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring02);

                }
                break;
            case 3:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring03);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring03);

                }
                break;
            case 4:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring04);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring04);

                }
                break;
            case 5:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring05);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring05);

                }
                break;
            case 6:
                if (player!=null && player.isPlaying()){
                    player.stop();
                    player.release();
                    player = MediaPlayer.create(this,R.raw.ring06);
                }else {
                    player = MediaPlayer.create(this,R.raw.ring06);

                }
                break;

        }
        assert player != null;
        player.setLooping(true);
        player.start();

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_alarm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

//            case R.id.save_Alarm:
//                iivClockName.setRightText(mTitle);
//                if (mTitleText.getText().toString().length() == 0)
//                    mTitleText.setError("闹钟名不能为空");
//                else {
//                    updateAlarm();
//                }
//                return true;
            case R.id.discard_alarm:
                Toast.makeText(getApplicationContext(), "取消编辑",
                        Toast.LENGTH_SHORT).show();

                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateAlarm() {

        mCheckedAlarm.setTitle(mTitle);
        mCheckedAlarm.setTime(mTime);
        mCheckedAlarm.setRepeatType(mRepeatType);
        mCheckedAlarm.setRepeatCode(mRepeatCode);
        mCheckedAlarm.setWakeType(mWake);
        mCheckedAlarm.setActive(mActive);
        mCheckedAlarm.setRing(mRing);
        db.updateAlarm(mCheckedAlarm);

        if (mActive.equals("true")){
            connection = new ServiceConnection(){
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    binder = (AlarmService.MyBinder)service;

                    switch (mRepeatType) {
                        case "只响一次":
                            binder.setSingleAlarm(getApplicationContext(), mTime, mAlarmID);
                            break;
                        case "每天":
                            binder.setEverydayAlarm(getApplicationContext(), mTime, mAlarmID);
                            break;
                        default:
                            binder.setDiyAlarm(getApplicationContext(), mRepeatType, mTime, mAlarmID,mRepeatCode);
                            break;
                    }
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            Intent intent = new Intent(this, AlarmService.class);
            bindService(intent, connection, BIND_AUTO_CREATE);
        }

        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {

        if (connection!=null) {
            unbindService(connection);
        }
        if (player!=null){
            player.release();
        }
        super.onDestroy();
    }

    @OnClick({R.id.iiv_clock_name, R.id.iiv_clock_ring, R.id.iiv_clock_wake, R.id.iiv_clock_repeat, R.id.iiv_clock_time})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iiv_clock_name:
                ClockNameEditDgFrag.newInstance(this).show(getSupportFragmentManager());
                break;
            case R.id.iiv_clock_ring:
                selectRing(view);
                break;
            case R.id.iiv_clock_time:
                selectTime(view);
                break;
            case R.id.iiv_clock_repeat:
                selectRepeat(view);
                break;
            case R.id.iiv_clock_wake:
                selectWake(view);
                break;
        }
    }

    @Override
    public void onConfirmClick(String num) {
        iivClockName.setRightText(num);
    }

}
