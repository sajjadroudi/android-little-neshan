package ir.roudi.littleneshan.core;

import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ir.roudi.littleneshan.utils.Event;

public abstract class BaseViewModel extends ViewModel {

    private final MutableLiveData<Event<Integer>> _errorMessage = new MutableLiveData<>(new Event<>(null));

    private final MutableLiveData<Event<Boolean>> _navigateUp = new MutableLiveData<>(new Event<>(false));

    LiveData<Event<Integer>> getErrorMessage() {
        return _errorMessage;
    }

    LiveData<Event<Boolean>> getNavigateUpEvent() {
        return _navigateUp;
    }

    public void showError(@StringRes int message) {
        _errorMessage.postValue(new Event<>(message));
    }

    public void navigateUp() {
        _navigateUp.postValue(new Event<>(true));
    }

}
