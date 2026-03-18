package com.example.bg_ble_scanner

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log

class BleScanService : Service() {

    companion object {
        const val SCAN_RESULT_ACTION = "com.example.bg_ble_scanner.SCAN_RESULT"
        const val DEVICE_NAME = "device_name"
        const val DEVICE_ADDRESS = "device_address"
        const val RSSI = "rssi"
        private const val TAG = "BleScanService"
    }

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private lateinit var notificationHelper: NotificationHelper

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val intent = Intent(SCAN_RESULT_ACTION)
            intent.putExtra(DEVICE_NAME, device.name ?: "Desconhecido")
            intent.putExtra(DEVICE_ADDRESS, device.address)
            intent.putExtra(RSSI, result.rssi)
            sendBroadcast(intent)
            Log.d(TAG, "Device found: ${device.address} - ${device.name}")
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error: $errorCode")
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.NOTIFICATION_ID,
                notificationHelper.getNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_ID, notificationHelper.getNotification())
        }

        startScan()
        return START_STICKY
    }

    private fun startScan() {
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        // Important: Many devices require at least one ScanFilter to scan with screen off
        val filters = mutableListOf<ScanFilter>()
        filters.add(ScanFilter.Builder().build())

        bluetoothLeScanner?.startScan(filters, settings, scanCallback)
        Log.d(TAG, "BLE Scan started")
    }

    override fun onDestroy() {
        bluetoothLeScanner?.stopScan(scanCallback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
