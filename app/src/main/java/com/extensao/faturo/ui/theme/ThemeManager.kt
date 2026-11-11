package com.extensao.faturo.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ThemeManager {
    private const val PREFS_NAME = "faturo_theme_prefs"
    private const val KEY_THEME = "tema_selecionado"

    // 0 = Sistema, 1 = Claro, 2 = Escuro
    private val _tema = MutableStateFlow(0)
    val tema: StateFlow<Int> = _tema

    fun inicializar(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _tema.value = prefs.getInt(KEY_THEME, 0)
    }

    fun setTema(context: Context, novoTema: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME, novoTema).apply()
        _tema.value = novoTema
    }
}