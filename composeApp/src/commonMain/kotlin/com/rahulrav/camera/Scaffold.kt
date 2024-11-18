package com.rahulrav.camera

import alpha_shot.composeapp.generated.resources.Res
import alpha_shot.composeapp.generated.resources.noun_hamburger_menu
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

// Copied from ModalDrawerSheet. The use of predictive back here breaks Compose Multiplatform
// Desktop Previews.

@Composable
fun DrawerSheet(
    windowInsets: WindowInsets = DrawerDefaults.windowInsets,
    modifier: Modifier = Modifier,
    drawerShape: Shape = RectangleShape,
    drawerContainerColor: Color = DrawerDefaults.standardContainerColor,
    drawerContentColor: Color = contentColorFor(drawerContainerColor),
    drawerTonalElevation: Dp = DrawerDefaults.PermanentDrawerElevation,
    content: @Composable ColumnScope.() -> Unit
) {
    val predictiveBackDrawerContainerModifier = Modifier // Snip Snip
    Surface(
        modifier = modifier.sizeIn(minWidth = 240.dp, maxWidth = DrawerDefaults.MaximumDrawerWidth)
            .then(predictiveBackDrawerContainerModifier).fillMaxHeight(),
        shape = drawerShape,
        color = drawerContainerColor,
        contentColor = drawerContentColor,
        tonalElevation = drawerTonalElevation
    ) {
        val predictiveBackDrawerChildModifier = Modifier
        Column(
            Modifier.sizeIn(
                minWidth = 240.dp, maxWidth = DrawerDefaults.MaximumDrawerWidth
            ).then(predictiveBackDrawerChildModifier).windowInsetsPadding(windowInsets),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerScaffold() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    MaterialTheme {
        ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            DrawerSheet {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Spacer(Modifier.height(12.dp))
                    TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
                        NavigationDrawerItem(
                            label = {
                                Text(topLevelRoute.name)
                            },
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                }
                                navController.navigate(topLevelRoute.route) {
                                    // I just copied this section from the documentation.
                                    // https://developer.android.com/develop/ui/compose/navigation#bottom-nav

                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // re-selecting the same item
                                    launchSingleTop = true
                                    // Restore state when re-selecting a previously selected item
                                    restoreState = true
                                }
                            },
                            selected = currentDestination?.hierarchy?.any {
                                it.hasRoute(
                                    topLevelRoute.route::class
                                )
                            } == true,
                            modifier = Modifier.padding(
                                NavigationDrawerItemDefaults.ItemPadding
                            )
                        )
                    }
                }
            }
        }, content = {
            val currentRoute = remember(navController) {
                TOP_LEVEL_ROUTES.find { topLevelRoute ->
                    navController.currentDestination?.hierarchy?.any {
                        it.hasRoute(topLevelRoute.route::class)
                    } ?: false
                } ?: TOP_LEVEL_HOME_ROUTE
            }

            Scaffold(topBar = {
                TopAppBar(title = {
                    Text(currentRoute.name)
                }, navigationIcon = {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(
                            painter = painterResource(Res.drawable.noun_hamburger_menu),
                            contentDescription = "Menu"
                        )
                    }
                })
            }) { innerPadding ->
                NavHost(
                    navController,
                    startDestination = Routes.CameraControl,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable<Routes.CameraControl> {
                        Text("Camera Control")
                    }
                    composable<Routes.Settings> {
                        Text("Settings")
                    }
                }
            }
        })
    }
}
