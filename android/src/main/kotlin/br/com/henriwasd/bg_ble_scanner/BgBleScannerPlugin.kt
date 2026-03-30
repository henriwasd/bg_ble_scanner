package br.com.henriwasd.bg_ble_scanner

import android.content.*
import android.util.Log
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothAdapter
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

    companion object {
        private var eventSink: EventChannel.EventSink? = null
        private const val TAG = "BgBleScannerPlugin"

        fun sendResult(data: Map<String, Any?>) {
            eventSink?.let { sink ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    try {
                        sink.success(data)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error sending to Flutter: ${e.message}")
                    }
                }
            }
        }
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "bg_ble_scanner/methods")
        methodChannel.setMethodCallHandler(this)
        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "bg_ble_scanner/events")
        eventChannel.setStreamHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "startScan" -> {
                val serviceUuids = call.argument<List<String>>("serviceUuids")
                val intent = Intent(context, BleScanService::class.java).apply {
                    if (serviceUuids != null) {
                        putStringArrayListExtra("serviceUuids", ArrayList(serviceUuids))
                    }
                }
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
            "isBluetoothEnabled" -> {
                val bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                val bluetoothAdapter = bluetoothManager?.adapter
                result.success(bluetoothAdapter?.isEnabled == true)
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
        context = null
    }
}