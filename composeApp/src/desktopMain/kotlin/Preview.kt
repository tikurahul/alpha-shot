import alpha_shot.composeapp.generated.resources.Res
import alpha_shot.composeapp.generated.resources.noun_hamburger_menu
import alpha_shot.composeapp.generated.resources.noun_home
import alpha_shot.composeapp.generated.resources.noun_settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun PreviewApp() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val controller = rememberNavController()
    val screens = remember { arrayOf(Screen.Home, Screen.Settings) }
    MaterialTheme {
        NavHost(controller, startDestination = Screen.Home.id()) {
            screens.forEach { screen ->
                composable(screen.id()) {
                    Screen(coroutineScope, drawerState, controller, screens)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(
    scope: CoroutineScope,
    drawerState: DrawerState,
    navController: NavController,
    screens: Array<Screen>
) {
    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        DrawerSheet {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(12.dp))
                screens.forEach { screen ->
                    NavigationDrawerItem(
                        label = {
                            Text(screen.id())
                        },
                        onClick = {
                            scope.launch {
                                drawerState.close()
                            }
                            navController.navigate(screen.id()) {
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
                        selected = navController.currentDestination?.route == screen.id(),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    }, content = {
        val screen = remember(navController, screens) {
            screens.find { it.id() == navController.currentDestination?.route }
        } ?: Screen.Home

        Scaffold(topBar = {
            TopAppBar(title = {
                Text(screen.id())
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
        }) { paddingValues ->
            screen.Slot(Modifier.padding(paddingValues))
        }
    })
}

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

// Screens

@Serializable
abstract class Screen {
    abstract fun id(): String
    abstract fun icon(): DrawableResource

    abstract fun description(): String

    @Composable
    abstract fun Slot(modifier: Modifier): Unit

    object Home : Screen() {
        override fun id(): String = "Home"
        override fun icon(): DrawableResource = Res.drawable.noun_home
        override fun description(): String = "Home Icon"

        @Composable
        override fun Slot(modifier: Modifier) {
            Column(
                modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("There is no place like it.")
            }
        }
    }

    object Settings : Screen() {
        override fun id(): String = "Settings"
        override fun icon(): DrawableResource = Res.drawable.noun_settings
        override fun description(): String = "Settings Icon"

        @Composable
        override fun Slot(modifier: Modifier) {
            Column(
                modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("More settings go here.")
            }
        }
    }
}
