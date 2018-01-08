package com.gersion.superlock.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gersion.superlock.R;
import com.gersion.superlock.activity.AboutActivity;
import com.gersion.superlock.activity.DonationActivity;
import com.gersion.superlock.activity.SettingActivity;
import com.gersion.superlock.activity.WebActivity;
import com.gersion.superlock.base.BaseFragment;
import com.gersion.superlock.utils.ConfigManager;
import com.gersion.superlock.utils.EmailUtil;
import com.gersion.superlock.utils.FileUtils;
import com.gersion.superlock.utils.SDCardUtils;
import com.gersion.superlock.utils.ToastUtils;
import com.gersion.superlock.view.ItemView;
import com.orhanobut.logger.Logger;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by a3266 on 2017/6/11.
 */

public class MineFragment extends BaseFragment {
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

        mName.setText(ConfigManager.getInstance().getUserName());
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    @Override
    protected void initListener() {
        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toActivity(SettingActivity.class);
            }
        });
        mBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupDB(getActivity());
            }
        });
        mRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoveryDB();
            }
        });
        mDonation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toActivity(DonationActivity.class);
            }
        });
        mProjectAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toActivity(WebActivity.class);
            }
        });

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

    }

    private void exit() {
        getActivity().finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    //备份数据库文件
    public void backupDB(Activity activity){
        boolean sdCardEnable = SDCardUtils.isSDCardEnable();
        if (!sdCardEnable) {
            ToastUtils.showTasty(activity, "没有SD卡，备份工作无法继续进行", TastyToast.WARNING);
            return ;
        }

        ConfigManager instance = ConfigManager.getInstance();
        File srcDbFile = instance.getSrcDbFile();
        if (!srcDbFile.exists()) {
            ToastUtils.showTasty(activity, "还没有任何数据，不需要备份", TastyToast.WARNING);
            return ;
        }

        if (SDCardUtils.getSDCardAllSize() < srcDbFile.length()) {
            ToastUtils.showTasty(activity, "SD卡剩余容量不足，无法备份", TastyToast.WARNING);
            return;
        }

        boolean success = FileUtils.copyFile(instance.getSrcDbFile(), instance.getDestDbFile());
        if (success){
            ToastUtils.showTasty(activity, "备份成功", TastyToast.SUCCESS);
        }
    }

    //恢复数据库文件
    public void recoveryDB(){

        boolean sdCardEnable = SDCardUtils.isSDCardEnable();
        if (!sdCardEnable) {
            ToastUtils.showTasty(getActivity(), "没有SD卡，无法读取备份的文件", TastyToast.WARNING);
            return;
        }

        ConfigManager instance = ConfigManager.getInstance();
        File destDbFile = instance.getDestDbFile();
        if (!destDbFile.exists()) {
            ToastUtils.showTasty(getActivity(), "没有找到任何备份了的文件", TastyToast.WARNING);
            return;
        }

        boolean success = FileUtils.copyFile( instance.getDestDbFile(),instance.getSrcDbFile());
        if (success){
            ToastUtils.showTasty(activity, "恢复成功", TastyToast.SUCCESS);
        }
    }

    public void sendMail(final String toMail, final String title,
                         final String body, final String path){
        new Thread(new Runnable() {
            public void run() {
                EmailUtil emailUtil = new EmailUtil();
                try {

                    String account = "cmmailserver@canmou123.com";
                    String password = "CANmou123";
                    // String authorizedPwd = "vxosxkgtwrtxvoqz";
                    emailUtil.sendMail(toMail, account, "smtp.mxhichina.com",
                            account, password, title, body, path);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean loadSqlData() {
        boolean sdCardEnable = SDCardUtils.isSDCardEnable();
        if (!sdCardEnable) {
            ToastUtils.showTasty(getActivity(), "没有SD卡，无法读取备份的文件", TastyToast.WARNING);
            return false;
        }

        File sqlPath = new File(SDCardUtils.getSDCardPath() + "biabia.db");
        if (!sqlPath.exists()) {
            ToastUtils.showTasty(getActivity(), "没有找到任何备份了的文件", TastyToast.WARNING);
            return false;
        }

        boolean isSaved = false;
        try {
            FileInputStream fis = new FileInputStream(sqlPath);
            String path = getActivity().getDatabasePath("biabia.db").getAbsolutePath();
            Logger.d(path);
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            byte[] buffer = new byte[1];
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            BufferedInputStream bis = new BufferedInputStream(fis);
            int len;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer);
            }
            bis.close();
            bos.close();
            isSaved = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSaved;
    }

}
