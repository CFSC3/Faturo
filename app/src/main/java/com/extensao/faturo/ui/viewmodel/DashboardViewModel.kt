package com.extensao.faturo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.extensao.faturo.data.local.entity.ConfiguracaoNegocioEntity
import com.extensao.faturo.data.local.entity.VendaEntity
import com.extensao.faturo.data.repository.FaturoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ResumoMes(
    val mesAno: String,
    val faturamentoBruto: Double,
    val custoProducao: Double,
    val lucroLiquido: Double,
    val valorMaoDeObra: Double,
    val topProdutos: List<ProdutoRanking>
)

data class ProdutoRanking(
    val nome: String,
    val quantidade: Int,
    val lucroGerado: Double
)

class DashboardViewModel(private val repository: FaturoRepository) : ViewModel() {

    val configuracao: StateFlow<ConfiguracaoNegocioEntity?> = repository.buscarConfiguracao()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val todasVendas = repository.buscarTodasVendas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mesesDisponiveis = MutableStateFlow<List<String>>(emptyList())
    val mesesDisponiveis = _mesesDisponiveis.asStateFlow()

    private val _mesSelecionado = MutableStateFlow("")
    val mesSelecionado = _mesSelecionado.asStateFlow()

    private val _resumoAtual = MutableStateFlow<ResumoMes?>(null)
    val resumoAtual = _resumoAtual.asStateFlow()

    init {
        viewModelScope.launch {
            todasVendas.collect { vendas ->
                processarVendas(vendas, _mesSelecionado.value)
            }
        }
    }

    fun selecionarMes(mesAno: String) {
        _mesSelecionado.value = mesAno
        processarVendas(todasVendas.value, mesAno)
    }

    private fun processarVendas(vendas: List<VendaEntity>, mesFiltro: String) {
        if (vendas.isEmpty()) {
            _mesesDisponiveis.value = emptyList()
            _resumoAtual.value = null
            return
        }

        val formatador = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))

        val vendasPorMes = vendas.groupBy {
            formatador.format(Date(it.dataVenda)).replaceFirstChar { char -> char.uppercase() }
        }

        val listaMeses = vendasPorMes.keys.toList()
        _mesesDisponiveis.value = listaMeses

        val mesAlvo = if (mesFiltro.isBlank() || !listaMeses.contains(mesFiltro)) listaMeses.first() else mesFiltro
        if (_mesSelecionado.value != mesAlvo) {
            _mesSelecionado.value = mesAlvo
        }

        val vendasDoMes = vendasPorMes[mesAlvo] ?: emptyList()

        val faturamento = vendasDoMes.sumOf { it.faturamentoBruto }
        val custo = vendasDoMes.sumOf { it.custoProducao }
        val maoDeObra = vendasDoMes.sumOf { it.valorMaoDeObra }

        val lucro = vendasDoMes.sumOf {
            if (it.lucroLiquido > 0) it.lucroLiquido else (it.faturamentoBruto - it.custoProducao)
        }

        val ranking = vendasDoMes.groupBy { it.produtoNome }.map { (nome, listaVendas) ->
            ProdutoRanking(
                nome = nome,
                quantidade = listaVendas.size,
                lucroGerado = listaVendas.sumOf {
                    if (it.lucroLiquido > 0) it.lucroLiquido else (it.faturamentoBruto - it.custoProducao)
                }
            )
        }.sortedByDescending { it.lucroGerado }

        _resumoAtual.value = ResumoMes(
            mesAno = mesAlvo,
            faturamentoBruto = faturamento,
            custoProducao = custo,
            lucroLiquido = lucro,
            valorMaoDeObra = maoDeObra,
            topProdutos = ranking
        )
    }
}

class DashboardViewModelFactory(private val repository: FaturoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel Desconhecido")
    }
}