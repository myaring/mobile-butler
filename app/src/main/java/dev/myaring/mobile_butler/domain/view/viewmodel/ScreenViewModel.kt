package dev.myaring.mobile_butler.domain.view.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.myaring.mobile_butler.domain.view.screen.EScreen

class ScreenViewModel : ViewModel()
{
    private val _eCurrentScreen = mutableStateOf<EScreen>(EScreen.MAIN_MENU)

    public val eCurrentScreen: State<EScreen>
        get() = _eCurrentScreen

    public fun changeScreen(eScreen: EScreen)
    {
        val screenChanged: Boolean = (eScreen != _eCurrentScreen.value)

        _eCurrentScreen.value = eScreen

        if (screenChanged)
        {
            Log.d("VM_Screen", "Screen changed to ${eScreen.name}.")
        }
    }
}