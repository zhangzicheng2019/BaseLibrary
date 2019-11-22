package com.app.base.ui.view.rv;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;

import androidx.annotation.CallSuper;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Space;

import com.app.base.BuildConfig;
import com.app.base.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class ScrollToLoadAdapter extends RecyclerView.Adapter {
    protected final static int VIEW_TYPE_LOADING = -1000;
    protected final static int VIEW_TYPE_LOAD_FAILED = -1001;
    protected final static int VIEW_TYPE_NO_MORE_RESULTS = -1002;

    private final List<Object> mObjectList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private OnLoadMoreListener mOnLoadMoreListener;
    private ReachBottomScrollListener mReachBottomScrollListener;
    private BottomLoadingTag mBottomLoadingTag;
    private boolean mIsRefreshing;
    @LayoutRes
    private Integer mBottomLoadingLayoutRes;
    @LayoutRes
    private Integer mBottomLoadFailedLayoutRes;
    @LayoutRes
    private Integer mNoMoreResultsLayoutRes;

    public ScrollToLoadAdapter() {
        this(null, false, true);
    }

    public ScrollToLoadAdapter(boolean isShowNoMoreResultsView) {
        this(null, false, isShowNoMoreResultsView);
    }

    public ScrollToLoadAdapter(@NonNull OnLoadMoreListener listener) {
        this(listener, true, true);
    }

    public ScrollToLoadAdapter(@NonNull OnLoadMoreListener listener, boolean isShowNoMoreResultsView) {
        this(listener, true, isShowNoMoreResultsView);
    }

    protected ScrollToLoadAdapter(@Nullable OnLoadMoreListener listener, boolean isShowLoadingView, boolean isShowNoMoreResultsView) {
        if (listener != null) {
            mOnLoadMoreListener = listener;
            mReachBottomScrollListener = new ReachBottomScrollListener();
            mBottomLoadingTag = new BottomLoadingTag(BottomLoadingTag.STATE_DEFAULT, isShowLoadingView, isShowNoMoreResultsView);
        } else {
            mBottomLoadingTag = new BottomLoadingTag(BottomLoadingTag.STATE_NO_MORE_RESULTS, isShowLoadingView, isShowNoMoreResultsView);
        }
    }

    public void update(@Nullable List<?> dataList, boolean noMoreResults) {
        update(null, dataList, noMoreResults);
    }

    public void update(@Nullable List<?> headerList, @Nullable List<?> dataList, boolean noMoreResults) {
        mIsRefreshing = false;
        mObjectList.clear();
        if (headerList != null && !headerList.isEmpty()) {
            mObjectList.addAll(headerList);
        }
        if (dataList != null && !dataList.isEmpty()) {
            mObjectList.addAll(dataList);
        }
        setNoMoreResults(noMoreResults);
        notifyDataSetChanged();
        doLoadMoreIfAtBottom();
    }

    public void addAll(@Nullable List<?> dataList, boolean hasNoMoreResults) {
        if (dataList != null && !dataList.isEmpty()) {
            mObjectList.addAll(dataList);
        }
        setNoMoreResults(hasNoMoreResults);
        notifyDataSetChanged();
    }

    public void addAll(int position, @Nullable List<?> dataList, boolean hasNoMoreResults) {
        if (dataList != null && !dataList.isEmpty()) {
            mObjectList.addAll(position, dataList);
        }
        setNoMoreResults(hasNoMoreResults);
        notifyDataSetChanged();
    }

    public Object getData(int position) {
        if (position >= 0 && position < mObjectList.size()) {
            return mObjectList.get(position);
        }
        return null;
    }

    @SuppressWarnings("unused")
    public List<Object> getDataList() {
        return mObjectList;
    }

    public void notifyPageRefreshing() {
        mIsRefreshing = true;
    }

    public void setBottomLoadingLayoutRes(@LayoutRes int layoutRes) {
        mBottomLoadingLayoutRes = layoutRes;
    }

    public void setBottomLoadFailedLayoutRes(@LayoutRes int layoutRes) {
        mBottomLoadFailedLayoutRes = layoutRes;
    }

    public void setNoMoreResultsLayoutRes(@LayoutRes int layoutRes) {
        mNoMoreResultsLayoutRes = layoutRes;
    }

    public void setLoadMoreFailed() {
        if (mOnLoadMoreListener != null) {
            updateLoadingViewState(BottomLoadingTag.STATE_LOAD_FAILED, true);
        }
    }

    public void setBottomLoadingViewIsVisible(boolean isVisible) {
        if (mBottomLoadingTag != null && isVisible != mBottomLoadingTag.isVisible()) {
            mBottomLoadingTag.setIsVisible(isVisible);
            notifyDataSetChanged();
        }
    }

    public void removeItemWithAutoLoadingMore(int index) {
        mObjectList.remove(index);
        notifyItemRemoved(index);

        if (mRecyclerView != null) {
            final RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mReachBottomScrollListener.tryToLoadMore((LinearLayoutManager) layoutManager);
                    }
                });
            }
        }
    }

    protected void setNoMoreResults(boolean hasNoMoreResults) {
        if (hasNoMoreResults) {
            updateLoadingViewState(BottomLoadingTag.STATE_NO_MORE_RESULTS, false);
        } else {
            updateLoadingViewState(BottomLoadingTag.STATE_DEFAULT, false);
        }
    }

    private void doLoadMoreIfAtBottom() {
        if (mOnLoadMoreListener == null || mRecyclerView == null) {
            return;
        }

        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                Context context = mRecyclerView.getContext();
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    if (!activity.isFinishing()) {
                        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
                        if (layoutManager instanceof LinearLayoutManager) {
                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                            int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
                            int listSize = mObjectList.size();
                            if (listSize > 0 && lastPosition > 0 && lastPosition == listSize
                                    && getItemViewType(lastPosition) == VIEW_TYPE_LOADING
                                    && !mIsRefreshing
                                    && mBottomLoadingTag.getState() == BottomLoadingTag.STATE_DEFAULT) {
                                mOnLoadMoreListener.onLoadMore();
                            }
                        }
                    }
                }
            }
        });
    }

    private void updateLoadingViewState(@BottomLoadingTag.State int state, boolean isNeedNotify) {
        if (mBottomLoadingTag != null && mBottomLoadingTag.getState() != state) {
            mBottomLoadingTag.setState(state);
            if (isNeedNotify) {
                notifyDataSetChanged();
            }
        }
    }

    private boolean isShowBottomLoadingView() {
        return mBottomLoadingTag.isVisible() &&
                (mBottomLoadingTag.isShowLoadingView() ||
                        mBottomLoadingTag.isShowLoadFailedView() ||
                        mBottomLoadingTag.isShowNoMoreResultsView());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
        if (mReachBottomScrollListener != null) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager == null) {
                throw new RuntimeException("You must setLayoutManager before setAdapter");
            }

            if (layoutManager instanceof GridLayoutManager) {
                final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
                if (spanSizeLookup != null) {
                    gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            int viewType = getItemViewType(position);
                            if (viewType == VIEW_TYPE_LOADING ||
                                    viewType == VIEW_TYPE_LOAD_FAILED ||
                                    viewType == VIEW_TYPE_NO_MORE_RESULTS) {
                                return gridLayoutManager.getSpanCount();
                            } else {
                                return spanSizeLookup.getSpanSize(position);
                            }
                        }
                    });
                }
            }

            recyclerView.addOnScrollListener(mReachBottomScrollListener);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        if (mReachBottomScrollListener != null) {
            recyclerView.removeOnScrollListener(mReachBottomScrollListener);
        }
    }

    @Override
    @CallSuper
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LOADING) {
            int layoutRes = (mBottomLoadingLayoutRes != null) ? mBottomLoadingLayoutRes : R.layout.layout_bottom_loading;
            View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
            return new LoadingViewHolder(itemView);
        } else if (viewType == VIEW_TYPE_LOAD_FAILED) {
            int layoutRes = (mBottomLoadFailedLayoutRes != null) ? mBottomLoadFailedLayoutRes : R.layout.layout_bottom_load_failed;
            View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
            return new LoadingFailedViewHolder(itemView);
        } else if (viewType == VIEW_TYPE_NO_MORE_RESULTS) {
            int layoutRes = (mNoMoreResultsLayoutRes != null) ? mNoMoreResultsLayoutRes : R.layout.layout_no_more_results;
            View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
            return new NoMoreResultsViewHolder(itemView);
        }
        if (BuildConfig.DEBUG) {
            Log.e("ScrollToLoadAdapter", "unable to handle onCreateViewHolder = [" + viewType + "]");
        }
        return new RecyclerView.ViewHolder(new Space(parent.getContext())) {};
    }

    @Override
    @CallSuper
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadingViewHolder) {
            ((LoadingViewHolder) holder).update();
        } else if (holder instanceof LoadingFailedViewHolder) {
            ((LoadingFailedViewHolder) holder).update();
        } else if (holder instanceof NoMoreResultsViewHolder) {
            ((NoMoreResultsViewHolder) holder).update();
        }
    }

    @Override
    public final int getItemCount() {
        return mObjectList.size() == 0 ? 0 : (mObjectList.size() + (isShowBottomLoadingView() ? 1 : 0));
    }

    @Override
    @CallSuper
    public int getItemViewType(int position) {
        if (position == mObjectList.size() && isShowBottomLoadingView()) {
            int state = mBottomLoadingTag.getState();
            if (mBottomLoadingTag.isShowLoadingView()) {
                return VIEW_TYPE_LOADING;
            } else if (mBottomLoadingTag.isShowLoadFailedView()) {
                return VIEW_TYPE_LOAD_FAILED;
            } else if (mBottomLoadingTag.isShowNoMoreResultsView() && state == BottomLoadingTag.STATE_NO_MORE_RESULTS) {
                return VIEW_TYPE_NO_MORE_RESULTS;
            }
        }
        if (BuildConfig.DEBUG) {
            Log.e("ScrollToLoadAdapter", "unable to getItemViewType:  position = [" + position + "],Model---->" + getData(position));
        }
        return super.getItemViewType(position);
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar mProgressBar;

        private LoadingViewHolder(View itemView) {
            super(itemView);
            if (mBottomLoadingLayoutRes == null) {
                mProgressBar = (ProgressBar) itemView.findViewById(R.id.pb_loading);
            }
        }

        public void update() {
            if (mProgressBar != null) {
                int color = ContextCompat.getColor(itemView.getContext(), R.color.colorPrimaryDark);
                mProgressBar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        }
    }

    private class LoadingFailedViewHolder extends RecyclerView.ViewHolder {

        private LoadingFailedViewHolder(View itemView) {
            super(itemView);
        }

        public void update() {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnLoadMoreListener != null && !mIsRefreshing) {
                        updateLoadingViewState(BottomLoadingTag.STATE_LOADING, true);
                        mOnLoadMoreListener.onLoadMore();
                    }
                }
            });
        }
    }

    private class NoMoreResultsViewHolder extends RecyclerView.ViewHolder {

        private NoMoreResultsViewHolder(View itemView) {
            super(itemView);
        }

        public void update() {
            //no-op
        }
    }

    private class ReachBottomScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            //When the RecyclerView re-lays out, dy will be zero.
            if (mIsRefreshing || mObjectList.isEmpty() ||
                    mBottomLoadingTag.getState() != BottomLoadingTag.STATE_DEFAULT) {
                return;
            }

            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                boolean reverseLayout = linearLayoutManager.getReverseLayout();
                if ((!reverseLayout && dy > 0) || (reverseLayout && dy < 0)) {
                    tryToLoadMore(linearLayoutManager);
                }
            }
        }

        public void tryToLoadMore(LinearLayoutManager layoutManager) {
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
            if (visibleItemCount + pastVisibleItems + 1 >= totalItemCount) {
                mBottomLoadingTag.setState(BottomLoadingTag.STATE_LOADING);
                mOnLoadMoreListener.onLoadMore();
            }
        }
    }

    public static class BottomLoadingTag {
        public static final int STATE_DEFAULT = 0;
        public static final int STATE_LOADING = 1;
        public static final int STATE_LOAD_FAILED = 2;
        public static final int STATE_NO_MORE_RESULTS = 3;

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({STATE_DEFAULT, STATE_LOADING, STATE_LOAD_FAILED, STATE_NO_MORE_RESULTS})
        public @interface State {

        }

        @BottomLoadingTag.State
        private int mState;
        private boolean mIsVisible = true;
        private boolean mIsShowLoadingView;
        private boolean mIsShowNoMoreResultsView;

        public BottomLoadingTag(@BottomLoadingTag.State int state, boolean isShowLoadingView, boolean isShowNoMoreResultsView) {
            this.mState = state;
            this.mIsShowLoadingView = isShowLoadingView;
            this.mIsShowNoMoreResultsView = isShowNoMoreResultsView;
        }

        public int getState() {
            return mState;
        }

        public void setState(@BottomLoadingTag.State int state) {
            this.mState = state;
        }

        public boolean isShowLoadingView() {
            return mIsShowLoadingView &&
                    (mState == BottomLoadingTag.STATE_DEFAULT || mState == BottomLoadingTag.STATE_LOADING);
        }

        public boolean isShowLoadFailedView() {
            return mIsShowLoadingView && (mState == BottomLoadingTag.STATE_LOAD_FAILED);
        }

        public boolean isShowNoMoreResultsView() {
            return mIsShowNoMoreResultsView && (mState == BottomLoadingTag.STATE_NO_MORE_RESULTS);
        }

        public boolean isVisible() {
            return mIsVisible;
        }

        public void setIsVisible(boolean visible) {
            this.mIsVisible = visible;
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}