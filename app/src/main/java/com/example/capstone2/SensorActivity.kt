package com.example.capstone2

import android.app.Application
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.capstone2.ui.theme.Capstone2Theme
import com.juul.kable.ConnectionLostException
import com.juul.kable.characteristicOf
import com.juul.kable.peripheral
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class SensorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the Intent that started this activity and extract the string
        val macAddress = intent.getStringExtra("heyo")

        setContent {
            Capstone2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Row() {
                        OutlinedButtonE("$macAddress")
                        OutlinedButtonS("$macAddress")
                        OutlinedButtonM("$macAddress")
                    }

                }
            }
        }
    }
}

private val ConfigCharacteristic = characteristicOf(
    service = "0000ffe0-0000-1000-8000-00805f9b34fb",
    characteristic = "0000ffe1-0000-1000-8000-00805f9b34fb",
)

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun OutlinedButtonE(macAddress: String) {
    val coroutineScope = rememberCoroutineScope()

    val peripheral = coroutineScope.peripheral(bluetoothDeviceFrom(macAddress))

    OutlinedButton(onClick = {

        coroutineScope.launch {
            try {
                peripheral.connect()
                peripheral.write(ConfigCharacteristic, byteArrayOf(0x61, 0x0))
            } catch (e: ConnectionLostException) {
            }
        }


    }) {
        Text("Left")
    }
}

@Composable
fun OutlinedButtonS(macAddress: String) {
    val coroutineScope = rememberCoroutineScope()

    val peripheral = coroutineScope.peripheral(bluetoothDeviceFrom(macAddress))

    OutlinedButton(onClick = {

        coroutineScope.launch {
            try {
                peripheral.connect()
                peripheral.write(ConfigCharacteristic, byteArrayOf(0x62, 0x0))
            } catch (e: ConnectionLostException) {
            }
        }


    }) {
        Text("Stop")
    }
}

@Composable
fun OutlinedButtonM(macAddress: String) {
    val coroutineScope = rememberCoroutineScope()

    val peripheral = coroutineScope.peripheral(bluetoothDeviceFrom(macAddress))

    OutlinedButton(onClick = {

        coroutineScope.launch {
            try {
                peripheral.connect()
                peripheral.write(ConfigCharacteristic, byteArrayOf(0x61, 0x0))
            } catch (e: ConnectionLostException) {
            }
        }


    }) {
        Text("Right")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Capstone2Theme {
        Greeting("Android")
    }
}