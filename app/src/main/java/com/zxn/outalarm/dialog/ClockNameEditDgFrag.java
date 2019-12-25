package com.zxn.outalarm.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zcommon.lib.UIUtils;
import com.zxn.outalarm.R;
import com.zxn.presenter.view.BaseDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by zxn on 2019-6-12 10:46:40.
 */
public class ClockNameEditDgFrag extends BaseDialogFragment {
    private static final String ARG_PARAM1 = "param1";


    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.tv_dialog_content)
    TextView tvDialogContent;
    @BindView(R.id.iv_dialog_cancle)
    ImageView ivDialogCancle;
    @BindView(R.id.et_money)
    EditText etMoney;
    @BindView(R.id.tv_dialog_notice)
    TextView tvDialogNotice;
    @BindView(R.id.tv_ok)
    TextView tvOk;

    private String mTotalAmount = "";


    public ClockNameEditDgFrag() {
    }

    public static ClockNameEditDgFrag newInstance(OnDialogClickListener listener) {
        ClockNameEditDgFrag fragment = new ClockNameEditDgFrag();
        fragment.mOnDialogClickListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTotalAmount = getArguments().getString(ARG_PARAM1);
            if (TextUtils.isEmpty(mTotalAmount)) {
                mTotalAmount = "";
            }
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomEditBaseDialogFragment);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog mDialog = super.onCreateDialog(savedInstanceState);
        mDialog.setContentView(R.layout.fragment_pre_auth_confirm_dg);
        mDialog.setCanceledOnTouchOutside(true);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams layoutParams;
        if (window != null) {
            layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.BOTTOM;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }
        return mDialog;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pre_auth_confirm_dg;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @OnClick({R.id.iv_dialog_cancle, R.id.tv_ok})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_dialog_cancle:
                dismiss();
                break;
            case R.id.tv_ok:
                String money = etMoney.getText().toString().trim();
                if (TextUtils.isEmpty(money)) {
                    showToast("输入不能为空!");
                    return;
                }
                if (null != mOnDialogClickListener) {
                    mOnDialogClickListener.onConfirmClick(money);
                    dismiss();
                }
                break;
        }
    }

    private OnDialogClickListener mOnDialogClickListener;

    public interface OnDialogClickListener {
        void onConfirmClick(String num);
    }

}
