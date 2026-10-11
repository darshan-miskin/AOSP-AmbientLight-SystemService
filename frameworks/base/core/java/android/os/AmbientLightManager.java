package android.os;

import android.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;

import java.util.Map;

/**
 * Public client-facing manager exposed via Context.getSystemService().
 *
 * @hide
 */
public class AmbientLightManager {
    private static final String TAG = "AmbientLightMgr";

    private final IAmbientLightService mService;
    private final Map<LuxChangeListener, IAmbientLightCallback> mCallbacks = new ArrayMap<>();

    /**
     * Interface to receive real-time ambient light sensor changes.
     */
    public interface LuxChangeListener {
        void onLuxChanged(int lux);
    }

    /** @hide */
    public AmbientLightManager(@NonNull IAmbientLightService service) {
        mService = service;
    }

    /**
     * Gets the current ambient light level in lux.
     */
    public int getLux() {
        try {
            return mService.getLux();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get lux from AmbientLightService", e);
            return 0;
        }
    }

    /**
     * Sets the target ambient light level in lux.
     */
    public void setLux(int lux) {
        try {
            mService.setLux(lux);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to set lux on AmbientLightService", e);
        }
    }

    /**
     * Registers a listener to observe real-time lux updates.
     */
    public boolean registerCallback(@NonNull LuxChangeListener listener) {
        synchronized (mCallbacks) {
            if (mCallbacks.containsKey(listener)) {
                return true;
            }

            IAmbientLightCallback aidlCallback = new IAmbientLightCallback.Stub() {
                @Override
                public void onLuxChanged(int lux) {
                    listener.onLuxChanged(lux);
                }
            };

            try {
                mService.registerCallback(aidlCallback);
                mCallbacks.put(listener, aidlCallback);
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to register callback", e);
                return false;
            }
        }
    }

    /**
     * Unregisters a previously registered lux listener.
     */
    public boolean unregisterCallback(@NonNull LuxChangeListener listener) {
        synchronized (mCallbacks) {
            IAmbientLightCallback aidlCallback = mCallbacks.remove(listener);
            if (aidlCallback == null) {
                return false;
            }

            try {
                mService.unregisterCallback(aidlCallback);
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to unregister callback", e);
                return false;
            }
        }
    }
}
