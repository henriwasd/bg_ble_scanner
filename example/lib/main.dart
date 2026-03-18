import 'package:flutter/material.dart';
import 'package:bg_ble_scanner/bg_ble_scanner.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MaterialApp(home: MyApp()));
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _bgBleScanner = BgBleScanner();
  final List<Map<dynamic, dynamic>> _devices = [];
  bool _isScanning = false;

  @override
  void initState() {
    super.initState();
    _listenToScanResults();
  }

  void _listenToScanResults() {
    _bgBleScanner.scanResults.listen((device) {
      setState(() {
        final index = _devices.indexWhere((d) => d['address'] == device['address']);
        if (index != -1) {
          _devices[index] = device;
        } else {
          _devices.add(device);
        }
      });
    });
  }

  Future<void> _startScan() async {
    // No Android 14, precisamos dessas permissões específicas
    final permissions = [
      Permission.bluetoothScan,
      Permission.bluetoothConnect,
      Permission.location,
      Permission.notification, // OBRIGATÓRIO para Foreground Service
    ];

    Map<Permission, PermissionStatus> statuses = await permissions.request();

    bool allGranted = true;
    statuses.forEach((permission, status) {
      if (!status.isGranted) {
        debugPrint("Permissão negada: $permission -> $status");
        allGranted = false;
      }
    });

    if (allGranted) {
      try {
        final success = await _bgBleScanner.startScan();
        setState(() {
          _isScanning = success;
          if (success) _devices.clear();
        });
      } catch (e) {
        _showError("Erro ao iniciar scan: $e");
      }
    } else {
      _showError("Erro: Conceda todas as permissões (incluindo Notificações).");
      
      // Se a permissão foi negada permanentemente, abre as configurações
      if (statuses[Permission.notification]?.isPermanentlyDenied ?? false) {
        openAppSettings();
      }
    }
  }

  void _showError(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message)),
    );
  }

  Future<void> _stopScan() async {
    final success = await _bgBleScanner.stopScan();
    setState(() {
      _isScanning = !success;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('BLE Background Scanner'),
        actions: [
          if (_isScanning)
            const Center(
              child: Padding(
                padding: EdgeInsets.all(8.0),
                child: CircularProgressIndicator(color: Colors.blue),
              ),
            )
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: _isScanning ? null : _startScan,
                  child: const Text('Iniciar Scan'),
                ),
                ElevatedButton(
                  onPressed: _isScanning ? _stopScan : null,
                  style: ElevatedButton.styleFrom(backgroundColor: Colors.red[100]),
                  child: const Text('Parar Scan'),
                ),
              ],
            ),
          ),
          const Divider(),
          Expanded(
            child: ListView.builder(
              itemCount: _devices.length,
              itemBuilder: (context, index) {
                final device = _devices[index];
                return ListTile(
                  leading: const Icon(Icons.bluetooth),
                  title: Text(device['name'] ?? 'Desconhecido'),
                  subtitle: Text(device['address']),
                  trailing: Text('${device['rssi']} dBm'),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
