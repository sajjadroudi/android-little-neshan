package ir.roudi.littleneshan.utils;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class LiveDataUtils {

    private LiveDataUtils() {

    }

    public static <T> void observeOnce(
            LiveData<T> liveData,
            LifecycleOwner lifecycleOwner,
            Observer<T> observer
    ) {
        liveData.observe(lifecycleOwner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                liveData.removeObserver(this);
                observer.onChanged(t);
            }
        });
    }

    public static <T> void firstNonNull(
            LiveData<T> liveData,
            LifecycleOwner lifecycleOwner,
            Observer<T> observer
    ) {
        liveData.observe(lifecycleOwner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                if(t != null) {
                    liveData.removeObserver(this);
                    observer.onChanged(t);
                }
            }
        });
    }

}
