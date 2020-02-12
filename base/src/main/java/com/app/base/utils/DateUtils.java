package com.app.base.utils;

import android.content.Context;

import com.app.base.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
    private static SimpleDateFormat mSdfHourMinute = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static SimpleDateFormat mSdfMinute = new SimpleDateFormat("mm", Locale.getDefault());
    private static SimpleDateFormat mSdfYMD = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final int TIME_ONE_DAY = 24 * 60 * 60 * 1000;
    private static final int TIME_ONE_HOUR = 60 * 60 * 1000;
    private static final int TIME_ONE_MINUTE = 60 * 1000;

    public static String geDateTimeStr(Context context, String dateTimeStr) {
        return geDateTimeStr(context, getDateTime(dateTimeStr));
    }

    public static String geDateTimeStr(Context context, long dateTime) {
        String dateTimeStr = "";
        double timeDifference = System.currentTimeMillis() - dateTime;
        if (timeDifference >= TIME_ONE_DAY) {
            dateTimeStr = mSimpleDateFormat.format(new Date(dateTime));
        } else if (timeDifference >= TIME_ONE_HOUR) {
            int hour = (int) Math.ceil(timeDifference / TIME_ONE_HOUR);
            dateTimeStr = context.getString(R.string.text_time_hours_ago, hour);
        } else {
            int minute = (int) Math.ceil(timeDifference / TIME_ONE_MINUTE);
            dateTimeStr = context.getString(R.string.text_time_minutes_ago, minute);
        }
        return dateTimeStr;
    }

    public static long getDateTime(String dateTimeStr){
        long dateTime = 0;
        try {
            dateTime = mSimpleDateFormat.parse(dateTimeStr).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    public static String getCurrentTime(){
        return mSdfHourMinute.format(new Date());
    }

    public static String formatToHM(Date date){
        return mSdfHourMinute.format(date);
    }

    public static int getMinute(long date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.MINUTE);
    }

    public static String getDuration(long milliseconds){
        long seconds = milliseconds / 1000 % 60;
        long minutes = milliseconds / (60 * 1000) % 60;
        long hours = milliseconds / (60 * 60 * 1000);

        StringBuilder duration = new StringBuilder();
        if(hours > 0){
            if(hours > 9){
                duration.append(hours);
            } else {
                duration.append("0");
                duration.append(hours);
            }
            duration.append(":");
        }
        if(minutes > 0){
            if(minutes > 9){
                duration.append(minutes);
            } else {
                duration.append("0");
                duration.append(minutes);
            }
            duration.append(":");
        } else {
            duration.append("00:");
        }
        if(seconds > 0){
            if(seconds > 9){
                duration.append(seconds);
            } else {
                duration.append("0");
                duration.append(seconds);
            }
        } else {
            duration.append("00");
        }
        return duration.toString();
    }

    public static String formatTimestamp(Date date){
        return mSimpleDateFormat.format(date);
    }

    public static String formatTimestamp(Long date){
        if(date == null){
            return "";
        }
        return mSimpleDateFormat.format(new Date(date));
    }

    public static String formatToYMD(Date date){
        return mSdfYMD.format(date);
    }

    public static String formatToYMD(Long date){
        if(date == null){
            return "";
        }
        return mSdfYMD.format(new Date(date));
    }

    //获取指定月份的天数
    public static int getDaysByYearMonth(int year, int month) {

        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month - 1);
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        return a.get(Calendar.DATE);
    }
}
