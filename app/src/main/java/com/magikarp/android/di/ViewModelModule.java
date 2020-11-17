package com.magikarp.android.di;

import androidx.lifecycle.MutableLiveData;
import com.magikarp.android.data.model.Message;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityRetainedComponent;
import java.util.List;

/**
 * A Hilt module for dependency injections that are needed at the {@code View Model} level or below.
 */
@Module
@InstallIn(ActivityRetainedComponent.class)
public class ViewModelModule {

  @Provides
  public static MutableLiveData<List<Message>> provideMutableLiveData() {
    return new MutableLiveData<>();
  }

}
