package ir.roudi.littleneshan.data.repository.location;

import android.content.IntentSender;

import com.google.android.gms.common.api.ResolvableApiException;

public interface OnTurnOnLocationResultListener {
    void onRequireResolution(ResolvableApiException exception) throws IntentSender.SendIntentException;
    void onSettingsChangeUnavailable();
    void onSendIntentException(IntentSender.SendIntentException exception);
}
