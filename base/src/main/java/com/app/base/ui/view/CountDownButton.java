package com.app.base.ui.view;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import com.app.base.R;

public class CountDownButton extends AppCompatButton {

    private OnStartCountDownListener mOnStartCountDownListener;
    private CountDownTimer mCountDownTimer;

    private String mGetCodeText = "";

    public CountDownButton(Context context) {
        this(context, null);
    }

    public CountDownButton(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CountDownButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCountDown();
                if(mOnStartCountDownListener != null){
                    mOnStartCountDownListener.onCountDown();
                }
            }
        });
        mGetCodeText = getText().toString();
    }

    public void setOnStartCountDownListener(OnStartCountDownListener onStartCountDownListener){
        mOnStartCountDownListener = onStartCountDownListener;
    }

    private void startCountDown(){
        cancelCountDown();
        mCountDownTimer = new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                setClickable(false);
                setText(getContext().getString(R.string.text_yet_send_time, millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                setClickable(true);
                setText(R.string.text_resend);

            }
        }.start();
    }

    public void reset(){
        cancelCountDown();
        setClickable(true);
        setText(mGetCodeText);
    }

    private void cancelCountDown(){
        if(mCountDownTimer != null){
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelCountDown();
    }

    public interface OnStartCountDownListener{

        void onCountDown();

    }
}
