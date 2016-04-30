package io.reactnative.fingerprint;

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.NoSuchPaddingException;

@SuppressWarnings("unused")
public class ReactNativeFingerprintModule extends ReactContextBaseJavaModule {

    private final ReactNativeFingerprintManager fingerprintManager;

    public ReactNativeFingerprintModule(ReactApplicationContext reactContext) {
        super(reactContext);
        fingerprintManager = new ReactNativeFingerprintManager(reactContext);
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
//                    promise.reject("NO_AUTH", "Cannot authenticate");
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
        }
    }
}
