package com.app.base.ui.view;

import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

public class FrameAnimation {

    private ImageView mImageView;
    private int[] mFrameRes;
    private int mDuration;
    private int mLastFrameNo;
    private int mCurrentFrame = 0;
    private boolean isStop = false;
    private boolean isLooper;

    private int mAnimType = -1;
    private OnCompleteListener mOnCompleteListener;

    private FrameAnimation(){}

    public FrameAnimation(ImageView pImageView, int[] pFrameRes, int pDuration, boolean isLooper, int animType) {
        mImageView = pImageView;
        mFrameRes = pFrameRes;
        mDuration = pDuration;
        mLastFrameNo = pFrameRes.length - 1;
        mAnimType = animType;
        this.isLooper = isLooper;
    }


    private void play() {
        mImageView.removeCallbacks(mPlayFrameRun);
        mImageView.postDelayed(mPlayFrameRun, mDuration);
    }

    private Runnable mPlayFrameRun = new Runnable() {
        public void run() {
            if (isStop) {
                return;
            }
//            recycleImage();
            if (mCurrentFrame > mLastFrameNo) {
                if(!isLooper){
                    if(mOnCompleteListener != null){
                        mOnCompleteListener.onComplete(FrameAnimation.this);
                    }
                    return;
                } else {
                    mCurrentFrame = 0;
                }
            }
            mImageView.setBackgroundResource(mFrameRes[mCurrentFrame]);
            mCurrentFrame++;
            play();
        }
    };

    public void start(){
        isStop = false;
        play();
    }

    public void start(OnCompleteListener onCompleteListener){
        mOnCompleteListener = onCompleteListener;
        start();
    }

    public void restart(){
        mCurrentFrame = 0;
        play();
    }

    public void stop() {
        isStop = true;
    }

    private void recycleImage() {
        BitmapDrawable bd = (BitmapDrawable)mImageView.getBackground();
       if(bd != null){
           mImageView.setBackgroundResource(0);//别忘了把背景设为null，避免onDraw刷新背景时候出现used a recycled bitmap错误
           bd.setCallback(null);
           bd.getBitmap().recycle();
       }
    }

    public int getAnimType() {
        return mAnimType;
    }

    public void setAnimType(int animType) {
        this.mAnimType = animType;
    }

    public interface OnCompleteListener {
        void onComplete(FrameAnimation frameAnimation);
    }

    public boolean isRunning() {
        return !isStop;
    }
}