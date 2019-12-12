package com.app.base.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.app.base.been.LocalPhoto;
import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.PictureFileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName PhotoSelectorHelper
 * @Description TODO
 * @Author zhangzicheng
 * @Date 2019/12/12 17:12
 */
public class PhotoSelectorHelper {

    private int selectNum = 1;
    private boolean isCompress = true;
    private boolean isCrop = false;
    private int minimumCompressSize;//than how many KB images are not compressed
    private int compressQuality = 90;//default 90
    private int cropWidth;
    private int cropHeight;
    private int cropWidthRatio = 1;
    private int cropHeightRatio = 1;

    private Callback callback;

    public PhotoSelectorHelper setCropRatio(int cropWidthRatio, int cropHeightRatio) {
        this.cropWidthRatio = cropWidthRatio;
        this.cropHeightRatio = cropHeightRatio;
        return this;
    }

    public PhotoSelectorHelper setCropSize(int cropWidth, int cropHeight) {
        this.cropWidth = cropWidth;
        this.cropHeight = cropHeight;
        return this;
    }

    public PhotoSelectorHelper setSelectNum(int selectNum) {
        this.selectNum = selectNum;
        return this;
    }

    public PhotoSelectorHelper setCompress(boolean isCompress) {
        this.isCompress = isCompress;
        return this;
    }

    public PhotoSelectorHelper setCrop(boolean isCrop) {
        this.isCrop = isCrop;
        return this;
    }

    public PhotoSelectorHelper setCompressQuality(int compressQuality) {
        this.compressQuality = compressQuality;
        return this;
    }

    public PhotoSelectorHelper setMinimumCompressSize(int minimumCompressSize) {
        this.minimumCompressSize = minimumCompressSize;
        return this;
    }

    public void select(Activity act, Callback callback) {
        select(act, null, callback);
    }

    public void select(Activity act, List<LocalPhoto> localPhotoList, Callback callback) {
        if(this.callback != callback){
            this.callback = callback;
        }
        PictureSelectionModel selectionModel = PictureSelector.create(act)
                .openGallery(PictureMimeType.ofImage());
        if (localPhotoList != null && localPhotoList.size() > 0) {
            selectionModel.selectionMedia(convertToLocalMedia(localPhotoList));
        }
        if(isCompress){
            selectionModel.cropCompressQuality(compressQuality);
            if(minimumCompressSize > 0){
                selectionModel.minimumCompressSize(minimumCompressSize);
            }
        }
        if(isCrop){
            if(cropWidth > 0 && cropHeight > 0){
                selectionModel.cropWH(cropWidth, cropHeight);
            } else {
                selectionModel.withAspectRatio(cropWidthRatio, cropHeightRatio);
            }
        }
        selectionModel.compress(isCompress)
                .enableCrop(isCrop)
                .maxSelectNum(selectNum)
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode == PictureConfig.CHOOSE_REQUEST){
            List<LocalMedia> selectLocalMediaList = PictureSelector.obtainMultipleResult(data);
            if (selectLocalMediaList == null || selectLocalMediaList.isEmpty()) {
                return;
            }
            if(callback != null){
                callback.call(convertLocalPhoto(selectLocalMediaList));
            }
        }
    }

    private List<LocalMedia> convertToLocalMedia(List<LocalPhoto> localPhotoList) {
        List<LocalMedia> localMediaList = new ArrayList<>();
        for (LocalPhoto localPhoto : localPhotoList) {
            LocalMedia localMedia = new LocalMedia();
            localMedia.setWidth(localPhoto.getWidth());
            localMedia.setHeight(localPhoto.getHeight());
            localMedia.setPath(localPhoto.getPath());
            localMedia.setCutPath(localPhoto.getCropPath());
            localMedia.setCompressPath(localPhoto.getCompressPath());
            localMedia.setMimeType(localPhoto.getMimeType());
            localMedia.setPictureType(localPhoto.getPhotoType());
            localMedia.setCut(localPhoto.isCrop());
            localMedia.setCompressed(localPhoto.isCompress());
            localMediaList.add(localMedia);
        }
        return localMediaList;
    }

    private List<LocalPhoto> convertLocalPhoto(List<LocalMedia> localMediaList) {
        List<LocalPhoto> localPhotoList = new ArrayList<>();
        for (LocalMedia localMedia : localMediaList) {
            LocalPhoto localPhoto = new LocalPhoto();
            localPhoto.setWidth(localMedia.getWidth());
            localPhoto.setHeight(localMedia.getHeight());
            localPhoto.setPath(localMedia.getPath());
            localPhoto.setCropPath(localMedia.getCutPath());
            localPhoto.setCompressPath(localMedia.getCompressPath());
            localPhoto.setMimeType(localMedia.getMimeType());
            localPhoto.setPhotoType(localMedia.getPictureType());
            localPhoto.setCrop(localMedia.isCut());
            localPhoto.setCompress(localMedia.isCompressed());
            localPhotoList.add(localPhoto);
        }
        return localPhotoList;
    }

    public void release(Context context){
        PictureFileUtils.deleteCacheDirFile(context);
        PictureFileUtils.deleteExternalCacheDirFile(context);
    }

    public interface Callback {

        void call(List<LocalPhoto> localPhotoList);

    }
}
