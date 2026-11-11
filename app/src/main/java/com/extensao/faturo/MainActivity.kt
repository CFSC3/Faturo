package com.extensao.faturo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.extensao.faturo.ui.FaturoApp
import com.extensao.faturo.ui.theme.FaturôTheme
import com.extensao.faturo.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeManager.inicializar(this)

        setContent {
            val temaSelecionado by ThemeManager.tema.collectAsState()

            val useDarkTheme = when (temaSelecionado) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            FaturôTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val application = applicationContext as FaturoApplication
                    val repository = application.repository

                    FaturoApp(repository = repository)
                }
            }
        }
    }
}