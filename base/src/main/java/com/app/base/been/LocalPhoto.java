package com.app.base.been;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @ClassName LocalPhoto
 * @Description TODO
 * @Author zhangzicheng
 * @Date 2019/12/12 17:30
 */
public class LocalPhoto implements Parcelable {

    private int width;
    private int height;
    private String path;
    private String cropPath;
    private String compressPath;
    private int mimeType;
    private String photoType;
    private boolean isCrop;
    private boolean isCompress;

    public LocalPhoto(){}

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCropPath() {
        return cropPath;
    }

    public void setCropPath(String cropPath) {
        this.cropPath = cropPath;
    }

    public String getCompressPath() {
        return compressPath;
    }

    public void setCompressPath(String compressPath) {
        this.compressPath = compressPath;
    }

    public int getMimeType() {
        return mimeType;
    }

    public void setMimeType(int mimeType) {
        this.mimeType = mimeType;
    }

    public String getPhotoType() {
        return photoType;
    }

    public void setPhotoType(String photoType) {
        this.photoType = photoType;
    }

    public boolean isCrop() {
        return isCrop;
    }

    public void setCrop(boolean crop) {
        isCrop = crop;
    }

    public boolean isCompress() {
        return isCompress;
    }

    public void setCompress(boolean compress) {
        isCompress = compress;
    }

    protected LocalPhoto(Parcel in) {
        width = in.readInt();
        height = in.readInt();
        path = in.readString();
        cropPath = in.readString();
        compressPath = in.readString();
        mimeType = in.readInt();
        photoType = in.readString();
        isCrop = in.readByte() != 0;
        isCompress = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(path);
        dest.writeString(cropPath);
        dest.writeString(compressPath);
        dest.writeInt(mimeType);
        dest.writeString(photoType);
        dest.writeByte((byte) (isCrop ? 1 : 0));
        dest.writeByte((byte) (isCompress ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocalPhoto> CREATOR = new Creator<LocalPhoto>() {
        @Override
        public LocalPhoto createFromParcel(Parcel in) {
            return new LocalPhoto(in);
        }

        @Override
        public LocalPhoto[] newArray(int size) {
            return new LocalPhoto[size];
        }
    };
}
