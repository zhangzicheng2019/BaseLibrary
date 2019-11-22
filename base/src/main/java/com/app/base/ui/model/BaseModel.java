package com.app.base.ui.model;


import androidx.lifecycle.ViewModel;

public abstract class BaseModel<T> extends ViewModel {

    abstract T loadData();

}