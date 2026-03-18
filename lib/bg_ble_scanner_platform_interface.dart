import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'bg_ble_scanner_method_channel.dart';

abstract class BgBleScannerPlatform extends PlatformInterface {
  /// Constructs a BgBleScannerPlatform.
  BgBleScannerPlatform() : super(token: _token);

  static final Object _token = Object();

  static BgBleScannerPlatform _instance = MethodChannelBgBleScanner();

  /// The default instance of [BgBleScannerPlatform] to use.
  ///
  /// Defaults to [MethodChannelBgBleScanner].
  static BgBleScannerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [BgBleScannerPlatform] when
  /// they register themselves.
  static set instance(BgBleScannerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool> startScan() {
    throw UnimplementedError('startScan() has not been implemented.');
  }

  Future<bool> stopScan() {
    throw UnimplementedError('stopScan() has not been implemented.');
  }

  Stream<Map<dynamic, dynamic>> get scanResults {
    throw UnimplementedError('scanResults has not been implemented.');
  }
}
