package com.gersion.superlock.fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gersion.superlock.R;
import com.gersion.superlock.activity.AboutActivity;
import com.gersion.superlock.activity.DonationActivity;
import com.gersion.superlock.activity.SelectLockTypeActivity;
import com.gersion.superlock.base.BasePermissionFragment;
import com.gersion.superlock.bean.Keyer;
import com.gersion.superlock.bean.PasswordData;
import com.gersion.superlock.db.DbManager;
import com.gersion.superlock.listener.ResultCallback;
import com.gersion.superlock.share.SharePopup;
import com.gersion.superlock.utils.BackupHelper;
import com.gersion.superlock.utils.ConfigManager;
import com.gersion.superlock.utils.GsonHelper;
import com.gersion.superlock.utils.LogUtils;
import com.gersion.superlock.utils.RecoveryHelper;
import com.gersion.superlock.utils.ToastUtils;
import com.gersion.superlock.view.ItemView;
import com.gersion.superlock.view.SettingView;
import com.kyleduo.switchbutton.SwitchButton;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.List;

import static com.gersion.superlock.utils.GsonHelper.getGson;

/**
 * Created by a3266 on 2017/6/11.
 */

public class MineFragment extends BasePermissionFragment {
    private ImageView mIcon;
    private TextView mName;
    private ImageView mCode2d;
    private ItemView mCollection;
    private ItemView mSetting;
    private ItemView mAbout;
    private ItemView mShare;
    private ItemView mExit;
    private ItemView mBackup;
    private ItemView mRecovery;
    private ItemView mDonation;
    private View mProjectAddress;
    private View mAppLockType;
    private View mViewSuperPassword;
    private BackupHelper mBackupHelper;
    private RecoveryHelper mRecoveryHelper;
    private DbManager mDbManager;
    private String mDataJson;
    private SettingView mOpenLock;
    private SettingView mFloatBall;
    private ConfigManager mConfigManager;
    private boolean mIsRecoveryPwd=false;//是否是恢复密码

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_mine;
    }

    @Override
    protected void initView() {
        mIcon = findView(R.id.icon);
        mName = findView(R.id.name);
        mCode2d = findView(R.id.code2d);
        mCollection = findView(R.id.collection);
        mSetting = findView(R.id.setting);
        mAbout = findView(R.id.about);
        mShare = findView(R.id.share);
        mExit = findView(R.id.exit);
        mBackup = findView(R.id.backup);
        mRecovery = findView(R.id.recovery);
        mDonation = findView(R.id.donation);
        mProjectAddress = findView(R.id.project_address);
        mViewSuperPassword = findView(R.id.superpassword);
        mAppLockType = findView(R.id.app_lock);
        mOpenLock = findView(R.id.open_lock);
        mFloatBall = findView(R.id.float_ball);

//        mName.setText(ConfigManager.getInstance().getUserName());
    }

    @Override
    protected void initData(Bundle bundle) {
        mConfigManager = ConfigManager.getInstance();
        mOpenLock.setSwitchStatus(mConfigManager.isLock());
        mDbManager = DbManager.getInstance();
        getData();
        mBackupHelper = BackupHelper.getInstance();
        mBackupHelper.setOnResultCallback(new ResultCallback() {
            @Override
            public void onResultSuccess(String result) {
                ToastUtils.show(getActivity(),result);
            }

            @Override
            public void onResultFailed(String result) {
                ToastUtils.show(getActivity(),result);
            }
        });

        mRecoveryHelper = RecoveryHelper.getInstance();
        mRecoveryHelper.setOnResultCallback(new ResultCallback() {
            @Override
            public void onResultSuccess(String result) {
                parseDataJson(result);
            }

            @Override
            public void onResultFailed(String result) {
                ToastUtils.show(getActivity(),result);
            }
        });
    }

    private void getData() {
        List<PasswordData> datas = mDbManager.queryAll();
        if (datas != null && datas.size() > 0) {
            List<Keyer> keyers = new ArrayList<>();
            for (PasswordData data : datas) {
                Keyer keyer = new Keyer(data);
                keyers.add(keyer);
            }
            mDataJson = GsonHelper.toJsonFromList(keyers);
        }
    }

    @Override
    protected void initListener() {
        mOpenLock.setSwitchChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton switchButton, boolean isChecked) {
                String toast = isChecked ? "开启程序锁" : "关闭程序锁";
                ToastUtils.showTasty(getActivity(), toast, TastyToast.INFO);
                mConfigManager.setLock(isChecked);
            }
        });
//        mSetting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toActivity(SettingActivity.class);
//            }
//        });
        mBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsRecoveryPwd = false;
                checkExternalPermission();
            }
        });
        mRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsRecoveryPwd = true;
                checkExternalPermission();
            }
        });
        mDonation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toActivity(DonationActivity.class);
            }
        });
//        mProjectAddress.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toActivity(WebActivity.class);
//            }
//        });

        mAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toActivity(AboutActivity.class);
            }
        });

        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        mAppLockType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toActivity(SelectLockTypeActivity.class);
            }
        });

        mViewSuperPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toActivity(SharePopup.class);
            }
        });

    }

    @Override
    protected void onPermissionFialed() {
        if (mIsRecoveryPwd){
            ToastUtils.showTasty(getActivity(),"没有存储卡读写权限，无法读取数据！",TastyToast.ERROR);
        }else {
            ToastUtils.showTasty(getActivity(),"没有存储卡读写权限，无法写入数据！",TastyToast.ERROR);
        }
    }

    @Override
    protected void onPermissionSuccess() {
        if (mIsRecoveryPwd) {
            mRecoveryHelper.getDataFromBackup();
        }else {
            mBackupHelper.backup2Local(mDataJson);
        }
    }

    public void parseDataJson(String dataJson) {
        Gson gson = getGson();
        TypeToken<List<Keyer>> type = new TypeToken<List<Keyer>>() {
        };
        List<Keyer> keyers = gson.fromJson(dataJson, type.getType());
        for (int i = keyers.size() - 1; i >= 0; i--) {
            Keyer keyer = keyers.get(i);
            PasswordData passwordData = keyer.keyer2DbBean();
            LogUtils.e(passwordData);
            mDbManager.addOrReplace(passwordData);
        }
        mDbManager.onDataChange();
        ToastUtils.show(getActivity(), "导入成功");
    }

    private void exit() {
        getActivity().finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
