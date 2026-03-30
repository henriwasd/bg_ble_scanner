package br.com.henriwasd.bg_ble_scanner

import android.content.Context
import android.content.Intent
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothAdapter
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import kotlin.test.BeforeTest
import kotlin.test.Test
import java.lang.reflect.Field

internal class BgBleScannerPluginTest {
    private lateinit var plugin: BgBleScannerPlugin
    private lateinit var mockContext: Context
    private lateinit var mockResult: MethodChannel.Result
    private lateinit var mockBluetoothManager: BluetoothManager
    private lateinit var mockBluetoothAdapter: BluetoothAdapter

    @BeforeTest
    fun setup() {
        plugin = BgBleScannerPlugin()
        mockContext = Mockito.mock(Context::class.java)
        mockResult = Mockito.mock(MethodChannel.Result::class.java)
        mockBluetoothManager = Mockito.mock(BluetoothManager::class.java)
        mockBluetoothAdapter = Mockito.mock(BluetoothAdapter::class.java)

        Mockito.`when`(mockContext.getSystemService(Context.BLUETOOTH_SERVICE)).thenReturn(mockBluetoothManager)
        Mockito.`when`(mockBluetoothManager.adapter).thenReturn(mockBluetoothAdapter)
        
        // Use reflection to set the private context field
        val contextField: Field = BgBleScannerPlugin::class.java.getDeclaredField("context")
        contextField.isAccessible = true
        contextField.set(plugin, mockContext)
    }

    @Test
    fun onMethodCall_startScan_returnsTrue() {
        val call = MethodCall("startScan", null)
        plugin.onMethodCall(call, mockResult)
        Mockito.verify(mockResult).success(true)
    }

    @Test
    fun onMethodCall_stopScan_returnsTrue() {
        val call = MethodCall("stopScan", null)
        plugin.onMethodCall(call, mockResult)
        Mockito.verify(mockResult).success(true)
        Mockito.verify(mockContext).stopService(any(Intent::class.java))
    }

    @Test
    fun onMethodCall_isBluetoothEnabled_returnsTrue() {
        Mockito.`when`(mockBluetoothAdapter.isEnabled).thenReturn(true)
        val call = MethodCall("isBluetoothEnabled", null)
        plugin.onMethodCall(call, mockResult)
        Mockito.verify(mockResult).success(true)
    }

    @Test
    fun onMethodCall_isBluetoothEnabled_returnsFalse() {
        Mockito.`when`(mockBluetoothAdapter.isEnabled).thenReturn(false)
        val call = MethodCall("isBluetoothEnabled", null)
        plugin.onMethodCall(call, mockResult)
        Mockito.verify(mockResult).success(false)
    }
}
