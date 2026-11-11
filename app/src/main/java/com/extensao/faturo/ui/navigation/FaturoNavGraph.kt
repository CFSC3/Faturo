package com.extensao.faturo.ui.navigation

import androidx.compose.runtime.Composable
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
import com.extensao.faturo.ui.screens.SobreScreen
import com.extensao.faturo.ui.screens.SplashScreen // IMPORTAÇÃO DA NOVA TELA
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
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {

        composable(route = Screen.Splash.route) {
            SplashScreen(
                onCarregamentoFinalizado = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(repository)
            )
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToSobre = { navController.navigate(Screen.Sobre.route) }
            )
        }

        composable(route = Screen.Produtos.route) {
            val viewModel: ProdutosViewModel = viewModel(
                factory = ProdutosViewModelFactory(repository)
            )
            ProdutosScreen(
                viewModel = viewModel,
                onProdutoClick = { produtoId ->
                    navController.navigate(Screen.FichaTecnica.criarRota(produtoId))
                },
                onNavigateToSobre = { navController.navigate(Screen.Sobre.route) }
            )
        }

        composable(route = Screen.Insumos.route) {
            val viewModel: InsumosViewModel = viewModel(
                factory = InsumosViewModelFactory(repository)
            )
            InsumosScreen(
                viewModel = viewModel,
                onNavigateToSobre = { navController.navigate(Screen.Sobre.route) }
            )
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
            ConfiguracoesScreen(
                viewModel = viewModel,
                onNavigateToSobre = { navController.navigate(Screen.Sobre.route) }
            )
        }

        composable(route = Screen.Sobre.route) {
            SobreScreen(onVoltar = { navController.popBackStack() })
        }
    }
}