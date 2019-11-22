package com.app.base.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.app.base.R;
import com.app.base.common.ImageGallery;
import com.app.base.common.PermissionHolder;
import com.app.base.ui.adapter.ImagePagerAdapter;
import com.app.base.ui.model.BImage;
import com.app.base.ui.model.ShareContent;
import com.app.base.ui.view.ImageViewPager;
import com.app.base.utils.UiUtils;
import com.app.base.utils.BaseUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by zhangzicheng
 * 2016-12-15 15:44
 */
public class ActivityImageGallery extends AppCompatActivity {

    protected View vTranslucent;
    protected ImageViewPager ivpImageGallery;
    protected ImageView ivBack;
    protected TextView tvImageIndicator;
    protected ImageView ivShare;
    protected RelativeLayout rlContent;
    protected TextView tvDesc;
    protected TextView tvDesc2;
    protected RelativeLayout rlBottomToolbar;
    protected TextView tvSave;
    protected TextView tvLike;

    protected int mCurrentIndex = 0;
    protected List<BImage> mImages;
    protected boolean isShowShare;
    protected boolean isShowLike;
    protected boolean isShowSave;
    protected boolean isZoomable;
    protected ShareContent mShareContent;
    protected boolean isSelectLike;
    protected int mPraiseCount = 0;
    private PermissionHolder mPermissionHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);
        handleIntent(getIntent());
        if (mImages == null || mImages.size() == 0) {
            Toast.makeText(this, R.string.text_unselected_picture, Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }
        initView();
        initData();
    }

    protected void handleIntent(Intent intent) {
        if (null == intent) {
            return;
        }
        mCurrentIndex = intent.getIntExtra(ImageGallery.INTENT_EXTRA_CURRENT_IMAGE_INDEX, 0);
        mImages = intent.getParcelableArrayListExtra(ImageGallery.INTENT_EXTRA_IMAGES);
        isShowShare = intent.getBooleanExtra(ImageGallery.INTENT_EXTRA_SHOW_SHARE, false);
        isShowLike = intent.getBooleanExtra(ImageGallery.INTENT_EXTRA_SHOW_LIKE, false);
        isShowSave = intent.getBooleanExtra(ImageGallery.INTENT_EXTRA_SHOW_SAVE, false);
        isZoomable = intent.getBooleanExtra(ImageGallery.INTENT_EXTRA_IS_ZOOMABLE, false);
        mShareContent = (ShareContent) intent.getSerializableExtra(ImageGallery.INTENT_EXTRA_SHARE_CONTENT);
    }

    private void initView() {
        vTranslucent = findViewById(R.id.v_translucent);
        ivpImageGallery = findViewById(R.id.ivp_image_gallery);
        ivBack = findViewById(R.id.iv_back);
        tvImageIndicator = findViewById(R.id.tv_image_indicator);
        ivShare = findViewById(R.id.iv_share);
        rlContent = findViewById(R.id.rl_content);
        tvDesc = findViewById(R.id.tv_desc);
        tvDesc2 = findViewById(R.id.tv_desc2);
        rlBottomToolbar = findViewById(R.id.rl_bottom_toolbar);
        tvSave = findViewById(R.id.tv_save);
        tvLike = findViewById(R.id.tv_like);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            vTranslucent.setVisibility(View.VISIBLE);
        } else {
            vTranslucent.setVisibility(View.GONE);
        }
        if (isShowShare) {
            ivShare.setVisibility(View.VISIBLE);
            ivShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleShare(mImages.get(mCurrentIndex));
                }
            });
        }
        if (isShowLike) {
            tvLike.setVisibility(View.VISIBLE);
            rlBottomToolbar.setVisibility(View.VISIBLE);
            tvLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleLike(mImages.get(mCurrentIndex));
                }
            });
        }
        if (isShowSave) {
            tvSave.setVisibility(View.VISIBLE);
            rlBottomToolbar.setVisibility(View.VISIBLE);
            tvSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestStoragePermission();
                }
            });
        }
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ivpImageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initData() {
        setTvImageIndicator();
        setCurrentImageData();
        ivpImageGallery.setAdapter(new ImagePagerAdapter(this, mImages, isZoomable));
        ivpImageGallery.setCurrentItem(mCurrentIndex);
        ivpImageGallery.addOnPageChangeListener(mPageChangeListener);
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mCurrentIndex = position;
            setTvImageIndicator();
            setCurrentImageData();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    protected void setTvImageIndicator() {
        tvImageIndicator.setText(String.format(Locale.getDefault(), "%1$d / %2$d", mCurrentIndex + 1, mImages.size()));
    }

    protected void setCurrentImageData() {
        BImage image = mImages.get(mCurrentIndex);
        if (null == image) {
            return;
        }
        String desc;
        String desc2;
        mPraiseCount = image.getPraiseCount();
        isSelectLike = image.getPraiseFlag();
        desc = image.getDesc();
        desc2 = image.getDesc2();
        tvLike.setSelected(isSelectLike);
        if (mPraiseCount != 0) {
            tvLike.setText(String.valueOf(mPraiseCount));
        } else {
            tvLike.setText("Like");
        }
        setImageContent(desc, desc2);
    }

    protected void setImageContent(String desc, String desc2) {
        if (TextUtils.isEmpty(desc) && TextUtils.isEmpty(desc2)) {
            rlContent.setVisibility(View.GONE);
            return;
        }
        if (!TextUtils.isEmpty(desc)) {
            tvDesc.setText(desc);
            tvDesc.setVisibility(View.VISIBLE);
        } else {
            tvDesc.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(desc2)) {
            if (BaseUtils.isNumeric(desc2)) {
//                tvDesc2.setText(getString(R.string.nig_album_price_format, desc2));
            } else {
                tvDesc2.setText(desc2);
            }
            tvDesc2.setVisibility(View.VISIBLE);
        } else {
            tvDesc2.setVisibility(View.GONE);
        }
        rlContent.setVisibility(View.VISIBLE);
    }

    protected void requestStoragePermission() {
        if(mPermissionHolder == null){
            mPermissionHolder = new PermissionHolder(this);
        }
        mPermissionHolder.request(new PermissionHolder.PermissionResultCallback() {
            @Override
            public void callback(boolean result) {
                if(result){
                    handleSave(mImages.get(mCurrentIndex));
                }
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    protected void handleSave(BImage image) {
        if (image == null) {
            UiUtils.showToastShort(ActivityImageGallery.this, "Save Failed");
            return;
        }
        Glide.with(this)
                .asBitmap()
                .load(image.getLargeUrl())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        boolean saveFlag = BaseUtils.saveImageToLocal(ActivityImageGallery.this, resource);
                        if (saveFlag) {
                            UiUtils.showToastShort(ActivityImageGallery.this, "Save Successful");
                        } else {
                            UiUtils.showToastShort(ActivityImageGallery.this, "Save Failed");
                        }
                    }
                });
    }

    @CallSuper
    protected void handleLike(BImage image) {
        if (image == null) {
            return;
        }
        isSelectLike = !isSelectLike;
        mPraiseCount = image.getPraiseCount();
        tvLike.setSelected(isSelectLike);
        if (isSelectLike) {
            tvLike.setText(String.valueOf(++mPraiseCount));
        } else {
            tvLike.setText(--mPraiseCount != 0 ? "" + mPraiseCount : "Like");
        }
        image.setPraiseFlag(isSelectLike);
        image.setPraiseCount(mPraiseCount);
        mImages.set(mCurrentIndex, image);
    }

    protected void handleShare(BImage image) {

    }


    @Override
    public void onBackPressed() {
        if (isShowLike && mImages != null) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(ImageGallery.INTENT_EXTRA_IMAGES, (ArrayList<BImage>) mImages);
            setResult(RESULT_OK, intent);
        }
        finish();
        overridePendingTransition(0, R.anim.fade_out_short);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPermissionHolder != null){
            mPermissionHolder.cancel();
            mPermissionHolder = null;
        }
    }
}
