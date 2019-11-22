package com.app.base.ui.view.flowlayout;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Tag {

    @JsonIgnore
    private int index;

    private String text;

    public Tag(){
    }

    public Tag(String text){
        this.text = text;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
