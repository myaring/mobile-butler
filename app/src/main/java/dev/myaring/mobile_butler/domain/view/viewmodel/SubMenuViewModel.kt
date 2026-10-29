package dev.myaring.mobile_butler.domain.view.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubMenuViewModel : ViewModel()
{
    private val _uiState = MutableStateFlow<Int>(0)

    public val uiState: StateFlow<Int> = _uiState.asStateFlow()

    public fun onButtonClick() : Unit
    {
        // 1. 화면 좌하단의 Logcat 버튼을 눌러 로그 화면을 여세요.
        // 2. 앱을 실행합니다.
        // 3. 버튼 클릭 후 로그 화면을 확인하세요.
        Log.d("VM_SubMenu", "Button Clicked.")
    }

    public fun onScoreUp() : Unit
    {
        _uiState.value += 1
        Log.d("VM_SubMenu", "Score == ${_uiState.value}")
    }
}