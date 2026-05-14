package br.com.henriwasd.bg_ble_scanner

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import java.util.*

class BleScanService : Service() {

    companion object {
        const val SCAN_RESULT_ACTION = "br.com.henriwasd.bg_ble_scanner.SCAN_RESULT"
        const val DEVICE_NAME = "device_name"
        const val DEVICE_ADDRESS = "device_address"
        const val RSSI = "rssi"
        const val MANUFACTURER_DATA = "manufacturer_data"
        const val SERVICE_DATA = "service_data"
        const val RAW_DATA = "raw_data"
        private const val TAG = "BleScanService"

        private const val APPLE_MANUFACTURER_ID = 0x004C
        private const val TELTONIKA_MANUFACTURER_ID = 0x089A
        private val EDDYSTONE_SERVICE_UUID = ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb")
    }

    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private lateinit var notificationHelper: NotificationHelper
    private var isScanning = false
    private var currentServiceUuids: List<String> = emptyList()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceData = mutableMapOf<String, Any?>(
                "name" to (device.name ?: "Unknown"),
                "address" to device.address,
                "rssi" to result.rssi,
                "txPower" to result.txPower
            )

            result.scanRecord?.bytes?.let { deviceData["rawData"] = it }

            result.scanRecord?.manufacturerSpecificData?.let { data ->
                if (data.size() > 0) {
                    deviceData["manufacturerData"] = data.valueAt(0)
                }
            }

            result.scanRecord?.serviceData?.let { serviceData ->
                if (serviceData.isNotEmpty()) {
                    deviceData["serviceData"] = serviceData.values.first()
                }
            }

            BgBleScannerPlugin.sendResult(deviceData)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed: $errorCode")
            isScanning = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NotificationHelper.NOTIFICATION_ID,
                    notificationHelper.getNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(
                    NotificationHelper.NOTIFICATION_ID,
                    notificationHelper.getNotification()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service: ${e.message}")
        }
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothLeScanner = bluetoothManager.adapter?.bluetoothLeScanner
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uuids = intent?.getStringArrayListExtra("serviceUuids")
        if (uuids != null) {
            currentServiceUuids = uuids
        }
        startScan()
        return START_STICKY
    }

    private fun startScan() {
        if (isScanning) stopScan()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        val filters = mutableListOf<ScanFilter>()

        for (uuidStr in currentServiceUuids) {
            try {
                filters.add(ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(uuidStr)).build())
            } catch (e: Exception) {}
        }

        filters.add(ScanFilter.Builder().setManufacturerData(APPLE_MANUFACTURER_ID, byteArrayOf()).build())
        filters.add(ScanFilter.Builder().setServiceUuid(EDDYSTONE_SERVICE_UUID).build())
        filters.add(ScanFilter.Builder().setManufacturerData(TELTONIKA_MANUFACTURER_ID, byteArrayOf()).build())

        try {
            bluetoothLeScanner?.startScan(filters, settings, scanCallback)
            isScanning = true
        } catch (e: Exception) {
            Log.e(TAG, "StartScan Error: ${e.message}")
            isScanning = false
        }
    }

    private fun stopScan() {
        if (isScanning) {
            try {
                bluetoothLeScanner?.stopScan(scanCallback)
            } catch (e: Exception) { }
        }
        isScanning = false
    }

    override fun onDestroy() {
        stopScan()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
