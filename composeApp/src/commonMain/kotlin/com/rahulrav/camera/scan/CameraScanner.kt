package com.rahulrav.camera.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow

@Composable
fun CameraScanner(
    viewModel: CameraScannerViewModel,
    modifier: Modifier = Modifier,
    onCameraSelected: (SupportedAlphaCamera) -> Unit,
) {
    when (val state = viewModel.state.value) {
        is ScanState.StartingScan -> Scanning(modifier)
        is ScanState.ScanResults -> ShowResults(state.cameras, { viewModel.pair(it) }, modifier)
    }
}

@Composable
private fun Scanning(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Looking for cameras...")
        }
    }
}

@Composable
private fun ShowResults(
    cameras: List<DiscoveredCamera>,
    onPairCamera: (DiscoveredCamera) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Discovered cameras!")

        LazyColumn {
            items(cameras, key = { it.identifier }) {
                Text(it.modelInfo.modelName)
            }
        }
    }
}
