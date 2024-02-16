package ir.roudi.littleneshan.utils;

import javax.annotation.Nullable;

public class Event<T> {

    public interface OnDoIfNotHandledListener<T> {
        void doIfNotHandled(T content);
    }

    private final T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    @Nullable
    public T getContentIfNotHandled() {
        if(hasBeenHandled)
            return null;

        hasBeenHandled = true;
        return content;
    }

    public T peekContent() {
        return content;
    }

    public void doIfNotHandled(OnDoIfNotHandledListener<T> listener) {
        if(!hasBeenHandled) {
            hasBeenHandled = true;
            listener.doIfNotHandled(content);
        }
    }

}
