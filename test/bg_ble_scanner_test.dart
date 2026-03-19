import 'package:flutter_test/flutter_test.dart';
import 'package:bg_ble_scanner/bg_ble_scanner.dart';
import 'package:bg_ble_scanner/bg_ble_scanner_platform_interface.dart';
import 'package:bg_ble_scanner/bg_ble_scanner_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockBgBleScannerPlatform
    with MockPlatformInterfaceMixin
    implements BgBleScannerPlatform {
  @override
  Future<bool> startScan() => Future.value(true);

  @override
  Future<bool> stopScan() => Future.value(true);

  @override
  Future<bool> isBluetoothEnabled() => Future.value(true);

  @override
  Stream<Map<dynamic, dynamic>> get scanResults => Stream.value({
        'name': 'Test Device',
        'address': '00:11:22:33:44:55',
        'rssi': -60,
      });
}

void main() {
  final BgBleScannerPlatform initialPlatform = BgBleScannerPlatform.instance;

  test('$MethodChannelBgBleScanner is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelBgBleScanner>());
  });

  test('startScan', () async {
    BgBleScanner bgBleScannerPlugin = BgBleScanner();
    MockBgBleScannerPlatform fakePlatform = MockBgBleScannerPlatform();
    BgBleScannerPlatform.instance = fakePlatform;

    expect(await bgBleScannerPlugin.startScan(), true);
  });

  test('stopScan', () async {
    BgBleScanner bgBleScannerPlugin = BgBleScanner();
    MockBgBleScannerPlatform fakePlatform = MockBgBleScannerPlatform();
    BgBleScannerPlatform.instance = fakePlatform;

    expect(await bgBleScannerPlugin.stopScan(), true);
  });

  test('isBluetoothEnabled', () async {
    BgBleScanner bgBleScannerPlugin = BgBleScanner();
    MockBgBleScannerPlatform fakePlatform = MockBgBleScannerPlatform();
    BgBleScannerPlatform.instance = fakePlatform;

    expect(await bgBleScannerPlugin.isBluetoothEnabled(), true);
  });

  test('scanResults', () async {
    BgBleScanner bgBleScannerPlugin = BgBleScanner();
    MockBgBleScannerPlatform fakePlatform = MockBgBleScannerPlatform();
    BgBleScannerPlatform.instance = fakePlatform;

    final result = await bgBleScannerPlugin.scanResults.first;
    expect(result['name'], 'Test Device');
    expect(result['address'], '00:11:22:33:44:55');
    expect(result['rssi'], -60);
  });
}
