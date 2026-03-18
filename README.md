# bg_ble_scanner

<a href="https://www.buymeacoffee.com/henriwasd"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>

A Flutter plugin for continuous BLE (Bluetooth Low Energy) scanning on Android. It uses a **Foreground Service** to ensure that the scan keeps running even when the app is in the background or the device screen is locked.

## Features

*   **Continuous Background Scanning:** Runs as a Foreground Service for high priority.
*   **Screen Lock Support:** Scans even when the screen is off (utilizes empty ScanFilters for maximum compatibility).
*   **Real-time Stream:** Get results instantly in Flutter through an `EventChannel`.
*   **Permissions Management:** Designed to work with Android 12, 13, and 14+.

## Android Setup

### Permissions

Add the following permissions to your `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## Usage

Import the package:

```dart
import 'package:bg_ble_scanner/bg_ble_scanner.dart';
```

### 1. Request Permissions

Use a package like `permission_handler` to request all required permissions before starting the scan.

### 2. Listen for Scan Results

```dart
final _bgBleScanner = BgBleScanner();

_bgBleScanner.scanResults.listen((device) {
  print("Device found: ${device['name']} - ${device['address']} (${device['rssi']} dBm)");
});
```

### 3. Start Scanning

```dart
await _bgBleScanner.startScan();
```

### 4. Stop Scanning

```dart
await _bgBleScanner.stopScan();
```

## Note on Android 13+

Starting from Android 13, users must grant the **Notification Permission** for the Foreground Service notification to be visible.

## License

MIT License.
