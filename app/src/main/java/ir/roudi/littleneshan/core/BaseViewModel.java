package ir.roudi.littleneshan.core;

import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ir.roudi.littleneshan.utils.Event;

public abstract class BaseViewModel extends ViewModel {

    private final MutableLiveData<Event<Integer>> _errorMessage = new MutableLiveData<>(new Event<>(null));

    LiveData<Event<Integer>> getErrorMessage() {
        return _errorMessage;
    }

    public void showError(@StringRes int message) {
        _errorMessage.postValue(new Event<>(message));
    }

}
