package com.gersion.superlock.activity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.gersion.superlock.R;
import com.gersion.superlock.base.BaseActivity;
import com.gersion.superlock.bean.DbBean;
import com.gersion.superlock.bean.Keyer;
import com.gersion.superlock.db.DbManager;
import com.gersion.superlock.utils.Aes;
import com.gersion.superlock.utils.ConfigManager;
import com.gersion.superlock.utils.MyConstants;
import com.gersion.superlock.utils.SDCardUtils;
import com.gersion.superlock.utils.ToastUtils;
import com.gersion.superlock.view.TitleView;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.List;

import static com.gersion.superlock.utils.GsonHelper.getGson;

public class ImportOldDataActivity extends BaseActivity {
    private static final int SELECT_FILE_CODE = 101;
    private TitleView mTitleView;
    private EditText mEtPassword;
    private RadioButton mRbBackup;
    private RadioButton mRbFile;
    private RadioGroup mRgLocation;
    private RadioButton mRbDifference;
    private RadioButton mRbAll;
    private RadioButton mRbAllAndClear;
    private RadioGroup mRgDataType;
    private TextView mTvCommit;
    private LinearLayout mActivityAddPassword;
    private TextView mTvSelectFile;
    private RadioGroup mRgSuperPassword;
    private DbManager mDbManager;
    private int currentSuperPasswordId;
    private int currentLocationId;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_import_old_data;
    }

    @Override
    protected void initView() {
        mRbDifference = (RadioButton) findViewById(R.id.rb_difference);
        mRbBackup = (RadioButton) findViewById(R.id.rb_backup);
        mRbAll = (RadioButton) findViewById(R.id.rb_all);
        mRbAllAndClear = (RadioButton) findViewById(R.id.rb_all_and_clear);
        mRbFile = (RadioButton) findViewById(R.id.rb_file);
        mRgDataType = (RadioGroup) findViewById(R.id.rg_data_type);
        mRgSuperPassword = (RadioGroup) findViewById(R.id.rg_superpassowrd);
        mRgLocation = (RadioGroup) findViewById(R.id.rg_location);
        mTitleView = (TitleView) findViewById(R.id.titleView);
        mTvCommit = (TextView) findViewById(R.id.tv_commit);
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mTvSelectFile = (TextView) findViewById(R.id.tv_select_file);

        mTitleView.setTitleText("导入备份的数据")
                .setAddVisiable(false)
                .setSearchVisiable(false);
        mRgLocation.check(R.id.rb_backup);
        mRgDataType.check(R.id.rb_difference);
        mRgSuperPassword.check(R.id.rb_current_superpassowrd);

        currentLocationId = R.id.rb_backup;
        mDbManager = DbManager.getInstance();
        mDbManager.onStart();
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        mTitleView.setOnBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mTvSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toActivityForResult(SelectFileActivity.class, SELECT_FILE_CODE);
            }
        });

        mRgLocation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                mTvSelectFile.setVisibility(checkedId == R.id.rb_file ? View.VISIBLE : View.GONE);
                currentLocationId = checkedId;
            }
        });

        mRgSuperPassword.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                mEtPassword.setVisibility(checkedId == R.id.rb_input_superpassowrd ? View.VISIBLE : View.GONE);
                currentSuperPasswordId = checkedId;
            }
        });

        mTvCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoveryData();
            }
        });
    }

    private void recoveryData() {
        switch (currentLocationId) {
            case R.id.rb_file:
                getDataFromFile();
                break;
            case R.id.rb_backup:
                getDataFromBackup();
                break;
        }
    }

    private void getDataFromFile() {
        toActivity(SelectFileActivity.class);
    }

    //从本地备份恢复
    public void getDataFromBackup() {
        byte[] bytes = SDCardUtils.readFileFromSDCard(MyConstants.BACKUP_PATH, MyConstants.BACKUP_FILE_NAME + "." + MyConstants.FILE_TYPE);
        if (bytes==null){
            ToastUtils.showTasty(this,"还没有任何备份数据",TastyToast.ERROR);
            return;
        }
        String s = new String(bytes);
        String decrypt = Aes.decryptWithSuperPassword(s);
        Gson gson = getGson();
        TypeToken<List<Keyer>> type = new TypeToken<List<Keyer>>() {
        };
        int dataListCount = ConfigManager.getInstance().getDataListCount();
        List<Keyer> keyers = gson.fromJson(decrypt, type.getType());
        for (Keyer keyer : keyers) {
            dataListCount++;
            DbBean dbBean = keyer.keyer2DbBean();
            dbBean.setIndex(dataListCount);
            dbBean.setId(dataListCount);
            mDbManager.add(dbBean);
        }
        ToastUtils.showTasty(this, "导入成功", TastyToast.SUCCESS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbManager.destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE_CODE) {

            }
        }
    }
}
