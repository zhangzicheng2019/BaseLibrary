package com.app.base.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Movie;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.app.base.ui.model.BImage;
import com.app.base.ui.model.LocalImage;
import com.app.base.ui.view.subscaleview.ImageSource;
import com.app.base.ui.view.subscaleview.SubSamplingScaleImageView;
import com.app.base.utils.UiUtils;
import com.app.base.utils.BaseUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhangzicheng
 * 2016-12-19 15:18
 */
public class ImagePagerAdapter extends PagerAdapter {

    private List<BImage> mImages = new ArrayList<>();
    private boolean isZoomable;
    private Context mContext;

    public ImagePagerAdapter(Context context, List<BImage> images, boolean isZoomable) {
        this.mContext = context;
        this.mImages = images;
        this.isZoomable = isZoomable;
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public View instantiateItem(ViewGroup container, final int position) {
        BImage image = mImages.get(position);
        if (null == image) {
            return null;
        }
        final String imageUrl = image.getLargeUrl();
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }
        final RelativeLayout rl = new RelativeLayout(mContext);
        final ProgressBar pb = new ProgressBar(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pb.setIndeterminateTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, android.R.color.white)));
            pb.setIndeterminateTintMode(PorterDuff.Mode.SRC_ATOP);
        }
        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(UiUtils.dpToPx(mContext, 50), UiUtils.dpToPx(mContext, 50));
        rllp.addRule(RelativeLayout.CENTER_IN_PARENT);
        rl.addView(pb, rllp);
        //Does not support the GIF Images
        final SubSamplingScaleImageView scaleImageView = new SubSamplingScaleImageView(mContext);
        scaleImageView.setZoomEnabled(isZoomable);
        Disposable disposable = Observable.just(imageUrl)
                .map(new Function<String, LocalImage>() {

                    @Override
                    public LocalImage apply(String url) throws Exception {
                        LocalImage localImage = new LocalImage();
                        try {
                            String imagePath = getLocalImagePath(mContext, url);
                            localImage.setImagePath(imagePath);
                            Movie gif = Movie.decodeFile(imagePath);
                            if (gif != null) {
                                localImage.setGif(true);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return localImage;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<LocalImage>() {
                    @Override
                    public void accept(LocalImage localImage) throws Exception {
                        if (mContext instanceof Activity && ((Activity) mContext).isFinishing()) {
                            return;
                        }
                        final String imagePath = localImage.getImagePath();
                        if (TextUtils.isEmpty(imagePath)) {
                            return;
                        }
                        if (!localImage.isGif()) {
                            scaleImageView.setOnImageEventListener(new SubSamplingScaleImageView.DefaultOnImageEventListener() {
                                @Override
                                public void onImageLoaded() {
                                    super.onImageLoaded();
                                    if (mContext instanceof Activity && ((Activity) mContext).isFinishing()) {
                                        return;
                                    }
                                    pb.setVisibility(View.GONE);
                                }

                                @Override
                                public void onImageLoadError(Exception e) {
                                    super.onImageLoadError(e);
                                    e.printStackTrace();
                                    if (mContext instanceof Activity && ((Activity) mContext).isFinishing()) {
                                        return;
                                    }
                                    // Some special cases images load failed(for example images is cmyk color space), need to use the system ImageView
                                    // and does not support scaling。If you have a better method, please support
                                    final ImageView iv = new ImageView(mContext);
                                    Glide.with(mContext).load(imagePath)
                                            .listener(new RequestListener<Drawable>() {
                                                @Override
                                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                    pb.setVisibility(View.GONE);
                                                    return false;
                                                }

                                                @Override
                                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                    pb.setVisibility(View.GONE);
                                                    return false;
                                                }
                                            })
                                            .into(iv);
                                    rl.removeAllViews();
                                    rl.addView(iv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                }
                            });
                            scaleImageView.setImage(ImageSource.uri(imagePath));
                            rl.addView(scaleImageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        } else {
                            final ImageView gifIv = new ImageView(mContext);
                            Glide.with(mContext)
                                    .asGif()
                                    .load(imagePath)
                                    .listener(new RequestListener<GifDrawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                            pb.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                                            pb.setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(gifIv);
                            rl.addView(gifIv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
        container.addView(rl, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return rl;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private String getLocalImagePath(Context context, String imageUrl) throws Exception {
        String imagePath;
        if (imageUrl.contains("http") || imageUrl.contains("https")) {
            imagePath = Glide.with(context)
                    .load(imageUrl)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get().getPath();
        } else if (BaseUtils.isNumeric(imageUrl)) {
            imagePath = Glide.with(context)
                    .load(Integer.valueOf(imageUrl))
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get().getPath();
        } else {
            File file = new File(imageUrl);
            imagePath = Glide.with(context)
                    .load(file)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get().getPath();
        }
        return imagePath;
    }
}
