package dev.myaring.mobile_butler.ui.component

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import dev.myaring.mobile_butler.ui.theme.MbInversePrimary
import dev.myaring.mobile_butler.ui.theme.MbOnSurface
import dev.myaring.mobile_butler.ui.theme.MbShapes
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer

/**
 * [B-9] 화면 하단 스낵바 호스트 상태를 주입하기 위한 CompositionLocal.
 */
val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

/**
 * [B-9] 기록 스낵바와 되돌리기 호스트 컴포넌트.
 * Scaffold의 snackbarHost 자리에 배치되어 스낵바 메시지와 되돌리기 버튼을 렌더링합니다.
 */
@Composable
fun MbSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data: SnackbarData ->
            Snackbar(
                snackbarData = data,
                shape = MbShapes.small,
                containerColor = MbSurfaceContainer,
                contentColor = MbOnSurface,
                actionColor = MbInversePrimary,
                dismissActionContentColor = MbOnSurface,
            )
        }
    )
}

/**
 * 메시지와 되돌리기 액션을 표시하고 되돌리기 클릭 시 콜백을 실행하는 헬퍼 함수.
 *
 * @param message 표시할 안내 문구 (예: "오늘 20쪽을 모두 기록했습니다")
 * @param actionLabel 버튼 문구 (기본값: "되돌리기")
 * @param duration 노출 시간 (기본값: Short)
 * @param onUndo 되돌리기 버튼 클릭 시 호출되는 콜백
 */
suspend fun SnackbarHostState.showUndoSnackbar(
    message: String,
    actionLabel: String = "되돌리기",
    duration: SnackbarDuration = SnackbarDuration.Short,
    onUndo: suspend () -> Unit,
) {
    currentSnackbarData?.dismiss()
    val result = showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = duration,
    )
    if (result == SnackbarResult.ActionPerformed) {
        onUndo()
    }
}
