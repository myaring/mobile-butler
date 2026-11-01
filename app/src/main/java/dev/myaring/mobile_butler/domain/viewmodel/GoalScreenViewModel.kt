package dev.myaring.mobile_butler.domain.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.myaring.mobile_butler.ui.navigation.EGoalTab
import dev.myaring.mobile_butler.ui.navigation.GoalScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GoalScreenViewModel : ViewModel(), ITopBarViewModel
{
    // 관찰 가능한 변수를 이 곳에 선언하세요. Composable 구성 요소가 관찰합니다.

    private val _title = mutableStateOf("목표")
    override val title: State<String> = _title

    private val _returnSubTabOrdinal = mutableIntStateOf(-1)
    override val showBackButton: State<Boolean> =
        derivedStateOf { _returnSubTabOrdinal.value >= 0 }

    private val _uiState = MutableStateFlow(GoalScreenUiState())
    public val uiState = _uiState.asStateFlow()

    public val currentTab: EGoalTab
        get() = uiState.value.selectedTab

    public override fun onBackTab()
    {
        when (_returnSubTabOrdinal.value)
        {
            0 -> onChangeTabToList()
            1 -> onChangeTabToArchive()
        }
    }

    public fun onChangeTabToList()
    {
        _returnSubTabOrdinal.value = -1
        this.onChangeTab(EGoalTab.LIST)
    }

    public fun onChangeTabToDetail()
    {
        _returnSubTabOrdinal.value = uiState.value.subTabOrdinal
        this.onChangeTab(EGoalTab.DETAIL)
    }

    public fun onChangeTabToCreation()
    {
        _returnSubTabOrdinal.value = uiState.value.subTabOrdinal
        this.onChangeTab(EGoalTab.CREATION)
    }

    public fun onChangeTabToArchive()
    {
        _returnSubTabOrdinal.value = -1
        this.onChangeTab(EGoalTab.ARCHIVE)
    }

    private fun onChangeTab(tab: EGoalTab)
    {
        _uiState.update { it.copy(selectedTab = tab) }

        // TEMP: 임시 코드입니다. 상단 바 제목을 커스텀하려면 다른 이벤트 함수에서 _title.value를 설정하세요.
        _title.value = when (tab)
        {
            EGoalTab.LIST -> "목표"
            EGoalTab.DETAIL -> "목표 상세"
            EGoalTab.CREATION -> "목표 만들기"
            EGoalTab.ARCHIVE -> "목표 보관함"
        }
    }
}