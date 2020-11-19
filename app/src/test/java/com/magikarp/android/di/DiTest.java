package com.magikarp.android.di;

import androidx.lifecycle.MutableLiveData;
import com.magikarp.android.data.model.Message;
import java.util.List;
import org.junit.Test;

/**
 * Class for unit tests.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DiTest {

  @Test
  public void ViewModelModuleProvideMutableLiveData() {
    MutableLiveData<List<Message>> liveData = ViewModelModule.provideMutableLiveData();
    assert (liveData != null);
  }

}
