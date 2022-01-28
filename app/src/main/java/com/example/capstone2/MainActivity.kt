package com.example.capstone2

import android.Manifest.permission.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capstone2.ui.theme.Capstone2Theme
import com.juul.kable.Advertisement

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<ScanViewModel>()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {

                Column(Modifier.background(color = MaterialTheme.colors.background)) {
                    AppBar(viewModel, true)

                    Box(Modifier.weight(1f)) {
                        ProvideTextStyle(
                            TextStyle(color = contentColorFor(backgroundColor = MaterialTheme.colors.background))
                        ) {
                            val permissions = listOf(ACCESS_FINE_LOCATION, BLUETOOTH_SCAN, BLUETOOTH_CONNECT)
                            val permissionsState = rememberMultiplePermissionsState(permissions)
                            PermissionsRequired(
                                multiplePermissionsState = permissionsState,
                                permissionsNotGrantedContent = { BluetoothPermissionsNotGranted(permissionsState) },
                                permissionsNotAvailableContent = { BluetoothPermissionsNotAvailable(::openAppDetails) }
                            ) {
                                if (true) {
                                    val advertisements = viewModel.advertisements.collectAsState().value
                                    AdvertisementsList(advertisements, ::onAdvertisementClicked)
                                } else {
                                    BluetoothDisabled(::enableBluetooth)
                                }
                            }
                        }

                        StatusSnackbar(viewModel)
                    }
                }
            }
        }
    }

    private fun onAdvertisementClicked(advertisement: Advertisement) {
        viewModel.stop()
        val intent = Intent(this, SensorActivity::class.java).apply {
            putExtra("heyo", advertisement.address)
        }
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}

@Composable
private fun AppBar(viewModel: ScanViewModel, isBluetoothEnabled: Boolean) {
    val status = viewModel.status.collectAsState().value

    TopAppBar(
        title = {
            Text("EDealer bluetooth")
        },
        actions = {
            if (isBluetoothEnabled) {
                if (status !is ScanStatus.Scanning) {
                    IconButton(onClick = viewModel::start) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
                IconButton(onClick = viewModel::clear) {
                    Icon(Icons.Filled.Delete, contentDescription = "Clear")
                }
            }
        }
    )
}

@Composable
private fun BoxScope.StatusSnackbar(viewModel: ScanViewModel) {
    val status = viewModel.status.collectAsState().value

    if (status !is ScanStatus.Stopped) {
        val text = when (status) {
            is ScanStatus.Scanning -> "Scanning"
            is ScanStatus.Stopped -> "Idle"
            is ScanStatus.Failed -> "Error: ${status.message}"
        }
        Snackbar(
            Modifier
                .align(BottomCenter)
                .padding(10.dp)
        ) {
            Text(text, style = MaterialTheme.typography.body1)
        }
    }
}

@Composable
private fun ActionRequired(
    icon: ImageVector,
    contentDescription: String?,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Center,
    ) {
        Icon(
            modifier = Modifier.size(150.dp),
            tint = contentColorFor(backgroundColor = MaterialTheme.colors.background),
            imageVector = icon,
            contentDescription = contentDescription,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            modifier = Modifier.fillMaxWidth().align(CenterHorizontally),
            textAlign = TextAlign.Center,
            text = description,
        )
        Spacer(Modifier.size(15.dp))
        Button(onClick) {
            Text(buttonText)
        }
    }
}

@Composable
private fun BluetoothDisabled(enableAction: () -> Unit) {
    ActionRequired(
        icon = Icons.Rounded.Menu,
        contentDescription = "Bluetooth disabled",
        description = "Bluetooth is disabled.",
        buttonText = "Enable",
        onClick = enableAction,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun BluetoothPermissionsNotGranted(permissions: MultiplePermissionsState) {
    ActionRequired(
        icon = Icons.Filled.Menu,
        contentDescription = "Bluetooth permissions required",
        description = "Bluetooth permissions are required for scanning. Please grant the permission.",
        buttonText = "Continue",
        onClick = permissions::launchMultiplePermissionRequest,
    )
}

@Composable
private fun BluetoothPermissionsNotAvailable(openSettingsAction: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.Warning,
        contentDescription = "Bluetooth permissions required",
        description = "Bluetooth permission denied. Please, grant access on the Settings screen.",
        buttonText = "Open Settings",
        onClick = openSettingsAction,
    )
}

@Composable
private fun AdvertisementsList(
    advertisements: List<Advertisement>,
    onRowClick: (Advertisement) -> Unit
) {
    LazyColumn {
        items(advertisements.size) { index ->
            val advertisement = advertisements[index]
            AdvertisementRow(advertisement) { onRowClick(advertisement) }
        }
    }
}

@Composable
private fun AdvertisementRow(advertisement: Advertisement, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                fontSize = 22.sp,
                text = advertisement.name ?: "Unknown",
            )
            Text(advertisement.address)
        }

        Text(
            modifier = Modifier.align(CenterVertically),
            text = "${advertisement.rssi} dBm",
        )
    }
}

private fun Intent.getIsBluetoothEnabled(): Boolean = when (getIntExtra(EXTRA_STATE, ERROR)) {
    STATE_TURNING_ON, STATE_ON, STATE_CONNECTING, STATE_CONNECTED, STATE_DISCONNECTING, STATE_DISCONNECTED -> true
    else -> false // STATE_TURNING_OFF, STATE_OFF
}

/*
@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Capstone2Theme {
        AppBar(viewModels<ScanViewModel>(), true)
    }
}
 */