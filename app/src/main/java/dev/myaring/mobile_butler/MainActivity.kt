package dev.myaring.mobile_butler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.myaring.mobile_butler.domain.viewmodel.ITopBarViewModel
import dev.myaring.mobile_butler.ui.navigation.EScreen
import dev.myaring.mobile_butler.ui.navigation.Screen
import dev.myaring.mobile_butler.ui.theme.MbOnSurface
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme

val INIT_E_SCREEN: EScreen = EScreen.TODAY

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MobilebutlerTheme() {
                val navController = rememberNavController()
                val appContainer = LocalContext.current.appContainer

                val implScreens = appContainer.implScreens

                var vmTopBar by remember { mutableStateOf<ITopBarViewModel?>(null) }

                fun notifyTopBar(viewModel: ITopBarViewModel)
                {
                    vmTopBar = viewModel
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        AppTopBar(vmTopBar)
                    },
                    bottomBar = {
                        AppNavigationBar(navController, implScreens)
                    },
                    content = { innerPadding ->
                        AppContent(
                            navController = navController,
                            implScreens = implScreens,
                            onNotifyTopBar = ::notifyTopBar,
                            modifier = Modifier.padding(innerPadding))
                    })
            }
        }
    }
}

@Composable
private fun NavController.GetCurrentScreen(implScreens: Map<EScreen, Screen>): Screen
{
    val backStackEntry by currentBackStackEntryAsState()

    var eScreen: EScreen = backStackEntry
        ?.destination
        ?.route
        ?.let { route ->
            EScreen.entries.find { it.name == route }
        }
        ?: INIT_E_SCREEN

    val currentScreenOrNull: Screen? = implScreens[eScreen]

    if (currentScreenOrNull == null)
    {
        throw IllegalArgumentException("Screen not found")
    }
    else
    {
        return currentScreenOrNull
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(viewModel: ITopBarViewModel?)
{
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MbSurfaceContainer,
            titleContentColor = MbOnSurface
        ),
        navigationIcon =  {
            if (viewModel != null && viewModel.showBackButton.value)
            {
                TextButton(onClick = viewModel::onBackTab) {
                    Text("←")
                }
            }
        },
        title = {  Text(viewModel?.title?.value ?: "null") }
    )
}

@Composable
private fun AppNavigationBar(navController: NavHostController, implScreens: Map<EScreen, Screen>)
{
    var showNotReadyDialog by remember {
        mutableStateOf(false)
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar()
    {
        for (eScreen in EScreen.entries)
        {
            val targetScreen = implScreens[eScreen]

            NavigationBarItem(
                selected = (currentRoute == eScreen.name),
                onClick = {
                    if (targetScreen?.screenDrawerDelegateeOrNull == null)
                    {
                        // 화면이 준비되지 않은 경우의 내비게이션 버튼 동작
                        showNotReadyDialog = true
                    }
                    else
                    {
                        if (currentRoute != eScreen.name)
                        {
                            // 화면이 준비된 경우의 내비게이션 버튼 동작
                            // 우선은, navigation stack의 최상위만 남기는 동작을 수행
                            navController.navigate(eScreen.name) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }

                                launchSingleTop = true
                            }
                        }
                    }
                },
                icon = {},
                label = { Text(eScreen.name) }
            )
        }
    }

    if (showNotReadyDialog)
    {
        AlertDialog(
            onDismissRequest = { showNotReadyDialog = false },
            title = { Text("준비 중") },
            text = { Text("기대해 주세요") },
            confirmButton = {
                TextButton(
                    onClick = { showNotReadyDialog = false }
                ) {
                    Text("확인")
                }
            }
        )
    }
}

@Composable
private fun AppContent(
    navController: NavHostController,
    implScreens: Map<EScreen, Screen>,
    onNotifyTopBar: (ITopBarViewModel) -> Unit,
    modifier: Modifier)
{
    NavHost(
        navController = navController,
        startDestination = INIT_E_SCREEN.name,
        modifier = modifier
    ) {
        for (eScreen in EScreen.entries)
        {
            val route = eScreen.name
            val contentDrawer = implScreens[eScreen]?.screenDrawerDelegateeOrNull

            if (contentDrawer != null)
            {
                composable(route) {
                    contentDrawer(onNotifyTopBar, modifier)
                }
            }
        }
    }
}