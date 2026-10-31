package dev.myaring.mobile_butler.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.myaring.mobile_butler.domain.viewmodel.ITopBarViewModel

typealias ScreenRouter = @Composable ((ITopBarViewModel) -> Unit, Modifier) -> Unit
typealias TabDrawer<T_VM> = @Composable (T_VM, Modifier) -> Unit

class Screen(
    val eScreen: EScreen,
    var screenDrawerDelegateeOrNull: ScreenRouter? = null
)
{

}