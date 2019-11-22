package com.app.base.ui.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by zhangzicheng
 * 2017-01-23 10:11
 */
public class BImage implements Parcelable, Serializable {

    private static final long serialVersionUID = -1866165277751559589L;
    private String smallUrl;
    private String largeUrl;
    private String desc;
    private String desc2;
    private int praiseCount;
    private boolean praiseFlag;

    public BImage() {
    }

    public String getSmallUrl() {
        return smallUrl;
    }

    public void setSmallUrl(String smallUrl) {
        this.smallUrl = smallUrl;
    }

    public String getLargeUrl() {
        return largeUrl;
    }

    public void setLargeUrl(String largeUrl) {
        this.largeUrl = largeUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc2() {
        return desc2;
    }

    public void setDesc2(String desc2) {
        this.desc2 = desc2;
    }

    public int getPraiseCount() {
        return praiseCount;
    }

    public void setPraiseCount(int praiseCount) {
        this.praiseCount = praiseCount;
    }

    public boolean getPraiseFlag() {
        return praiseFlag;
    }

    public void setPraiseFlag(boolean praiseFlag) {
        this.praiseFlag = praiseFlag;
    }

    @Override
    public String toString() {
        return "BImage{" +
                "smallUrl='" + smallUrl + '\'' +
                ", largeUrl='" + largeUrl + '\'' +
                ", desc='" + desc + '\'' +
                ", desc2='" + desc2 + '\'' +
                ", praiseCount=" + praiseCount +
                ", praiseFlag=" + praiseFlag +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.smallUrl);
        dest.writeString(this.largeUrl);
        dest.writeString(this.desc);
        dest.writeString(this.desc2);
        dest.writeInt(this.praiseCount);
        dest.writeByte(this.praiseFlag ? (byte) 1 : (byte) 0);
    }

    protected BImage(Parcel in) {
        this.smallUrl = in.readString();
        this.largeUrl = in.readString();
        this.desc = in.readString();
        this.desc2 = in.readString();
        this.praiseCount = in.readInt();
        this.praiseFlag = in.readByte() != 0;
    }

    public static final Parcelable.Creator<BImage> CREATOR = new Parcelable.Creator<BImage>() {
        @Override
        public BImage createFromParcel(Parcel source) {
            return new BImage(source);
        }

        @Override
        public BImage[] newArray(int size) {
            return new BImage[size];
        }
    };
}
