package com.rahulrav.camera

import alpha_shot.composeapp.generated.resources.Res
import alpha_shot.composeapp.generated.resources.noun_hamburger_menu
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
            ModalDrawerSheet {
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
                    startDestination = Routes.CameraScan,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable<Routes.CameraScan> {
                        CameraRoute(navController)
                    }
                    composable<Routes.Settings> {
                        Settings(navController)
                    }
                }
            }
        })
    }
}
