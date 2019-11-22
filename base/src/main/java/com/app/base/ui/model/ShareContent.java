package com.app.base.ui.model;

import java.io.Serializable;

/**
 * Created by zhangzicheng
 * 2016-12-20 10:49
 */
public class ShareContent implements Serializable {
    private static final long serialVersionUID = 2874729095675340930L;

    private String title;
    private String description;
    private String url;
    private String thumbnail;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
