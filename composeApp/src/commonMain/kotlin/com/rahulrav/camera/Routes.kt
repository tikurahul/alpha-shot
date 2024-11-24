package com.rahulrav.camera

import alpha_shot.composeapp.generated.resources.Res
import alpha_shot.composeapp.generated.resources.noun_camera_outline
import alpha_shot.composeapp.generated.resources.noun_home
import alpha_shot.composeapp.generated.resources.noun_settings
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource

@Serializable
sealed class Routes {
    @Serializable
    data object CameraScan : Routes()

    @Serializable
    data object Settings : Routes()
}

data class TopLevelRoute<R : Routes>(
    val name: String, val icon: DrawableResource, val route: R
)

val TOP_LEVEL_HOME_ROUTE = TopLevelRoute(
    "Home", Res.drawable.noun_home, Routes.CameraScan
)

val TOP_LEVEL_ROUTES = listOf(
    TOP_LEVEL_HOME_ROUTE,
    TopLevelRoute("Scan", Res.drawable.noun_camera_outline, Routes.CameraScan),
    TopLevelRoute("Settings", Res.drawable.noun_settings, Routes.Settings)
)
