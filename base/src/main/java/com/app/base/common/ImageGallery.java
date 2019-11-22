package com.app.base.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.app.base.R;
import com.app.base.ui.activity.ActivityImageGallery;
import com.app.base.ui.model.BImage;
import com.app.base.ui.model.ShareContent;

import java.util.ArrayList;

/**
 * Created by zhangzicheng
 * 2016-12-19 19:47
 */
public class ImageGallery {

    public static final int REQUEST_CODE_IMAGE_GALLERY_IMAGES    = 12580;
    public static final String INTENT_EXTRA_CURRENT_IMAGE_INDEX = "intent_extra_current_image_index";
    public static final String INTENT_EXTRA_IMAGES              = "intent_extra_images";
    public static final String INTENT_EXTRA_SHOW_SHARE          = "intent_extra_show_share";
    public static final String INTENT_EXTRA_SHOW_LIKE           = "intent_extra_show_like";
    public static final String INTENT_EXTRA_SHOW_SAVE           = "intent_extra_show_save";
    public static final String INTENT_EXTRA_IS_ZOOMABLE         = "intent_extra_is_zoomable";
    public static final String INTENT_EXTRA_SHARE_CONTENT       = "intent_extra_share_content";

    public static ImageGalleryBuilder builder() {
        return new ImageGalleryBuilder();
    }

    public static class ImageGalleryBuilder{

        private Intent mImageGalleryIntent;

        public ImageGalleryBuilder() {
            mImageGalleryIntent = new Intent();
        }

        public void start(@NonNull Context context) {
            context.startActivity(getIntent(context));
            AddJumpAnimation(context);
        }

        public void startForResult(@NonNull Activity activity) {
            startForResult(activity, REQUEST_CODE_IMAGE_GALLERY_IMAGES);
        }

        public void startForResult(@NonNull Activity activity, int requestCode) {
            activity.startActivityForResult(getIntent(activity), requestCode);
            AddJumpAnimation(activity);
        }

        public void startForResult(@NonNull Context context, @NonNull Fragment fragment) {
            startForResult(context, fragment, REQUEST_CODE_IMAGE_GALLERY_IMAGES);
        }

        public void startForResult(@NonNull Context context, @NonNull Fragment fragment, int requestCode) {
            fragment.startActivityForResult(getIntent(context), requestCode);
            AddJumpAnimation(context);
        }

        private Intent getIntent(Context context) {
            mImageGalleryIntent.setClass(context, ActivityImageGallery.class);
            return mImageGalleryIntent;
        }

        private void AddJumpAnimation(Context context){
            if(context instanceof Activity){
                ((Activity) context).overridePendingTransition(R.anim.fade_in_short, 0);
            }
        }

        public ImageGalleryBuilder setImages(ArrayList<BImage> images){
            mImageGalleryIntent.putExtra(INTENT_EXTRA_IMAGES, images);
            return this;
        }

        public ImageGalleryBuilder setImageIndex(int index){
            mImageGalleryIntent.putExtra(INTENT_EXTRA_CURRENT_IMAGE_INDEX, index);
            return this;
        }

        public ImageGalleryBuilder setShareContent(ShareContent shareContent){
            mImageGalleryIntent.putExtra(INTENT_EXTRA_SHARE_CONTENT, shareContent);
            return this;
        }

        public ImageGalleryBuilder showShare(){
            mImageGalleryIntent.putExtra(INTENT_EXTRA_SHOW_SHARE, true);
            return this;
        }
        public ImageGalleryBuilder showLike(){
            mImageGalleryIntent.putExtra(INTENT_EXTRA_SHOW_LIKE, true);
            return this;
        }
        public ImageGalleryBuilder showSave(){
            mImageGalleryIntent.putExtra(INTENT_EXTRA_SHOW_SAVE, true);
            return this;
        }
        public ImageGalleryBuilder zoomable(){
            mImageGalleryIntent.putExtra(INTENT_EXTRA_IS_ZOOMABLE, true);
            return this;
        }
    }
}
