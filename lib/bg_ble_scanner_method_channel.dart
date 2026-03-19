import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'bg_ble_scanner_platform_interface.dart';

/// An implementation of [BgBleScannerPlatform] that uses method channels.
class MethodChannelBgBleScanner extends BgBleScannerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('bg_ble_scanner/methods');

  /// The event channel used to receive scan results.
  final eventChannel = const EventChannel('bg_ble_scanner/events');

  @override
  Future<bool> startScan() async {
    final success = await methodChannel.invokeMethod<bool>('startScan');
    return success ?? false;
  }

  @override
  Future<bool> stopScan() async {
    final success = await methodChannel.invokeMethod<bool>('stopScan');
    return success ?? false;
  }

  @override
  Future<bool> isBluetoothEnabled() async {
    final isEnabled = await methodChannel.invokeMethod<bool>('isBluetoothEnabled');
    return isEnabled ?? false;
  }

  @override
  Stream<Map<dynamic, dynamic>> get scanResults {
    return eventChannel.receiveBroadcastStream().cast<Map<dynamic, dynamic>>();
  }
}
