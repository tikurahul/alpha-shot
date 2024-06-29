package com.rahulrav.camera.control

import App
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.juul.kable.Bluetooth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val granted = permissions.all { it.value }
                if (granted) {
                    setContent {
                        App()
                    }
                } else {
                    // Do something better here.
                    Log.w(TAG, "Permissions unavailable.")
                }
            }
        val requiredPermissions = Bluetooth.permissionsNeeded
        permissionLauncher.launch(requiredPermissions)
    }

    companion object {
        const val TAG = "MainActivity"
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
