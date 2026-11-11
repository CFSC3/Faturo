package com.extensao.faturo.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.extensao.faturo.data.repository.FaturoRepository
import com.extensao.faturo.ui.navigation.FaturoNavGraph
import com.extensao.faturo.ui.navigation.Screen

@Composable
fun FaturoApp(repository: FaturoRepository) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = navBackStackEntry?.destination?.route

    // CORREÇÃO: Usando a rota oficial do Screen.kt
    val telasComBarraInferior = listOf(
        Screen.Dashboard.route,
        Screen.Produtos.route,
        Screen.Insumos.route,
        Screen.Configuracoes.route
    )

    Scaffold(
        bottomBar = {
            if (rotaAtual in telasComBarraInferior) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    // Botão: Dashboard (Início)
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                        label = { Text("Início") },
                        selected = rotaAtual == Screen.Dashboard.route,
                        onClick = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    // Botão: Produtos
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Store, contentDescription = "Produtos") },
                        label = { Text("Produtos") },
                        selected = rotaAtual == Screen.Produtos.route,
                        onClick = {
                            navController.navigate(Screen.Produtos.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    // Botão: Materiais
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Materiais") },
                        label = { Text("Materiais") },
                        selected = rotaAtual == Screen.Insumos.route,
                        onClick = {
                            navController.navigate(Screen.Insumos.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    // Botão: Configurações
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Sell, contentDescription = "Ajustes") },
                        label = { Text("Meu valor") },
                        selected = rotaAtual == Screen.Configuracoes.route,
                        onClick = {
                            navController.navigate(Screen.Configuracoes.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingInterno ->
        FaturoNavGraph(
            navController = navController,
            repository = repository,
            modifier = Modifier.padding(paddingInterno)
        )
    }
}