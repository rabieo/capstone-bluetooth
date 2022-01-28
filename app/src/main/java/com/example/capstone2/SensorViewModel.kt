package com.example.capstone2

import android.app.Application
import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.absoluteValue
import kotlin.math.pow

private val DISCONNECT_TIMEOUT = TimeUnit.SECONDS.toMillis(5)

private val ConfigCharacteristic = characteristicOf(
    service = "0000ffe0-0000-1000-8000-00805f9b34fb",
    characteristic = "0000ffe1-0000-1000-8000-00805f9b34fb",
)

class SensorViewModel(
    application: Application,
    macAddress: String
) : AndroidViewModel(application) {

    private val peripheral = viewModelScope.peripheral(bluetoothDeviceFrom(macAddress))

    init {
        viewModelScope.connect()
    }

    fun CoroutineScope.connect() {
        launch {
            try {
                peripheral.connect()
                enableGyro()
            } catch (e: ConnectionLostException) {
            }
        }
    }

    suspend fun enableGyro() {
        peripheral.write(ConfigCharacteristic, byteArrayOf(0x61, 0x0))
    }

}

fun bluetoothDeviceFrom(macAddress: String) =
    BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress)
