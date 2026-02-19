package com.unifiedotaku.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unifiedotaku.app.data.extensions.LoadedExtension
import com.unifiedotaku.app.data.extensions.ExtensionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExtensionSettingsViewModel @Inject constructor(
    private val extensionManager: ExtensionManager
) : ViewModel() {

    val extensions: StateFlow<List<LoadedExtension>> = extensionManager.loadedExtensions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
