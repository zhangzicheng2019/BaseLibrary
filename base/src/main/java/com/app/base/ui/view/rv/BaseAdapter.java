package com.app.base.ui.view.rv;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.base.R;
import com.app.base.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class BaseAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected static final int VIEW_TYPE_HEADER = 10;
    protected static final int VIEW_TYPE_NORMAL = 11;
    protected static final int VIEW_TYPE_FOOTER = 12;

    protected Context mContext;

    private View headerView;
    private boolean showLoadingView = false;
    private int mPageLimit = 10;
    private boolean showNoMoreText = false;
    private String mNoMoreText = null;
    private boolean isLoading;
    private OnLoadMoreListener mOnLoadMoreListener;
    private ScrollBottomListener mScrollBottomListener;
    private RecyclerView recyclerView;
    private List<T> mDataList = new ArrayList<>();

    public BaseAdapter(Context context){
        mContext = context;
    }

    public void update(@Nullable List<T> dataList){
        isLoading = false;
        showNoMoreText = dataList == null || (dataList.size() < mPageLimit);
        mDataList.clear();
        if(dataList != null && dataList.size() > 0){
            removeNullElement(dataList);
            mDataList.addAll(dataList);
        }
        notifyDataSetChanged();
    }

    public void add(@Nullable List<T> dataList){
        isLoading = false;
        showNoMoreText = dataList == null || (dataList.size() < mPageLimit);
        if(dataList != null && dataList.size() > 0){
            removeNullElement(dataList);
            mDataList.addAll(dataList);
        }
        notifyDataSetChanged();
    }

    public void insertItem(@NonNull T item){
        mDataList.add(item);
        notifyItemInserted(showLoadingView ? getItemCount() - 2 : getItemCount() - 1);
    }

    public void removeIndex(int position){
        mDataList.remove(position);
        notifyItemRemoved(position);
    }

    public void updateItem(@NonNull T item){
        int index = mDataList.indexOf(item);
        if(index > -1){
            mDataList.set(index, item);
            notifyItemChanged(headerView != null ? index + 1 : index);
        }
    }

    public void removeItem(@NonNull T item){
        int index = mDataList.indexOf(item);
        if(index != -1){
            mDataList.remove(index);
            notifyItemRemoved(headerView != null ? index + 1 : index);
        }
    }

    private void removeNullElement(List<?> list){
        if(list == null){
            return;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            if(list.get(i) == null){
                list.remove(i);
            }
        }
    }

    /**
     * @link getData(position)
     *
     * */
    @Deprecated
    public List<T> getDataList(){
        return mDataList;
    }

    public void setHeaderView(View headerView){
        this.headerView = headerView;
    }

    public void setLoading(boolean isLoading){
        this.isLoading = isLoading;
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(recyclerView != null){
                    notifyItemChanged(getItemCount());
                }
            }
        }, this.isLoading ? 0 : 1000);
    }

    public void setPageLimit(int pageLimit){
        this.mPageLimit = pageLimit;
    }

    public void setNoMoreText(@Nullable String noMoreText){
        this.mNoMoreText = noMoreText;
    }

    public boolean isLoading(){
        return isLoading;
    }

    /**
     * 此方法需要在RecyclerView.setAdapter()前调用，否则可能会不生效
     *
     * */
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener){
        setOnLoadMoreListener(onLoadMoreListener, null);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener, @Nullable String noMoreText){
        showLoadingView = true;
        mOnLoadMoreListener = onLoadMoreListener;
        mNoMoreText = noMoreText;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_HEADER){
            HeaderViewHolder viewHolder = new HeaderViewHolder(headerView);
            viewHolder.setIsRecyclable(false);
            return viewHolder;
        } else if(viewType == VIEW_TYPE_FOOTER){
            View loadingView = LayoutInflater.from(mContext).inflate(R.layout.layout_bottom_loading, parent, false);
            FooterViewHolder viewHolder = new FooterViewHolder(loadingView);
            viewHolder.setIsRecyclable(false);
            return viewHolder;
        }
        return null;
    }

    @CallSuper
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        LogUtils.d("onBindViewHolder", "isFooter=" + (getItemViewType(position) == VIEW_TYPE_FOOTER));
        if(getItemViewType(position) == VIEW_TYPE_FOOTER){
            ((FooterViewHolder) viewHolder).update(isLoading, showNoMoreText);
        }
    }

    public T getData(int position){
        if(headerView != null){
            return mDataList.get(position - 1);
        } else {
            return mDataList.get(position);
        }
    }

    @Override
    public int getItemCount() {
        return (headerView != null ? 1 : 0) +
                (mDataList != null && mDataList.size() > 0 ? mDataList.size() + (showLoadingView ? 1 : 0): 0);
    }

    @Override
    public int getItemViewType(int position) {
        if(headerView != null && position == 0){
            return VIEW_TYPE_HEADER;
        }
        if(showLoadingView && position == getItemCount() - 1){
            return VIEW_TYPE_FOOTER;
        }
        return VIEW_TYPE_NORMAL;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (getItemViewType(position) == VIEW_TYPE_HEADER || getItemViewType(position) == VIEW_TYPE_FOOTER)
                            ? 1 : gridManager.getSpanCount();
                }
            });
        }
        if(mOnLoadMoreListener != null && mScrollBottomListener == null){
            mScrollBottomListener = new ScrollBottomListener();
            recyclerView.addOnScrollListener(mScrollBottomListener);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        if (mScrollBottomListener != null) {
            recyclerView.removeOnScrollListener(mScrollBottomListener);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
            int position = holder.getLayoutPosition();
            if (getItemViewType(position) == VIEW_TYPE_HEADER || getItemViewType(position) == VIEW_TYPE_FOOTER) {
                params.setFullSpan(true);
            }
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        private TextView tvNoMore;
        private ProgressBar pbLoading;
        private View vLine;

        FooterViewHolder(View itemView) {
            super(itemView);
            tvNoMore = itemView.findViewById(R.id.tv_no_more);
            pbLoading = itemView.findViewById(R.id.pb_loading);
            vLine = itemView.findViewById(R.id.v_line);
        }

        private void update(boolean isLoading, boolean showNoMoreText){
            if(!isLoading){
                tvNoMore.setText(showNoMoreText ? mNoMoreText : null);
                vLine.setVisibility(showNoMoreText && !TextUtils.isEmpty(mNoMoreText) ? View.VISIBLE : View.GONE);
            } else {
                tvNoMore.setText(null);
                vLine.setVisibility(View.GONE);
            }
            pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private class ScrollBottomListener extends RecyclerView.OnScrollListener {

        private int[] lastPositions;
        private int lastVisibleItemPosition;

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (isLoading) {
                return;
            }
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if(layoutManager == null){
                return;
            }
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof LinearLayoutManager) {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                if (lastPositions == null) {
                    lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                }
                staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                lastVisibleItemPosition = findMax(lastPositions);
            }

            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            LogUtils.i("onScrollStateChanged", "visibleItemCount=" + visibleItemCount + ", totalItemCount=" + totalItemCount +
                    ", lastVisibleItemPosition=" + lastVisibleItemPosition + ", dy=" + dy);

            if (dy > 10 && visibleItemCount > 0 && lastVisibleItemPosition >= totalItemCount - 2) {
                setLoading(true);
                mOnLoadMoreListener.onLoadMore();
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);


        }

        private int findMax(int[] lastPositions) {
            int max = lastPositions[0];
            for (int value : lastPositions) {
                if (value > max) {
                    max = value;
                }
            }
            return max;
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
