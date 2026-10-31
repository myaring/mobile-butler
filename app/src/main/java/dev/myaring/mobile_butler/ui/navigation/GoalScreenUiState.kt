package dev.myaring.mobile_butler.ui.navigation

// 현재 상태 selectedTab을 기반으로 파생될 수 있는 각종 상태들을 이 곳에 정의하세요.
data class GoalScreenUiState(
    val selectedTab: EGoalTab = EGoalTab.LIST
)
{
    public val subTabOrdinal: Int
        get() = when (selectedTab)
        {
            EGoalTab.LIST -> 0
            EGoalTab.ARCHIVE -> 1
            else -> -1
        }
}