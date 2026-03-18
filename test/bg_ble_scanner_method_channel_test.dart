import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:bg_ble_scanner/bg_ble_scanner_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelBgBleScanner platform = MethodChannelBgBleScanner();
  const MethodChannel channel = MethodChannel('bg_ble_scanner/methods');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
      switch (methodCall.method) {
        case 'startScan':
          return true;
        case 'stopScan':
          return true;
        default:
          return null;
      }
    });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('startScan', () async {
    expect(await platform.startScan(), true);
  });

  test('stopScan', () async {
    expect(await platform.stopScan(), true);
  });

  test('scanResults', () async {
    expect(platform.scanResults, isA<Stream<Map<dynamic, dynamic>>>());
  });
}
