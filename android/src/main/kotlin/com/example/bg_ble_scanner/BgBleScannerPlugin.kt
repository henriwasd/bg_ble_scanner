package com.example.bg_ble_scanner

import android.content.*
import android.os.Build
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class BgBleScannerPlugin : FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
    private lateinit var methodChannel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private var context: Context? = null
    private var eventSink: EventChannel.EventSink? = null

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BleScanService.SCAN_RESULT_ACTION) {
                val deviceData = mutableMapOf<String, Any?>(
                    "name" to intent.getStringExtra(BleScanService.DEVICE_NAME),
                    "address" to intent.getStringExtra(BleScanService.DEVICE_ADDRESS),
                    "rssi" to intent.getIntExtra(BleScanService.RSSI, 0)
                )
                
                intent.getByteArrayExtra(BleScanService.MANUFACTURER_DATA)?.let {
                    deviceData["manufacturerData"] = it
                }
                
                intent.getByteArrayExtra(BleScanService.SERVICE_DATA)?.let {
                    deviceData["serviceData"] = it
                }
                
                intent.getByteArrayExtra(BleScanService.RAW_DATA)?.let {
                    deviceData["rawData"] = it
                }
                
                eventSink?.success(deviceData)
            }
        }
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        
        methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "bg_ble_scanner/methods")
        methodChannel.setMethodCallHandler(this)

        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "bg_ble_scanner/events")
        eventChannel.setStreamHandler(this)
        
        val filter = IntentFilter(BleScanService.SCAN_RESULT_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context?.registerReceiver(scanReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context?.registerReceiver(scanReceiver, filter)
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "startScan" -> {
                val intent = Intent(context, BleScanService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context?.startForegroundService(intent)
                } else {
                    context?.startService(intent)
                }
                result.success(true)
            }
            "stopScan" -> {
                val intent = Intent(context, BleScanService::class.java)
                context?.stopService(intent)
                result.success(true)
            }
            else -> result.notImplemented()
        }
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
        context?.unregisterReceiver(scanReceiver)
        context = null
    }
}
