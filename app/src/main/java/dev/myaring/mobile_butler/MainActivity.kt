package dev.myaring.mobile_butler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dev.myaring.mobile_butler.repository.SharedPreferencesGoalRepository
import dev.myaring.mobile_butler.ui.ButlerMainScreen
import dev.myaring.mobile_butler.ui.ButlerViewModel
import dev.myaring.mobile_butler.ui.ButlerViewModelFactory
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        SharedPreferencesGoalRepository(applicationContext)
    }

    private val viewModel: ButlerViewModel by viewModels {
        ButlerViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobilebutlerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ButlerMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
