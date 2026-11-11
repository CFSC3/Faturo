package com.extensao.faturo.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.extensao.faturo.data.repository.FaturoRepository
import com.extensao.faturo.ui.screens.FichaTecnicaScreen
import com.extensao.faturo.ui.screens.InsumosScreen
import com.extensao.faturo.ui.screens.ProdutosScreen
import com.extensao.faturo.ui.viewmodel.FichaTecnicaViewModel
import com.extensao.faturo.ui.viewmodel.FichaTecnicaViewModelFactory
import com.extensao.faturo.ui.viewmodel.InsumosViewModel
import com.extensao.faturo.ui.viewmodel.InsumosViewModelFactory
import com.extensao.faturo.ui.viewmodel.ProdutosViewModel
import com.extensao.faturo.ui.viewmodel.ProdutosViewModelFactory
import com.extensao.faturo.ui.screens.ConfiguracoesScreen
import com.extensao.faturo.ui.screens.DashboardScreen
import com.extensao.faturo.ui.viewmodel.ConfiguracaoViewModel
import com.extensao.faturo.ui.viewmodel.ConfiguracaoViewModelFactory
import com.extensao.faturo.ui.viewmodel.DashboardViewModel
import com.extensao.faturo.ui.viewmodel.DashboardViewModelFactory

@Composable
fun FaturoNavGraph(
    navController: NavHostController,
    repository: FaturoRepository,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {

        // ================= ROTAS ================= //

        composable(route = Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(repository)
            )
            DashboardScreen(viewModel = viewModel)
        }

        composable(route = Screen.Produtos.route) {
            val viewModel: ProdutosViewModel = viewModel(
                factory = ProdutosViewModelFactory(repository)
            )
            ProdutosScreen(
                viewModel = viewModel,
                onProdutoClick = { produtoId ->
                    navController.navigate(Screen.FichaTecnica.criarRota(produtoId))
                }
            )
        }

        composable(route = Screen.Insumos.route) {
            val viewModel: InsumosViewModel = viewModel(
                factory = InsumosViewModelFactory(repository)
            )
            InsumosScreen(viewModel = viewModel)
        }

        composable(
            route = Screen.FichaTecnica.route,
            arguments = listOf(navArgument("produtoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val produtoId = backStackEntry.arguments?.getLong("produtoId") ?: 0L

            val viewModel: FichaTecnicaViewModel = viewModel(
                factory = FichaTecnicaViewModelFactory(repository, produtoId)
            )

            FichaTecnicaScreen(viewModel = viewModel)
        }

        composable(route = Screen.Configuracoes.route) {
            val viewModel: ConfiguracaoViewModel = viewModel(
                factory = ConfiguracaoViewModelFactory(repository)
            )
            ConfiguracoesScreen(viewModel = viewModel)
        }
    }
}