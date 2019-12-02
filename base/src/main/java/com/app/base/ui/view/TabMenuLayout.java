package com.app.base.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.app.base.R;

import java.util.List;

public class TabMenuLayout extends LinearLayout {

    private static final int TAB_MENU_WIDTH_MODEL_FIXED = 1;
    private static final int TAB_MENU_WIDTH_MODEL_SCROLL = 2;

    private OnSelectMenuListener mOnSelectMenuListener;
    private OnCreateMenuListener mOnCreateMenuListener;

    private int mSpace;
    private int mMenuId;
    private int mMenuWidth;
    private int mMenuHeight;
    private int mSelectedIndex;
    private int mTabMenuModel = TAB_MENU_WIDTH_MODEL_FIXED;
    private LinearLayout scrollChildView;

    public TabMenuLayout(Context context) {
        this(context, null);
    }

    public TabMenuLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public TabMenuLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabMenuLayout);
            mSpace = a.getDimensionPixelSize(R.styleable.TabMenuLayout_space, 0);
            mMenuWidth = a.getLayoutDimension(R.styleable.TabMenuLayout_menuWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            mMenuHeight = a.getLayoutDimension(R.styleable.TabMenuLayout_menuHeight, ViewGroup.LayoutParams.WRAP_CONTENT);
            mMenuId = a.getResourceId(R.styleable.TabMenuLayout_menuId, 0);
            mTabMenuModel = a.getInteger(R.styleable.TabMenuLayout_model, TAB_MENU_WIDTH_MODEL_FIXED);
            a.recycle();
        }

        if(mTabMenuModel == TAB_MENU_WIDTH_MODEL_SCROLL){
            scrollChildView = new LinearLayout(getContext());
            if(getOrientation() == HORIZONTAL){
                scrollChildView.setOrientation(HORIZONTAL);
                HorizontalScrollView scrollView = new HorizontalScrollView(getContext());
                scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                scrollView.setOverScrollMode(OVER_SCROLL_NEVER);
                scrollView.setHorizontalScrollBarEnabled(false);
                scrollChildView.setLayoutParams(new HorizontalScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                scrollView.addView(scrollChildView);
                addView(scrollView);
            } else {
                scrollChildView.setOrientation(VERTICAL);
                ScrollView scrollView = new ScrollView(getContext());
                scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                scrollView.setOverScrollMode(OVER_SCROLL_NEVER);
                scrollView.setVerticalScrollBarEnabled(false);
                scrollChildView.setLayoutParams(new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                scrollView.addView(scrollChildView);
                addView(scrollView);
            }
        }
    }

    /**
     * 需要在setData之前调用，否则默认select不会触发
     *
     * */
    public void setOnSelectMenuListener(OnSelectMenuListener onSelectMenuListener){
        this.mOnSelectMenuListener = onSelectMenuListener;
    }

    public void setOnCreateMenuListener(OnCreateMenuListener onCreateMenuListener){
        this.mOnCreateMenuListener = onCreateMenuListener;
    }

    public void setMenuEnabled(List<Boolean> enabledList){
        int childCount;
        if(mTabMenuModel == TAB_MENU_WIDTH_MODEL_SCROLL){
            childCount = scrollChildView.getChildCount();
        } else {
            childCount = getChildCount();
        }
        if(childCount != enabledList.size()){
            throw new IllegalStateException("The number of property Settings must equal the number of menus !");
        }
        for (int i = 0; i < enabledList.size(); i++) {
            if(mTabMenuModel == TAB_MENU_WIDTH_MODEL_SCROLL){
                scrollChildView.getChildAt(i).setEnabled(enabledList.get(i));
            } else {
                getChildAt(i).setEnabled(enabledList.get(i));
            }
        }
    }

    public void setData(List<String> menuText){
        setData(menuText, 0);
    }

    public void setData(List<String> menuText, int defaultSelectIndex){
        mSelectedIndex = defaultSelectIndex;
        if(mTabMenuModel == TAB_MENU_WIDTH_MODEL_SCROLL){
            scrollChildView.removeAllViews();
        } else {
            removeAllViews();
        }
        for (int i = 0; i < menuText.size(); i++) {
            final View menu = LayoutInflater.from(getContext()).inflate(mMenuId, this, false);
            if(mOnCreateMenuListener != null){
                mOnCreateMenuListener.onCreateMenu(menu, i);
            }
            final int index = i;
            menu.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetMenuSelectStatus();
                    setChildViewSelect(menu, true);
                    if(mOnSelectMenuListener != null && mSelectedIndex != index){
                        mSelectedIndex = index;
                        mOnSelectMenuListener.onSelectMenu(menu, mSelectedIndex);
                    }
                }
            });
            if(menu instanceof TextView){
                ((TextView) menu).setText(menuText.get(i));
            } else {
                ((TextView) menu.findViewById(R.id.tv_menu)).setText(menuText.get(i));
            }

            //默认选择menu
            if(mSelectedIndex == i){
                setChildViewSelect(menu, true);
                if(mOnSelectMenuListener != null){
                    mOnSelectMenuListener.onSelectMenu(menu, index);
                }
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mMenuWidth, mMenuHeight);
            if(mSpace > 0){
                if(i != menuText.size() - 1){
                    if(getOrientation() == HORIZONTAL){
                        lp.rightMargin = mSpace;
                    } else {
                        lp.bottomMargin = mSpace;
                    }
                }
            } else {
                lp.weight = 1;
            }

            if(mTabMenuModel == TAB_MENU_WIDTH_MODEL_SCROLL){
                scrollChildView.addView(menu, lp);
            } else {
                addView(menu, lp);
            }
        }
    }

    private void resetMenuSelectStatus() {
        if(mTabMenuModel == TAB_MENU_WIDTH_MODEL_SCROLL){
            setChildViewSelect(scrollChildView, false);
        } else {
            setChildViewSelect(this, false);
        }
    }

    private void setChildViewSelect(View view, boolean select){
        if(view instanceof ViewGroup){
            for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
                View child = ((ViewGroup)view).getChildAt(i);
                setChildViewSelect(child, select);
            }
        }
        view.setSelected(select);
    }

    public interface OnSelectMenuListener{

        void onSelectMenu(View menu, int index);

    }

    public interface OnCreateMenuListener{

        void onCreateMenu(View menu, int index);

    }
}
