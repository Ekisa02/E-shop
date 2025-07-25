package com.joseph.e_electronicshop.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Hi \uD83D\uDC4B there! It seems You dont have internet connection");
    }

    public LiveData<String> getText() {
        return mText;
    }
}