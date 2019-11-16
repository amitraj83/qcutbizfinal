package com.qcut.biz.ui.waiting_list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WaitingListModel extends ViewModel {

    private MutableLiveData<String> mText;

    public WaitingListModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}