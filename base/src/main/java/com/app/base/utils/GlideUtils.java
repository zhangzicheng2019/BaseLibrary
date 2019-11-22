package com.app.base.utils;

import androidx.annotation.DrawableRes;

import com.app.base.common.CornerCenterCropTransform;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class GlideUtils {

    public static RequestOptions getOptions(){
        return new RequestOptions().centerCrop();
    }

    public static RequestOptions getOptions(@DrawableRes int resourceId, boolean setPlaceholder){
        RequestOptions options = new RequestOptions().centerCrop().error(resourceId);
        if(setPlaceholder){
            options.placeholder(resourceId);
        }
        return options;
    }

    public static RequestOptions getCornerOptions(int corner){
        return new RequestOptions().transform(new RoundedCorners(corner));
    }

    public static RequestOptions getCornerOptions(int corner, @DrawableRes int errorId){
        return new RequestOptions().transform(new RoundedCorners(corner)).error(errorId);
    }

    public static RequestOptions getCornerOptions(int corner, @DrawableRes int placeholderId, @DrawableRes int errorId){
        return new RequestOptions().transform(new RoundedCorners(corner)).error(errorId).placeholder(placeholderId);
    }

    public static RequestOptions getCornerCenterCropOptions(int corner){
        return new RequestOptions().transform(new CornerCenterCropTransform(corner));
    }

    public static RequestOptions getCornerCenterCropOptions(int corner, @DrawableRes int errorId){
        return new RequestOptions().transform(new CornerCenterCropTransform(corner)).error(errorId);
    }

    public static RequestOptions getCornerCenterCropOptions(int corner, @DrawableRes int placeholderId, @DrawableRes int errorId){
        return new RequestOptions().transform(new CornerCenterCropTransform(corner)).error(errorId).placeholder(placeholderId);
    }
}
