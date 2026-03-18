import 'package:flutter_test/flutter_test.dart';
import 'package:bg_ble_scanner/bg_ble_scanner.dart';
import 'package:bg_ble_scanner/bg_ble_scanner_platform_interface.dart';
import 'package:bg_ble_scanner/bg_ble_scanner_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockBgBleScannerPlatform
    with MockPlatformInterfaceMixin
    implements BgBleScannerPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final BgBleScannerPlatform initialPlatform = BgBleScannerPlatform.instance;

  test('$MethodChannelBgBleScanner is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelBgBleScanner>());
  });

  test('getPlatformVersion', () async {
    BgBleScanner bgBleScannerPlugin = BgBleScanner();
    MockBgBleScannerPlatform fakePlatform = MockBgBleScannerPlatform();
    BgBleScannerPlatform.instance = fakePlatform;

    expect(await bgBleScannerPlugin.getPlatformVersion(), '42');
  });
}
