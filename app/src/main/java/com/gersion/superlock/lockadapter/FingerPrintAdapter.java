package com.gersion.superlock.lockadapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gersion.superlock.R;
import com.gersion.superlock.app.SuperLockApplication;
import com.gersion.superlock.utils.AnimatorUtils;
import com.gersion.superlock.utils.ConfigManager;
import com.gersion.superlock.utils.SPManager;
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint;

/**
 * Created by aa326 on 2018/1/9.
 */

public class FingerPrintAdapter implements LockAdapter {

    private LockCallback mLockCallback;
    private ConfigManager mInstance;
    private RelativeLayout mRlFingerContainer;
    private ImageView mIvFinger;
    private TextView mTvNotice;
    private TextView mBtnCancel;
    private TextView mBtnOtherType;

    @Override
    public View init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_finger_print, null);
        mRlFingerContainer = (RelativeLayout) view.findViewById(R.id.rl_finger_container);
        mTvNotice = (TextView) view.findViewById(R.id.tv_notice);
        mIvFinger = (ImageView) view.findViewById(R.id.iv_finger);
        mBtnCancel = (TextView) view.findViewById(R.id.btn_cancel);
        mBtnOtherType = (TextView) view.findViewById(R.id.btn_other_type);
        initListener();
        return view;
    }

    private void initListener() {
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mBtnOtherType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch2OtherType();
            }
        });
    }

    @Override
    public void onStart() {
        initFingerPrint();
    }

    private void initFingerPrint() {
        mInstance = ConfigManager.getInstance();
        SuperLockApplication.mFingerprintIdentify.startIdentify(5, mFingerprintIdentifyListener);
    }

    BaseFingerprint.FingerprintIdentifyListener mFingerprintIdentifyListener = new BaseFingerprint.FingerprintIdentifyListener() {
        @Override
        public void onSucceed() {
            mIvFinger.setImageResource(R.mipmap.success);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLockCallback.onSuccess();
                }
            }, 300);
        }

        @Override
        public void onNotMatch(int availableTimes) {
            mTvNotice.setText("指纹不匹配，还可以尝试 " + availableTimes + " 次");
            shake();
        }

        @Override
        public void onStartFailedByDeviceLocked() {

        }

        @Override
        public void onFailed(boolean isDeviceLocked) {
            mTvNotice.setText("指纹解锁已禁用，请 " + 15 + " 秒后重试");
            SPManager.setLockedTime(SystemClock.currentThreadTimeMillis());
            mIvFinger.setImageResource(R.mipmap.alert);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIvFinger.setImageResource(R.mipmap.finger);
                    mTvNotice.setText("请轻触指纹感应器验证指纹");
                    SuperLockApplication.mFingerprintIdentify.startIdentify(5, mFingerprintIdentifyListener);
                }
            }, 15000);
        }
    };

    private void switch2OtherType() {
        mRlFingerContainer.animate()
                .alpha(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLockCallback.onChangLockType();
                    }
                }).setDuration(500).start();
    }

    private void shake() {
        ObjectAnimator animator = AnimatorUtils.tada(mIvFinger, 5);
        animator.setRepeatCount(0);
        animator.start();
    }

    @Override
    public void setLockCallback(LockCallback lockCallback) {
        mLockCallback = lockCallback;
    }
}
