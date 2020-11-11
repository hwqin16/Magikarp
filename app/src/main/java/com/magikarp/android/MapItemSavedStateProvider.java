package com.magikarp.android;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.savedstate.SavedStateRegistry;

import org.json.JSONException;
import org.json.JSONObject;

public class MapItemSavedStateProvider implements SavedStateRegistry.SavedStateProvider {

    private static final String KEY_JSON_OBJECT = "jsonObject";

    public MapItemSavedStateProvider(JSONObject json) {

    }

    @NonNull
    @Override
    public Bundle saveState() {
        Bundle bundle = new Bundle();
        //   if (tempFile != null) {
        //        bundle.putString("path", );
        //   }
        return bundle;
    }

    @Nullable
    private static JSONObject restoreJsonObject(Bundle bundle) throws JSONException {
        if (bundle.containsKey(KEY_JSON_OBJECT)) {
            return new JSONObject(bundle.getString(KEY_JSON_OBJECT));
        }
        return null;
    }
}
