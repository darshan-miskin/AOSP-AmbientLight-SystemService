# AOSP Custom System Service: Ambient Light (`ambient_light`)

A custom system service implemented within the Android Open Source Project (AOSP). This project demonstrates low-level framework modification, custom Binder IPC architecture, system server registration, and strict SELinux policy enforcement on a Cuttlefish virtual device.


https://github.com/user-attachments/assets/3e8a839e-68e7-4900-b22f-56d2949830a0


---
## 🏗️ Architectural Overview

Unlike standard Android application-layer development, this project operates entirely within the OS framework layer (`frameworks/base`):

```
+------------------------------------------------------------------------------+
|                                [ Client App / UI ]                           |
+------------------------------------------------------------------------------+
                                    │
                                    ▼ (Binder IPC via Context.getSystemService)
+------------------------------------------------------------------------------+
|              [ AmbientLightManager (SDK Manager) ]                           |
+------------------------------------------------------------------------------+
                                    │
                                    ▼ (IAmbientLightService.Stub / Proxy)
+------------------------------------------------------------------------------+
|               [ AmbientLightService (SystemServer) ]                         |
|                        (PID 1000, SELinux Enforced)                          |
+------------------------------------------------------------------------------+
                                    │
                                    ▼
+------------------------------------------------------------------------------+
|               [ AOSP System Service Registry & Binder Driver ]               |
+------------------------------------------------------------------------------+
```
---

## 📂 Source Code Changes

The project integrates across the following core AOSP tree paths:

### 1. AIDL & Manager Layer (`frameworks/base`)
* `core/java/android/os/IAmbientLightCallback.aidl` — Defines the asynchronous callback interface for light status updates.
* `core/java/android/os/IAmbientLightService.aidl` — Defines the core Binder IPC interface methods.
* `core/java/android/os/AmbientLightManager.java` — Public SDK manager wrapping IPC calls for client consumption.
* `core/java/android/os/SystemServiceRegistry.java` — Registers the manager with the system context registry.
* `core/java/android/content/Context.java` — Exposes the service name constant.

### 2. Framework Services (`frameworks/base/services`)
* `services/core/java/com/android/server/AmbientLightService.kt` — Core system service business logic implemented in Kotlin, running inside `SystemServer`.
* `services/core/Android.bp` — Build configuration rules.
* `services/java/com/android/server/SystemServer.java` — Instantiates and registers the service during boot (`startOtherServices`).
* `services/proguard.flags` — Proguard rules to prevent stripping of service proxy/stub methods.

### 3. SELinux Security & Hardening (`system/sepolicy`)
* `private/service.te` — Declares the custom service type (`ambient_light_service`) and required attributes.
* `private/service_contexts` — Maps the string identifier to the SELinux security context.
* `private/system_server.te` — Grants `system_server` permission to add and manage the service.
* `private/platform_app.te` & `shell.te` — Configures client domain access rules.

---

## 🚀 Build & Deployment Instructions

### Prerequisites
* AOSP source tree initialized (Tested on Android 17 / `android-latest-release`).
* Linux development environment (Ubuntu 26.04 or compatible) with KVM and build dependencies configured.

### 1. Environment Setup & Lunch Target
Navigate to your AOSP root directory and set up the build environment for the AAOS Cuttlefish virtual device:

```bash
source build/envsetup.sh
lunch aosp_cf_x86_64_auto-aosp_current-userdebug
````

### 2. Compile the System Image

Trigger a full build or incremental policy and framework compilation:

Bash

```
m
```

_(If updating SELinux policies exclusively during development, you can force a clean policy rebuild via: `m clean-selinux_policy && m selinux_policy services`)_

### 3. Launch the Emulator

Start the Cuttlefish virtual machine instance:

Bash

```
launch_cvd
```

## 🔍 Verification & Testing

Connect to the running instance via `adb` or `scrcpy` to verify that the service is running under strict security constraints:

### Connect via ADB & Scrcpy

Bash

```
adb connect 127.0.0.1:6520
adb devices
scrcpy -s 127.0.0.1:6520
```

### 1. Verify SELinux Enforcement

Ensure the emulator is running in strict Enforcing mode:

Bash

```
adb -s 127.0.0.1:6520 shell getenforce
```

_Expected Output:_ `Enforcing`

### 2. Check Service Registration

Verify that the `servicemanager` recognizes the custom handle:

Bash

```
adb -s 127.0.0.1:6520 shell service check ambient_light
```

_Expected Output:_ `Service ambient_light: found`

### 3. Inspect Service List & Descriptors

Bash

```
adb -s 127.0.0.1:6520 shell service list | grep ambient_light
```

_Expected Output:_ `330 ambient_light: [android.os.IAmbientLightService]`

### 4. Query Runtime Dumpsys Status

Inspect active status and service health:

Bash

```
adb -s 127.0.0.1:6520 shell dumpsys ambient_light
```

_Expected Output:_

Plaintext

```
--- AmbientLightService Status ---
Service Operational: true
------------------------------------
```

### 5. Simulate Client App getLux() & setLux() calls to AmbientLightService

Bash

```
adb -s 127.0.0.1:6520 shell cmd ambient_light set 500
```

_Expected Output:_ `Successfully set ambient lux to 500`


Bash

```
adb -s 127.0.0.1:6520 shell cmd ambient_light get
```

_Expected Output:_ `Current ambient lux: 500`


## 🛡️ Troubleshooting SELinux Denials

If you encounter boot loops or `SecurityException` during bring-up, inspect kernel audit logs for `avc: denied` violations:

Bash

```
adb -s 127.0.0.1:6520 shell dmesg | grep avc
```

Ensure your `.te` policy files correctly grant `add` and `find` permissions for `ambient_light_service` under `system_server` and client domains.
