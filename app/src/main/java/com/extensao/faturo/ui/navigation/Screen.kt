package com.extensao.faturo.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard_screen")
    object Produtos : Screen("produtos_screen")
    object Insumos : Screen("insumos_screen")
    object Configuracoes : Screen("configuracoes_screen")

    object FichaTecnica : Screen("ficha_tecnica_screen/{produtoId}") {
        fun criarRota(produtoId: Long): String {
            return "ficha_tecnica_screen/$produtoId"
        }
    }
}