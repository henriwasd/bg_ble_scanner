import 'package:flutter_test/flutter_test.dart';
import 'package:bg_ble_scanner/bg_ble_scanner_platform_interface.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class BgBleScannerPlatformMock extends BgBleScannerPlatform
    with MockPlatformInterfaceMixin {}

void main() {
  test('BgBleScannerPlatform base class throws UnimplementedError', () {
    final platform = BgBleScannerPlatformMock();

    expect(() => platform.startScan(), throwsUnimplementedError);
    expect(() => platform.stopScan(), throwsUnimplementedError);
    expect(() => platform.scanResults, throwsUnimplementedError);
  });
}
