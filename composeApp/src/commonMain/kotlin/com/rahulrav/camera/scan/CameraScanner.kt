package com.rahulrav.camera.scan

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow

@Composable
fun CameraScanner(
    viewModel: CameraScannerViewModel,
    modifier: Modifier = Modifier,
    onCameraSelected: (SupportedAlphaCamera) -> Unit,
) {
    val state = viewModel.state.value
    AnimatedContent(state) { targetState ->
        when (targetState) {
            ScanState.Idle -> IdleScreen(modifier) { viewModel.doScan() }
            ScanState.Scanning -> Scanning(modifier)
            is ScanState.ScanResults -> ShowResults(targetState.cameras, modifier) { camera -> viewModel.pair(camera) }
        }
    }
}

@Composable
private fun IdleScreen(modifier: Modifier, onDoScanClick: () -> Unit) {
    Column(modifier) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Text("No cameras discovered yet")
        }

        Button(onDoScanClick, Modifier.padding(horizontal = 32.dp, vertical = 8.dp).fillMaxWidth()) {
            Text("Scan now")
        }
    }
}

@Composable
private fun Scanning(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(32.dp))
            Spacer(Modifier.width(8.dp))
            Text("Looking for cameras...")
        }
    }
}

@Composable
private fun ShowResults(
    cameras: List<DiscoveredCamera>,
    modifier: Modifier = Modifier,
    onPairCamera: (DiscoveredCamera) -> Unit,
) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Discovered cameras!")

        LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
            items(cameras, key = { it.identifier }) {
                Card(Modifier.fillMaxWidth().padding(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📷", fontSize = 48.sp)

                        Spacer(Modifier.width(8.dp))

                        Column {
                            Text(it.modelInfo.modelName, style = MaterialTheme.typography.body1)
                            Text(it.identifier.toString(), style = MaterialTheme.typography.caption)
                        }
                    }
                }
            }
        }
    }
}
