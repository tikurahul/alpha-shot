import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rahulrav.camera.scan.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
                val cameraControl by remember { mutableStateOf(SonyCameraControl(getPlatform())) }
                DisposableEffect(cameraControl) { onDispose { cameraControl.dispose() } }

                // TODO implement actual navigation
                val state by cameraControl.state.collectAsState()
                if (state == SonyCameraControl.CameraControlState.NoCamera) {
                    val viewModel =
                        remember(cameraControl) { CameraScanViewModelImpl(cameraControl) }
                    CameraScanner(viewModel)
                } else {
                    Camera(cameraControl)
                }
            }
    }
}
