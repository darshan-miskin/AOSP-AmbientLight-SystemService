package android.os;

import android.os.IAmbientLightCallback;

/** @hide */
interface IAmbientLightService{
    int getLux();
    void setLux(int lux);

    void registerCallback(IAmbientLightCallback callback);
    void unregisterCallback(IAmbientLightCallback callback);
}
