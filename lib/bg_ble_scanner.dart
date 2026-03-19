import 'bg_ble_scanner_platform_interface.dart';

class BgBleScanner {
  /// Inicia o escaneamento Bluetooth em segundo plano através de um Foreground Service.
  Future<bool> startScan() {
    return BgBleScannerPlatform.instance.startScan();
  }

  /// Para o escaneamento Bluetooth e o Foreground Service.
  Future<bool> stopScan() {
    return BgBleScannerPlatform.instance.stopScan();
  }

  /// Verifica se o Bluetooth do aparelho está ligado.
  Future<bool> isBluetoothEnabled() {
    return BgBleScannerPlatform.instance.isBluetoothEnabled();
  }

  /// Stream que emite os dispositivos Bluetooth encontrados.
  /// Cada mapa contém: 'name', 'address', 'rssi', 'manufacturerData', 'serviceData' e 'rawData'.
  Stream<Map<dynamic, dynamic>> get scanResults {
    return BgBleScannerPlatform.instance.scanResults;
  }
}
