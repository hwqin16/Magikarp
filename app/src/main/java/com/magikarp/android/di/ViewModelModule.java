package com.magikarp.android.di;

import com.magikarp.android.data.MapsRepository;
import com.magikarp.android.ui.maps.MapsViewModel;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityRetainedComponent;

/**
 * A Hilt module for dependency injections that are needed at the {@code ViewModel} level or below.
 */
@Module
@InstallIn(ActivityRetainedComponent.class)
public class ViewModelModule {

    @Provides
    public static MapsViewModel provideMapsViewModel(MapsRepository mapRepository) {
        return new MapsViewModel(mapRepository);
    }

}
