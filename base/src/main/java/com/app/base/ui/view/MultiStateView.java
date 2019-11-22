package com.app.base.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import androidx.annotation.AttrRes;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.app.base.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * View that contains 4 different states: Content, Error, Empty, and Loading.<br>
 * Each state has their own separate layout which can be shown/hidden by setting
 * the {@link MultiStateView.ViewState} accordingly
 * Every MultiStateView <b><i>MUST</i></b> contain a content view. The content view
 * is obtained from whatever is inside of the tags of the view via its XML declaration
 */
public class MultiStateView extends FrameLayout {

    private static final String TAG_EMPTY = "empty";
    private static final String TAG_LOADING = "loading";
    private static final String TAG_ERROR = "error";
    private static final String TAG_NO_NETWORK = "no_network";

    public static final int VIEW_STATE_UNKNOWN = -1;

    public static final int VIEW_STATE_CONTENT = 0;

    public static final int VIEW_STATE_ERROR = 1;

    public static final int VIEW_STATE_EMPTY = 2;

    public static final int VIEW_STATE_LOADING = 3;

    public static final int VIEW_STATE_NO_NETWORK = 4;
    private int mLoadingViewResId;
    private int mEmptyViewResId;
    private int mErrorViewResId;
    private int mNoNetworkViewResId;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VIEW_STATE_UNKNOWN, VIEW_STATE_CONTENT, VIEW_STATE_ERROR, VIEW_STATE_EMPTY, VIEW_STATE_LOADING, VIEW_STATE_NO_NETWORK})
    public @interface ViewState {
    }

    private LayoutInflater mInflater;

    private View mContentView;

    private View mLoadingView;

    private View mErrorView;

    private View mEmptyView;

    private View mNoNetworkView;

    private boolean mAnimateViewChanges = false;

    @Nullable
    private StateListener mListener;

    @ViewState
    private int mViewState = VIEW_STATE_UNKNOWN;

    public MultiStateView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public MultiStateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MultiStateView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MultiStateView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mInflater = LayoutInflater.from(getContext());
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MultiStateView);

        mLoadingViewResId = a.getResourceId(R.styleable.MultiStateView_msvLoadingView, -1);

        mEmptyViewResId = a.getResourceId(R.styleable.MultiStateView_msvEmptyView, -1);

        mErrorViewResId = a.getResourceId(R.styleable.MultiStateView_msvErrorView, -1);

        mNoNetworkViewResId = a.getResourceId(R.styleable.MultiStateView_msvNoNetworkView, -1);

        int viewState = a.getInt(R.styleable.MultiStateView_msvViewState, VIEW_STATE_CONTENT);
        mAnimateViewChanges = a.getBoolean(R.styleable.MultiStateView_msvAnimateViewChanges, false);

        switch (viewState) {
            case VIEW_STATE_CONTENT:
                mViewState = VIEW_STATE_CONTENT;
                break;

            case VIEW_STATE_ERROR:
                mViewState = VIEW_STATE_ERROR;
                break;

            case VIEW_STATE_EMPTY:
                mViewState = VIEW_STATE_EMPTY;
                break;

            case VIEW_STATE_LOADING:
                mViewState = VIEW_STATE_LOADING;
                break;

            case VIEW_STATE_NO_NETWORK:
                mViewState = VIEW_STATE_NO_NETWORK;
                break;
            default:
                mViewState = VIEW_STATE_UNKNOWN;
                break;
        }

        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mContentView == null) throw new IllegalArgumentException("Content view is not defined");
        setView(VIEW_STATE_UNKNOWN);
    }

    /* All of the addView methods have been overridden so that it can obtain the content view via XML
     It is NOT recommended to add views into MultiStateView via the addView methods, but rather use
     any of the setViewForState methods to set views for their given ViewState accordingly */
    @Override
    public void addView(View child) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, index, params);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, width, height);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        return super.addViewInLayout(child, index, params);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        if (isValidContentView(child)) mContentView = child;
        return super.addViewInLayout(child, index, params, preventRequestLayout);
    }

    /**
     * Returns the {@link View} associated with the {@link ViewState}
     *
     * @param state The {@link ViewState} with to return the view for
     * @return The {@link View} associated with the {@link ViewState}, null if no view is present
     */
    @Nullable
    public View getView(@ViewState int state) {
        switch (state) {
            case VIEW_STATE_LOADING:
                if (mLoadingView == null) {
                    mLoadingView = inflateView(mLoadingViewResId, VIEW_STATE_LOADING, TAG_LOADING);
                }
                return mLoadingView;
            case VIEW_STATE_CONTENT:
                return mContentView;
            case VIEW_STATE_EMPTY:
                if (mEmptyView == null) {
                    mEmptyView = inflateView(mEmptyViewResId, VIEW_STATE_EMPTY, TAG_EMPTY);
                }
                return mEmptyView;

            case VIEW_STATE_ERROR:
                if (mErrorView == null) {
                    mErrorView = inflateView(mErrorViewResId, VIEW_STATE_ERROR, TAG_ERROR);
                }
                return mErrorView;
            case VIEW_STATE_NO_NETWORK:
                if (mNoNetworkView == null) {
                    mNoNetworkView = inflateView(mNoNetworkViewResId, VIEW_STATE_NO_NETWORK, TAG_NO_NETWORK);
                }
                return mNoNetworkView;
            default:
                return null;
        }
    }

    /**
     * Returns the current {@link ViewState}
     *
     * @return
     */
    @ViewState
    public int getViewState() {
        return mViewState;
    }

    /**
     * Sets the current {@link ViewState}
     *
     * @param state The {@link ViewState} to set {@link MultiStateView} to
     */
    public void setViewState(@ViewState int state) {
        if (state != mViewState) {
            int previous = mViewState;
            mViewState = state;
            setView(previous);
            if (mListener != null) mListener.onStateChanged(mViewState);
        }
    }

    private OnClickListener mOnClickListener;
    public void setOnClickRetryListener(OnClickListener onClickListener){
        mOnClickListener = onClickListener;
    }

    /**
     * Shows the {@link View} based on the {@link ViewState}
     */
    private void setView(@ViewState int previousState) {
        switch (mViewState) {
            case VIEW_STATE_LOADING:
                if (mLoadingView == null) {
                    mLoadingView = inflateView(mLoadingViewResId, VIEW_STATE_LOADING, TAG_LOADING);
                }

                if (mLoadingView == null) {
                    throw new NullPointerException("Loading View");
                }

                if (mContentView != null) mContentView.setVisibility(View.GONE);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                if (mNoNetworkView != null) mNoNetworkView.setVisibility(View.GONE);

                if (mAnimateViewChanges) {
                    animateLayoutChange(getView(previousState));
                } else {
                    mLoadingView.setVisibility(View.VISIBLE);
                }
                break;

            case VIEW_STATE_EMPTY:

                if (mEmptyView == null) {
                    mEmptyView = inflateView(mEmptyViewResId, VIEW_STATE_EMPTY, TAG_EMPTY);
                }

                if (mEmptyView == null) {
                    throw new NullPointerException("Empty View");
                }

                if(mOnClickListener != null){
                    mEmptyView.setOnClickListener(mOnClickListener);
                }

                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                if (mContentView != null) mContentView.setVisibility(View.GONE);
                if (mNoNetworkView != null) mNoNetworkView.setVisibility(View.GONE);

                if (mAnimateViewChanges) {
                    animateLayoutChange(getView(previousState));
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
                break;

            case VIEW_STATE_ERROR:
                if (mErrorView == null) {
                    mErrorView = inflateView(mErrorViewResId, VIEW_STATE_ERROR, TAG_ERROR);
                }

                if (mErrorView == null) {
                    throw new NullPointerException("Error View");
                }

                if(mOnClickListener != null){
                    mErrorView.setOnClickListener(mOnClickListener);
                }

                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                if (mContentView != null) mContentView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                if (mNoNetworkView != null) mNoNetworkView.setVisibility(View.GONE);

                if (mAnimateViewChanges) {
                    animateLayoutChange(getView(previousState));
                } else {
                    mErrorView.setVisibility(View.VISIBLE);
                }
                break;

            case VIEW_STATE_NO_NETWORK:
                if (mNoNetworkView == null) {
                    mNoNetworkView = inflateView(mNoNetworkViewResId, VIEW_STATE_NO_NETWORK, TAG_NO_NETWORK);
                }

                if (mNoNetworkView == null) {
                    throw new NullPointerException("NoNetworkView View");
                }

                if(mOnClickListener != null){
                    mNoNetworkView.setOnClickListener(mOnClickListener);
                }

                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                if (mContentView != null) mContentView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);

                if (mAnimateViewChanges) {
                    animateLayoutChange(getView(previousState));
                } else {
                    mNoNetworkView.setVisibility(View.VISIBLE);
                }
                break;

            case VIEW_STATE_CONTENT:
            default:
                if (mContentView == null) {
                    // Should never happen, the view should throw an exception if no content view is present upon creation
                    throw new NullPointerException("Content View");
                }

                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                if (mNoNetworkView != null) mContentView.setVisibility(View.GONE);

                if (mAnimateViewChanges) {
                    animateLayoutChange(getView(previousState));
                } else {
                    mContentView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    /**
     * Checks if the given {@link View} is valid for the Content View
     *
     * @param view The {@link View} to check
     * @return
     */
    private boolean isValidContentView(View view) {
        if (mContentView != null && mContentView != view) {
            return false;
        }
        Object tag = view.getTag(R.id.tag_multi_state_view);
        if (tag == null) {
            return true;
        }
        if (tag instanceof String) {
            String viewTag = (String) tag;
            if (TextUtils.equals(viewTag, TAG_EMPTY)
                    || TextUtils.equals(viewTag, TAG_ERROR)
                    || TextUtils.equals(viewTag, TAG_LOADING)
                    ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the view for the given view state
     *
     * @param view          The {@link View} to use
     * @param state         The {@link ViewState}to set
     * @param switchToState If the {@link ViewState} should be switched to
     */
    public void setViewForState(View view, @ViewState int state, boolean switchToState) {
        switch (state) {
            case VIEW_STATE_LOADING:
                if (mLoadingView != null) removeView(mLoadingView);
                mLoadingView = view;
                mLoadingView.setTag(R.id.tag_multi_state_view, TAG_LOADING);
                addView(mLoadingView);
                break;

            case VIEW_STATE_EMPTY:
                if (mEmptyView != null) removeView(mEmptyView);
                mEmptyView = view;
                mEmptyView.setTag(R.id.tag_multi_state_view, TAG_EMPTY);
                addView(mEmptyView);
                break;

            case VIEW_STATE_ERROR:
                if (mErrorView != null) removeView(mErrorView);
                mErrorView = view;
                mErrorView.setTag(R.id.tag_multi_state_view, TAG_ERROR);
                addView(mErrorView);
                break;

            case VIEW_STATE_NO_NETWORK:
                if (mNoNetworkView != null) removeView(mNoNetworkView);
                mNoNetworkView = view;
                mNoNetworkView.setTag(R.id.tag_multi_state_view, TAG_NO_NETWORK);
                addView(mNoNetworkView);
                break;

            case VIEW_STATE_CONTENT:
                if (mContentView != null) removeView(mContentView);
                mContentView = view;
                addView(mContentView);
                break;
        }

        setView(VIEW_STATE_UNKNOWN);
        if (switchToState) setViewState(state);
    }

    /**
     * Sets the {@link View} for the given {@link ViewState}
     *
     * @param view  The {@link View} to use
     * @param state The {@link ViewState} to set
     */
    public void setViewForState(View view, @ViewState int state) {
        setViewForState(view, state, false);
    }

    /**
     * Sets the {@link View} for the given {@link ViewState}
     *
     * @param layoutRes     Layout resource id
     * @param state         The {@link ViewState} to set
     * @param switchToState If the {@link ViewState} should be switched to
     */
    public void setViewForState(@LayoutRes int layoutRes, @ViewState int state, boolean switchToState) {
        if (mInflater == null) mInflater = LayoutInflater.from(getContext());
        View view = mInflater.inflate(layoutRes, this, false);
        setViewForState(view, state, switchToState);
    }

    /**
     * Sets the {@link View} for the given {@link ViewState}
     *
     * @param layoutRes Layout resource id
     * @param state     The {@link View} state to set
     */
    public void setViewForState(@LayoutRes int layoutRes, @ViewState int state) {
        setViewForState(layoutRes, state, false);
    }

    /**
     * Sets whether an animate will occur when changing between {@link ViewState}
     *
     * @param animate
     */
    public void setAnimateLayoutChanges(boolean animate) {
        mAnimateViewChanges = animate;
    }

    /**
     * Sets the {@link StateListener} for the view
     *
     * @param listener The {@link StateListener} that will receive callbacks
     */
    public void setStateListener(StateListener listener) {
        mListener = listener;
    }

    /**
     * Animates the layout changes between {@link ViewState}
     *
     * @param previousView The view that it was currently on
     */
    private void animateLayoutChange(@Nullable final View previousView) {
        if (previousView == null) {
            getView(mViewState).setVisibility(View.VISIBLE);
            return;
        }

        previousView.setVisibility(View.VISIBLE);
        ObjectAnimator anim = ObjectAnimator.ofFloat(previousView, "alpha", 1.0f, 0.0f).setDuration(250L);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                previousView.setVisibility(View.GONE);
                getView(mViewState).setVisibility(View.VISIBLE);
                ObjectAnimator.ofFloat(getView(mViewState), "alpha", 0.0f, 1.0f).setDuration(250L).start();
            }
        });
        anim.start();
    }

    private View inflateView(int viewResId, int viewState, String tag){
        View view = null;
        if (viewResId > -1) {
            view = mInflater.inflate(viewResId, this, false);
            view.setTag(R.id.tag_multi_state_view, tag);
            addView(view, view.getLayoutParams());
            if (mListener != null) mListener.onStateInflated(viewState, view);

            if (mViewState != viewState) {
                view.setVisibility(GONE);
            }
        }
        return view;
    }

    public interface StateListener {
        /**
         * Callback for when the {@link ViewState} has changed
         *
         * @param viewState The {@link ViewState} that was switched to
         */
        void onStateChanged(@ViewState int viewState);

        /**
         * Callback for when a {@link ViewState} has been inflated
         *
         * @param viewState The {@link ViewState} that was inflated
         * @param view      The {@link View} that was inflated
         */
        void onStateInflated(@ViewState int viewState, @NonNull View view);
    }
}
