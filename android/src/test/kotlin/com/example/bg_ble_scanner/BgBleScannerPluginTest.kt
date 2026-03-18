package com.example.bg_ble_scanner

import android.content.Context
import android.content.Intent
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

    @BeforeTest
    fun setup() {
        plugin = BgBleScannerPlugin()
        mockContext = Mockito.mock(Context::class.java)
        mockResult = Mockito.mock(MethodChannel.Result::class.java)
        
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
}
