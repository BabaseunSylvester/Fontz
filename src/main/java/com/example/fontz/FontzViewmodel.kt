package com.example.fontz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class FontStylerViewModel : ViewModel() {
    // State is now held here, surviving configuration changes and screen switches
    var inputText by mutableStateOf("")
        private set

    var selectedSymbols by mutableStateOf("")
        private set

    var currentScreen by mutableStateOf(AppScreen.Styler)
        private set

    fun updateInputText(text: String) {
        inputText = text
    }

    fun updateSelectedSymbols(text: String) {
        selectedSymbols = text
    }

    fun appendSymbol(symbol: String) {
        selectedSymbols += symbol
    }

    fun clearSymbols() {
        selectedSymbols = ""
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
    }
}