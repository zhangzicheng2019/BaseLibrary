package com.app.base.ui.view.banner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.base.R;
import com.app.base.ui.view.banner.transformer.BannerPageTransformer;
import com.app.base.ui.view.banner.transformer.TransitionEffect;
import com.app.base.utils.UiUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * BannerView
 * <p>
 * https://github.com/bingoogolapple/BGABanner-Android
 */
public class BannerView extends RelativeLayout implements ViewPager.OnPageChangeListener {

    private static final int NO_PLACEHOLDER_DRAWABLE = -1;
    private BannerViewPager mViewPager;
    private List<View> mHackyViews;
    private List<View> mViews;
    private LinearLayout mPointRealContainerLl;
    private TextView mTitleTv;
    private boolean mAutoPlayAble = true;
    private int mAutoPlayInterval = 3000;
    private int mPageChangeDuration = 800;
    private int mPointGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
    private int mPointLeftRightMargin;
    private int mPointTopBottomMargin;
    private int mPointContainerLeftRightPadding;
    private int mTitleTextSize;
    private int mTitleTextColor = Color.WHITE;
    private int mPointDrawableResId = R.drawable.selector_banner_point;
    private Drawable mPointContainerBackgroundDrawable;
    private AutoPlayTask mAutoPlayTask;
    private TransitionEffect mTransitionEffect;
    private ImageView mPlaceholderIv;
    private int mPlaceholderDrawableResId = NO_PLACEHOLDER_DRAWABLE;
    private List<?> mModels;
    private OnBannerItemClickListener mOnBannerItemClickListener;
    private BannerAdapter mBannerAdapter;
    private int mOverScrollMode = OVER_SCROLL_NEVER;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private boolean mIsNumberIndicator = false;
    private TextView mNumberIndicatorTv;
    private int mNumberIndicatorTextColor = Color.WHITE;
    private int mNumberIndicatorTextSize;
    private Drawable mNumberIndicatorBackground;
    private boolean mIsNeedShowIndicatorOnOnlyOnePage;
    private boolean mAllowUserScrollable = true;
    private View mSkipView;
    private View mEnterView;
    private GuideDelegate mGuideDelegate;
    private int mContentBottomMargin;
    private boolean mIsFirstInvisible = true;
    private boolean showTitleText = false;
    private boolean showIndicator = true;

    private OnFilterDoubleClickListener mGuideOnFilterDoubleClickListener = new OnFilterDoubleClickListener() {
        @Override
        public void onFilterDoubleClick(View v) {
            if (mGuideDelegate != null) {
                mGuideDelegate.onClickEnterOrSkip();
            }
        }
    };

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDefaultAttrs(context);
        initCustomAttrs(context, attrs);
        initView(context);
    }

    private void initDefaultAttrs(Context context) {
        mAutoPlayTask = new AutoPlayTask(this);
        mPointLeftRightMargin = UiUtils.dpToPx(context, 3);
        mPointTopBottomMargin = UiUtils.dpToPx(context, 6);
        mPointContainerLeftRightPadding = UiUtils.dpToPx(context, 10);
        mTransitionEffect = TransitionEffect.Default;
        mNumberIndicatorTextSize = UiUtils.dpToPx(context, 12);
        mContentBottomMargin = 0;
    }

    private void initCustomAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
        if(typedArray != null){
            final int N = typedArray.getIndexCount();
            for (int i = 0; i < N; i++) {
                initCustomAttr(typedArray.getIndex(i), typedArray);
            }
            typedArray.recycle();
        }
    }

    private void initCustomAttr(int attr, TypedArray typedArray) {
        if (attr == R.styleable.BannerView_banner_pointDrawable) {
            mPointDrawableResId = typedArray.getResourceId(attr, R.drawable.selector_banner_point);
        } else if (attr == R.styleable.BannerView_banner_pointContainerBackground) {
            mPointContainerBackgroundDrawable = typedArray.getDrawable(attr);
        } else if (attr == R.styleable.BannerView_banner_pointLeftRightMargin) {
            mPointLeftRightMargin = typedArray.getDimensionPixelSize(attr, mPointLeftRightMargin);
        } else if (attr == R.styleable.BannerView_banner_pointContainerLeftRightPadding) {
            mPointContainerLeftRightPadding = typedArray.getDimensionPixelSize(attr, mPointContainerLeftRightPadding);
        } else if (attr == R.styleable.BannerView_banner_pointTopBottomMargin) {
            mPointTopBottomMargin = typedArray.getDimensionPixelSize(attr, mPointTopBottomMargin);
        } else if (attr == R.styleable.BannerView_banner_indicatorGravity) {
            mPointGravity = typedArray.getInt(attr, mPointGravity);
        } else if (attr == R.styleable.BannerView_banner_pointAutoPlayAble) {
            mAutoPlayAble = typedArray.getBoolean(attr, mAutoPlayAble);
        } else if (attr == R.styleable.BannerView_banner_pointAutoPlayInterval) {
            mAutoPlayInterval = typedArray.getInteger(attr, mAutoPlayInterval);
        } else if (attr == R.styleable.BannerView_banner_pageChangeDuration) {
            mPageChangeDuration = typedArray.getInteger(attr, mPageChangeDuration);
        } else if (attr == R.styleable.BannerView_banner_transitionEffect) {
            int ordinal = typedArray.getInt(attr, TransitionEffect.Accordion.ordinal());
            mTransitionEffect = TransitionEffect.values()[ordinal];
        } else if (attr == R.styleable.BannerView_banner_titleTextColor) {
            mTitleTextColor = typedArray.getColor(attr, mTitleTextColor);
        } else if (attr == R.styleable.BannerView_banner_titleTextSize) {
            mTitleTextSize = typedArray.getDimensionPixelSize(attr, mTitleTextSize);
        } else if (attr == R.styleable.BannerView_banner_placeholderDrawable) {
            mPlaceholderDrawableResId = typedArray.getResourceId(attr, mPlaceholderDrawableResId);
        } else if (attr == R.styleable.BannerView_banner_isNumberIndicator) {
            mIsNumberIndicator = typedArray.getBoolean(attr, mIsNumberIndicator);
        } else if (attr == R.styleable.BannerView_banner_numberIndicatorTextColor) {
            mNumberIndicatorTextColor = typedArray.getColor(attr, mNumberIndicatorTextColor);
        } else if (attr == R.styleable.BannerView_banner_numberIndicatorTextSize) {
            mNumberIndicatorTextSize = typedArray.getDimensionPixelSize(attr, mNumberIndicatorTextSize);
        } else if (attr == R.styleable.BannerView_banner_numberIndicatorBackground) {
            mNumberIndicatorBackground = typedArray.getDrawable(attr);
        } else if (attr == R.styleable.BannerView_banner_isNeedShowIndicatorOnOnlyOnePage) {
            mIsNeedShowIndicatorOnOnlyOnePage = typedArray.getBoolean(attr, mIsNeedShowIndicatorOnOnlyOnePage);
        } else if (attr == R.styleable.BannerView_banner_contentBottomMargin) {
            mContentBottomMargin = typedArray.getDimensionPixelSize(attr, mContentBottomMargin);
        } else if (attr == R.styleable.BannerView_banner_showTitleText) {
            showTitleText = typedArray.getBoolean(attr, showTitleText);
        } else if (attr == R.styleable.BannerView_banner_showIndicator) {
            showIndicator = typedArray.getBoolean(attr, showIndicator);
        }
    }

    @SuppressLint("RtlHardcoded")
    private void initView(Context context) {
        if(!showIndicator){
            return;
        }
        RelativeLayout pointContainerRl = new RelativeLayout(context);
        if (Build.VERSION.SDK_INT >= 16) {
            pointContainerRl.setBackground(mPointContainerBackgroundDrawable);
        } else {
            pointContainerRl.setBackgroundDrawable(mPointContainerBackgroundDrawable);
        }
        pointContainerRl.setPadding(mPointContainerLeftRightPadding, mPointTopBottomMargin, mPointContainerLeftRightPadding, mPointTopBottomMargin);
        LayoutParams pointContainerLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        // 处理圆点在顶部还是底部
        if ((mPointGravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP) {
            pointContainerLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            pointContainerLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        addView(pointContainerRl, pointContainerLp);

        LayoutParams indicatorLp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        indicatorLp.addRule(CENTER_VERTICAL);
        if (mIsNumberIndicator) {
            mNumberIndicatorTv = new TextView(context);
            mNumberIndicatorTv.setId(R.id.banner_indicatorId);
            mNumberIndicatorTv.setGravity(Gravity.CENTER_VERTICAL);
            mNumberIndicatorTv.setSingleLine(true);
            mNumberIndicatorTv.setEllipsize(TextUtils.TruncateAt.END);
            mNumberIndicatorTv.setTextColor(mNumberIndicatorTextColor);
            mNumberIndicatorTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mNumberIndicatorTextSize);
            mNumberIndicatorTv.setVisibility(View.INVISIBLE);
            if (mNumberIndicatorBackground != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mNumberIndicatorTv.setBackground(mNumberIndicatorBackground);
                } else {
                    mNumberIndicatorTv.setBackgroundDrawable(mNumberIndicatorBackground);
                }
            }
            pointContainerRl.addView(mNumberIndicatorTv, indicatorLp);
        } else {
            mPointRealContainerLl = new LinearLayout(context);
            mPointRealContainerLl.setId(R.id.banner_indicatorId);
            mPointRealContainerLl.setOrientation(LinearLayout.HORIZONTAL);
            mPointRealContainerLl.setGravity(Gravity.CENTER_VERTICAL);
            pointContainerRl.addView(mPointRealContainerLl, indicatorLp);
        }

        int horizontalGravity = mPointGravity & Gravity.HORIZONTAL_GRAVITY_MASK;

        if(showTitleText){
            LayoutParams titleLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            titleLp.addRule(CENTER_VERTICAL);
            mTitleTv = new TextView(context);
            mTitleTv.setGravity(Gravity.CENTER_VERTICAL);
            mTitleTv.setSingleLine(true);
            mTitleTv.setEllipsize(TextUtils.TruncateAt.END);
            mTitleTv.setTextColor(mTitleTextColor);
            mTitleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleTextSize);
            mTitleTv.setVisibility(View.GONE);
            pointContainerRl.addView(mTitleTv, titleLp);

            if (horizontalGravity == Gravity.LEFT) {
                titleLp.addRule(RelativeLayout.RIGHT_OF, R.id.banner_indicatorId);
            } else if (horizontalGravity == Gravity.RIGHT) {
                titleLp.addRule(RelativeLayout.LEFT_OF, R.id.banner_indicatorId);
            } else {
                titleLp.addRule(RelativeLayout.LEFT_OF, R.id.banner_indicatorId);
            }
        }

        // 处理圆点在左边、右边还是水平居中
        if (horizontalGravity == Gravity.LEFT) {
            indicatorLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (horizontalGravity == Gravity.RIGHT) {
            indicatorLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            indicatorLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }

        showPlaceholder();
    }

    public void showPlaceholder() {
        if (mPlaceholderIv == null && mPlaceholderDrawableResId != NO_PLACEHOLDER_DRAWABLE) {
            mPlaceholderIv = getItemImageView(getContext(), mPlaceholderDrawableResId, ImageView.ScaleType.FIT_XY);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0, 0, 0, mContentBottomMargin);
            addView(mPlaceholderIv, layoutParams);
        }
    }

    private ImageView getItemImageView(Context context, @DrawableRes int placeholderResId, ImageView.ScaleType scaleType) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(placeholderResId);
        imageView.setClickable(true);
        imageView.setScaleType(scaleType);
        return imageView;
    }

    /**
     * 设置页码切换过程的时间长度
     *
     * @param duration 页码切换过程的时间长度
     */
    public void setPageChangeDuration(int duration) {
        if (duration >= 0 && duration <= 2000) {
            mPageChangeDuration = duration;
            if (mViewPager != null) {
                mViewPager.setPageChangeDuration(duration);
            }
        }
    }

    /**
     * 设置是否开启自动轮播，需要在 setData 方法之前调用，并且调了该方法后必须再调用一次 setData 方法
     * 例如根据图片当图片数量大于 1 时开启自动轮播，等于 1 时不开启自动轮播
     * mDefaultBanner.setAutoPlayAble(bannerModel.imgs.size() > 1);
     * mDefaultBanner.setData(bannerModel.imgs, bannerModel.tips);
     *
     * @param autoPlayAble boolean
     */
    public void setAutoPlayAble(boolean autoPlayAble) {
        mAutoPlayAble = autoPlayAble;

        stopAutoPlay();

        if (mViewPager != null && mViewPager.getAdapter() != null) {
            mViewPager.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * 设置自动轮播的时间间隔
     *
     * @param autoPlayInterval 毫秒值
     */
    public void setAutoPlayInterval(int autoPlayInterval) {
        mAutoPlayInterval = autoPlayInterval;
    }

    /**
     * 设置每一页的控件、数据模型和文案
     *
     * @param views  每一页的控件集合
     * @param models 每一页的数据模型集合
     */
    public void setData(List<View> views, List<?> models) {
        if (views == null || views.size() < 1) {
            return;
        }
        if (views.size() == 1) {
            mAutoPlayAble = false;
            setAllowUserScrollable(false);
        } else if (mAutoPlayAble && views.size() < 3 && mHackyViews == null) {
            mAutoPlayAble = false;
        }
        mModels = models;
        mViews = views;

        initIndicator();
        initViewPager();
        removePlaceholder();
    }

    /**
     * 设置布局资源id、数据模型和文案
     *
     * @param layoutResId item布局文件资源id
     * @param models      每一页的数据模型集合
     */
    public void setData(@LayoutRes int layoutResId, List<?> models) {
        mViews = new ArrayList<>();
        if (models == null) {
            models = new ArrayList<>();
        }
        for (int i = 0; i < models.size(); i++) {
            mViews.add(View.inflate(getContext(), layoutResId, null));
        }
        if (mAutoPlayAble && mViews.size() < 3) {
            mHackyViews = new ArrayList<>(mViews);
            mHackyViews.add(View.inflate(getContext(), layoutResId, null));
            if (mHackyViews.size() == 2) {
                mHackyViews.add(View.inflate(getContext(), layoutResId, null));
            }
        }
        setData(mViews, models);
    }

    /**
     * 设置数据模型和文案，布局资源默认为 ImageView
     *
     * @param models 每一页的数据模型集合
     */
    public void setData(List<?> models) {
        setData(R.layout.item_banner_image, models);
    }

    /**
     * 设置每一页图片的资源 id，主要针对引导页的情况
     *
     * @param resIds DrawableRes
     */
    public void setData(@DrawableRes int... resIds) {
        List<View> views = new ArrayList<>();
        for (int resId : resIds) {
            views.add(getItemImageView(getContext(), resId, ImageView.ScaleType.CENTER_CROP));
        }
        setData(views, null);
    }

    /**
     * 设置是否允许用户手指滑动
     *
     * @param allowUserScrollable true表示允许跟随用户触摸滑动，false反之
     */
    public void setAllowUserScrollable(boolean allowUserScrollable) {
        mAllowUserScrollable = allowUserScrollable;
        if (mViewPager != null) {
            mViewPager.setAllowUserScrollable(mAllowUserScrollable);
        }
    }

    /**
     * 添加ViewPager滚动监听器
     *
     * @param onPageChangeListener ViewPager.OnPageChangeListener
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        mOnPageChangeListener = onPageChangeListener;
    }

    /**
     * 设置进入按钮和跳过按钮控件资源 id，需要开发者自己处理这两个按钮的点击事件
     *
     * @param enterResId 进入按钮控件
     * @param skipResId  跳过按钮控件
     */
    public void setEnterSkipViewId(int enterResId, int skipResId) {
        if (enterResId != 0) {
            mEnterView = ((Activity) getContext()).findViewById(enterResId);
        }
        if (skipResId != 0) {
            mSkipView = ((Activity) getContext()).findViewById(skipResId);
        }
    }

    /**
     * 设置进入按钮和跳过按钮控件资源 id 及其点击事件监听器
     * 如果进入按钮和跳过按钮有一个不存在的话就传 0
     * 在 BannerView 里已经帮开发者处理了重复点击事件
     * 在 BannerView 里已经帮开发者处理了「跳过按钮」和「进入按钮」的显示与隐藏
     *
     * @param enterResId    进入按钮控件资源 id，没有的话就传 0
     * @param skipResId     跳过按钮控件资源 id，没有的话就传 0
     * @param guideDelegate 引导页「进入」和「跳过」按钮点击事件监听器
     */
    public void setEnterSkipViewIdAndDelegate(int enterResId, int skipResId, GuideDelegate guideDelegate) {
        if (guideDelegate != null) {
            mGuideDelegate = guideDelegate;
            if (enterResId != 0) {
                mEnterView = ((Activity) getContext()).findViewById(enterResId);
                mEnterView.setOnClickListener(mGuideOnFilterDoubleClickListener);
            }
            if (skipResId != 0) {
                mSkipView = ((Activity) getContext()).findViewById(skipResId);
                mSkipView.setOnClickListener(mGuideOnFilterDoubleClickListener);
            }
        }
    }

    /**
     * 获取当前选中界面索引
     *
     * @return position
     */
    public int getCurrentItem() {
        if (mViewPager == null || mViews == null) {
            return 0;
        } else {
            return mViewPager.getCurrentItem() % mViews.size();
        }
    }

    /**
     * 获取广告页面总个数
     *
     * @return count
     */
    public int getItemCount() {
        return mViews == null ? 0 : mViews.size();
    }

    public List<? extends View> getViews() {
        return mViews;
    }

    public <VT extends View> VT getItemView(int position) {
        return mViews == null ? null : (VT) mViews.get(position);
    }

    public ImageView getItemImageView(int position) {
        return getItemView(position);
    }

    public BannerViewPager getViewPager() {
        return mViewPager;
    }

    public void setOverScrollMode(int overScrollMode) {
        mOverScrollMode = overScrollMode;
        if (mViewPager != null) {
            mViewPager.setOverScrollMode(mOverScrollMode);
        }
    }

    private void initIndicator() {
        if (mPointRealContainerLl != null) {
            mPointRealContainerLl.removeAllViews();

            if (mIsNeedShowIndicatorOnOnlyOnePage || mViews.size() > 1) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(mPointLeftRightMargin, 0, mPointLeftRightMargin, 0);
                ImageView imageView;
                for (int i = 0; i < mViews.size(); i++) {
                    imageView = new ImageView(getContext());
                    imageView.setLayoutParams(lp);
                    imageView.setImageResource(mPointDrawableResId);
                    mPointRealContainerLl.addView(imageView);
                }
            }
        }
        if (mNumberIndicatorTv != null) {
            if (mIsNeedShowIndicatorOnOnlyOnePage || (!mIsNeedShowIndicatorOnOnlyOnePage && mViews.size() > 1)) {
                mNumberIndicatorTv.setVisibility(View.VISIBLE);
            } else {
                mNumberIndicatorTv.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void initViewPager() {
        if (mViewPager != null && this.equals(mViewPager.getParent())) {
            this.removeView(mViewPager);
            mViewPager = null;
        }

        mViewPager = new BannerViewPager(getContext());
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(new PageAdapter());
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOverScrollMode(mOverScrollMode);
        mViewPager.setAllowUserScrollable(mAllowUserScrollable);
        mViewPager.setPageTransformer(true, BannerPageTransformer.getPageTransformer(mTransitionEffect));
        setPageChangeDuration(mPageChangeDuration);

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, mContentBottomMargin);
        addView(mViewPager, 0, layoutParams);

        if (mEnterView != null || mSkipView != null) {
            mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (position == getItemCount() - 2) {
                        if (mEnterView != null) {
                            ViewCompat.setAlpha(mEnterView, positionOffset);
                        }
                        if (mSkipView != null) {
                            ViewCompat.setAlpha(mSkipView, 1.0f - positionOffset);
                        }

                        if (positionOffset > 0.5f) {
                            if (mEnterView != null) {
                                mEnterView.setVisibility(View.VISIBLE);
                            }
                            if (mSkipView != null) {
                                mSkipView.setVisibility(View.GONE);
                            }
                        } else {
                            if (mEnterView != null) {
                                mEnterView.setVisibility(View.GONE);
                            }
                            if (mSkipView != null) {
                                mSkipView.setVisibility(View.VISIBLE);
                            }
                        }
                    } else if (position == getItemCount() - 1) {
                        if (mSkipView != null) {
                            mSkipView.setVisibility(View.GONE);
                        }
                        if (mEnterView != null) {
                            mEnterView.setVisibility(View.VISIBLE);
                            ViewCompat.setAlpha(mEnterView, 1.0f);
                        }
                    } else {
                        if (mSkipView != null) {
                            mSkipView.setVisibility(View.VISIBLE);
                            ViewCompat.setAlpha(mSkipView, 1.0f);
                        }
                        if (mEnterView != null) {
                            mEnterView.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }

        if (mAutoPlayAble) {
            int zeroItem = Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2) % mViews.size();
            mViewPager.setCurrentItem(zeroItem);
            startAutoPlay();
        } else {
            switchToPoint(0);
        }
    }

    public void removePlaceholder() {
        if (mPlaceholderIv != null && this.equals(mPlaceholderIv.getParent())) {
            removeView(mPlaceholderIv);
            mPlaceholderIv = null;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mAutoPlayAble && mAllowUserScrollable) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stopAutoPlay();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    startAutoPlay();
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 设置当只有一页数据时是否显示指示器
     *
     * @param isNeedShowIndicatorOnOnlyOnePage boolean
     */
    public void setIsNeedShowIndicatorOnOnlyOnePage(boolean isNeedShowIndicatorOnOnlyOnePage) {
        mIsNeedShowIndicatorOnOnlyOnePage = isNeedShowIndicatorOnOnlyOnePage;
    }

    public void setCurrentItem(int item) {
        if (mViewPager == null || mViews == null || item > getItemCount() - 1) {
            return;
        }

        if (mAutoPlayAble) {
            int realCurrentItem = mViewPager.getCurrentItem();
            int currentItem = realCurrentItem % mViews.size();
            int offset = item - currentItem;

            // 这里要使用循环递增或递减设置，否则会ANR
            if (offset < 0) {
                for (int i = -1; i >= offset; i--) {
                    mViewPager.setCurrentItem(realCurrentItem + i, false);
                }
            } else if (offset > 0) {
                for (int i = 1; i <= offset; i++) {
                    mViewPager.setCurrentItem(realCurrentItem + i, false);
                }
            }

            startAutoPlay();
        } else {
            mViewPager.setCurrentItem(item, false);
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            startAutoPlay();
        } else if (visibility == INVISIBLE || visibility == GONE) {
            onInvisibleToUser();
        }
    }

    private void onInvisibleToUser() {
        stopAutoPlay();

        // 处理 RecyclerView 中从对用户不可见变为可见时卡顿的问题
        if (!mIsFirstInvisible && mAutoPlayAble && mViewPager != null && getItemCount() > 0) {
            switchToNextPage();
        }
        mIsFirstInvisible = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onInvisibleToUser();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAutoPlay();
    }

    public void startAutoPlay() {
        stopAutoPlay();
        if (mAutoPlayAble) {
            postDelayed(mAutoPlayTask, mAutoPlayInterval);
        }
    }

    public void stopAutoPlay() {
        if (mAutoPlayTask != null) {
            removeCallbacks(mAutoPlayTask);
        }
    }

    private void switchToPoint(int newCurrentPoint) {
        if (mTitleTv != null && mBannerAdapter != null) {
            String title = mBannerAdapter.getTitle(mModels.get(newCurrentPoint), newCurrentPoint);
            if (TextUtils.isEmpty(title) || mModels.size() < 1) {
                mTitleTv.setVisibility(View.GONE);
            } else {
                mTitleTv.setVisibility(View.VISIBLE);
                mTitleTv.setText(title);
            }
        }

        if (mPointRealContainerLl != null) {
            if (mViews != null && mViews.size() > 0 && newCurrentPoint < mViews.size() && (mIsNeedShowIndicatorOnOnlyOnePage || mViews.size() > 1)) {
                mPointRealContainerLl.setVisibility(View.VISIBLE);
                for (int i = 0; i < mPointRealContainerLl.getChildCount(); i++) {
                    mPointRealContainerLl.getChildAt(i).setEnabled(i == newCurrentPoint);
                    // 处理指示器选中和未选中状态图片尺寸不相等
                    mPointRealContainerLl.getChildAt(i).requestLayout();
                }
            } else {
                mPointRealContainerLl.setVisibility(View.GONE);
            }
        }

        if (mNumberIndicatorTv != null) {
            if (mViews != null && mViews.size() > 0 && newCurrentPoint < mViews.size() && ((mIsNeedShowIndicatorOnOnlyOnePage || mViews.size() > 1))) {
                mNumberIndicatorTv.setVisibility(View.VISIBLE);
                mNumberIndicatorTv.setText((newCurrentPoint + 1) + "/" + mViews.size());
            } else {
                mNumberIndicatorTv.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置页面切换换动画
     *
     * @param effect TransitionEffect
     */
    public void setTransitionEffect(TransitionEffect effect) {
        mTransitionEffect = effect;
        if (mViewPager != null) {
            initViewPager();
            if (mHackyViews == null) {
                UiUtils.resetBannerPageTransformer(mViews);
            } else {
                UiUtils.resetBannerPageTransformer(mHackyViews);
            }
        }
    }

    /**
     * 设置自定义页面切换动画
     *
     * @param transformer ViewPager.PageTransformer
     */
    public void setPageTransformer(ViewPager.PageTransformer transformer) {
        if (transformer != null && mViewPager != null) {
            mViewPager.setPageTransformer(true, transformer);
        }
    }

    /**
     * 切换到下一页
     */
    private void switchToNextPage() {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
    }

    @Override
    public void onPageSelected(int position) {
        position = position % mViews.size();
        switchToPoint(position);

        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        handleGuideViewVisibility(position, positionOffset);
        if (mTitleTv != null && mBannerAdapter != null) {
            if (mModels != null && mModels.size() > 0) {
                mTitleTv.setVisibility(View.VISIBLE);

                int leftPosition = position % mModels.size();
                int rightPosition = (position + 1) % mModels.size();
                if (rightPosition < mModels.size() && leftPosition < mModels.size()) {
                    String title;
                    if (positionOffset > 0.5) {
                        title = mBannerAdapter.getTitle(mModels.get(rightPosition), rightPosition);
                        mTitleTv.setText(title);
                        ViewCompat.setAlpha(mTitleTv, positionOffset);
                    } else {
                        title = mBannerAdapter.getTitle(mModels.get(leftPosition), leftPosition);
                        mTitleTv.setText(title);
                        ViewCompat.setAlpha(mTitleTv, 1 - positionOffset);
                    }
                }
            } else {
                mTitleTv.setVisibility(View.GONE);
            }
        }

        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrolled(position % mViews.size(), positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrollStateChanged(state);
        }
    }

    private void handleGuideViewVisibility(int position, float positionOffset) {
        if (mEnterView == null && mSkipView == null) {
            return;
        }

        if (position == getItemCount() - 2) {
            if (mEnterView != null) {
                ViewCompat.setAlpha(mEnterView, positionOffset);
            }
            if (mSkipView != null) {
                ViewCompat.setAlpha(mSkipView, 1.0f - positionOffset);
            }

            if (positionOffset > 0.5f) {
                if (mEnterView != null) {
                    mEnterView.setVisibility(View.VISIBLE);
                }
                if (mSkipView != null) {
                    mSkipView.setVisibility(View.GONE);
                }
            } else {
                if (mEnterView != null) {
                    mEnterView.setVisibility(View.GONE);
                }
                if (mSkipView != null) {
                    mSkipView.setVisibility(View.VISIBLE);
                }
            }
        } else if (position == getItemCount() - 1) {
            if (mSkipView != null) {
                mSkipView.setVisibility(View.GONE);
            }
            if (mEnterView != null) {
                mEnterView.setVisibility(View.VISIBLE);
                ViewCompat.setAlpha(mEnterView, 1.0f);
            }
        } else {
            if (mSkipView != null) {
                mSkipView.setVisibility(View.VISIBLE);
                ViewCompat.setAlpha(mSkipView, 1.0f);
            }
            if (mEnterView != null) {
                mEnterView.setVisibility(View.GONE);
            }
        }
    }

    public void setOnBannerItemClickListener(OnBannerItemClickListener onBannerItemClickListener) {
        mOnBannerItemClickListener = onBannerItemClickListener;
    }

    public void setBannerAdapter(BannerAdapter bannerAdapter) {
        mBannerAdapter = bannerAdapter;
    }

    private class PageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mViews == null ? 0 : (mAutoPlayAble ? Integer.MAX_VALUE : mViews.size());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final int finalPosition = position % mViews.size();

            View view;
            if (mHackyViews == null) {
                view = mViews.get(finalPosition);
            } else {
                view = mHackyViews.get(position % mHackyViews.size());
            }

            if (mOnBannerItemClickListener != null) {
                view.setOnClickListener(new OnFilterDoubleClickListener() {
                    @Override
                    public void onFilterDoubleClick(View view) {
                        int currentPosition = mViewPager.getCurrentItem() % mViews.size();
                        mOnBannerItemClickListener.onBannerItemClick(view, mModels == null ? null : mModels.get(currentPosition), currentPosition);
                    }
                });
            }

            if (mBannerAdapter != null) {
                mBannerAdapter.fillBannerItem(view, mModels == null ? null : mModels.get(finalPosition), finalPosition);
            }

            ViewParent viewParent = view.getParent();
            if (viewParent != null) {
                ((ViewGroup) viewParent).removeView(view);
            }

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    private static class AutoPlayTask implements Runnable {
        private final WeakReference<BannerView> mBannerWeakReference;

        private AutoPlayTask(BannerView banner) {
            mBannerWeakReference = new WeakReference<>(banner);
        }

        @Override
        public void run() {
            BannerView banner = mBannerWeakReference.get();
            if (banner != null) {
                banner.switchToNextPage();
                banner.startAutoPlay();
            }
        }
    }

    /**
     * item 点击事件监听器，在 BannerView 里已经帮开发者处理了防止重复点击事件
     *
     * @param <V> item 视图类型，如果没有在 setData 方法里指定自定义的 item 布局资源文件的话，这里的 V 就是 ImageView
     * @param <M> item 数据模型
     */
    public interface OnBannerItemClickListener<V extends View, M> {
        void onBannerItemClick(V itemView, M model, int position);
    }

    /**
     * 适配器，在 fillBannerItem 方法中填充数据，加载网络图片等
     *
     * @param <V> item 视图类型，如果没有在 setData 方法里指定自定义的 item 布局资源文件的话，这里的 V 就是 ImageView
     * @param <M> item 数据模型
     */
    public static abstract class BannerAdapter<V extends View, M> {
        public abstract void fillBannerItem(V itemView, M model, int position);

        public String getTitle(M model, int position) {
            return null;
        }
    }

    /**
     * 引导页「进入」和「跳过」按钮点击事件监听器，在 BannerView 里已经帮开发者处理了防止重复点击事件
     */
    public interface GuideDelegate {
        void onClickEnterOrSkip();
    }
}