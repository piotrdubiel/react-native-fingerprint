package io.reactnative.fingerprint;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.util.Log;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class ReactNativeFingerprintManager extends FingerprintManagerCompat.AuthenticationCallback {
    private final FingerprintManagerCompat fingerprintManager;
    private CancellationSignal cancellationSignal;
    private Callback callback;
    private String KEY_NAME = "key";
    private KeyStore keyStore;
    final private ReactContext context;
    private boolean selfCancelled;

    public ReactNativeFingerprintManager(ReactContext context) {
        this.context = context;
        fingerprintManager = FingerprintManagerCompat.from(context);
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    public boolean hasEnrolledFingerprints() {
        return fingerprintManager.hasEnrolledFingerprints();
    }

    public boolean isHardwareDetected() {
        return fingerprintManager.isHardwareDetected();
    }

    public boolean isFingerprintAvailable() {
        return isHardwareDetected() && hasEnrolledFingerprints();
    }

    public void authenticate(Callback cb) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException {
        if (!isFingerprintAvailable()) {
            cb.onError();
        }
        cancellationSignal = new CancellationSignal();
        callback = cb;
        fingerprintManager.authenticate(
            new FingerprintManagerCompat.CryptoObject(initCipher()),
                0, cancellationSignal, this, null);
    }

    public void cancel() {
        if (cancellationSignal != null) {
            selfCancelled = true;
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Log.d("FINERR", errString.toString());
        if (!selfCancelled) {
            final WritableMap response = new WritableNativeMap();
            response.putString("code", String.valueOf(errMsgId));
            response.putString("message", errString.toString());
            sendEvent("fingerprintError", response);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        super.onAuthenticationHelp(helpMsgId, helpString);
        Log.d("FINHELP", helpString.toString());
        final WritableMap response = new WritableNativeMap();
        response.putString("code", String.valueOf(helpMsgId));
        response.putString("message", helpString.toString());
        sendEvent("fingerprintError", response);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        Log.d("FINSUC", result.toString());
//        callback.onAuthenticated();
        final WritableMap response = new WritableNativeMap();
        response.putString("result", "OK");
        sendEvent("fingerprintAuthorized", response);
    }

    @Override
    public void onAuthenticationFailed() {
        Log.d("FINFAIL", "FAILED");
//        callback.onError();
//        cancel();
        final WritableMap response = new WritableNativeMap();
        response.putString("message", "Fingerprint not recognized. Try again.");
        sendEvent("fingerprintError", response);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private Cipher initCipher() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnrecoverableKeyException, KeyStoreException, IOException, CertificateException {
        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        keyStore.load(null);
        SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);

        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher;
    }

    public void init() {
        if (!isFingerprintAvailable()) {
            final WritableMap response = new WritableNativeMap();
            response.putString("message", "fingerprint sensor not available");
            sendEvent("fingerprintError", response);
        }
        selfCancelled = false;
        cancellationSignal = new CancellationSignal();
        try {
            fingerprintManager.authenticate(
                    new FingerprintManagerCompat.CryptoObject(initCipher()),
                    0, cancellationSignal, this, null);
        } catch (NoSuchPaddingException
                | NoSuchAlgorithmException
                | UnrecoverableKeyException
                | InvalidKeyException
                | IOException
                | KeyStoreException
                | CertificateException e) {
            e.printStackTrace();
        }
    }

    private void sendEvent(String eventName,
                           @Nullable WritableMap params) {
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    public interface Callback {
        void onAuthenticated();
        void onError();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void createKey() throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        try {
            keyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
