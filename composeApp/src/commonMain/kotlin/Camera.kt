import alpha_shot.composeapp.generated.resources.Res
import alpha_shot.composeapp.generated.resources.noun_camera_crossed
import alpha_shot.composeapp.generated.resources.noun_camera_filled
import alpha_shot.composeapp.generated.resources.noun_camera_outline
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.juul.kable.State
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class CameraStates {
  OTHER,
  CONNECTED
}

private fun CameraStates.label(): String =
    if (this == CameraStates.CONNECTED) {
      "Connected to Camera"
    } else {
      "Attempting to connect to Camera"
    }

@Composable
fun Camera(cameraControl: SonyCameraControl) {
  val scope = rememberCoroutineScope()
  var cameraState by remember { mutableStateOf(CameraStates.OTHER) }
  AnimatedContent(
      targetState = cameraState,
      transitionSpec = {
        val enter = slideInVertically { height -> height } + fadeIn()
        val exit = slideOutVertically { height -> -height } + fadeOut()
        enter
            .togetherWith(exit)
            .using(
                // We want to animate out of bounds to give the impression that this is
                // flying outside the container.
                SizeTransform(clip = false))
      },
      label = "Camera control") {
        var isTakingAPicture by remember { mutableStateOf(false) }
        val fractionalValue = if (isTakingAPicture) 0.80F else 0.6F
        val fraction by
            animateFloatAsState(
                fractionalValue,
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessMediumLow),
                label = "Icon Zoom Animation",
                finishedListener = { isTakingAPicture = false })
        val resource: DrawableResource =
            when {
              isTakingAPicture -> Res.drawable.noun_camera_filled
              cameraState == CameraStates.OTHER -> Res.drawable.noun_camera_crossed
              else -> Res.drawable.noun_camera_outline
            }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Image(
                  painterResource(resource),
                  modifier =
                      Modifier.fillMaxWidth(fraction = fraction).clickable {
                        scope.launch {
                          if (cameraState == CameraStates.CONNECTED) {
                            isTakingAPicture = true
                            cameraControl.capturePhoto()
                          }
                        }
                      },
                  contentScale = ContentScale.FillWidth,
                  contentDescription = "Camera Icon")
              Text(it.label())
            }
      }
  LaunchedEffect(Unit) {
    scope.launch { cameraControl.connect() }
    scope.launch {
      cameraControl.cameraState().collect {
        cameraState =
            when (it) {
              is State.Connected -> CameraStates.CONNECTED
              else -> CameraStates.OTHER
            }
      }
    }
  }
  DisposableEffect(cameraControl) { onDispose { cameraControl.dispose() } }
}
