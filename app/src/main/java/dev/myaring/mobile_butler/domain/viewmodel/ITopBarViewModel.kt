package dev.myaring.mobile_butler.domain.viewmodel

import androidx.compose.runtime.State

interface ITopBarViewModel {
    val title: State<String>
    val showBackButton: State<Boolean>

    fun onBackTab()
}