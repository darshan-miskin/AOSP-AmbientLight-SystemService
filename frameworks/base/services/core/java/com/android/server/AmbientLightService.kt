package com.android.server

import android.content.Context
import android.os.IAmbientLightCallback
import android.os.IAmbientLightService
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Slog
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AmbientLightService(private val context: Context) : IAmbientLightService.Stub() {

    companion object {
        private const val TAG = "AmbientLightService"
    }

    private val callbackList = RemoteCallbackList<IAmbientLightCallback>()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _luxState = MutableStateFlow(0)
    val luxState = _luxState.asStateFlow()

    override fun getLux(): Int = _luxState.value

    override fun setLux(lux: Int) {
        Slog.d(TAG, "Updating ambient light intensity to: $lux lux")
        _luxState.value = lux

        serviceScope.launch {
            notifyCallbacks(lux)
        }
    }

    override fun registerCallback(callback: IAmbientLightCallback?) {
        if (callback != null) {
            callbackList.register(callback)
        }
    }

    override fun unregisterCallback(callback: IAmbientLightCallback?) {
        if (callback != null) {
            callbackList.unregister(callback)
        }
    }

    private fun notifyCallbacks(lux: Int) {
        val count = callbackList.beginBroadcast()
        for (i in 0 until count) {
            try {
                callbackList.getBroadcastItem(i).onLuxChanged(lux)
            } catch (e: RemoteException) {
                Slog.e(TAG, "Failed to dispatch callback to process index $i", e)
            }
        }
        callbackList.finishBroadcast()
    }

    override fun dump(fd: FileDescriptor, args: Array<out String>?) {
        val pw = PrintWriter(FileOutputStream(fd))
        try {
            pw.println("--- AmbientLightService Status ---")
            pw.println("Service Operational: true")
            pw.println("----------------------------------------")
        } finally {
            pw.flush()
        }
    }

    override fun onShellCommand(
        inF: FileDescriptor?,
        out: FileDescriptor?,
        err: FileDescriptor?,
        args: Array<out String>,
        callback: android.os.ShellCallback?,
        resultReceiver: android.os.ResultReceiver
    ) {
        PrintWriter(FileOutputStream(out ?: FileDescriptor.out)).use { writer ->
            if (args.isNotEmpty()) {
                when (args[0]) {
                    "set" -> {
                        val value = args.getOrNull(1)?.toIntOrNull() ?: 0
                        setLux(value)
                        writer.println("Successfully set ambient lux to $value")
                    }
                    "get" -> {
                        writer.println("Current ambient lux: ${getLux()}")
                    }
                    else -> {
                        writer.println("Unknown command. Usage: set [value] or get")
                    }
                }
            } else {
                writer.println("AmbientLightService commands: set, get")
            }
            writer.flush()
        }
        resultReceiver.send(0, null)
    }
}
