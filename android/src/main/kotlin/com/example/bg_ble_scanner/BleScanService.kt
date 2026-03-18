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
        const val MANUFACTURER_DATA = "manufacturer_data"
        const val SERVICE_DATA = "service_data"
        const val RAW_DATA = "raw_data"
        private const val TAG = "BleScanService"
    }

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private lateinit var notificationHelper: NotificationHelper
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var isScanning = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val intent = Intent(SCAN_RESULT_ACTION)
            intent.putExtra(DEVICE_NAME, device.name ?: "Desconhecido")
            intent.putExtra(DEVICE_ADDRESS, device.address)
            intent.putExtra(RSSI, result.rssi)
            
            // Extract Raw Scan Record Bytes
            result.scanRecord?.bytes?.let { bytes ->
                intent.putExtra(RAW_DATA, bytes)
            }
            
            // Extract Manufacturer Data
            result.scanRecord?.manufacturerSpecificData?.let { manufacturerData ->
                if (manufacturerData.size() > 0) {
                    val firstKey = manufacturerData.keyAt(0)
                    val data = manufacturerData.get(firstKey)
                    intent.putExtra(MANUFACTURER_DATA, data)
                }
            }
            
            // Extract Service Data
            result.scanRecord?.serviceData?.let { serviceData ->
                if (serviceData.isNotEmpty()) {
                    val firstEntry = serviceData.entries.first()
                    intent.putExtra(SERVICE_DATA, firstEntry.value)
                }
            }
            
            sendBroadcast(intent)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error: $errorCode")
            if (errorCode == SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
                // Restart scan if registration failed
                stopScan()
                handler.postDelayed({ startScan() }, 1000)
            }
        }
    }

    private val scanRestartRunnable = object : Runnable {
        override fun run() {
            if (isScanning) {
                Log.d(TAG, "Restarting scan to avoid system timeout...")
                stopScan()
                handler.postDelayed({ startScan() }, 1000)
            }
            handler.postDelayed(this, 5 * 60 * 1000) // Restart every 5 minutes
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        
        // Call startForeground immediately in onCreate to avoid ForegroundServiceDidNotStartInTimeException
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE or 
                                          ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                startForeground(
                    NotificationHelper.NOTIFICATION_ID,
                    notificationHelper.getNotification(),
                    foregroundServiceType
                )
            } else {
                startForeground(NotificationHelper.NOTIFICATION_ID, notificationHelper.getNotification())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground: ${e.message}")
        }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothLeScanner = bluetoothManager.adapter?.bluetoothLeScanner
        
        if (bluetoothLeScanner == null) {
            Log.e(TAG, "BluetoothLeScanner is null. Is Bluetooth enabled?")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startScan()
        handler.removeCallbacks(scanRestartRunnable)
        handler.postDelayed(scanRestartRunnable, 5 * 60 * 1000)
        return START_STICKY
    }

    private fun startScan() {
        if (isScanning) return
        
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        val filters = mutableListOf<ScanFilter>()
        filters.add(ScanFilter.Builder().build())

        try {
            bluetoothLeScanner?.startScan(filters, settings, scanCallback)
            isScanning = true
            Log.d(TAG, "BLE Scan started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting scan: ${e.message}")
        }
    }

    private fun stopScan() {
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping scan: ${e.message}")
        }
        isScanning = false
    }

    override fun onDestroy() {
        handler.removeCallbacks(scanRestartRunnable)
        stopScan()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
