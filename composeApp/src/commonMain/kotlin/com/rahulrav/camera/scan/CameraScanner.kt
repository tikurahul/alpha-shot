package com.rahulrav.camera.scan

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.imageResource

@Composable
fun CameraScanner(
    viewModel: CameraScannerViewModel,
    modifier: Modifier = Modifier,
) {
    val state = viewModel.state.value
    AnimatedContent(state, transitionSpec = { fadeIn() togetherWith fadeOut() }) { targetState ->
        when (targetState) {
            ScanState.Idle -> IdleScreen(modifier) { viewModel.doScan() }
            is ScanState.Scanning ->
                Scanning(
                    targetState.cameras,
                    modifier,
                    { camera -> viewModel.pairAndConnect(camera) },
                    { viewModel.stopScan() },
                )
            is ScanState.IdleWithResults ->
                ScanStopped(
                    targetState.cameras,
                    modifier,
                    { camera -> viewModel.pairAndConnect(camera) },
                    { viewModel.doScan() },
                )
        }
    }
}

@Composable
private fun IdleScreen(modifier: Modifier, onDoScanClick: () -> Unit) {
    Column(modifier) {
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Text("No cameras discovered yet")
        }

        Button(
            onDoScanClick, Modifier.padding(horizontal = 32.dp, vertical = 8.dp).fillMaxWidth()) {
                Text("Scan now")
            }
    }
}

@Composable
private fun Scanning(
    cameras: List<DiscoveredCamera>,
    modifier: Modifier = Modifier,
    onPairCamera: (DiscoveredCamera) -> Unit,
    onStopScanClick: () -> Unit,
) {
    Column(modifier) {
        Spacer(Modifier.height(72.dp))

        ScanningHeader(Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.height(16.dp))

        if (cameras.isNotEmpty()) {
            ScanResults(cameras, Modifier.fillMaxWidth().weight(1f), onPairCamera)
        } else {
            Spacer(Modifier.weight(1f))
        }

        Button(
            onStopScanClick, Modifier.padding(horizontal = 32.dp, vertical = 8.dp).fillMaxWidth()) {
                Text("Stop scanning")
            }
    }
}

@Composable
private fun ScanningHeader(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(32.dp))

            Spacer(Modifier.width(16.dp))

            DisabledAlpha { Text("Looking for cameras...") }
        }
    }
}

@Composable
private fun ScanStopped(
    cameras: List<DiscoveredCamera>,
    modifier: Modifier = Modifier,
    onPairCamera: (DiscoveredCamera) -> Unit,
    onRestartScanClick: () -> Unit,
) {
    Column(modifier) {
        ScanResults(cameras, modifier, onPairCamera)

        Button(
            onRestartScanClick,
            Modifier.padding(horizontal = 32.dp, vertical = 8.dp).fillMaxWidth()) {
                Text("Restart scanning")
            }
    }
}

@Composable
private fun ScanResults(
    cameras: List<DiscoveredCamera>,
    modifier: Modifier = Modifier,
    onPairCamera: (DiscoveredCamera) -> Unit,
) {
    Column(
        modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Discovered cameras!")

            LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                items(cameras, key = { it.identifier }) { camera ->
                    Card(Modifier.fillMaxWidth().clickable { onPairCamera(camera) }) {
                        Row(
                            Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    imageResource(camera.modelInfo.picture),
                                    null,
                                    Modifier.size(48.dp))

                                Spacer(Modifier.width(8.dp))

                                Column {
                                    Text(
                                        camera.modelInfo.modelName,
                                        style = MaterialTheme.typography.body1)
                                    DisabledAlpha {
                                        Text(
                                            "${camera.name} (${camera.identifier.toString()})",
                                            style = MaterialTheme.typography.caption)
                                    }
                                }
                            }
                    }
                }
            }
        }
}

@Composable
private fun DisabledAlpha(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled, content)
}
