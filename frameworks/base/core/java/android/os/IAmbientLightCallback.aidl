package android.os;

/** @hide */
oneway interface IAmbientLightCallback {
    void onLuxChanged(int lux);
}
