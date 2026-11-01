package dev.myaring.mobile_butler.domain.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ScheduleScreenViewModel : ViewModel(), ITopBarViewModel
{
    private val _title = mutableStateOf("일정")
    override val title: State<String> = _title

    private val _showBackButton = mutableStateOf(false)
    override val showBackButton: State<Boolean> = _showBackButton

    override fun onBackTab()
    {
        Log.d("VM_ScheduleScreen", "Back OK")
    }
}