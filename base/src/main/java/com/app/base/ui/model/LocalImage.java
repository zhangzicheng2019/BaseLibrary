package com.app.base.ui.model;

/**
 * Created by zzc on 17/7/12.
 */

public class LocalImage {

    private String imagePath;
    private boolean isGif;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isGif() {
        return isGif;
    }

    public void setGif(boolean gif) {
        isGif = gif;
    }
}
