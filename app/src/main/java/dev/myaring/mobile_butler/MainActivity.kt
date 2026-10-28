package dev.myaring.mobile_butler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.myaring.mobile_butler.domain.view.screen.initAllScreens
import dev.myaring.mobile_butler.domain.view.viewmodel.ScreenViewModel
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme

class MainActivity : ComponentActivity()
{
    private val vmScreen: ScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as MobileButlerApplication).container
        container.screenMap = initAllScreens()

        setContent {
            MobilebutlerTheme(
                content = ::updateScreenContent
            )
        }
    }

    @Composable
    public fun updateScreenContent() : Unit
    {
        val container = (application as MobileButlerApplication).container
        val eCurrentScreen by vmScreen.eCurrentScreen

        container.screenMap[eCurrentScreen]?.drawScreen(this, Modifier)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MobilebutlerTheme {
        Greeting("Android")
    }
}