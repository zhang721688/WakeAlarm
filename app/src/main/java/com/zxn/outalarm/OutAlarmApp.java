package com.zxn.outalarm;

import android.app.Application;

import com.zcommon.lib.UIUtils;

/**
 * Created by zxn on 2019/12/25.
 */
public class OutAlarmApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        UIUtils.init(this);
    }
}
