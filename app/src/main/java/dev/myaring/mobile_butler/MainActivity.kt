package dev.myaring.mobile_butler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.myaring.mobile_butler.ui.navigation.EScreen
import dev.myaring.mobile_butler.ui.navigation.Screen
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MobilebutlerTheme() {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { AppNavigationBar(navController) },
                    content = { padding -> AppContent(navController, padding) })
            }
        }
    }
}

@Composable
private fun AppNavigationBar(navController: NavHostController)
{
    var showNotReadyDialog by remember {
        mutableStateOf(false)
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val implScreens: Map<EScreen, Screen> = LocalContext.current.appContainer.implScreens

    NavigationBar()
    {
        for (eScreen in EScreen.entries)
        {
            NavigationBarItem(
                selected = (currentRoute == eScreen.name),
                onClick = {
                    if (implScreens[eScreen]?.screenDrawerDelegatee == null)
                    {
                        // 화면이 준비되지 않은 경우의 내비게이션 버튼 동작
                        showNotReadyDialog = true
                    }
                    else
                    {
                        // 화면이 준비된 경우의 내비게이션 버튼 동작
                        navController.navigate(eScreen.name) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
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
private fun AppContent(navController: NavHostController, padding: PaddingValues): Unit {
    AppNavigation(navController, Modifier.padding(padding))
}

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier)
{
    val implScreens: Map<EScreen, Screen> = LocalContext.current.appContainer.implScreens

    NavHost(
        navController = navController,
        startDestination = EScreen.TODAY.name
    ) {
        for (eScreen in EScreen.entries)
        {
            val drawer = implScreens[eScreen]?.screenDrawerDelegatee

            if (drawer != null)
            {
                composable(eScreen.name) {
                    drawer()
                }
            }
        }
    }
}