package com.pandadentist.ui.base;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.pandadentist.R;
import com.pandadentist.config.AppConfig;
import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoFragment;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.TResult;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public abstract class BaseFragment extends TakePhotoFragment {


    private CompositeSubscription mCompositeSubscription;

    @Nullable
    @Bind(R.id.tv_toolbar_title)
    public TextView mToolBarTtitle;
    @Nullable
    @Bind(R.id.rl_toolbar_func)
    public RelativeLayout mToolbarFuncRl;
    @Nullable
    @Bind(R.id.iv_toolbar_func)
    public ImageView mToolbarFuncIv;

    @Nullable
    @Bind(R.id.tv_toolbar)
    public TextView mToolbarFuncTv;

    @Nullable
    @Bind(R.id.rl_toolbar_back)
    public RelativeLayout mToolBackRl;


    @Nullable
    @OnClick(R.id.rl_toolbar_back)
    public void onClickBack() {
        getActivity().onBackPressed();
    }


    public void addSubscription(Subscription s) {
        if (this.mCompositeSubscription == null) {
            this.mCompositeSubscription = new CompositeSubscription();
        }
        this.mCompositeSubscription.add(s);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, v);
        createView(container, v, savedInstanceState);
        return v;
    }

    public abstract void createView(ViewGroup container, View parentView, Bundle savedInstanceState);

    public abstract int getLayoutId();

    @Override
    public void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
        if (this.mCompositeSubscription != null) {
            this.mCompositeSubscription.unsubscribe();
        }
    }

    @Override
    public void takeCancel() {
        super.takeCancel();
    }

    @Override
    public void takeFail(TResult result, String msg) {
        super.takeFail(result, msg);
    }

    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
    }

    protected void takeAlbum() {
        Uri imageUri = createTempFile();
        configCompress(getTakePhoto());
        int limit = AppConfig.TAKE_PIC_CONFIG_SELECT_NUM;
        if (limit > 1) {
            if (AppConfig.TAKE_PIC_CONFIG_IS_CROP) {
                getTakePhoto().onPickMultipleWithCrop(limit, getCropOptions());
            } else {
                getTakePhoto().onPickMultiple(limit);
            }
            return;
        }
        if (AppConfig.TAKE_PIC_CONFIG_SELECT_IS_ALBUM) {
            if (AppConfig.TAKE_PIC_CONFIG_IS_CROP) {
                getTakePhoto().onPickFromDocumentsWithCrop(imageUri, getCropOptions());
            } else {
                getTakePhoto().onPickFromDocuments();
            }
        } else {
            if (AppConfig.TAKE_PIC_CONFIG_IS_CROP) {
                getTakePhoto().onPickFromGalleryWithCrop(imageUri, getCropOptions());
            } else {
                getTakePhoto().onPickFromGallery();
            }
        }
    }

    protected void takePhoto() {
        Uri imageUri = createTempFile();
        configCompress(getTakePhoto());
        if(AppConfig.TAKE_PIC_CONFIG_IS_CROP){
            getTakePhoto().onPickFromCaptureWithCrop(imageUri,getCropOptions());
        }else {
            getTakePhoto().onPickFromCapture(imageUri);
        }
    }

    /**
     * 创建临时文件夹
     */
    private Uri createTempFile() {
        File file = new File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        return Uri.fromFile(file);
    }

    /**
     * 压缩配置
     */
    private void configCompress(TakePhoto takePhoto) {
        if (AppConfig.TAKE_PIC_CONFIG_IS_COMPRESS) {
            int maxSize = AppConfig.TAKE_PIC_CONFIG_PHOTO_MAX_SIZE;
            int maxPixel = AppConfig.TAKE_PIC_CONFIG_PHOTO_MAX_WIDTH_OR_HEIGHT;
            CompressConfig config = new CompressConfig.Builder().setMaxPixel(maxSize).setMaxPixel(maxPixel).create();
            takePhoto.onEnableCompress(config, AppConfig.TAKE_PIC_CONFIG_IS_SHOW_PROGRESS);
        } else {
            takePhoto.onEnableCompress(null, false);
        }
    }

    /**
     * 裁剪配置
     */
    private CropOptions getCropOptions() {
        if (AppConfig.TAKE_PIC_CONFIG_IS_CROP) {
            CropOptions.Builder builder = new CropOptions.Builder();
            if (AppConfig.TAKE_PIC_CONFIG_IS_RATIO_CROP) {
                builder.setAspectX(AppConfig.TAKE_PIC_CONFIG_CROP_WIDTH).setAspectY(AppConfig.TAKE_PIC_CONFIG_CROP_HEIGHT);
            } else {
                builder.setOutputX(AppConfig.TAKE_PIC_CONFIG_CROP_WIDTH).setOutputY(AppConfig.TAKE_PIC_CONFIG_CROP_HEIGHT);
            }
            builder.setWithOwnCrop(AppConfig.TAKE_PIC_CONFIG_CROP_TOOL_IS_CUSTOM);
            return builder.create();
        }
        return null;
    }
}
