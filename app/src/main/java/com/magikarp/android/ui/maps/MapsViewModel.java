package com.magikarp.android.ui.maps;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.magikarp.android.MapItem;

import java.util.List;

public class MapsViewModel extends ViewModel {

    private static final String KEY_SAVED_STATE = "savedState";

    private final SavedStateHandle savedStateHandle;

    private MutableLiveData<List<MapItem>> mapItems;

    public MapsViewModel(SavedStateHandle savedStateHandle) {
        this.savedStateHandle = savedStateHandle;
    }

    public LiveData<List<MapItem>> getMapItems() {
        return mapItems;
    }

    public void setSavedState(String savedState) {
        savedStateHandle.set(KEY_SAVED_STATE, savedState);
    }

}
