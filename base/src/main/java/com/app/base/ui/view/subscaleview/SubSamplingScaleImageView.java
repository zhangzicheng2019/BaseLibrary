/*
Copyright 2013-2015 David Morrissey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.app.base.ui.view.subscaleview;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;

import com.app.base.R;
import com.app.base.ui.view.subscaleview.decoder.CompatDecoderFactory;
import com.app.base.ui.view.subscaleview.decoder.DecoderFactory;
import com.app.base.ui.view.subscaleview.decoder.ImageDecoder;
import com.app.base.ui.view.subscaleview.decoder.ImageRegionDecoder;
import com.app.base.ui.view.subscaleview.decoder.SkiaImageDecoder;
import com.app.base.ui.view.subscaleview.decoder.SkiaImageRegionDecoder;
import com.app.base.utils.UiUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Displays an image subsampled as necessary to avoid loading too much image data into memory. After a pinch to zoom in,
 * a set of image tiles subsampled at higher resolution are loaded and displayed over the base layer. During pinch and
 * zoom, tiles off screen or higher/lower resolution than required are discarded from memory.
 * <p>
 * Tiles are no larger than the max supported bitmap size, so with large images tiling may be used even when zoomed out.
 * <p>
 * v prefixes - coordinates, translations and distances measured in screen (view) pixels
 * s prefixes - coordinates, translations and distances measured in source image pixels (scaled)
 * <p>
 * https://github.com/davemorrissey/subsampling-scale-image-view
 */
@SuppressWarnings("unused")
public class SubSamplingScaleImageView extends View {

    private static final String TAG = SubSamplingScaleImageView.class.getSimpleName();

    /**
     * Attempt to use EXIF information on the image to rotate it. Works for external files only.
     */
    public static final int ORIENTATION_USE_EXIF = -1;
    /**
     * Display the image file in its native orientation.
     */
    public static final int ORIENTATION_0 = 0;
    /**
     * Rotate the image 90 degrees clockwise.
     */
    public static final int ORIENTATION_90 = 90;
    /**
     * Rotate the image 180 degrees.
     */
    public static final int ORIENTATION_180 = 180;
    /**
     * Rotate the image 270 degrees clockwise.
     */
    public static final int ORIENTATION_270 = 270;

    private static final List<Integer> VALID_ORIENTATIONS = Arrays.asList(ORIENTATION_0, ORIENTATION_90, ORIENTATION_180, ORIENTATION_270, ORIENTATION_USE_EXIF);

    /**
     * During zoom animation, keep the point of the image that was tapped in the same place, and scale the image around it.
     */
    public static final int ZOOM_FOCUS_FIXED = 1;
    /**
     * During zoom animation, move the point of the image that was tapped to the center of the screen.
     */
    public static final int ZOOM_FOCUS_CENTER = 2;
    /**
     * Zoom in to and center the tapped point immediately without animating.
     */
    public static final int ZOOM_FOCUS_CENTER_IMMEDIATE = 3;

    private static final List<Integer> VALID_ZOOM_STYLES = Arrays.asList(ZOOM_FOCUS_FIXED, ZOOM_FOCUS_CENTER, ZOOM_FOCUS_CENTER_IMMEDIATE);

    /**
     * Quadratic ease out. Not recommended for scale animation, but good for panning.
     */
    public static final int EASE_OUT_QUAD = 1;
    /**
     * Quadratic ease in and out.
     */
    public static final int EASE_IN_OUT_QUAD = 2;

    private static final List<Integer> VALID_EASING_STYLES = Arrays.asList(EASE_IN_OUT_QUAD, EASE_OUT_QUAD);

    /**
     * Don't allow the image to be panned off screen. As much of the image as possible is always displayed, centered in the view when it is smaller. This is the best option for galleries.
     */
    public static final int PAN_LIMIT_INSIDE = 1;
    /**
     * Allows the image to be panned until it is just off screen, but no further. The edge of the image will stop when it is flush with the screen edge.
     */
    public static final int PAN_LIMIT_OUTSIDE = 2;
    /**
     * Allows the image to be panned until a corner reaches the center of the screen but no further. Useful when you want to pan any spot on the image to the exact center of the screen.
     */
    public static final int PAN_LIMIT_CENTER = 3;

    private static final List<Integer> VALID_PAN_LIMITS = Arrays.asList(PAN_LIMIT_INSIDE, PAN_LIMIT_OUTSIDE, PAN_LIMIT_CENTER);

    /**
     * Scale the image so that both dimensions of the image will be equal to or less than the corresponding dimension of the view. The image is then centered in the view. This is the default behaviour and best for galleries.
     */
    public static final int SCALE_TYPE_CENTER_INSIDE = 1;
    /**
     * Scale the image uniformly so that both dimensions of the image will be equal to or larger than the corresponding dimension of the view. The image is then centered in the view.
     */
    public static final int SCALE_TYPE_CENTER_CROP = 2;
    /**
     * Scale the image so that both dimensions of the image will be equal to or less than the maxScale and equal to or larger than minScale. The image is then centered in the view.
     */
    public static final int SCALE_TYPE_CUSTOM = 3;

    private static final List<Integer> VALID_SCALE_TYPES = Arrays.asList(SCALE_TYPE_CENTER_CROP, SCALE_TYPE_CENTER_INSIDE, SCALE_TYPE_CUSTOM);

    /**
     * State change originated from animation.
     */
    public static final int ORIGIN_ANIM = 1;
    /**
     * State change originated from touch gesture.
     */
    public static final int ORIGIN_TOUCH = 2;
    /**
     * State change originated from a fling momentum anim.
     */
    public static final int ORIGIN_FLING = 3;
    /**
     * State change originated from a double tap zoom anim.
     */
    public static final int ORIGIN_DOUBLE_TAP_ZOOM = 4;

    // Bitmap (preview or full image)
    private Bitmap mBitmap;

    // Whether the bitmap is a preview image
    private boolean isBitmapPreview;

    // Specifies if a cache handler is also referencing the bitmap. Do not recycle if so.
    private boolean isBitmapCached;

    // Uri of full size image
    private Uri mUri;

    // Sample size used to display the whole image when fully zoomed out
    private int mFullImageSampleSize;

    // Map of zoom level to tile grid
    private Map<Integer, List<Tile>> mTileMap;

    // Overlay tile boundaries and other info
    private boolean isDebug;

    // BImage orientation setting
    private int mOrientation = ORIENTATION_0;

    // Max scale allowed (prevent infinite zoom)
    private float mMaxScale = 4F;

    // Min scale allowed (prevent infinite zoom)
    private float mMinScale = 1f;

    // Density to reach before loading higher resolution tiles
    private int mMinimumTileDpi = -1;

    // Pan limiting style
    private int mPanLimit = PAN_LIMIT_INSIDE;

    // Minimum scale type
    private int mMinimumScaleType = SCALE_TYPE_CENTER_INSIDE;

    // overrides for the dimensions of the generated tiles
    public static int TILE_SIZE_AUTO = Integer.MAX_VALUE;
    private int mMaxTileWidth = TILE_SIZE_AUTO;
    private int mMaxTileHeight = TILE_SIZE_AUTO;

    // Whether to use the thread pool executor to load tiles
    private boolean isParallelLoadingEnabled;

    // Gesture detection settings
    private boolean isPanEnabled = true;
    private boolean isZoomEnabled = true;
    /**
     * this is true will be some problems
     */
    private boolean isQuickScaleEnabled = false;

    // Double tap zoom behaviour
    private float mDoubleTapZoomScale = 2f;
    private int mDoubleTapZoomStyle = ZOOM_FOCUS_FIXED;
    private int mDoubleTapZoomDuration = 250;

    // Current scale and scale at start of zoom
    private float mScale;
    private float mScaleStart;

    // Screen coordinate of top-left corner of source image
    private PointF mPfTranslate;
    private PointF mPfTranslateStart;
    private PointF mPfTranslateBefore;

    // Source coordinate to center on, used when new position is set externally before view is ready
    private Float mPendingScale;
    private PointF mPendingCenter;
    private PointF mRequestedCenter;

    // Source image dimensions and orientation - dimensions relate to the unrotated image
    private int mWidth;
    private int mHeight;
    private int mSOrientation;
    private Rect mSRegion;
    private Rect mPRegion;

    // Is two-finger zooming in progress
    private boolean isZooming;
    // Is one-finger panning in progress
    private boolean isPanning;
    // Is quick-scale gesture in progress
    private boolean isQuickScaling;
    // Max touches used in current gesture
    private int mMaxTouchCount;

    // Fling detector
    private GestureDetector mGestureDetector;

    // Tile and image decoding
    private ImageRegionDecoder mImageRegionDecoder;
    private final Object mDecoderLock = new Object();
    private DecoderFactory<? extends ImageDecoder> mBitmapDecoderFactory = new CompatDecoderFactory<ImageDecoder>(SkiaImageDecoder.class);
    private DecoderFactory<? extends ImageRegionDecoder> mRegionDecoderFactory = new CompatDecoderFactory<ImageRegionDecoder>(SkiaImageRegionDecoder.class);

    // Debug values
    private PointF mVCenterStart;
    private float mVDistStart;

    // Current quickscale state
    private final float mQuickScaleThreshold;
    private float mQuickScaleLastDistance;
    private boolean isQuickScaleMoved;
    private PointF mQuickScaleVLastPoint;
    private PointF mQuickScaleSCenterPoint;
    private PointF mQuickScaleVStartPoint;

    // Scale and center animation tracking
    private Anim mAnim;

    // Whether a ready notification has been sent to subclasses
    private boolean isReadySent;
    // Whether a base layer loaded notification has been sent to subclasses
    private boolean isImageLoadedSent;

    // Event listener
    private OnImageEventListener mOnImageEventListener;

    // Scale and center listener
    private OnStateChangedListener mOnStateChangedListener;

    // Long click listener
    private OnLongClickListener mOnLongClickListener;

    // Long click handler
    private Handler mHandler;
    private static final int MESSAGE_LONG_CLICK = 1;

    // Paint objects created once and reused for efficiency
    private Paint mBitmapPaint;
    private Paint mDebugPaint;
    private Paint mTileBgPaint;

    // Volatile fields used to reduce object creation
    private ScaleAndTranslate mSatTemp;
    private Matrix mMatrix;
    private RectF mSRect;
    private float[] mSrcArray = new float[8];
    private float[] mDstArray = new float[8];

    //The logical density of the display
    private float mDensity;

    private static int mImageHeight;

    public SubSamplingScaleImageView(Context context) {
        this(context, null);
    }

    public SubSamplingScaleImageView(Context context, AttributeSet attr) {
        super(context, attr);
        mDensity = getResources().getDisplayMetrics().density;
//        setMinimumDpi(160);
//        setDoubleTapZoomDpi(160);
        mDoubleTapZoomScale = mDensity + 1;
        mMaxScale = mDoubleTapZoomScale + 2;
        setGestureDetector(context);
        this.mHandler = new Handler(new Handler.Callback() {
            public boolean handleMessage(Message message) {
                if (message.what == MESSAGE_LONG_CLICK && mOnLongClickListener != null) {
                    mMaxTouchCount = 0;
                    SubSamplingScaleImageView.super.setOnLongClickListener(mOnLongClickListener);
                    performLongClick();
                    SubSamplingScaleImageView.super.setOnLongClickListener(null);
                }
                return true;
            }
        });
        // Handle XML attributes
        if (attr != null) {
            TypedArray typedAttr = getContext().obtainStyledAttributes(attr, R.styleable.SubSamplingScaleImageView);
            if (typedAttr.hasValue(R.styleable.SubsamplingScaleImageView_assetName)) {
                String assetName = typedAttr.getString(R.styleable.SubsamplingScaleImageView_assetName);
                if (assetName != null && assetName.length() > 0) {
                    setImage(ImageSource.asset(assetName).tilingEnabled());
                }
            }
            if (typedAttr.hasValue(R.styleable.SubsamplingScaleImageView_src)) {
                int resId = typedAttr.getResourceId(R.styleable.SubsamplingScaleImageView_src, 0);
                if (resId > 0) {
                    setImage(ImageSource.resource(resId).tilingEnabled());
                }
            }
            if (typedAttr.hasValue(R.styleable.SubsamplingScaleImageView_panEnabled)) {
                setPanEnabled(typedAttr.getBoolean(R.styleable.SubsamplingScaleImageView_panEnabled, true));
            }
            if (typedAttr.hasValue(R.styleable.SubsamplingScaleImageView_zoomEnabled)) {
                setZoomEnabled(typedAttr.getBoolean(R.styleable.SubsamplingScaleImageView_zoomEnabled, true));
            }
            if (typedAttr.hasValue(R.styleable.SubsamplingScaleImageView_quickScaleEnabled)) {
                setQuickScaleEnabled(typedAttr.getBoolean(R.styleable.SubsamplingScaleImageView_quickScaleEnabled, true));
            }
            if (typedAttr.hasValue(R.styleable.SubsamplingScaleImageView_tileBackgroundColor)) {
                setTileBackgroundColor(typedAttr.getColor(R.styleable.SubsamplingScaleImageView_tileBackgroundColor, Color.argb(0, 0, 0, 0)));
            }
            typedAttr.recycle();
        }

        mQuickScaleThreshold = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
        if (mImageHeight == 0) {
            mImageHeight = UiUtils.getAppUsableScreenSize(getContext()).y;
        }
    }

    /**
     * Sets the image orientation. It's best to call this before setting the image file or asset, because it may waste
     * loading of tiles. However, this can be freely called at any time.
     */
    public final void setOrientation(int orientation) {
        if (!VALID_ORIENTATIONS.contains(orientation)) {
            throw new IllegalArgumentException("Invalid orientation: " + orientation);
        }
        this.mOrientation = orientation;
        reset(false);
        invalidate();
        requestLayout();
    }

    /**
     * Set the image source from a bitmap, resource, asset, file or other URI.
     *
     * @param imageSource BImage source.
     */
    public final void setImage(ImageSource imageSource) {
        setImage(imageSource, null, null);
    }

    /**
     * Set the image source from a bitmap, resource, asset, file or other URI, starting with a given orientation
     * setting, scale and center. This is the best method to use when you want scale and center to be restored
     * after screen orientation change; it avoids any redundant loading of tiles in the wrong orientation.
     *
     * @param imageSource BImage source.
     * @param state       State to be restored. Nullable.
     */
    public final void setImage(ImageSource imageSource, ImageViewState state) {
        setImage(imageSource, null, state);
    }

    /**
     * Set the image source from a bitmap, resource, asset, file or other URI, providing a preview image to be
     * displayed until the full size image is loaded.
     * <p>
     * You must declare the dimensions of the full size image by calling {@link ImageSource#dimensions(int, int)}
     * on the imageSource object. The preview source will be ignored if you don't provide dimensions,
     * and if you provide a bitmap for the full size image.
     *
     * @param imageSource   BImage source. Dimensions must be declared.
     * @param previewSource Optional source for a preview image to be displayed and allow interaction while the full size image loads.
     */
    public final void setImage(ImageSource imageSource, ImageSource previewSource) {
        setImage(imageSource, previewSource, null);
    }

    /**
     * Set the image source from a bitmap, resource, asset, file or other URI, providing a preview image to be
     * displayed until the full size image is loaded, starting with a given orientation setting, scale and center.
     * This is the best method to use when you want scale and center to be restored after screen orientation change;
     * it avoids any redundant loading of tiles in the wrong orientation.
     * <p>
     * You must declare the dimensions of the full size image by calling {@link ImageSource#dimensions(int, int)}
     * on the imageSource object. The preview source will be ignored if you don't provide dimensions,
     * and if you provide a bitmap for the full size image.
     *
     * @param imageSource   BImage source. Dimensions must be declared.
     * @param previewSource Optional source for a preview image to be displayed and allow interaction while the full size image loads.
     * @param state         State to be restored. Nullable.
     */
    public final void setImage(ImageSource imageSource, ImageSource previewSource, ImageViewState state) {
        if (imageSource == null) {
            throw new NullPointerException("imageSource must not be null");
        }

        reset(true);
        if (state != null) {
            restoreState(state);
        }

        if (previewSource != null) {
            if (imageSource.getBitmap() != null) {
                throw new IllegalArgumentException("Preview image cannot be used when a bitmap is provided for the main image");
            }
            if (imageSource.getSWidth() <= 0 || imageSource.getSHeight() <= 0) {
                throw new IllegalArgumentException("Preview image cannot be used unless dimensions are provided for the main image");
            }
            this.mWidth = imageSource.getSWidth();
            this.mHeight = imageSource.getSHeight();
            this.mPRegion = previewSource.getSRegion();
            if (previewSource.getBitmap() != null) {
                this.isBitmapCached = previewSource.isCached();
                onPreviewLoaded(previewSource.getBitmap());
            } else {
                Uri uri = previewSource.getUri();
                if (uri == null && previewSource.getResource() != null) {
                    uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getContext().getPackageName() + "/" + previewSource.getResource());
                }
                BitmapLoadTask task = new BitmapLoadTask(this, getContext(), mBitmapDecoderFactory, uri, true);
                execute(task);
            }
        }

        if (imageSource.getBitmap() != null && imageSource.getSRegion() != null) {
            onImageLoaded(Bitmap.createBitmap(imageSource.getBitmap(), imageSource.getSRegion().left, imageSource.getSRegion().top, imageSource.getSRegion().width(), imageSource.getSRegion().height()), ORIENTATION_0, false);
        } else if (imageSource.getBitmap() != null) {
            onImageLoaded(imageSource.getBitmap(), ORIENTATION_0, imageSource.isCached());
        } else {
            mSRegion = imageSource.getSRegion();
            mUri = imageSource.getUri();
            if (mUri == null && imageSource.getResource() != null) {
                mUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getContext().getPackageName() + "/" + imageSource.getResource());
            }
            if (imageSource.getTile() || mSRegion != null) {
                // Load the bitmap using tile decoding.
                TilesInitTask task = new TilesInitTask(this, getContext(), mRegionDecoderFactory, mUri);
                execute(task);
            } else {
                // Load the bitmap as a single image.
                BitmapLoadTask task = new BitmapLoadTask(this, getContext(), mBitmapDecoderFactory, mUri, false);
                execute(task);
            }
        }
    }

    /**
     * Reset all state before setting/changing image or setting new rotation.
     */
    private void reset(boolean newImage) {
        debug("reset newImage=" + newImage);
        mScale = 0f;
        mScaleStart = 0f;
        mPfTranslate = null;
        mPfTranslateStart = null;
        mPfTranslateBefore = null;
        mPendingScale = 0f;
        mPendingCenter = null;
        mRequestedCenter = null;
        isZooming = false;
        isPanning = false;
        isQuickScaling = false;
        mMaxTouchCount = 0;
        mFullImageSampleSize = 0;
        mVCenterStart = null;
        mVDistStart = 0;
        mQuickScaleLastDistance = 0f;
        isQuickScaleMoved = false;
        mQuickScaleSCenterPoint = null;
        mQuickScaleVLastPoint = null;
        mQuickScaleVStartPoint = null;
        mAnim = null;
        mSatTemp = null;
        mMatrix = null;
        mSRect = null;
        if (newImage) {
            mUri = null;
            if (mImageRegionDecoder != null) {
                synchronized (mDecoderLock) {
                    mImageRegionDecoder.recycle();
                    mImageRegionDecoder = null;
                }
            }
            if (mBitmap != null && !isBitmapCached) {
                mBitmap.recycle();
            }
            if (mBitmap != null && isBitmapCached && mOnImageEventListener != null) {
                mOnImageEventListener.onPreviewReleased();
            }
            mWidth = 0;
            mHeight = 0;
            mSOrientation = 0;
            mSRegion = null;
            mPRegion = null;
            isReadySent = false;
            isImageLoadedSent = false;
            mBitmap = null;
            isBitmapPreview = false;
            isBitmapCached = false;
        }
        if (mTileMap != null) {
            for (Map.Entry<Integer, List<Tile>> tileMapEntry : mTileMap.entrySet()) {
                for (Tile tile : tileMapEntry.getValue()) {
                    tile.visible = false;
                    if (tile.bitmap != null) {
                        tile.bitmap.recycle();
                        tile.bitmap = null;
                    }
                }
            }
            mTileMap = null;
        }
        setGestureDetector(getContext());
    }

    private void setGestureDetector(final Context context) {
        this.mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (isPanEnabled && isReadySent && mPfTranslate != null && e1 != null && e2 != null && (Math.abs(e1.getX() - e2.getX()) > 50 || Math.abs(e1.getY() - e2.getY()) > 50) && (Math.abs(velocityX) > 500 || Math.abs(velocityY) > 500) && !isZooming) {
                    PointF vTranslateEnd = new PointF(mPfTranslate.x + (velocityX * 0.25f), mPfTranslate.y + (velocityY * 0.25f));
                    float sCenterXEnd = ((getWidth() / 2) - vTranslateEnd.x) / mScale;
                    float sCenterYEnd = ((getHeight() / 2) - vTranslateEnd.y) / mScale;
                    new AnimationBuilder(new PointF(sCenterXEnd, sCenterYEnd)).withEasing(EASE_OUT_QUAD).withPanLimited(false).withOrigin(ORIGIN_FLING).start();
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                performClick();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isZoomEnabled && isReadySent && mPfTranslate != null) {
                    // Hacky solution for #15 - after a double tap the GestureDetector gets in a state
                    // where the next fling is ignored, so here we replace it with a new one.
                    setGestureDetector(context);
                    if (isQuickScaleEnabled) {
                        // Store quick scale params. This will become either a double tap zoom or a
                        // quick scale depending on whether the user swipes.
                        mVCenterStart = new PointF(e.getX(), e.getY());
                        mPfTranslateStart = new PointF(mPfTranslate.x, mPfTranslate.y);
                        mScaleStart = mScale;
                        isQuickScaling = true;
                        isZooming = true;
                        mQuickScaleLastDistance = -1F;
                        mQuickScaleSCenterPoint = viewToSourceCoordinate(mVCenterStart);
                        mQuickScaleVStartPoint = new PointF(e.getX(), e.getY());
                        mQuickScaleVLastPoint = new PointF(mQuickScaleSCenterPoint.x, mQuickScaleSCenterPoint.y);
                        isQuickScaleMoved = false;
                        // We need to get events in onTouchEvent after this.
                        return false;
                    } else {
                        // Start double tap zoom animation.
                        doubleTapZoom(viewToSourceCoordinate(new PointF(e.getX(), e.getY())), new PointF(e.getX(), e.getY()));
                        return true;
                    }
                }
                return super.onDoubleTapEvent(e);
            }
        });
    }

    /**
     * On resize, preserve center and scale. Various behaviours are possible, override this method to use another.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        debug("onSizeChanged %dx%d -> %dx%d", oldw, oldh, w, h);
        PointF sCenter = getCenter();
        if (isReadySent && sCenter != null) {
            this.mAnim = null;
            this.mPendingScale = mScale;
            this.mPendingCenter = sCenter;
        }
    }

    /**
     * Measures the width and height of the view, preserving the aspect ratio of the image displayed if wrap_content is
     * used. The image will scale within this box, not resizing the view as it is zoomed.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        boolean resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
        boolean resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
        int width = parentWidth;
        int height = parentHeight;
        if (mWidth > 0 && mHeight > 0) {
            if (resizeWidth && resizeHeight) {
                width = sWidth();
                height = sHeight();
            } else if (resizeHeight) {
                height = (int) ((((double) sHeight() / (double) sWidth()) * width));
            } else if (resizeWidth) {
                width = (int) ((((double) sWidth() / (double) sHeight()) * height));
            }
        }
        width = Math.max(width, getSuggestedMinimumWidth());
        height = Math.max(height, getSuggestedMinimumHeight());
        setMeasuredDimension(width, height);
    }

    /**
     * Handle touch events. One finger pans, and two finger pinch and zoom plus panning.
     */
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        // During non-interruptible anims, ignore all touch events
        if (mAnim != null && !mAnim.interruptible) {
            requestDisallowInterceptTouchEvent(true);
            return true;
        } else {
            if (mAnim != null && mAnim.listener != null) {
                try {
                    mAnim.listener.onInterruptedByUser();
                } catch (Exception e) {
                    Log.w(TAG, "Error thrown by animation listener", e);
                }
            }
            mAnim = null;
        }

        // Abort if not ready
        if (mPfTranslate == null) {
            return true;
        }
        // Detect flings, taps and double taps
        if (!isQuickScaling && (mGestureDetector == null || mGestureDetector.onTouchEvent(event))) {
            isZooming = false;
            isPanning = false;
            mMaxTouchCount = 0;
            return true;
        }

        if (mPfTranslateStart == null) {
            mPfTranslateStart = new PointF(0, 0);
        }
        if (mPfTranslateBefore == null) {
            mPfTranslateBefore = new PointF(0, 0);
        }
        if (mVCenterStart == null) {
            mVCenterStart = new PointF(0, 0);
        }

        // Store current values so we can send an event if they change
        float scaleBefore = mScale;
        mPfTranslateBefore.set(mPfTranslate);

        boolean handled = onTouchEventInternal(event);
        sendStateChanged(scaleBefore, mPfTranslateBefore, ORIGIN_TOUCH);
        return handled || super.onTouchEvent(event);
    }

    @SuppressWarnings("deprecation")
    private boolean onTouchEventInternal(@NonNull MotionEvent event) {
        int touchCount = event.getPointerCount();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_1_DOWN:
            case MotionEvent.ACTION_POINTER_2_DOWN:
                mAnim = null;
                requestDisallowInterceptTouchEvent(true);
                mMaxTouchCount = Math.max(mMaxTouchCount, touchCount);
                if (touchCount >= 2) {
                    if (isZoomEnabled) {
                        // Start pinch to zoom. Calculate distance between touch points and center point of the pinch.
                        float distance = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
                        mScaleStart = mScale;
                        mVDistStart = distance;
                        mPfTranslateStart.set(mPfTranslate.x, mPfTranslate.y);
                        mVCenterStart.set((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2);
                    } else {
                        // Abort all gestures on second touch
                        mMaxTouchCount = 0;
                    }
                    // Cancel long click timer
                    mHandler.removeMessages(MESSAGE_LONG_CLICK);
                } else if (!isQuickScaling) {
                    // Start one-finger pan
                    mPfTranslateStart.set(mPfTranslate.x, mPfTranslate.y);
                    mVCenterStart.set(event.getX(), event.getY());

                    // Start long click timer
                    mHandler.sendEmptyMessageDelayed(MESSAGE_LONG_CLICK, 600);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                boolean consumed = false;
                if (mMaxTouchCount > 0) {
                    if (touchCount >= 2) {
                        // Calculate new distance between touch points, to scale and pan relative to start values.
                        float vDistEnd = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
                        float vCenterEndX = (event.getX(0) + event.getX(1)) / 2;
                        float vCenterEndY = (event.getY(0) + event.getY(1)) / 2;

                        if (isZoomEnabled && (distance(mVCenterStart.x, vCenterEndX, mVCenterStart.y, vCenterEndY) > 5 || Math.abs(vDistEnd - mVDistStart) > 5 || isPanning)) {
                            isZooming = true;
                            isPanning = true;
                            consumed = true;

                            double previousScale = mScale;
                            mScale = Math.min(mMaxScale, (vDistEnd / mVDistStart) * mScaleStart);

                            if (mScale <= minScale()) {
                                // Minimum scale reached so don't pan. Adjust start settings so any expand will zoom in.
                                mVDistStart = vDistEnd;
                                mScaleStart = minScale();
                                mVCenterStart.set(vCenterEndX, vCenterEndY);
                                mPfTranslateStart.set(mPfTranslate);
                            } else if (isPanEnabled) {
                                // Translate to place the source image coordinate that was at the center of the pinch at the start
                                // at the center of the pinch now, to give simultaneous pan + zoom.
                                float vLeftStart = mVCenterStart.x - mPfTranslateStart.x;
                                float vTopStart = mVCenterStart.y - mPfTranslateStart.y;
                                float vLeftNow = vLeftStart * (mScale / mScaleStart);
                                float vTopNow = vTopStart * (mScale / mScaleStart);
                                mPfTranslate.x = vCenterEndX - vLeftNow;
                                mPfTranslate.y = vCenterEndY - vTopNow;
                                if ((previousScale * sHeight() < getHeight() && mScale * sHeight() >= getHeight()) || (previousScale * sWidth() < getWidth() && mScale * sWidth() >= getWidth())) {
                                    fitToBounds(true);
                                    mVCenterStart.set(vCenterEndX, vCenterEndY);
                                    mPfTranslateStart.set(mPfTranslate);
                                    mScaleStart = mScale;
                                    mVDistStart = vDistEnd;
                                }
                            } else if (mRequestedCenter != null) {
                                // With a center specified from code, zoom around that point.
                                mPfTranslate.x = (getWidth() / 2) - (mScale * mRequestedCenter.x);
                                mPfTranslate.y = (getHeight() / 2) - (mScale * mRequestedCenter.y);
                            } else {
                                // With no requested center, scale around the image center.
                                mPfTranslate.x = (getWidth() / 2) - (mScale * (sWidth() / 2));
                                mPfTranslate.y = (getHeight() / 2) - (mScale * (sHeight() / 2));
                            }

                            fitToBounds(true);
                            refreshRequiredTiles(false);
                        }
                    } else if (isQuickScaling) {
                        // One finger zoom
                        // Stole Google's Magical Formula™ to make sure it feels the exact same
                        float dist = Math.abs(mQuickScaleVStartPoint.y - event.getY()) * 2 + mQuickScaleThreshold;

                        if (mQuickScaleLastDistance == -1f) {
                            mQuickScaleLastDistance = dist;
                        }
                        boolean isUpwards = event.getY() > mQuickScaleVLastPoint.y;
                        mQuickScaleVLastPoint.set(0, event.getY());

                        float spanDiff = Math.abs(1 - (dist / mQuickScaleLastDistance)) * 0.5f;

                        if (spanDiff > 0.03f || isQuickScaleMoved) {
                            isQuickScaleMoved = true;

                            float multiplier = 1;
                            if (mQuickScaleLastDistance > 0) {
                                multiplier = isUpwards ? (1 + spanDiff) : (1 - spanDiff);
                            }

                            double previousScale = mScale;
                            mScale = Math.max(minScale(), Math.min(mMaxScale, mScale * multiplier));

                            if (isPanEnabled) {
                                float vLeftStart = mVCenterStart.x - mPfTranslateStart.x;
                                float vTopStart = mVCenterStart.y - mPfTranslateStart.y;
                                float vLeftNow = vLeftStart * (mScale / mScaleStart);
                                float vTopNow = vTopStart * (mScale / mScaleStart);
                                mPfTranslate.x = mVCenterStart.x - vLeftNow;
                                mPfTranslate.y = mVCenterStart.y - vTopNow;
                                if ((previousScale * sHeight() < getHeight() && mScale * sHeight() >= getHeight()) || (previousScale * sWidth() < getWidth() && mScale * sWidth() >= getWidth())) {
                                    fitToBounds(true);
                                    mVCenterStart.set(sourceToViewCoordinate(mQuickScaleSCenterPoint));
                                    mPfTranslateStart.set(mPfTranslate);
                                    mScaleStart = mScale;
                                    dist = 0;
                                }
                            } else if (mRequestedCenter != null) {
                                // With a center specified from code, zoom around that point.
                                mPfTranslate.x = (getWidth() / 2) - (mScale * mRequestedCenter.x);
                                mPfTranslate.y = (getHeight() / 2) - (mScale * mRequestedCenter.y);
                            } else {
                                // With no requested center, scale around the image center.
                                mPfTranslate.x = (getWidth() / 2) - (mScale * (sWidth() / 2));
                                mPfTranslate.y = (getHeight() / 2) - (mScale * (sHeight() / 2));
                            }
                        }

                        mQuickScaleLastDistance = dist;

                        fitToBounds(true);
                        refreshRequiredTiles(false);

                        consumed = true;
                    } else if (!isZooming) {
                        // One finger pan - translate the image. We do this calculation even with pan disabled so click
                        // and long click behaviour is preserved.
                        float dx = Math.abs(event.getX() - mVCenterStart.x);
                        float dy = Math.abs(event.getY() - mVCenterStart.y);

                        //On the Samsung S6 long click event does not work, because the dx > 5 usually true
                        float offset = mDensity * 5;
                        if (dx > offset || dy > offset || isPanning) {
                            consumed = true;
                            mPfTranslate.x = mPfTranslateStart.x + (event.getX() - mVCenterStart.x);
                            mPfTranslate.y = mPfTranslateStart.y + (event.getY() - mVCenterStart.y);

                            float lastX = mPfTranslate.x;
                            float lastY = mPfTranslate.y;
                            fitToBounds(true);
                            boolean atXEdge = lastX != mPfTranslate.x;
                            boolean edgeXSwipe = atXEdge && dx > dy && !isPanning;
                            boolean yPan = lastY == mPfTranslate.y && dy > offset * 3;
                            if (!edgeXSwipe && (!atXEdge || yPan || isPanning)) {
                                isPanning = true;
                            } else if (dx > offset) {
                                // Haven't panned the image, and we're at the left or right edge. Switch to page swipe.
                                mMaxTouchCount = 0;
                                mHandler.removeMessages(MESSAGE_LONG_CLICK);
                                requestDisallowInterceptTouchEvent(false);
                            }

                            if (!isPanEnabled) {
                                mPfTranslate.x = mPfTranslateStart.x;
                                mPfTranslate.y = mPfTranslateStart.y;
                                requestDisallowInterceptTouchEvent(false);
                            }

                            refreshRequiredTiles(false);
                        }
                    }
                }
                if (consumed) {
                    mHandler.removeMessages(MESSAGE_LONG_CLICK);
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_POINTER_2_UP:
                mHandler.removeMessages(MESSAGE_LONG_CLICK);
                if (isQuickScaling) {
                    isQuickScaling = false;
//                    if (!isQuickScaleMoved) {
//                        doubleTapZoom(mQuickScaleSCenterPoint, mVCenterStart);
//                    }
                }
                if (mMaxTouchCount > 0 && (isZooming || isPanning)) {

                    if (isZooming && touchCount == 2) {
                        // Convert from zoom to pan with remaining touch
                        isPanning = true;
                        mPfTranslateStart.set(mPfTranslate.x, mPfTranslate.y);
                        if (event.getActionIndex() == 1) {
                            mVCenterStart.set(event.getX(0), event.getY(0));
                        } else {
                            mVCenterStart.set(event.getX(1), event.getY(1));
                        }
                    }
                    if (touchCount < 3) {
                        // End zooming when only one touch point
                        isZooming = false;
                    }
                    if (touchCount < 2) {
                        // End panning when no touch points
                        isPanning = false;
                        mMaxTouchCount = 0;
                    }
                    // Trigger load of tiles now required
                    refreshRequiredTiles(true);
                    return true;
                }
                if (touchCount == 1) {
                    isZooming = false;
                    isPanning = false;
                    mMaxTouchCount = 0;
                }
                return true;
        }
        return false;
    }

    private void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    /**
     * Double tap zoom handler triggered from gesture detector or on touch, depending on whether
     * quick scale is enabled.
     */
    private void doubleTapZoom(PointF sCenter, PointF vFocus) {
        if (!isPanEnabled) {
            if (mRequestedCenter != null) {
                // With a center specified from code, zoom around that point.
                sCenter.x = mRequestedCenter.x;
                sCenter.y = mRequestedCenter.y;
            } else {
                // With no requested center, scale around the image center.
                sCenter.x = sWidth() / 2;
                sCenter.y = sHeight() / 2;
            }
        }
        float doubleTapZoomScale = Math.min(mMaxScale, SubSamplingScaleImageView.this.mDoubleTapZoomScale);
        boolean zoomIn = mScale <= doubleTapZoomScale * 0.9;
        float targetScale = zoomIn ? doubleTapZoomScale : minScale();
        if (mDoubleTapZoomStyle == ZOOM_FOCUS_CENTER_IMMEDIATE) {
            setScaleAndCenter(targetScale, sCenter);
        } else if (mDoubleTapZoomStyle == ZOOM_FOCUS_CENTER || !zoomIn || !isPanEnabled) {
            new AnimationBuilder(targetScale, sCenter).withInterruptible(false).withDuration(mDoubleTapZoomDuration).withOrigin(ORIGIN_DOUBLE_TAP_ZOOM).start();
        } else if (mDoubleTapZoomStyle == ZOOM_FOCUS_FIXED) {
            new AnimationBuilder(targetScale, sCenter, vFocus).withInterruptible(false).withDuration(mDoubleTapZoomDuration).withOrigin(ORIGIN_DOUBLE_TAP_ZOOM).start();
        }
        invalidate();
    }

    private boolean isFirstDraw = true;

    /**
     * Draw method should not be called until the view has dimensions so the first calls are used as triggers to calculate
     * the scaling and tiling required. Once the view is setup, tiles are displayed as they are loaded.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        createPaints();

        // If image or view dimensions are not known yet, abort.
        if (mWidth == 0 || mHeight == 0 || getWidth() == 0 || getHeight() == 0) {
            return;
        }

        // When using tiles, on first render with no tile map ready, initialise it and kick off async base image loading.
        if (mTileMap == null && mImageRegionDecoder != null) {
            initialiseBaseLayer(getMaxBitmapDimensions(canvas));
        }

        // If image has been loaded or supplied as a bitmap, onDraw may be the first time the view has
        // dimensions and therefore the first opportunity to set scale and translate. If this call returns
        // false there is nothing to be drawn so return immediately.
        if (!checkReady()) {
            return;
        }

        // Set scale and translate before draw.
        preDraw();

        // If animating scale, calculate current scale and center with easing equations
        if (mAnim != null) {
            // Store current values so we can send an event if they change
            float scaleBefore = mScale;
            if (mPfTranslateBefore == null) {
                mPfTranslateBefore = new PointF(0, 0);
            }
            mPfTranslateBefore.set(mPfTranslate);

            long scaleElapsed = System.currentTimeMillis() - mAnim.time;
            boolean finished = scaleElapsed > mAnim.duration;
            scaleElapsed = Math.min(scaleElapsed, mAnim.duration);
            mScale = ease(mAnim.easing, scaleElapsed, mAnim.scaleStart, mAnim.scaleEnd - mAnim.scaleStart, mAnim.duration);

            // Apply required animation to the focal point
            float vFocusNowX = ease(mAnim.easing, scaleElapsed, mAnim.vFocusStart.x, mAnim.vFocusEnd.x - mAnim.vFocusStart.x, mAnim.duration);
            float vFocusNowY = ease(mAnim.easing, scaleElapsed, mAnim.vFocusStart.y, mAnim.vFocusEnd.y - mAnim.vFocusStart.y, mAnim.duration);
            // Find out where the focal point is at this scale and adjust its position to follow the animation path
            mPfTranslate.x -= sourceToViewX(mAnim.sCenterEnd.x) - vFocusNowX;
            mPfTranslate.y -= sourceToViewY(mAnim.sCenterEnd.y) - vFocusNowY;

            // For translate anims, showing the image non-centered is never allowed, for scaling anims it is during the animation.
            fitToBounds(finished || (mAnim.scaleStart == mAnim.scaleEnd));
            sendStateChanged(scaleBefore, mPfTranslateBefore, mAnim.origin);
            refreshRequiredTiles(finished);
            if (finished) {
                if (mAnim.listener != null) {
                    try {
                        mAnim.listener.onComplete();
                    } catch (Exception e) {
                        Log.w(TAG, "Error thrown by animation listener", e);
                    }
                }
                mAnim = null;
            }
            invalidate();
        }

        if (mTileMap != null && isBaseLayerReady()) {

            // Optimum sample size for current scale
            int sampleSize = Math.min(mFullImageSampleSize, calculateInSampleSize(mScale));

            // First check for missing tiles - if there are any we need the base layer underneath to avoid gaps
            boolean hasMissingTiles = false;
            for (Map.Entry<Integer, List<Tile>> tileMapEntry : mTileMap.entrySet()) {
                if (tileMapEntry.getKey() == sampleSize) {
                    for (Tile tile : tileMapEntry.getValue()) {
                        if (tile.visible && (tile.loading || tile.bitmap == null)) {
                            hasMissingTiles = true;
                        }
                    }
                }
            }

            // Render all loaded tiles. LinkedHashMap used for bottom up rendering - lower res tiles underneath.
            for (Map.Entry<Integer, List<Tile>> tileMapEntry : mTileMap.entrySet()) {
                if (tileMapEntry.getKey() == sampleSize || hasMissingTiles) {
                    for (Tile tile : tileMapEntry.getValue()) {
                        sourceToViewRect(tile.sRect, tile.vRect);
                        if (!tile.loading && tile.bitmap != null) {
                            if (mTileBgPaint != null) {
                                canvas.drawRect(tile.vRect, mTileBgPaint);
                            }
                            if (mMatrix == null) {
                                mMatrix = new Matrix();
                            }
                            mMatrix.reset();
                            setMatrixArray(mSrcArray, 0, 0, tile.bitmap.getWidth(), 0, tile.bitmap.getWidth(), tile.bitmap.getHeight(), 0, tile.bitmap.getHeight());
                            if (getRequiredRotation() == ORIENTATION_0) {
                                setMatrixArray(mDstArray, tile.vRect.left, tile.vRect.top, tile.vRect.right, tile.vRect.top, tile.vRect.right, tile.vRect.bottom, tile.vRect.left, tile.vRect.bottom);
                            } else if (getRequiredRotation() == ORIENTATION_90) {
                                setMatrixArray(mDstArray, tile.vRect.right, tile.vRect.top, tile.vRect.right, tile.vRect.bottom, tile.vRect.left, tile.vRect.bottom, tile.vRect.left, tile.vRect.top);
                            } else if (getRequiredRotation() == ORIENTATION_180) {
                                setMatrixArray(mDstArray, tile.vRect.right, tile.vRect.bottom, tile.vRect.left, tile.vRect.bottom, tile.vRect.left, tile.vRect.top, tile.vRect.right, tile.vRect.top);
                            } else if (getRequiredRotation() == ORIENTATION_270) {
                                setMatrixArray(mDstArray, tile.vRect.left, tile.vRect.bottom, tile.vRect.left, tile.vRect.top, tile.vRect.right, tile.vRect.top, tile.vRect.right, tile.vRect.bottom);
                            }
                            mMatrix.setPolyToPoly(mSrcArray, 0, mDstArray, 0, 4);
                            canvas.drawBitmap(tile.bitmap, mMatrix, mBitmapPaint);
                            if (isDebug) {
                                canvas.drawRect(tile.vRect, mDebugPaint);
                            }
                        } else if (tile.loading && isDebug) {
                            canvas.drawText("LOADING", tile.vRect.left + 5, tile.vRect.top + 35, mDebugPaint);
                        }
                        if (tile.visible && isDebug) {
                            canvas.drawText("ISS " + tile.sampleSize + " RECT " + tile.sRect.top + "," + tile.sRect.left + "," + tile.sRect.bottom + "," + tile.sRect.right, tile.vRect.left + 5, tile.vRect.top + 15, mDebugPaint);
                        }
                    }
                }
            }

        } else if (mBitmap != null) {

            float xScale = mScale, yScale = mScale;
            if (isBitmapPreview) {
                xScale = mScale * ((float) mWidth / mBitmap.getWidth());
                yScale = mScale * ((float) mHeight / mBitmap.getHeight());
            }

            if (mMatrix == null) {
                mMatrix = new Matrix();
            }
            mMatrix.reset();
            mMatrix.postScale(xScale, yScale);
            mMatrix.postRotate(getRequiredRotation());
            if (isFirstDraw && mBitmap.getHeight() * mScale > mImageHeight) {
                mPfTranslate.y = 0;
            }
            isFirstDraw = false;
            mMatrix.postTranslate(mPfTranslate.x, mPfTranslate.y);
            if (getRequiredRotation() == ORIENTATION_180) {
                mMatrix.postTranslate(mScale * mWidth, mScale * mHeight);
            } else if (getRequiredRotation() == ORIENTATION_90) {
                mMatrix.postTranslate(mScale * mHeight, 0);
            } else if (getRequiredRotation() == ORIENTATION_270) {
                mMatrix.postTranslate(0, mScale * mWidth);
            }

            if (mTileBgPaint != null) {
                if (mSRect == null) {
                    mSRect = new RectF();
                }
                mSRect.set(0f, 0f, isBitmapPreview ? mBitmap.getWidth() : mWidth, isBitmapPreview ? mBitmap.getHeight() : mHeight);
                mMatrix.mapRect(mSRect);
                canvas.drawRect(mSRect, mTileBgPaint);
            }
            canvas.drawBitmap(mBitmap, mMatrix, mBitmapPaint);

        }

        if (isDebug) {
            canvas.drawText("Scale: " + String.format(Locale.ENGLISH, "%.2f", mScale), 5, 15, mDebugPaint);
            canvas.drawText("Translate: " + String.format(Locale.ENGLISH, "%.2f", mPfTranslate.x) + ":" + String.format(Locale.ENGLISH, "%.2f", mPfTranslate.y), 5, 35, mDebugPaint);
            PointF center = getCenter();
            canvas.drawText("Source center: " + String.format(Locale.ENGLISH, "%.2f", center.x) + ":" + String.format(Locale.ENGLISH, "%.2f", center.y), 5, 55, mDebugPaint);
            mDebugPaint.setStrokeWidth(2f);
            if (mAnim != null) {
                PointF vCenterStart = sourceToViewCoordinate(mAnim.sCenterStart);
                PointF vCenterEndRequested = sourceToViewCoordinate(mAnim.sCenterEndRequested);
                PointF vCenterEnd = sourceToViewCoordinate(mAnim.sCenterEnd);
                canvas.drawCircle(vCenterStart.x, vCenterStart.y, 10, mDebugPaint);
                mDebugPaint.setColor(Color.RED);
                canvas.drawCircle(vCenterEndRequested.x, vCenterEndRequested.y, 20, mDebugPaint);
                mDebugPaint.setColor(Color.BLUE);
                canvas.drawCircle(vCenterEnd.x, vCenterEnd.y, 25, mDebugPaint);
                mDebugPaint.setColor(Color.CYAN);
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, 30, mDebugPaint);
            }
            if (mVCenterStart != null) {
                mDebugPaint.setColor(Color.RED);
                canvas.drawCircle(mVCenterStart.x, mVCenterStart.y, 20, mDebugPaint);
            }
            if (mQuickScaleSCenterPoint != null) {
                mDebugPaint.setColor(Color.BLUE);
                canvas.drawCircle(sourceToViewX(mQuickScaleSCenterPoint.x), sourceToViewY(mQuickScaleSCenterPoint.y), 35, mDebugPaint);
            }
            if (mQuickScaleVStartPoint != null) {
                mDebugPaint.setColor(Color.CYAN);
                canvas.drawCircle(mQuickScaleVStartPoint.x, mQuickScaleVStartPoint.y, 30, mDebugPaint);
            }
            mDebugPaint.setColor(Color.MAGENTA);
            mDebugPaint.setStrokeWidth(1f);
        }
    }

    /**
     * Helper method for setting the values of a tile matrix array.
     */
    private void setMatrixArray(float[] array, float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7) {
        array[0] = f0;
        array[1] = f1;
        array[2] = f2;
        array[3] = f3;
        array[4] = f4;
        array[5] = f5;
        array[6] = f6;
        array[7] = f7;
    }

    /**
     * Checks whether the base layer of tiles or full size bitmap is ready.
     */
    private boolean isBaseLayerReady() {
        if (mBitmap != null && !isBitmapPreview) {
            return true;
        } else if (mTileMap != null) {
            boolean baseLayerReady = true;
            for (Map.Entry<Integer, List<Tile>> tileMapEntry : mTileMap.entrySet()) {
                if (tileMapEntry.getKey() == mFullImageSampleSize) {
                    for (Tile tile : tileMapEntry.getValue()) {
                        if (tile.loading || tile.bitmap == null) {
                            baseLayerReady = false;
                        }
                    }
                }
            }
            return baseLayerReady;
        }
        return false;
    }

    /**
     * Check whether view and image dimensions are known and either a preview, full size image or
     * base layer tiles are loaded. First time, send ready event to listener. The next draw will
     * display an image.
     */
    private boolean checkReady() {
        boolean ready = getWidth() > 0 && getHeight() > 0 && mWidth > 0 && mHeight > 0 && (mBitmap != null || isBaseLayerReady());
        if (!isReadySent && ready) {
            preDraw();
            isReadySent = true;
            onReady();
            if (mOnImageEventListener != null) {
                mOnImageEventListener.onReady();
            }
        }
        return ready;
    }

    /**
     * Check whether either the full size bitmap or base layer tiles are loaded. First time, send image
     * loaded event to listener.
     */
    private boolean checkImageLoaded() {
        boolean imageLoaded = isBaseLayerReady();
        if (!isImageLoadedSent && imageLoaded) {
            preDraw();
            isImageLoadedSent = true;
            onImageLoaded();
            if (mOnImageEventListener != null) {
                mOnImageEventListener.onImageLoaded();
            }
        }
        return imageLoaded;
    }

    /**
     * Creates Paint objects once when first needed.
     */
    private void createPaints() {
        if (mBitmapPaint == null) {
            mBitmapPaint = new Paint();
            mBitmapPaint.setAntiAlias(true);
            mBitmapPaint.setFilterBitmap(true);
            mBitmapPaint.setDither(true);
        }
        if (mDebugPaint == null && isDebug) {
            mDebugPaint = new Paint();
            mDebugPaint.setTextSize(18);
            mDebugPaint.setColor(Color.MAGENTA);
            mDebugPaint.setStyle(Style.STROKE);
        }
    }

    /**
     * Called on first draw when the view has dimensions. Calculates the initial sample size and starts async loading of
     * the base layer image - the whole source subsampled as necessary.
     */
    private synchronized void initialiseBaseLayer(Point maxTileDimensions) {
        debug("initialiseBaseLayer maxTileDimensions=%dx%d", maxTileDimensions.x, maxTileDimensions.y);

        mSatTemp = new ScaleAndTranslate(0f, new PointF(0, 0));
        fitToBounds(true, mSatTemp);

        // Load double resolution - next level will be split into four tiles and at the center all four are required,
        // so don't bother with tiling until the next level 16 tiles are needed.
        mFullImageSampleSize = calculateInSampleSize(mSatTemp.scale);
        if (mFullImageSampleSize > 1) {
            mFullImageSampleSize /= 2;
        }

        if (mFullImageSampleSize == 1 && mSRegion == null && sWidth() < maxTileDimensions.x && sHeight() < maxTileDimensions.y) {

            // Whole image is required at native resolution, and is smaller than the canvas max bitmap size.
            // Use BitmapDecoder for better image support.
            mImageRegionDecoder.recycle();
            mImageRegionDecoder = null;
            BitmapLoadTask task = new BitmapLoadTask(this, getContext(), mBitmapDecoderFactory, mUri, false);
            execute(task);

        } else {

            initialiseTileMap(maxTileDimensions);

            List<Tile> baseGrid = mTileMap.get(mFullImageSampleSize);
            for (Tile baseTile : baseGrid) {
                TileLoadTask task = new TileLoadTask(this, mImageRegionDecoder, baseTile);
                execute(task);
            }
            refreshRequiredTiles(true);

        }

    }

    /**
     * Loads the optimum tiles for display at the current scale and translate, so the screen can be filled with tiles
     * that are at least as high resolution as the screen. Frees up bitmaps that are now off the screen.
     *
     * @param load Whether to load the new tiles needed. Use false while scrolling/panning for performance.
     */
    private void refreshRequiredTiles(boolean load) {
        if (mImageRegionDecoder == null || mTileMap == null) {
            return;
        }

        int sampleSize = Math.min(mFullImageSampleSize, calculateInSampleSize(mScale));

        // Load tiles of the correct sample size that are on screen. Discard tiles off screen, and those that are higher
        // resolution than required, or lower res than required but not the base layer, so the base layer is always present.
        for (Map.Entry<Integer, List<Tile>> tileMapEntry : mTileMap.entrySet()) {
            for (Tile tile : tileMapEntry.getValue()) {
                if (tile.sampleSize < sampleSize || (tile.sampleSize > sampleSize && tile.sampleSize != mFullImageSampleSize)) {
                    tile.visible = false;
                    if (tile.bitmap != null) {
                        tile.bitmap.recycle();
                        tile.bitmap = null;
                    }
                }
                if (tile.sampleSize == sampleSize) {
                    if (tileVisible(tile)) {
                        tile.visible = true;
                        if (!tile.loading && tile.bitmap == null && load) {
                            TileLoadTask task = new TileLoadTask(this, mImageRegionDecoder, tile);
                            execute(task);
                        }
                    } else if (tile.sampleSize != mFullImageSampleSize) {
                        tile.visible = false;
                        if (tile.bitmap != null) {
                            tile.bitmap.recycle();
                            tile.bitmap = null;
                        }
                    }
                } else if (tile.sampleSize == mFullImageSampleSize) {
                    tile.visible = true;
                }
            }
        }

    }

    /**
     * Determine whether tile is visible.
     */
    private boolean tileVisible(Tile tile) {
        float sVisLeft = viewToSourceX(0),
                sVisRight = viewToSourceX(getWidth()),
                sVisTop = viewToSourceY(0),
                sVisBottom = viewToSourceY(getHeight());
        return !(sVisLeft > tile.sRect.right || tile.sRect.left > sVisRight || sVisTop > tile.sRect.bottom || tile.sRect.top > sVisBottom);
    }

    /**
     * Sets scale and translate ready for the next draw.
     */
    private void preDraw() {
        if (getWidth() == 0 || getHeight() == 0 || mWidth <= 0 || mHeight <= 0) {
            return;
        }

        // If waiting to translate to new center position, set translate now
        if (mPendingCenter != null && mPendingScale != null) {
            mScale = mPendingScale;
            if (mPfTranslate == null) {
                mPfTranslate = new PointF();
            }
            mPfTranslate.x = (getWidth() / 2) - (mScale * mPendingCenter.x);
            mPfTranslate.y = (getHeight() / 2) - (mScale * mPendingCenter.y);
            mPendingCenter = null;
            mPendingScale = null;
            fitToBounds(true);
            refreshRequiredTiles(true);
        }

        // On first display of base image set up position, and in other cases make sure scale is correct.
        fitToBounds(false);
    }

    /**
     * Calculates sample size to fit the source image in given bounds.
     */
    private int calculateInSampleSize(float scale) {
        if (mMinimumTileDpi > 0) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float averageDpi = (metrics.xdpi + metrics.ydpi) / 2;
            scale = (mMinimumTileDpi / averageDpi) * scale;
        }

        int reqWidth = (int) (sWidth() * scale);
        int reqHeight = (int) (sHeight() * scale);

        // Raw height and width of image
        int inSampleSize = 1;
        if (reqWidth == 0 || reqHeight == 0) {
            return 32;
        }

        if (sHeight() > reqHeight || sWidth() > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) sHeight() / (float) reqHeight);
            final int widthRatio = Math.round((float) sWidth() / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        // We want the actual sample size that will be used, so round down to nearest power of 2.
        int power = 1;
        while (power * 2 < inSampleSize) {
            power = power * 2;
        }

        return power;
    }

    /**
     * Adjusts hypothetical future scale and translate values to keep scale within the allowed range and the image on screen. Minimum scale
     * is set so one dimension fills the view and the image is centered on the other dimension. Used to calculate what the target of an
     * animation should be.
     *
     * @param center Whether the image should be centered in the dimension it's too small to fill. While animating this can be false to avoid changes in direction as bounds are reached.
     * @param sat    The scale we want and the translation we're aiming for. The values are adjusted to be valid.
     */
    private void fitToBounds(boolean center, ScaleAndTranslate sat) {
        if (mPanLimit == PAN_LIMIT_OUTSIDE && isReady()) {
            center = false;
        }

        PointF vTranslate = sat.vTranslate;
        float scale = limitedScale(sat.scale);
        float scaleWidth = scale * sWidth();
        float scaleHeight = scale * sHeight();

        if (mPanLimit == PAN_LIMIT_CENTER && isReady()) {
            vTranslate.x = Math.max(vTranslate.x, getWidth() / 2 - scaleWidth);
            vTranslate.y = Math.max(vTranslate.y, getHeight() / 2 - scaleHeight);
        } else if (center) {
            vTranslate.x = Math.max(vTranslate.x, getWidth() - scaleWidth);
            vTranslate.y = Math.max(vTranslate.y, getHeight() - scaleHeight);
        } else {
            vTranslate.x = Math.max(vTranslate.x, -scaleWidth);
            vTranslate.y = Math.max(vTranslate.y, -scaleHeight);
        }

        // Asymmetric padding adjustments
        float xPaddingRatio = getPaddingLeft() > 0 || getPaddingRight() > 0 ? getPaddingLeft() / (float) (getPaddingLeft() + getPaddingRight()) : 0.5f;
        float yPaddingRatio = getPaddingTop() > 0 || getPaddingBottom() > 0 ? getPaddingTop() / (float) (getPaddingTop() + getPaddingBottom()) : 0.5f;

        float maxTx;
        float maxTy;
        if (mPanLimit == PAN_LIMIT_CENTER && isReady()) {
            maxTx = Math.max(0, getWidth() / 2);
            maxTy = Math.max(0, getHeight() / 2);
        } else if (center) {
            maxTx = Math.max(0, (getWidth() - scaleWidth) * xPaddingRatio);
            maxTy = Math.max(0, (getHeight() - scaleHeight) * yPaddingRatio);
        } else {
            maxTx = Math.max(0, getWidth());
            maxTy = Math.max(0, getHeight());
        }

        vTranslate.x = Math.min(vTranslate.x, maxTx);
        vTranslate.y = Math.min(vTranslate.y, maxTy);

        sat.scale = scale;
    }

    /**
     * Adjusts current scale and translate values to keep scale within the allowed range and the image on screen. Minimum scale
     * is set so one dimension fills the view and the image is centered on the other dimension.
     *
     * @param center Whether the image should be centered in the dimension it's too small to fill. While animating this can be false to avoid changes in direction as bounds are reached.
     */
    private void fitToBounds(boolean center) {
        boolean init = false;
        if (mPfTranslate == null) {
            init = true;
            mPfTranslate = new PointF(0, 0);
        }
        if (mSatTemp == null) {
            mSatTemp = new ScaleAndTranslate(0, new PointF(0, 0));
        }
        mSatTemp.scale = mScale;
        mSatTemp.vTranslate.set(mPfTranslate);
        fitToBounds(center, mSatTemp);
        mScale = mSatTemp.scale;
        mPfTranslate.set(mSatTemp.vTranslate);
        if (init) {
            mPfTranslate.set(vTranslateForSCenter(sWidth() / 2, sHeight() / 2, mScale));
        }
    }

    /**
     * Once source image and view dimensions are known, creates a map of sample size to tile grid.
     */
    private void initialiseTileMap(Point maxTileDimensions) {
        debug("initialiseTileMap maxTileDimensions=%dx%d", maxTileDimensions.x, maxTileDimensions.y);
        this.mTileMap = new LinkedHashMap<>();
        int sampleSize = mFullImageSampleSize;
        int xTiles = 1;
        int yTiles = 1;
        while (true) {
            int sTileWidth = sWidth() / xTiles;
            int sTileHeight = sHeight() / yTiles;
            int subTileWidth = sTileWidth / sampleSize;
            int subTileHeight = sTileHeight / sampleSize;
            while (subTileWidth + xTiles + 1 > maxTileDimensions.x || (subTileWidth > getWidth() * 1.25 && sampleSize < mFullImageSampleSize)) {
                xTiles += 1;
                sTileWidth = sWidth() / xTiles;
                subTileWidth = sTileWidth / sampleSize;
            }
            while (subTileHeight + yTiles + 1 > maxTileDimensions.y || (subTileHeight > getHeight() * 1.25 && sampleSize < mFullImageSampleSize)) {
                yTiles += 1;
                sTileHeight = sHeight() / yTiles;
                subTileHeight = sTileHeight / sampleSize;
            }
            List<Tile> tileGrid = new ArrayList<>(xTiles * yTiles);
            for (int x = 0; x < xTiles; x++) {
                for (int y = 0; y < yTiles; y++) {
                    Tile tile = new Tile();
                    tile.sampleSize = sampleSize;
                    tile.visible = sampleSize == mFullImageSampleSize;
                    tile.sRect = new Rect(
                            x * sTileWidth,
                            y * sTileHeight,
                            x == xTiles - 1 ? sWidth() : (x + 1) * sTileWidth,
                            y == yTiles - 1 ? sHeight() : (y + 1) * sTileHeight
                    );
                    tile.vRect = new Rect(0, 0, 0, 0);
                    tile.fileSRect = new Rect(tile.sRect);
                    tileGrid.add(tile);
                }
            }
            mTileMap.put(sampleSize, tileGrid);
            if (sampleSize == 1) {
                break;
            } else {
                sampleSize /= 2;
            }
        }
    }

    /**
     * Async task used to get image details without blocking the UI thread.
     */
    private static class TilesInitTask extends AsyncTask<Void, Void, int[]> {
        private final WeakReference<SubSamplingScaleImageView> viewRef;
        private final WeakReference<Context> contextRef;
        private final WeakReference<DecoderFactory<? extends ImageRegionDecoder>> decoderFactoryRef;
        private final Uri source;
        private ImageRegionDecoder decoder;
        private Exception exception;

        TilesInitTask(SubSamplingScaleImageView view, Context context, DecoderFactory<? extends ImageRegionDecoder> decoderFactory, Uri source) {
            this.viewRef = new WeakReference<>(view);
            this.contextRef = new WeakReference<>(context);
            this.decoderFactoryRef = new WeakReference<DecoderFactory<? extends ImageRegionDecoder>>(decoderFactory);
            this.source = source;
        }

        @Override
        protected int[] doInBackground(Void... params) {
            try {
                String sourceUri = source.toString();
                Context context = contextRef.get();
                DecoderFactory<? extends ImageRegionDecoder> decoderFactory = decoderFactoryRef.get();
                SubSamplingScaleImageView view = viewRef.get();
                if (context != null && decoderFactory != null && view != null) {
                    view.debug("TilesInitTask.doInBackground");
                    decoder = decoderFactory.make();
                    Point dimensions = decoder.init(context, source);
                    int sWidth = dimensions.x;
                    int sHeight = dimensions.y;
                    int exifOrientation = view.getExifOrientation(context, sourceUri);
                    if (view.mSRegion != null) {
                        sWidth = view.mSRegion.width();
                        sHeight = view.mSRegion.height();
                    }
                    return new int[]{sWidth, sHeight, exifOrientation};
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialise bitmap decoder", e);
                this.exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(int[] xyo) {
            final SubSamplingScaleImageView view = viewRef.get();
            if (view != null) {
                if (decoder != null && xyo != null && xyo.length == 3) {
                    view.onTilesInited(decoder, xyo[0], xyo[1], xyo[2]);
                } else if (exception != null && view.mOnImageEventListener != null) {
                    view.mOnImageEventListener.onImageLoadError(exception);
                }
            }
        }
    }

    /**
     * Called by worker task when decoder is ready and image size and EXIF orientation is known.
     */
    private synchronized void onTilesInited(ImageRegionDecoder decoder, int sWidth, int sHeight, int sOrientation) {
        debug("onTilesInited sWidth=%d, sHeight=%d, sOrientation=%d", sWidth, sHeight, mOrientation);
        // If actual dimensions don't match the declared size, reset everything.
        if (this.mWidth > 0 && this.mHeight > 0 && (this.mWidth != sWidth || this.mHeight != sHeight)) {
            reset(false);
            if (mBitmap != null) {
                if (!isBitmapCached) {
                    mBitmap.recycle();
                }
                mBitmap = null;
                if (mOnImageEventListener != null && isBitmapCached) {
                    mOnImageEventListener.onPreviewReleased();
                }
                isBitmapPreview = false;
                isBitmapCached = false;
            }
        }
        this.mImageRegionDecoder = decoder;
        this.mWidth = sWidth;
        this.mHeight = sHeight;
        this.mSOrientation = sOrientation;
        checkReady();
        if (!checkImageLoaded() && mMaxTileWidth > 0 && mMaxTileWidth != TILE_SIZE_AUTO && mMaxTileHeight > 0 && mMaxTileHeight != TILE_SIZE_AUTO && getWidth() > 0 && getHeight() > 0) {
            initialiseBaseLayer(new Point(mMaxTileWidth, mMaxTileHeight));
        }
        invalidate();
        requestLayout();
    }

    /**
     * Async task used to load images without blocking the UI thread.
     */
    private static class TileLoadTask extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<SubSamplingScaleImageView> viewRef;
        private final WeakReference<ImageRegionDecoder> decoderRef;
        private final WeakReference<Tile> tileRef;
        private Exception exception;

        TileLoadTask(SubSamplingScaleImageView view, ImageRegionDecoder decoder, Tile tile) {
            this.viewRef = new WeakReference<>(view);
            this.decoderRef = new WeakReference<>(decoder);
            this.tileRef = new WeakReference<>(tile);
            tile.loading = true;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                SubSamplingScaleImageView view = viewRef.get();
                ImageRegionDecoder decoder = decoderRef.get();
                Tile tile = tileRef.get();
                if (decoder != null && tile != null && view != null && decoder.isReady() && tile.visible) {
                    view.debug("TileLoadTask.doInBackground, tile.sRect=%s, tile.sampleSize=%d", tile.sRect, tile.sampleSize);
                    synchronized (view.mDecoderLock) {
                        // Update tile's file sRect according to rotation
                        view.fileSRect(tile.sRect, tile.fileSRect);
                        if (view.mSRegion != null) {
                            tile.fileSRect.offset(view.mSRegion.left, view.mSRegion.top);
                        }
                        return decoder.decodeRegion(tile.fileSRect, tile.sampleSize);
                    }
                } else if (tile != null) {
                    tile.loading = false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to decode tile", e);
                this.exception = e;
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "Failed to decode tile - OutOfMemoryError", e);
                this.exception = new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            final SubSamplingScaleImageView subsamplingScaleImageView = viewRef.get();
            final Tile tile = tileRef.get();
            if (subsamplingScaleImageView != null && tile != null) {
                if (bitmap != null) {
                    tile.bitmap = bitmap;
                    tile.loading = false;
                    subsamplingScaleImageView.onTileLoaded();
                } else if (exception != null && subsamplingScaleImageView.mOnImageEventListener != null) {
                    subsamplingScaleImageView.mOnImageEventListener.onTileLoadError(exception);
                }
            }
        }
    }

    /**
     * Called by worker task when a tile has loaded. Redraws the view.
     */
    private synchronized void onTileLoaded() {
        debug("onTileLoaded");
        checkReady();
        checkImageLoaded();
        if (isBaseLayerReady() && mBitmap != null) {
            if (!isBitmapCached) {
                mBitmap.recycle();
            }
            mBitmap = null;
            if (mOnImageEventListener != null && isBitmapCached) {
                mOnImageEventListener.onPreviewReleased();
            }
            isBitmapPreview = false;
            isBitmapCached = false;
        }
        invalidate();
    }

    /**
     * Async task used to load bitmap without blocking the UI thread.
     */
    private static class BitmapLoadTask extends AsyncTask<Void, Void, Integer> {
        private final WeakReference<SubSamplingScaleImageView> viewRef;
        private final WeakReference<Context> contextRef;
        private final WeakReference<DecoderFactory<? extends ImageDecoder>> decoderFactoryRef;
        private final Uri source;
        private final boolean preview;
        private Bitmap bitmap;
        private Exception exception;

        BitmapLoadTask(SubSamplingScaleImageView view, Context context, DecoderFactory<? extends ImageDecoder> decoderFactory, Uri source, boolean preview) {
            this.viewRef = new WeakReference<>(view);
            this.contextRef = new WeakReference<>(context);
            this.decoderFactoryRef = new WeakReference<DecoderFactory<? extends ImageDecoder>>(decoderFactory);
            this.source = source;
            this.preview = preview;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String sourceUri = source.toString();
                Context context = contextRef.get();
                DecoderFactory<? extends ImageDecoder> decoderFactory = decoderFactoryRef.get();
                SubSamplingScaleImageView view = viewRef.get();
                if (context != null && decoderFactory != null && view != null) {
                    view.debug("BitmapLoadTask.doInBackground");
                    bitmap = decoderFactory.make().decode(context, source);
                    return view.getExifOrientation(context, sourceUri);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to load bitmap", e);
                this.exception = e;
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "Failed to load bitmap - OutOfMemoryError", e);
                this.exception = new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer orientation) {
            SubSamplingScaleImageView subsamplingScaleImageView = viewRef.get();
            if (subsamplingScaleImageView != null) {
                if (bitmap != null && orientation != null) {
                    if (preview) {
                        subsamplingScaleImageView.onPreviewLoaded(bitmap);
                    } else {
                        subsamplingScaleImageView.onImageLoaded(bitmap, orientation, false);
                    }
                } else if (exception != null && subsamplingScaleImageView.mOnImageEventListener != null) {
                    if (preview) {
                        subsamplingScaleImageView.mOnImageEventListener.onPreviewLoadError(exception);
                    } else {
                        subsamplingScaleImageView.mOnImageEventListener.onImageLoadError(exception);
                    }
                }
            }
        }
    }

    /**
     * Called by worker task when preview image is loaded.
     */
    private synchronized void onPreviewLoaded(Bitmap previewBitmap) {
        debug("onPreviewLoaded");
        if (mBitmap != null || isImageLoadedSent) {
            previewBitmap.recycle();
            return;
        }
        if (mPRegion != null) {
            mBitmap = Bitmap.createBitmap(previewBitmap, mPRegion.left, mPRegion.top, mPRegion.width(), mPRegion.height());
        } else {
            mBitmap = previewBitmap;
        }
        isBitmapPreview = true;
        if (checkReady()) {
            invalidate();
            requestLayout();
        }
    }

    /**
     * Called by worker task when full size image bitmap is ready (tiling is disabled).
     */
    private synchronized void onImageLoaded(Bitmap bitmap, int sOrientation, boolean isBitmapCached) {
        debug("onImageLoaded");
        // If actual dimensions don't match the declared size, reset everything.
        if (this.mWidth > 0 && this.mHeight > 0 && (this.mWidth != bitmap.getWidth() || this.mHeight != bitmap.getHeight())) {
            reset(false);
        }
        if (this.mBitmap != null && !this.isBitmapCached) {
            this.mBitmap.recycle();
        }

        if (this.mBitmap != null && this.isBitmapCached && mOnImageEventListener != null) {
            mOnImageEventListener.onPreviewReleased();
        }

        this.isBitmapPreview = false;
        this.isBitmapCached = isBitmapCached;
        this.mBitmap = bitmap;
        this.mWidth = bitmap.getWidth();
        this.mHeight = bitmap.getHeight();
        this.mSOrientation = sOrientation;
        boolean ready = checkReady();
        boolean imageLoaded = checkImageLoaded();
        if (ready || imageLoaded) {
            invalidate();
            requestLayout();
        }
    }

    /**
     * Helper method for load tasks. Examines the EXIF info on the image file to determine the orientation.
     * This will only work for external files, not assets, resources or other URIs.
     */
    @AnyThread
    private int getExifOrientation(Context context, String sourceUri) {
        int exifOrientation = ORIENTATION_0;
        if (sourceUri.startsWith(ContentResolver.SCHEME_CONTENT)) {
            Cursor cursor = null;
            try {
                String[] columns = {MediaStore.Images.Media.ORIENTATION};
                cursor = context.getContentResolver().query(Uri.parse(sourceUri), columns, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int orientation = cursor.getInt(0);
                        if (VALID_ORIENTATIONS.contains(orientation) && orientation != ORIENTATION_USE_EXIF) {
                            exifOrientation = orientation;
                        } else {
                            Log.w(TAG, "Unsupported orientation: " + orientation);
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not get orientation of image from media store");
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (sourceUri.startsWith(ImageSource.FILE_SCHEME) && !sourceUri.startsWith(ImageSource.ASSET_SCHEME)) {
            try {
                ExifInterface exifInterface = new ExifInterface(sourceUri.substring(ImageSource.FILE_SCHEME.length() - 1));
                int orientationAttr = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                if (orientationAttr == ExifInterface.ORIENTATION_NORMAL || orientationAttr == ExifInterface.ORIENTATION_UNDEFINED) {
                    exifOrientation = ORIENTATION_0;
                } else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_90) {
                    exifOrientation = ORIENTATION_90;
                } else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_180) {
                    exifOrientation = ORIENTATION_180;
                } else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_270) {
                    exifOrientation = ORIENTATION_270;
                } else {
                    Log.w(TAG, "Unsupported EXIF orientation: " + orientationAttr);
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not get EXIF orientation of image");
            }
        }
        return exifOrientation;
    }

    private void execute(AsyncTask<Void, Void, ?> asyncTask) {
        if (isParallelLoadingEnabled && VERSION.SDK_INT >= 16) {
            try {
                Field executorField = AsyncTask.class.getField("THREAD_POOL_EXECUTOR");
                Executor executor = (Executor) executorField.get(null);
                Method executeMethod = AsyncTask.class.getMethod("executeOnExecutor", Executor.class, Object[].class);
                executeMethod.invoke(asyncTask, executor, null);
                return;
            } catch (Exception e) {
                Log.i(TAG, "Failed to execute AsyncTask on thread pool executor, falling back to single threaded executor", e);
            }
        }
        asyncTask.execute();
    }

    private static class Tile {

        private Rect sRect;
        private int sampleSize;
        private Bitmap bitmap;
        private boolean loading;
        private boolean visible;

        // Volatile fields instantiated once then updated before use to reduce GC.
        private Rect vRect;
        private Rect fileSRect;

    }

    private static class Anim {

        private float scaleStart; // Scale at start of anim
        private float scaleEnd; // Scale at end of anim (target)
        private PointF sCenterStart; // Source center point at start
        private PointF sCenterEnd; // Source center point at end, adjusted for pan limits
        private PointF sCenterEndRequested; // Source center point that was requested, without adjustment
        private PointF vFocusStart; // View point that was double tapped
        private PointF vFocusEnd; // Where the view focal point should be moved to during the anim
        private long duration = 500; // How long the anim takes
        private boolean interruptible = true; // Whether the anim can be interrupted by a touch
        private int easing = EASE_IN_OUT_QUAD; // Easing style
        private int origin = ORIGIN_ANIM; // Animation origin (API, double tap or fling)
        private long time = System.currentTimeMillis(); // Start time
        private OnAnimationEventListener listener; // Event listener

    }

    private static class ScaleAndTranslate {
        private ScaleAndTranslate(float scale, PointF vTranslate) {
            this.scale = scale;
            this.vTranslate = vTranslate;
        }

        private float scale;
        private PointF vTranslate;
    }

    /**
     * Set scale, center and orientation from saved state.
     */
    private void restoreState(ImageViewState state) {
        if (state != null && state.getCenter() != null && VALID_ORIENTATIONS.contains(state.getOrientation())) {
            this.mOrientation = state.getOrientation();
            this.mPendingScale = state.getScale();
            this.mPendingCenter = state.getCenter();
            invalidate();
        }
    }

    /**
     * By default the View automatically calculates the optimal tile size. Set this to override this, and force an upper limit to the dimensions of the generated tiles. Passing {@link #TILE_SIZE_AUTO} will re-enable the default behaviour.
     *
     * @param maxPixels Maximum tile size X and Y in pixels.
     */
    public void setMaxTileSize(int maxPixels) {
        this.mMaxTileWidth = maxPixels;
        this.mMaxTileHeight = maxPixels;
    }

    /**
     * By default the View automatically calculates the optimal tile size. Set this to override this, and force an upper limit to the dimensions of the generated tiles. Passing {@link #TILE_SIZE_AUTO} will re-enable the default behaviour.
     *
     * @param maxPixelsX Maximum tile width.
     * @param maxPixelsY Maximum tile height.
     */
    public void setMaxTileSize(int maxPixelsX, int maxPixelsY) {
        this.mMaxTileWidth = maxPixelsX;
        this.mMaxTileHeight = maxPixelsY;
    }

    /**
     * In SDK 14 and above, use canvas max bitmap width and height instead of the default 2048, to avoid redundant tiling.
     */
    private Point getMaxBitmapDimensions(Canvas canvas) {
        int maxWidth = 2048;
        int maxHeight = 2048;
        if (VERSION.SDK_INT >= 16) {
            try {
                maxWidth = (Integer) Canvas.class.getMethod("getMaximumBitmapWidth").invoke(canvas);
                maxHeight = (Integer) Canvas.class.getMethod("getMaximumBitmapHeight").invoke(canvas);
            } catch (Exception e) {
                // Return default
            }
        }
        return new Point(Math.min(maxWidth, mMaxTileWidth), Math.min(maxHeight, mMaxTileHeight));
    }

    /**
     * Get source width taking rotation into account.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private int sWidth() {
        int rotation = getRequiredRotation();
        if (rotation == 90 || rotation == 270) {
            return mHeight;
        } else {
            return mWidth;
        }
    }

    /**
     * Get source height taking rotation into account.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private int sHeight() {
        int rotation = getRequiredRotation();
        if (rotation == 90 || rotation == 270) {
            return mWidth;
        } else {
            return mHeight;
        }
    }

    /**
     * Converts source rectangle from tile, which treats the image file as if it were in the correct orientation already,
     * to the rectangle of the image that needs to be loaded.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    @AnyThread
    private void fileSRect(Rect sRect, Rect target) {
        if (getRequiredRotation() == 0) {
            target.set(sRect);
        } else if (getRequiredRotation() == 90) {
            target.set(sRect.top, mHeight - sRect.right, sRect.bottom, mHeight - sRect.left);
        } else if (getRequiredRotation() == 180) {
            target.set(mWidth - sRect.right, mHeight - sRect.bottom, mWidth - sRect.left, mHeight - sRect.top);
        } else {
            target.set(mWidth - sRect.bottom, sRect.left, mWidth - sRect.top, sRect.right);
        }
    }

    /**
     * Determines the rotation to be applied to tiles, based on EXIF orientation or chosen setting.
     */
    @AnyThread
    private int getRequiredRotation() {
        if (mOrientation == ORIENTATION_USE_EXIF) {
            return mSOrientation;
        } else {
            return mOrientation;
        }
    }

    /**
     * Pythagoras distance between two points.
     */
    private float distance(float x0, float x1, float y0, float y1) {
        float x = x0 - x1;
        float y = y0 - y1;
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Releases all resources the view is using and resets the state, nulling any fields that use significant memory.
     * After you have called this method, the view can be re-used by setting a new image. Settings are remembered
     * but state (scale and center) is forgotten. You can restore these yourself if required.
     */
    public void recycle() {
        reset(true);
        mBitmapPaint = null;
        mDebugPaint = null;
        mTileBgPaint = null;
    }

    /**
     * Convert screen to source x coordinate.
     */
    private float viewToSourceX(float vx) {
        if (mPfTranslate == null) {
            return Float.NaN;
        }
        return (vx - mPfTranslate.x) / mScale;
    }

    /**
     * Convert screen to source y coordinate.
     */
    private float viewToSourceY(float vy) {
        if (mPfTranslate == null) {
            return Float.NaN;
        }
        return (vy - mPfTranslate.y) / mScale;
    }

    /**
     * Convert screen coordinate to source coordinate.
     */
    public final PointF viewToSourceCoordinate(PointF vxy) {
        return viewToSourceCoordinate(vxy.x, vxy.y, new PointF());
    }

    /**
     * Convert screen coordinate to source coordinate.
     */
    public final PointF viewToSourceCoordinate(float vx, float vy) {
        return viewToSourceCoordinate(vx, vy, new PointF());
    }

    /**
     * Convert screen coordinate to source coordinate.
     */
    public final PointF viewToSourceCoordinate(PointF vxy, PointF sTarget) {
        return viewToSourceCoordinate(vxy.x, vxy.y, sTarget);
    }

    /**
     * Convert screen coordinate to source coordinate.
     */
    public final PointF viewToSourceCoordinate(float vx, float vy, PointF sTarget) {
        if (mPfTranslate == null) {
            return null;
        }
        sTarget.set(viewToSourceX(vx), viewToSourceY(vy));
        return sTarget;
    }

    /**
     * Convert source to screen x coordinate.
     */
    private float sourceToViewX(float sx) {
        if (mPfTranslate == null) {
            return Float.NaN;
        }
        return (sx * mScale) + mPfTranslate.x;
    }

    /**
     * Convert source to screen y coordinate.
     */
    private float sourceToViewY(float sy) {
        if (mPfTranslate == null) {
            return Float.NaN;
        }
        return (sy * mScale) + mPfTranslate.y;
    }

    /**
     * Convert source coordinate to screen coordinate.
     */
    public final PointF sourceToViewCoordinate(PointF sxy) {
        return sourceToViewCoordinate(sxy.x, sxy.y, new PointF());
    }

    /**
     * Convert source coordinate to screen coordinate.
     */
    public final PointF sourceToViewCoordinate(float sx, float sy) {
        return sourceToViewCoordinate(sx, sy, new PointF());
    }

    /**
     * Convert source coordinate to screen coordinate.
     */
    public final PointF sourceToViewCoordinate(PointF sxy, PointF vTarget) {
        return sourceToViewCoordinate(sxy.x, sxy.y, vTarget);
    }

    /**
     * Convert source coordinate to screen coordinate.
     */
    public final PointF sourceToViewCoordinate(float sx, float sy, PointF vTarget) {
        if (mPfTranslate == null) {
            return null;
        }
        vTarget.set(sourceToViewX(sx), sourceToViewY(sy));
        return vTarget;
    }

    /**
     * Convert source rect to screen rect, integer values.
     */
    private Rect sourceToViewRect(Rect sRect, Rect vTarget) {
        vTarget.set(
                (int) sourceToViewX(sRect.left),
                (int) sourceToViewY(sRect.top),
                (int) sourceToViewX(sRect.right),
                (int) sourceToViewY(sRect.bottom)
        );
        return vTarget;
    }

    /**
     * Get the translation required to place a given source coordinate at the center of the screen, with the center
     * adjusted for asymmetric padding. Accepts the desired scale as an argument, so this is independent of current
     * translate and scale. The result is fitted to bounds, putting the image point as near to the screen center as permitted.
     */
    private PointF vTranslateForSCenter(float sCenterX, float sCenterY, float scale) {
        int vxCenter = getPaddingLeft() + (getWidth() - getPaddingRight() - getPaddingLeft()) / 2;
        int vyCenter = getPaddingTop() + (getHeight() - getPaddingBottom() - getPaddingTop()) / 2;
        if (mSatTemp == null) {
            mSatTemp = new ScaleAndTranslate(0, new PointF(0, 0));
        }
        mSatTemp.scale = scale;
        mSatTemp.vTranslate.set(vxCenter - (sCenterX * scale), vyCenter - (sCenterY * scale));
        fitToBounds(true, mSatTemp);
        return mSatTemp.vTranslate;
    }

    /**
     * Given a requested source center and scale, calculate what the actual center will have to be to keep the image in
     * pan limits, keeping the requested center as near to the middle of the screen as allowed.
     */
    private PointF limitedSCenter(float sCenterX, float sCenterY, float scale, PointF sTarget) {
        PointF vTranslate = vTranslateForSCenter(sCenterX, sCenterY, scale);
        int vxCenter = getPaddingLeft() + (getWidth() - getPaddingRight() - getPaddingLeft()) / 2;
        int vyCenter = getPaddingTop() + (getHeight() - getPaddingBottom() - getPaddingTop()) / 2;
        float sx = (vxCenter - vTranslate.x) / scale;
        float sy = (vyCenter - vTranslate.y) / scale;
        sTarget.set(sx, sy);
        return sTarget;
    }

    /**
     * Returns the minimum allowed scale.
     */
    private float minScale() {
        int vPadding = getPaddingBottom() + getPaddingTop();
        int hPadding = getPaddingLeft() + getPaddingRight();
        if (mMinimumScaleType == SCALE_TYPE_CENTER_CROP) {
            return Math.max((getWidth() - hPadding) / (float) sWidth(), (getHeight() - vPadding) / (float) sHeight());
        } else if (mMinimumScaleType == SCALE_TYPE_CUSTOM && mMinScale > 0) {
            return mMinScale;
        } else {
//            return Math.min((getWidth() - hPadding) / (float) sWidth(), (getHeight() - vPadding) / (float) sHeight());
            //设置图片的最小缩放值为 屏幕宽度 / 图片宽度
            return (getWidth() - hPadding) / (float) sWidth();
        }
    }

    /**
     * Adjust a requested scale to be within the allowed limits.
     */
    private float limitedScale(float targetScale) {
        targetScale = Math.max(minScale(), targetScale);
        targetScale = Math.min(mMaxScale, targetScale);
        return targetScale;
    }

    /**
     * Apply a selected type of easing.
     *
     * @param type     Easing type, from static fields
     * @param time     Elapsed time
     * @param from     Start value
     * @param change   Target value
     * @param duration Anm duration
     * @return Current value
     */
    private float ease(int type, long time, float from, float change, long duration) {
        switch (type) {
            case EASE_IN_OUT_QUAD:
                return easeInOutQuad(time, from, change, duration);
            case EASE_OUT_QUAD:
                return easeOutQuad(time, from, change, duration);
            default:
                throw new IllegalStateException("Unexpected easing type: " + type);
        }
    }

    /**
     * Quadratic easing for fling. With thanks to Robert Penner - http://gizma.com/easing/
     *
     * @param time     Elapsed time
     * @param from     Start value
     * @param change   Target value
     * @param duration Anm duration
     * @return Current value
     */
    private float easeOutQuad(long time, float from, float change, long duration) {
        float progress = (float) time / (float) duration;
        return -change * progress * (progress - 2) + from;
    }

    /**
     * Quadratic easing for scale and center animations. With thanks to Robert Penner - http://gizma.com/easing/
     *
     * @param time     Elapsed time
     * @param from     Start value
     * @param change   Target value
     * @param duration Anm duration
     * @return Current value
     */
    private float easeInOutQuad(long time, float from, float change, long duration) {
        float timeF = time / (duration / 2f);
        if (timeF < 1) {
            return (change / 2f * timeF * timeF) + from;
        } else {
            timeF--;
            return (-change / 2f) * (timeF * (timeF - 2) - 1) + from;
        }
    }

    /**
     * Debug logger
     */
    @AnyThread
    private void debug(String message, Object... args) {
        if (isDebug) {
            Log.d(TAG, String.format(message, args));
        }
    }

    /**
     * Swap the default region decoder implementation for one of your own. You must do this before setting the image file or
     * asset, and you cannot use a custom decoder when using layout XML to set an asset name. Your class must have a
     * public default constructor.
     *
     * @param regionDecoderClass The {@link ImageRegionDecoder} implementation to use.
     */
    public final void setRegionDecoderClass(Class<? extends ImageRegionDecoder> regionDecoderClass) {
        if (regionDecoderClass == null) {
            throw new IllegalArgumentException("Decoder class cannot be set to null");
        }
        this.mRegionDecoderFactory = new CompatDecoderFactory<>(regionDecoderClass);
    }

    /**
     * Swap the default region decoder implementation for one of your own. You must do this before setting the image file or
     * asset, and you cannot use a custom decoder when using layout XML to set an asset name.
     *
     * @param regionDecoderFactory The {@link DecoderFactory} implementation that produces {@link ImageRegionDecoder}
     *                             instances.
     */
    public final void setRegionDecoderFactory(DecoderFactory<? extends ImageRegionDecoder> regionDecoderFactory) {
        if (regionDecoderFactory == null) {
            throw new IllegalArgumentException("Decoder factory cannot be set to null");
        }
        this.mRegionDecoderFactory = regionDecoderFactory;
    }

    /**
     * Swap the default bitmap decoder implementation for one of your own. You must do this before setting the image file or
     * asset, and you cannot use a custom decoder when using layout XML to set an asset name. Your class must have a
     * public default constructor.
     *
     * @param bitmapDecoderClass The {@link ImageDecoder} implementation to use.
     */
    public final void setBitmapDecoderClass(Class<? extends ImageDecoder> bitmapDecoderClass) {
        if (bitmapDecoderClass == null) {
            throw new IllegalArgumentException("Decoder class cannot be set to null");
        }
        this.mBitmapDecoderFactory = new CompatDecoderFactory<>(bitmapDecoderClass);
    }

    /**
     * Swap the default bitmap decoder implementation for one of your own. You must do this before setting the image file or
     * asset, and you cannot use a custom decoder when using layout XML to set an asset name.
     *
     * @param bitmapDecoderFactory The {@link DecoderFactory} implementation that produces {@link ImageDecoder} instances.
     */
    public final void setBitmapDecoderFactory(DecoderFactory<? extends ImageDecoder> bitmapDecoderFactory) {
        if (bitmapDecoderFactory == null) {
            throw new IllegalArgumentException("Decoder factory cannot be set to null");
        }
        this.mBitmapDecoderFactory = bitmapDecoderFactory;
    }

    /**
     * Set the pan limiting style. See static fields. Normally {@link #PAN_LIMIT_INSIDE} is best, for image galleries.
     */
    public final void setPanLimit(int panLimit) {
        if (!VALID_PAN_LIMITS.contains(panLimit)) {
            throw new IllegalArgumentException("Invalid pan limit: " + panLimit);
        }
        this.mPanLimit = panLimit;
        if (isReady()) {
            fitToBounds(true);
            invalidate();
        }
    }

    /**
     * Set the minimum scale type. See static fields. Normally {@link #SCALE_TYPE_CENTER_INSIDE} is best, for image galleries.
     */
    public final void setMinimumScaleType(int scaleType) {
        if (!VALID_SCALE_TYPES.contains(scaleType)) {
            throw new IllegalArgumentException("Invalid scale type: " + scaleType);
        }
        this.mMinimumScaleType = scaleType;
        if (isReady()) {
            fitToBounds(true);
            invalidate();
        }
    }

    /**
     * Set the maximum scale allowed. A value of 1 means 1:1 pixels at maximum scale. You may wish to set this according
     * to screen density - on a retina screen, 1:1 may still be too small. Consider using {@link #setMinimumDpi(int)},
     * which is density aware.
     */
    public final void setMaxScale(float maxScale) {
        this.mMaxScale = maxScale;
    }

    /**
     * Set the minimum scale allowed. A value of 1 means 1:1 pixels at minimum scale. You may wish to set this according
     * to screen density. Consider using {@link #setMaximumDpi(int)}, which is density aware.
     */
    public final void setMinScale(float minScale) {
        this.mMinScale = minScale;
    }

    /**
     * This is a screen density aware alternative to {@link #setMaxScale(float)}; it allows you to express the maximum
     * allowed scale in terms of the minimum pixel density. This avoids the problem of 1:1 scale still being
     * too small on a high density screen. A sensible starting point is 160 - the default used by this view.
     *
     * @param dpi Source image pixel density at maximum zoom.
     */
    public final void setMinimumDpi(int dpi) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float averageDpi = (metrics.xdpi + metrics.ydpi) / 2;
        setMaxScale(averageDpi / dpi);
        System.out.println("setMaxScale():" + averageDpi / dpi);
    }

    /**
     * This is a screen density aware alternative to {@link #setMinScale(float)}; it allows you to express the minimum
     * allowed scale in terms of the maximum pixel density.
     *
     * @param dpi Source image pixel density at minimum zoom.
     */
    public final void setMaximumDpi(int dpi) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float averageDpi = (metrics.xdpi + metrics.ydpi) / 2;
        setMinScale(averageDpi / dpi);
    }

    /**
     * Returns the maximum allowed scale.
     */
    public float getMaxScale() {
        return mMaxScale;
    }

    /**
     * Returns the minimum allowed scale.
     */
    public final float getMinScale() {
        return minScale();
    }

    /**
     * By default, image tiles are at least as high resolution as the screen. For a retina screen this may not be
     * necessary, and may increase the likelihood of an OutOfMemoryError. This method sets a DPI at which higher
     * resolution tiles should be loaded. Using a lower number will on average use less memory but result in a lower
     * quality image. 160-240dpi will usually be enough. This should be called before setting the image source,
     * because it affects which tiles get loaded. When using an untiled source image this method has no effect.
     *
     * @param minimumTileDpi Tile loading threshold.
     */
    public void setMinimumTileDpi(int minimumTileDpi) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float averageDpi = (metrics.xdpi + metrics.ydpi) / 2;
        this.mMinimumTileDpi = (int) Math.min(averageDpi, minimumTileDpi);
        if (isReady()) {
            reset(false);
            invalidate();
        }
    }

    /**
     * Returns the source point at the center of the view.
     */
    public final PointF getCenter() {
        int mX = getWidth() / 2;
        int mY = getHeight() / 2;
        return viewToSourceCoordinate(mX, mY);
    }

    /**
     * Returns the current scale value.
     */
    public final float getScale() {
        return mScale;
    }

    /**
     * Externally change the scale and translation of the source image. This may be used with getCenter() and getScale()
     * to restore the scale and zoom after a screen rotate.
     *
     * @param scale   New scale to set.
     * @param sCenter New source image coordinate to center on the screen, subject to boundaries.
     */
    public final void setScaleAndCenter(float scale, PointF sCenter) {
        this.mAnim = null;
        this.mPendingScale = scale;
        this.mPendingCenter = sCenter;
        this.mRequestedCenter = sCenter;
        invalidate();
    }

    /**
     * Fully zoom out and return the image to the middle of the screen. This might be useful if you have a view pager
     * and want images to be reset when the user has moved to another page.
     */
    public final void resetScaleAndCenter() {
        this.mAnim = null;
        this.mPendingScale = limitedScale(0);
        if (isReady()) {
            this.mPendingCenter = new PointF(sWidth() / 2, sHeight() / 2);
        } else {
            this.mPendingCenter = new PointF(0, 0);
        }
        invalidate();
    }

    /**
     * Call to find whether the view is initialised, has dimensions, and will display an image on
     * the next draw. If a preview has been provided, it may be the preview that will be displayed
     * and the full size image may still be loading. If no preview was provided, this is called once
     * the base layer tiles of the full size image are loaded.
     */
    public final boolean isReady() {
        return isReadySent;
    }

    /**
     * Called once when the view is initialised, has dimensions, and will display an image on the
     * next draw. This is triggered at the same time as {@link OnImageEventListener#onReady()} but
     * allows a subclass to receive this event without using a listener.
     */
    protected void onReady() {

    }

    /**
     * Call to find whether the main image (base layer tiles where relevant) have been loaded. Before
     * this event the view is blank unless a preview was provided.
     */
    public final boolean isImageLoaded() {
        return isImageLoadedSent;
    }

    /**
     * Called once when the full size image or its base layer tiles have been loaded.
     */
    protected void onImageLoaded() {

    }

    /**
     * Get source width, ignoring orientation. If {@link #getOrientation()} returns 90 or 270, you can use {@link #getSHeight()}
     * for the apparent width.
     */
    public final int getSWidth() {
        return mWidth;
    }

    /**
     * Get source height, ignoring orientation. If {@link #getOrientation()} returns 90 or 270, you can use {@link #getSWidth()}
     * for the apparent height.
     */
    public final int getSHeight() {
        return mHeight;
    }

    /**
     * Returns the orientation setting. This can return {@link #ORIENTATION_USE_EXIF}, in which case it doesn't tell you
     * the applied orientation of the image. For that, use {@link #getAppliedOrientation()}.
     */
    public final int getOrientation() {
        return mOrientation;
    }

    /**
     * Returns the actual orientation of the image relative to the source file. This will be based on the source file's
     * EXIF orientation if you're using ORIENTATION_USE_EXIF. Values are 0, 90, 180, 270.
     */
    public final int getAppliedOrientation() {
        return getRequiredRotation();
    }

    /**
     * Get the current state of the view (scale, center, orientation) for restoration after rotate. Will return null if
     * the view is not ready.
     */
    public final ImageViewState getState() {
        if (mPfTranslate != null && mWidth > 0 && mHeight > 0) {
            return new ImageViewState(getScale(), getCenter(), getOrientation());
        }
        return null;
    }

    /**
     * Returns true if zoom gesture detection is enabled.
     */
    public final boolean isZoomEnabled() {
        return isZoomEnabled;
    }

    /**
     * Enable or disable zoom gesture detection. Disabling zoom locks the the current scale.
     */
    public final void setZoomEnabled(boolean isZoomEnabled) {
        this.isZoomEnabled = isZoomEnabled;
    }

    /**
     * Returns true if double tap & swipe to zoom is enabled.
     */
    public final boolean isQuickScaleEnabled() {
        return isQuickScaleEnabled;
    }

    /**
     * Enable or disable double tap & swipe to zoom.
     */
    public final void setQuickScaleEnabled(boolean isQuickScaleEnabled) {
        this.isQuickScaleEnabled = isQuickScaleEnabled;
    }

    /**
     * Returns true if pan gesture detection is enabled.
     */
    public final boolean isPanEnabled() {
        return isPanEnabled;
    }

    /**
     * Enable or disable pan gesture detection. Disabling pan causes the image to be centered.
     */
    public final void setPanEnabled(boolean isPanEnabled) {
        this.isPanEnabled = isPanEnabled;
        if (!isPanEnabled && mPfTranslate != null) {
            mPfTranslate.x = (getWidth() / 2) - (mScale * (sWidth() / 2));
            mPfTranslate.y = (getHeight() / 2) - (mScale * (sHeight() / 2));
            if (isReady()) {
                refreshRequiredTiles(true);
                invalidate();
            }
        }
    }

    /**
     * Set a solid color to render behind tiles, useful for displaying transparent PNGs.
     *
     * @param tileBgColor Background color for tiles.
     */
    public final void setTileBackgroundColor(int tileBgColor) {
        if (Color.alpha(tileBgColor) == 0) {
            mTileBgPaint = null;
        } else {
            mTileBgPaint = new Paint();
            mTileBgPaint.setStyle(Style.FILL);
            mTileBgPaint.setColor(tileBgColor);
        }
        invalidate();
    }

    /**
     * Set the scale the image will zoom in to when double tapped. This also the scale point where a double tap is interpreted
     * as a zoom out gesture - if the scale is greater than 90% of this value, a double tap zooms out. Avoid using values
     * greater than the max zoom.
     *
     * @param doubleTapZoomScale New value for double tap gesture zoom scale.
     */
    public final void setDoubleTapZoomScale(float doubleTapZoomScale) {
        this.mDoubleTapZoomScale = doubleTapZoomScale;
    }

    /**
     * A density aware alternative to {@link #setDoubleTapZoomScale(float)}; this allows you to express the scale the
     * image will zoom in to when double tapped in terms of the image pixel density. Values lower than the max scale will
     * be ignored. A sensible starting point is 160 - the default used by this view.
     *
     * @param dpi New value for double tap gesture zoom scale.
     */
    public final void setDoubleTapZoomDpi(int dpi) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float averageDpi = (metrics.xdpi + metrics.ydpi) / 2;
        setDoubleTapZoomScale(averageDpi / dpi);
        System.out.println("setDoubleTapZoomScale():" + averageDpi / dpi);
    }

    /**
     * Set the type of zoom animation to be used for double taps. See static fields.
     *
     * @param doubleTapZoomStyle New value for zoom style.
     */
    public final void setDoubleTapZoomStyle(int doubleTapZoomStyle) {
        if (!VALID_ZOOM_STYLES.contains(doubleTapZoomStyle)) {
            throw new IllegalArgumentException("Invalid zoom style: " + doubleTapZoomStyle);
        }
        this.mDoubleTapZoomStyle = doubleTapZoomStyle;
    }

    /**
     * Set the duration of the double tap zoom animation.
     *
     * @param durationMs Duration in milliseconds.
     */
    public final void setDoubleTapZoomDuration(int durationMs) {
        this.mDoubleTapZoomDuration = Math.max(0, durationMs);
    }

    /**
     * Toggle parallel loading. When enabled, tiles are loaded using the thread pool executor available
     * in SDK 11+. In older versions this has no effect. Parallel loading may use more memory and there
     * is a possibility that it will make the tile loading unreliable, but it reduces the chances of
     * an app's background processes blocking loading.
     *
     * @param isParallelLoadingEnabled Whether to run AsyncTasks using a thread pool executor.
     */
    public void setParallelLoadingEnabled(boolean isParallelLoadingEnabled) {
        this.isParallelLoadingEnabled = isParallelLoadingEnabled;
    }

    /**
     * Enables visual debugging, showing tile boundaries and sizes.
     */
    public final void setDebug(boolean debug) {
        this.isDebug = debug;
    }

    /**
     * Check if an image has been set. The image may not have been loaded and displayed yet.
     *
     * @return If an image is currently set.
     */
    public boolean hasImage() {
        return mUri != null || mBitmap != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.mOnLongClickListener = onLongClickListener;
    }

    /**
     * Add a listener allowing notification of load and error events.
     */
    public void setOnImageEventListener(OnImageEventListener onImageEventListener) {
        this.mOnImageEventListener = onImageEventListener;
    }

    /**
     * Add a listener for pan and zoom events.
     */
    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        this.mOnStateChangedListener = onStateChangedListener;
    }

    private void sendStateChanged(float oldScale, PointF oldVTranslate, int origin) {
        if (mOnStateChangedListener != null) {
            if (mScale != oldScale) {
                mOnStateChangedListener.onScaleChanged(mScale, origin);
            }
            if (!mPfTranslate.equals(oldVTranslate)) {
                mOnStateChangedListener.onCenterChanged(getCenter(), origin);
            }
        }
    }

    /**
     * Creates a panning animation builder, that when started will animate the image to place the given coordinates of
     * the image in the center of the screen. If doing this would move the image beyond the edges of the screen, the
     * image is instead animated to move the center point as near to the center of the screen as is allowed - it's
     * guaranteed to be on screen.
     *
     * @param sCenter Target center point
     * @return {@link AnimationBuilder} instance. Call {@link AnimationBuilder#start()} to start the anim.
     */
    public AnimationBuilder animateCenter(PointF sCenter) {
        if (!isReady()) {
            return null;
        }
        return new AnimationBuilder(sCenter);
    }

    /**
     * Creates a scale animation builder, that when started will animate a zoom in or out. If this would move the image
     * beyond the panning limits, the image is automatically panned during the animation.
     *
     * @param scale Target scale.
     * @return {@link AnimationBuilder} instance. Call {@link AnimationBuilder#start()} to start the anim.
     */
    public AnimationBuilder animateScale(float scale) {
        if (!isReady()) {
            return null;
        }
        return new AnimationBuilder(scale);
    }

    /**
     * Creates a scale animation builder, that when started will animate a zoom in or out. If this would move the image
     * beyond the panning limits, the image is automatically panned during the animation.
     *
     * @param scale Target scale.
     * @return {@link AnimationBuilder} instance. Call {@link AnimationBuilder#start()} to start the anim.
     */
    public AnimationBuilder animateScaleAndCenter(float scale, PointF sCenter) {
        if (!isReady()) {
            return null;
        }
        return new AnimationBuilder(scale, sCenter);
    }

    /**
     * Builder class used to set additional options for a scale animation. Create an instance using {@link #animateScale(float)},
     * then set your options and call {@link #start()}.
     */
    private final class AnimationBuilder {

        private final float targetScale;
        private final PointF targetSCenter;
        private final PointF vFocus;
        private long duration = 500;
        private int easing = EASE_IN_OUT_QUAD;
        private int origin = ORIGIN_ANIM;
        private boolean interruptible = true;
        private boolean panLimited = true;
        private OnAnimationEventListener listener;

        private AnimationBuilder(PointF sCenter) {
            this.targetScale = mScale;
            this.targetSCenter = sCenter;
            this.vFocus = null;
        }

        private AnimationBuilder(float scale) {
            this.targetScale = scale;
            this.targetSCenter = getCenter();
            this.vFocus = null;
        }

        private AnimationBuilder(float scale, PointF sCenter) {
            this.targetScale = scale;
            this.targetSCenter = sCenter;
            this.vFocus = null;
        }

        private AnimationBuilder(float scale, PointF sCenter, PointF vFocus) {
            this.targetScale = scale;
            this.targetSCenter = sCenter;
            this.vFocus = vFocus;
        }

        /**
         * Desired duration of the anim in milliseconds. Default is 500.
         *
         * @param duration duration in milliseconds.
         * @return this builder for method chaining.
         */
        AnimationBuilder withDuration(long duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Whether the animation can be interrupted with a touch. Default is true.
         *
         * @param interruptible interruptible flag.
         * @return this builder for method chaining.
         */
        AnimationBuilder withInterruptible(boolean interruptible) {
            this.interruptible = interruptible;
            return this;
        }

        /**
         * Set the easing style. See static fields. {@link #EASE_IN_OUT_QUAD} is recommended, and the default.
         *
         * @param easing easing style.
         * @return this builder for method chaining.
         */
        AnimationBuilder withEasing(int easing) {
            if (!VALID_EASING_STYLES.contains(easing)) {
                throw new IllegalArgumentException("Unknown easing type: " + easing);
            }
            this.easing = easing;
            return this;
        }

        /**
         * Add an animation event listener.
         *
         * @param listener The listener.
         * @return this builder for method chaining.
         */
        public AnimationBuilder withOnAnimationEventListener(OnAnimationEventListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Only for internal use. When set to true, the animation proceeds towards the actual end point - the nearest
         * point to the center allowed by pan limits. When false, animation is in the direction of the requested end
         * point and is stopped when the limit for each axis is reached. The latter behaviour is used for flings but
         * nothing else.
         */
        private AnimationBuilder withPanLimited(boolean panLimited) {
            this.panLimited = panLimited;
            return this;
        }

        /**
         * Only for internal use. Indicates what caused the animation.
         */
        private AnimationBuilder withOrigin(int origin) {
            this.origin = origin;
            return this;
        }

        /**
         * Starts the animation.
         */
        public void start() {
            if (mAnim != null && mAnim.listener != null) {
                try {
                    mAnim.listener.onInterruptedByNewAnim();
                } catch (Exception e) {
                    Log.w(TAG, "Error thrown by animation listener", e);
                }
            }

            int vxCenter = getPaddingLeft() + (getWidth() - getPaddingRight() - getPaddingLeft()) / 2;
            int vyCenter = getPaddingTop() + (getHeight() - getPaddingBottom() - getPaddingTop()) / 2;
            float targetScale = limitedScale(this.targetScale);
            PointF targetSCenter = panLimited ? limitedSCenter(this.targetSCenter.x, this.targetSCenter.y, targetScale, new PointF()) : this.targetSCenter;
            mAnim = new Anim();
            mAnim.scaleStart = mScale;
            mAnim.scaleEnd = targetScale;
            mAnim.time = System.currentTimeMillis();
            mAnim.sCenterEndRequested = targetSCenter;
            mAnim.sCenterStart = getCenter();
            mAnim.sCenterEnd = targetSCenter;
            mAnim.vFocusStart = sourceToViewCoordinate(targetSCenter);
            mAnim.vFocusEnd = new PointF(
                    vxCenter,
                    vyCenter
            );
            mAnim.duration = duration;
            mAnim.interruptible = interruptible;
            mAnim.easing = easing;
            mAnim.origin = origin;
            mAnim.time = System.currentTimeMillis();
            mAnim.listener = listener;

            if (vFocus != null) {
                // Calculate where translation will be at the end of the anim
                float vTranslateXEnd = vFocus.x - (targetScale * mAnim.sCenterStart.x);
                float vTranslateYEnd = vFocus.y - (targetScale * mAnim.sCenterStart.y);
                ScaleAndTranslate satEnd = new ScaleAndTranslate(targetScale, new PointF(vTranslateXEnd, vTranslateYEnd));
                // Fit the end translation into bounds
                fitToBounds(true, satEnd);
                // Adjust the position of the focus point at end so image will be in bounds
                mAnim.vFocusEnd = new PointF(
                        vFocus.x + (satEnd.vTranslate.x - vTranslateXEnd),
                        vFocus.y + (satEnd.vTranslate.y - vTranslateYEnd)
                );
            }

            invalidate();
        }

    }

    /**
     * An event listener for animations, allows events to be triggered when an animation completes,
     * is aborted by another animation starting, or is aborted by a touch event. Note that none of
     * these events are triggered if the activity is paused, the image is swapped, or in other cases
     * where the view's internal state gets wiped or draw events stop.
     */
    interface OnAnimationEventListener {

        /**
         * The animation has completed, having reached its endpoint.
         */
        void onComplete();

        /**
         * The animation has been aborted before reaching its endpoint because the user touched the screen.
         */
        void onInterruptedByUser();

        /**
         * The animation has been aborted before reaching its endpoint because a new animation has been started.
         */
        void onInterruptedByNewAnim();

    }

    /**
     * Default implementation of {@link OnAnimationEventListener} for extension. This does nothing in any method.
     */
    public static class DefaultOnAnimationEventListener implements OnAnimationEventListener {

        @Override
        public void onComplete() {
        }

        @Override
        public void onInterruptedByUser() {
        }

        @Override
        public void onInterruptedByNewAnim() {
        }

    }

    /**
     * An event listener, allowing subclasses and activities to be notified of significant events.
     */
    interface OnImageEventListener {

        /**
         * Called when the dimensions of the image and view are known, and either a preview image,
         * the full size image, or base layer tiles are loaded. This indicates the scale and translate
         * are known and the next draw will display an image. This event can be used to hide a loading
         * graphic, or inform a subclass that it is safe to draw overlays.
         */
        void onReady();

        /**
         * Called when the full size image is ready. When using tiling, this means the lowest resolution
         * base layer of tiles are loaded, and when tiling is disabled, the image bitmap is loaded.
         * This event could be used as a trigger to enable gestures if you wanted interaction disabled
         * while only a preview is displayed, otherwise for most cases {@link #onReady()} is the best
         * event to listen to.
         */
        void onImageLoaded();

        /**
         * Called when a preview image could not be loaded. This method cannot be relied upon; certain
         * encoding types of supported image formats can result in corrupt or blank images being loaded
         * and displayed with no detectable error. The view will continue to load the full size image.
         *
         * @param e The exception thrown. This error is logged by the view.
         */
        void onPreviewLoadError(Exception e);

        /**
         * Indicates an error initiliasing the decoder when using a tiling, or when loading the full
         * size bitmap when tiling is disabled. This method cannot be relied upon; certain encoding
         * types of supported image formats can result in corrupt or blank images being loaded and
         * displayed with no detectable error.
         *
         * @param e The exception thrown. This error is also logged by the view.
         */
        void onImageLoadError(Exception e);

        /**
         * Called when an image tile could not be loaded. This method cannot be relied upon; certain
         * encoding types of supported image formats can result in corrupt or blank images being loaded
         * and displayed with no detectable error. Most cases where an unsupported file is used will
         * result in an error caught by {@link #onImageLoadError(Exception)}.
         *
         * @param e The exception thrown. This error is logged by the view.
         */
        void onTileLoadError(Exception e);

        /**
         * Called when a bitmap set using ImageSource.cachedBitmap is no longer being used by the View.
         * This is useful if you wish to manage the bitmap after the preview is shown
         */
        void onPreviewReleased();
    }

    /**
     * Default implementation of {@link OnImageEventListener} for extension. This does nothing in any method.
     */
    public static class DefaultOnImageEventListener implements OnImageEventListener {

        @Override
        public void onReady() {
        }

        @Override
        public void onImageLoaded() {
        }

        @Override
        public void onPreviewLoadError(Exception e) {
        }

        @Override
        public void onImageLoadError(Exception e) {
        }

        @Override
        public void onTileLoadError(Exception e) {
        }

        @Override
        public void onPreviewReleased() {
        }

    }

    /**
     * An event listener, allowing activities to be notified of pan and zoom events. Initialisation
     * and calls made by your code do not trigger events; touch events and animations do. Methods in
     * this listener will be called on the UI thread and may be called very frequently - your
     * implementation should return quickly.
     */
    interface OnStateChangedListener {

        /**
         * The scale has changed. Use with {@link #getMaxScale()} and {@link #getMinScale()} to determine
         * whether the image is fully zoomed in or out.
         *
         * @param newScale The new scale.
         * @param origin   Where the event originated from - one of {@link #ORIGIN_ANIM}, {@link #ORIGIN_TOUCH}.
         */
        void onScaleChanged(float newScale, int origin);

        /**
         * The source center has been changed. This can be a result of panning or zooming.
         *
         * @param newCenter The new source center point.
         * @param origin    Where the event originated from - one of {@link #ORIGIN_ANIM}, {@link #ORIGIN_TOUCH}.
         */
        void onCenterChanged(PointF newCenter, int origin);

    }

    /**
     * Default implementation of {@link OnStateChangedListener}. This does nothing in any method.
     */
    public static class DefaultOnStateChangedListener implements OnStateChangedListener {

        @Override
        public void onCenterChanged(PointF newCenter, int origin) {
        }

        @Override
        public void onScaleChanged(float newScale, int origin) {
        }

    }

}
