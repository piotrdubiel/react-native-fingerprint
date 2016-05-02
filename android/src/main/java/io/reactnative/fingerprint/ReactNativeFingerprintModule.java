package io.reactnative.fingerprint;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.NoSuchPaddingException;

@SuppressWarnings("unused")
public class ReactNativeFingerprintModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private final ReactNativeFingerprintManager fingerprintManager;
    private final ReactApplicationContext context;
    private boolean isListening = false;

    public ReactNativeFingerprintModule(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
        context.addLifecycleEventListener(this);
        fingerprintManager = new ReactNativeFingerprintManager(context);
        try {
            fingerprintManager.createKey();
        } catch (KeyStoreException | NoSuchProviderException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "Fingerprint";
    }


    @ReactMethod
    public void hasEnrolledFingerprints(final Promise promise) {
        promise.resolve(fingerprintManager.hasEnrolledFingerprints());
    }

    @ReactMethod
    public void isHardwareDetected(final Promise promise) {
        promise.resolve(fingerprintManager.isHardwareDetected());
    }

    @ReactMethod
    public void authenticate(final Promise promise) {
        try {
            fingerprintManager.authenticate(new ReactNativeFingerprintManager.Callback() {
                @Override
                public void onAuthenticated() {
                    final WritableMap response = new WritableNativeMap();
                    response.putString("authenticated", "OK");
                    promise.resolve(response);
                    try {
                        fingerprintManager.createKey();
                    } catch (KeyStoreException | NoSuchProviderException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError() {
                    try {
                        fingerprintManager.createKey();
                    } catch (KeyStoreException | NoSuchProviderException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    promise.reject("NO_AUTH", "Cannot authenticate");
                }
            });
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | UnrecoverableKeyException
                | CertificateException
                | IOException
                | KeyStoreException e) {
            e.printStackTrace();
            promise.reject(e);
        }
    }

    @ReactMethod
    public void init() {
        isListening = true;
        fingerprintManager.init();
    }

    private void sendEvent(String eventName,
                           @Nullable WritableMap params) {
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @Override
    public void onHostResume() {
        if (isListening) {
            fingerprintManager.init();
        }
    }

    @Override
    public void onHostPause() {
        fingerprintManager.cancel();
    }

    @Override
    public void onHostDestroy() {

    }
}
