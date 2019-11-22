package com.app.base.ui.view.flowlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.app.base.R;
import com.google.android.material.internal.FlowLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressLint("RestrictedApi")
public class TagFlowLayout extends FlowLayout {

    private Adapter mAdapter;
    private int mSelectedMax;
    private List<Tag> mSelectedList = new ArrayList<>();
    private OnTagClickListener mOnTagClickListener;
    private boolean isUpdateTagView = true;

    public TagFlowLayout(Context context) {
        this(context, null);
    }

    public TagFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout);
        mSelectedMax = ta.getInt(R.styleable.TagFlowLayout_maxSelect, -1);
        ta.recycle();
    }
    
    public void setAdapter(Adapter adapter){
        if(adapter != null){
            mAdapter = adapter;
            addTagViews();
        }
    }

    public void setOnTagClickListener(OnTagClickListener onTagClickListener, boolean isUpdateTagView){
        mOnTagClickListener = onTagClickListener;
        this.isUpdateTagView = isUpdateTagView;
    }
    
    private void addTagViews(){
        removeAllViews();
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            final Tag tag = mAdapter.getItem(i);
            tag.setIndex(i);
            final View tagView = mAdapter.getItemView(this, i, tag);
            tagView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnTagClickListener != null){
                        mOnTagClickListener.onTagClick(tag.getIndex());
                    }
                    if(isUpdateTagView){
                        updateTagView(tagView, tag);
                    }
                }
            });
            addView(tagView);
        }
    }

    private void updateTagView(View tagView, Tag tag) {
        if (!tagView.isSelected()) {
            //处理max_select=1的情况
            if (mSelectedMax == 1 && mSelectedList.size() == 1) {
                Iterator<Tag> iterator = mSelectedList.iterator();
                Tag next = iterator.next();
                View preTagView = getChildAt(next.getIndex());
                tagView.setSelected(true);
                preTagView.setSelected(false);
                mSelectedList.remove(next);
                mSelectedList.add(tag);
            } else {
                if (mSelectedMax > 0 && mSelectedList.size() >= mSelectedMax) {
                    return;
                }
                tagView.setSelected(true);
                mSelectedList.add(tag);
            }
        } else {
            tagView.setSelected(false);
            mSelectedList.remove(tag);
        }
    }
    
    private void updateTagViews(){
        if(mSelectedList == null || mSelectedList.size() == 0){
            return;
        }
        for (int i = 0; i < mSelectedList.size(); i++) {
            int index = mSelectedList.get(i).getIndex();
            View tagView = getChildAt(index);
            if(tagView != null){
                tagView.setSelected(true);
            }
        }
    }
    
    public List<Tag> getSelectedList(){
        return mSelectedList;
    }

    public void setSelectedList(List<Tag> tagList){
        mSelectedList = tagList;
        updateTagViews();
    }

    public void setmSelectedMax(int selectedMax){
        mSelectedMax = selectedMax;
    }

    public abstract static class Adapter<V extends View, M extends Tag>{

        List<M> mDataList;

        public Adapter(List<M> dataList){
            mDataList = dataList;
        }

        public abstract V getItemView(FlowLayout flowLayout, int position, M data);

        int getItemCount(){
            return mDataList != null ? mDataList.size() : 0;
        }

        M getItem(int position){
            return mDataList.get(position);
        }

    }

    public interface OnTagClickListener{

        void onTagClick(int index);

    }
}
