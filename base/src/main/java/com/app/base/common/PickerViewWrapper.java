package com.app.base.common;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.app.base.R;
import com.app.base.ui.view.pickerview.builder.OptionsPickerBuilder;
import com.app.base.ui.view.pickerview.builder.TimePickerBuilder;
import com.app.base.ui.view.pickerview.listener.OnOptionsSelectListener;
import com.app.base.ui.view.pickerview.listener.OnTimeSelectListener;
import com.app.base.ui.view.pickerview.view.OptionsPickerView;
import com.app.base.ui.view.pickerview.view.TimePickerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class PickerViewWrapper {

    private OptionsPickerView<String> cityOpv;
    private OptionsPickerView<String> sexOpv;
    private OptionsPickerView<String> durationOpv;
    private TimePickerView datePv;
    private TimePickerView timePv;

    private OnDateSelectListener onDateSelectListener;
    private OnDurationSelectListener onDurationSelectListener;
    private OnCitySelectListener onCitySelectListener;
    private OnIndexSelectListener onIndexSelectListener;

    public void showTimeSelectDialog(Context ctx, OnDateSelectListener onDateSelectListener) {
        this.onDateSelectListener = onDateSelectListener;
        if (timePv == null) {
            initTimePv(ctx, onDateSelectListener);
        }
        if (timePv.isShowing()) {
            timePv.dismiss();
        }
        timePv.show(false);
    }

    public void showDateSelectDialog(Context ctx, OnDateSelectListener onDateSelectListener) {
        this.onDateSelectListener = onDateSelectListener;
        if (datePv == null) {
            initDatePv(ctx, onDateSelectListener);
        }
        if (datePv.isShowing()) {
            datePv.dismiss();
        }
        datePv.show(false);
    }

    public void showDurationSelectDialog(Context ctx, OnDurationSelectListener onDurationSelectListener) {
        this.onDurationSelectListener = onDurationSelectListener;
        if (durationOpv == null) {
            initDurationPv(ctx, onDurationSelectListener);
        }
        if (durationOpv.isShowing()) {
            durationOpv.dismiss();
        }
        durationOpv.show(false);
    }


    public void showCitySelectDialog(Context ctx, OnCitySelectListener onCitySelectListener) {
        this.onCitySelectListener = onCitySelectListener;
        if (mProvinceList.size() == 0) {
            String jsonData = getJson(ctx, "province.json");
            parseJson(jsonData);
        }

        if (cityOpv == null) {
            initCityOpv(ctx, onCitySelectListener);
        }
        if (cityOpv.isShowing()) {
            cityOpv.dismiss();
        }
        cityOpv.show(false);
    }

    public void showSexSelectDialog(Context ctx, OnIndexSelectListener onIndexSelectListener) {
        this.onIndexSelectListener = onIndexSelectListener;
        if (sexOpv == null) {
            initSexOpv(ctx, onIndexSelectListener);
        }
        if (sexOpv.isShowing()) {
            sexOpv.dismiss();
        }
        sexOpv.show(false);
    }

    //  省份
    private ArrayList<String> mProvinceList = new ArrayList<>();
    //  城市
    private ArrayList<String> mCities;
    private ArrayList<List<String>> mCityList = new ArrayList<>();
    //  区/县
    private ArrayList<String> mDistrict;
    private ArrayList<List<String>> mDistricts;
    private ArrayList<List<List<String>>> mDistrictList = new ArrayList<>();

    private void initCityOpv(Context ctx, final OnCitySelectListener onCitySelectListener) {
        cityOpv = new OptionsPickerBuilder(ctx, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                String province = mProvinceList.get(options1);
                String city = mCityList.get(options1).get(options2);
                if (!TextUtils.isEmpty(city)) {
                    String district = mDistrictList.get(options1).get(options2).get(options3);
                    if (!TextUtils.isEmpty(district)) {
                        onCitySelectListener.onCitySelect(district, v);
                    } else {
                        onCitySelectListener.onCitySelect(city, v);
                    }
                } else {
                    onCitySelectListener.onCitySelect(province, v);
                }
            }
        }).setSubmitText(ctx.getString(R.string.text_confirm))//确定按钮文字
                .setCancelText(ctx.getString(R.string.text_cancel))//取消按钮文字
                .setTitleText(ctx.getString(R.string.text_select_region))//标题
                .setSubCalSize(18)//确定和取消文字大小
                .setTitleSize(20)//标题文字大小
                .setContentTextSize(20)//滚轮文字大小
                .setTitleColor(Color.BLACK)//标题文字颜色
                .setSubmitColor(Color.parseColor("#357af6"))//确定按钮文字颜色
                .setCancelColor(Color.parseColor("#357af6"))//取消按钮文字颜色
                .setTitleBgColor(ContextCompat.getColor(ctx, R.color.gray_background))//标题背景颜色 Night mode
                .setBgColor(Color.WHITE)//滚轮背景颜色 Night mode
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setCyclic(false, false, false)//循环与否
                .setSelectOptions(0, 0, 0)  //设置默认选中项
                .setOutSideCancelable(true)//点击外部dismiss default true
                .isDialog(true)//是否显示为对话框样式
                .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
                .build();
        cityOpv.setPicker(mProvinceList, mCityList, mDistrictList);//添加数据源
    }

    private void initTimePv(Context ctx, final OnDateSelectListener onDateSelectListener) {
        Calendar selectedDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        startDate.set(1900, 0, 1);
        Calendar endDate = Calendar.getInstance();
        timePv = new TimePickerBuilder(ctx, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                onDateSelectListener.onDateSelect(date, v);
            }
        })
                .setType(new boolean[]{false, false, false, true, true, true})// 默认全部显示
                .setTitleText(ctx.getString(R.string.text_select_date))//标题文字
                .setCancelText(ctx.getString(R.string.text_cancel))//取消按钮文字
                .setSubmitText(ctx.getString(R.string.text_confirm))//确认按钮文字
                .setSubCalSize(18)//确定和取消文字大小
                .setTitleSize(20)//标题文字大小
                .setContentTextSize(20)//滚轮文字大小
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .isCyclic(true)//是否循环滚动
                .setTitleColor(Color.BLACK)//标题文字颜色
                .setSubmitColor(Color.parseColor("#357af6"))//确定按钮文字颜色
                .setCancelColor(Color.parseColor("#357af6"))//取消按钮文字颜色
                .setTitleBgColor(ContextCompat.getColor(ctx, R.color.gray_background))//标题背景颜色 Night mode
                .setBgColor(Color.WHITE)//滚轮背景颜色 Night mode
                .setDate(selectedDate)// 如果不设置的话，默认是系统时间*/
                .setRangDate(startDate, endDate)//起始终止年月日设定
//                .setLabel("年","月","日","时","分","秒")//默认设置为年月日时分秒
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .isDialog(true)//是否显示为对话框样式
                .build();
    }

    private void initDatePv(Context ctx, final OnDateSelectListener onDateSelectListener) {
        Calendar selectedDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        startDate.set(1900, 0, 1);
        Calendar endDate = Calendar.getInstance();
        datePv = new TimePickerBuilder(ctx, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                onDateSelectListener.onDateSelect(date, v);
            }
        })
                .setType(new boolean[]{true, true, true, false, false, false})// 默认全部显示
                .setTitleText(ctx.getString(R.string.text_select_date))//标题文字
                .setCancelText(ctx.getString(R.string.text_cancel))//取消按钮文字
                .setSubmitText(ctx.getString(R.string.text_confirm))//确认按钮文字
                .setSubCalSize(18)//确定和取消文字大小
                .setTitleSize(20)//标题文字大小
                .setContentTextSize(20)//滚轮文字大小
                .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                .isCyclic(true)//是否循环滚动
                .setTitleColor(Color.BLACK)//标题文字颜色
                .setSubmitColor(Color.parseColor("#357af6"))//确定按钮文字颜色
                .setCancelColor(Color.parseColor("#357af6"))//取消按钮文字颜色
                .setTitleBgColor(ContextCompat.getColor(ctx, R.color.gray_background))//标题背景颜色 Night mode
                .setBgColor(Color.WHITE)//滚轮背景颜色 Night mode
                .setDate(selectedDate)// 如果不设置的话，默认是系统时间*/
                .setRangDate(startDate, endDate)//起始终止年月日设定
//                .setLabel("年","月","日","时","分","秒")//默认设置为年月日时分秒
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .isDialog(true)//是否显示为对话框样式
                .build();
    }

    private void initDurationPv(Context ctx, final OnDurationSelectListener onDurationSelectListener) {
        List<String> hourList = new ArrayList<>();
        List<String> minuteList = new ArrayList<>();
        List<String> secondList = new ArrayList<>();
        for (int i = 0; i < 61; i++) {
            if (i <= 12) {
                hourList.add(i + "");
            }
            minuteList.add(i + "");
            secondList.add(i + "");
        }

        durationOpv = new OptionsPickerBuilder(ctx, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                onDurationSelectListener.onDurationSelect(options1, options2, options3);
            }
        }).setSubmitText(ctx.getString(R.string.text_confirm))//确定按钮文字
                .setCancelText(ctx.getString(R.string.text_cancel))//取消按钮文字
                .setTitleText(ctx.getString(R.string.text_select_duration))//标题
                .setSubCalSize(18)//确定和取消文字大小
                .setTitleSize(20)//标题文字大小
                .setContentTextSize(20)//滚轮文字大小
                .setTitleColor(Color.BLACK)//标题文字颜色
                .setSubmitColor(Color.parseColor("#357af6"))//确定按钮文字颜色
                .setCancelColor(Color.parseColor("#357af6"))//取消按钮文字颜色
                .setTitleBgColor(ContextCompat.getColor(ctx, R.color.gray_background))//标题背景颜色 Night mode
                .setBgColor(Color.WHITE)//滚轮背景颜色 Night mode
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setCyclic(true, true, true)//循环与否
                .setSelectOptions(0, 0, 0)  //设置默认选中项
                .setOutSideCancelable(true)//点击外部dismiss default true
                .setLabels(ctx.getString(R.string.text_hour), ctx.getString(R.string.text_minute), ctx.getString(R.string.text_second))
                .isDialog(true)//是否显示为对话框样式
                .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
                .build();
        durationOpv.setNPicker(hourList, minuteList, secondList);//添加数据源
    }

    private void initSexOpv(Context ctx, final OnIndexSelectListener onIndexSelectListener) {

        sexOpv = new OptionsPickerBuilder(ctx, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                onIndexSelectListener.onIndexSelect(options1, v);
            }
        }).setSubmitText(ctx.getString(R.string.text_confirm))//确定按钮文字
                .setCancelText(ctx.getString(R.string.text_cancel))//取消按钮文字
                .setTitleText(ctx.getString(R.string.text_select_gender))//标题
                .setSubCalSize(18)//确定和取消文字大小
                .setTitleSize(20)//标题文字大小
                .setContentTextSize(20)//滚轮文字大小
                .setTitleColor(Color.BLACK)//标题文字颜色
                .setSubmitColor(Color.parseColor("#357af6"))//确定按钮文字颜色
                .setCancelColor(Color.parseColor("#357af6"))//取消按钮文字颜色
                .setTitleBgColor(ContextCompat.getColor(ctx, R.color.gray_background))//标题背景颜色 Night mode
                .setBgColor(Color.WHITE)//滚轮背景颜色 Night mode
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setCyclic(false, false, false)//循环与否
                .setSelectOptions(0, 0, 0)  //设置默认选中项
                .setOutSideCancelable(true)//点击外部dismiss default true
                .isDialog(true)//是否显示为对话框样式
                .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
                .build();
        sexOpv.setPicker(Arrays.asList(ctx.getString(R.string.text_male), ctx.getString(R.string.text_female)));//添加数据源
    }


    /**
     * 从asset目录下读取fileName文件内容
     *
     * @param fileName 待读取asset下的文件名
     * @return 得到省市县的String
     */
    private String getJson(Context ctx, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = ctx.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 解析json填充集合
     *
     * @param str 待解析的json，获取省市县
     */
    public void parseJson(String str) {
        try {
            //  获取json中的数组
            JSONArray jsonArray = new JSONArray(str);
            //  遍历数据组
            for (int i = 0; i < jsonArray.length(); i++) {
                //  获取省份的对象
                JSONObject provinceObject = jsonArray.optJSONObject(i);
                //  获取省份名称放入集合
                String provinceName = provinceObject.getString("name");
                mProvinceList.add(provinceName);
                //  获取城市数组
                JSONArray cityArray = provinceObject.optJSONArray("city");
                mCities = new ArrayList<>();
                //   声明存放城市的集合
                mDistricts = new ArrayList<>();
                //声明存放区县集合的集合
                //  遍历城市数组
                for (int j = 0; j < cityArray.length(); j++) {
                    //  获取城市对象
                    JSONObject cityObject = cityArray.optJSONObject(j);
                    //  将城市放入集合
                    String cityName = cityObject.optString("name");
                    mCities.add(cityName);
                    mDistrict = new ArrayList<>();
                    // 声明存放区县的集合
                    //  获取区县的数组
                    JSONArray areaArray = cityObject.optJSONArray("area");
                    //  遍历区县数组，获取到区县名称并放入集合
                    for (int k = 0; k < areaArray.length(); k++) {
                        String areaName = areaArray.getString(k);
                        mDistrict.add(areaName);
                    }
                    //  将区县的集合放入集合
                    mDistricts.add(mDistrict);
                }
                //  将存放区县集合的集合放入集合
                mDistrictList.add(mDistricts);
                //  将存放城市的集合放入集合
                mCityList.add(mCities);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void release() {
        if (sexOpv != null) {
            if (sexOpv.isShowing()) {
                sexOpv.dismiss();
                sexOpv.recycle();
            }
            sexOpv = null;
        }if (cityOpv != null) {
            if (cityOpv.isShowing()) {
                cityOpv.dismiss();
                cityOpv.recycle();
            }
            cityOpv = null;
        }
        if (durationOpv != null) {
            if (durationOpv.isShowing()) {
                durationOpv.dismiss();
                durationOpv.recycle();
            }
            durationOpv = null;
        }
        if (timePv != null) {
            if (timePv.isShowing()) {
                timePv.dismiss();
                timePv.recycle();
            }
            timePv = null;
        }
        if (datePv != null) {
            if (datePv.isShowing()) {
                datePv.dismiss();
                datePv.recycle();
            }
            datePv = null;
        }
        mProvinceList.clear();
        mCityList.clear();
        mDistrictList.clear();
        if (mCities != null) {
            mCities.clear();
        }

        if (mDistrict != null) {
            mDistrict.clear();
        }

        if (mDistricts != null) {
            mDistricts.clear();
        }
        onDateSelectListener = null;
        onCitySelectListener = null;
        onDurationSelectListener = null;
        onIndexSelectListener = null;
    }

    public interface OnDateSelectListener {

        void onDateSelect(Date date, View v);

    }

    public interface OnCitySelectListener {

        void onCitySelect(String region, View v);

    }

    public interface OnIndexSelectListener {

        void onIndexSelect(int index, View v);

    }

    public interface OnDurationSelectListener {

        void onDurationSelect(int hour, int minute, int second);

    }

}
