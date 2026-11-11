package com.extensao.faturo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.extensao.faturo.data.local.entity.InsumoEntity
import com.extensao.faturo.data.local.entity.ItemFichaTecnicaEntity
import com.extensao.faturo.data.local.entity.ProdutoEntity
import com.extensao.faturo.data.repository.FaturoRepository
import com.extensao.faturo.domain.CalculadoraFinanceira
import com.extensao.faturo.domain.ConversorDeMedidas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FichaTecnicaViewModel(
    private val repository: FaturoRepository,
    private val produtoId: Long
) : ViewModel() {

    private val _produto = MutableStateFlow<ProdutoEntity?>(null)
    val produto: StateFlow<ProdutoEntity?> = _produto

    private val _itensCompletos = MutableStateFlow<List<Pair<ItemFichaTecnicaEntity, InsumoEntity>>>(emptyList())
    val itensCompletos: StateFlow<List<Pair<ItemFichaTecnicaEntity, InsumoEntity>>> = _itensCompletos

    val todosInsumos: StateFlow<List<InsumoEntity>> = repository.buscarTodosInsumos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        carregarDados()
    }

    private fun carregarDados() {
        viewModelScope.launch(Dispatchers.IO) {
            _produto.value = repository.buscarProdutoPorId(produtoId)
            atualizarListaDeItens()
            recalcularCustosDoProduto()
        }
    }

    private suspend fun atualizarListaDeItens() {
        val itens = repository.obterItensDoProduto(produtoId)
        val insumos = repository.buscarTodosInsumos().firstOrNull() ?: emptyList()

        val listaCompleta = itens.mapNotNull { item ->
            val insumo = insumos.find { it.id == item.insumoId }
            if (insumo != null) Pair(item, insumo) else null
        }
        _itensCompletos.value = listaCompleta
    }

    fun adicionarItemNaReceita(insumo: InsumoEntity, quantidadeUsada: Double, unidadeUsada: String) {
        val custoFracionado = ConversorDeMedidas.calcularCustoFracionado(
            precoLote = insumo.precoLote,
            quantidadeLote = insumo.quantidadeLote,
            unidadeCompraSimbolo = insumo.unidadeCompra,
            quantidadeUsada = quantidadeUsada,
            unidadeUsoSimbolo = unidadeUsada
        )

        viewModelScope.launch(Dispatchers.IO) {
            val novoItem = ItemFichaTecnicaEntity(
                produtoId = produtoId,
                insumoId = insumo.id,
                quantidadeUtilizada = quantidadeUsada,
                unidadeUtilizada = unidadeUsada,
                custoCalculadoSnapshot = custoFracionado
            )
            repository.salvarItemFicha(novoItem)
            recalcularCustosDoProduto()
        }
    }

    fun removerItemDaReceita(item: ItemFichaTecnicaEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletarItemFicha(item)
            recalcularCustosDoProduto()
        }
    }

    fun atualizarPrecoPraticado(precoReal: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val prod = _produto.value ?: return@launch
            val lucroReal = CalculadoraFinanceira.calcularLucroUnitario(precoReal, prod.custoTotalProducao)

            val produtoAtualizado = prod.copy(
                precoVendaPraticado = precoReal,
                lucroUnitario = lucroReal
            )
            repository.atualizarProduto(produtoAtualizado)
            _produto.value = produtoAtualizado
        }
    }

    private suspend fun recalcularCustosDoProduto() {
        atualizarListaDeItens()

        val itens = repository.obterItensDoProduto(produtoId)
        val prod = repository.buscarProdutoPorId(produtoId) ?: return
        val config = repository.buscarConfiguracao().firstOrNull()

        val valorHora = config?.valorHoraCalculado ?: 0.0
        val custoInsumosTotal = itens.sumOf { it.custoCalculadoSnapshot }
        val custoMaoDeObra = CalculadoraFinanceira.calcularCustoMaoDeObra(prod.tempoMinutos, valorHora)

        val custoTotal = CalculadoraFinanceira.calcularCustoTotal(
            custoInsumos = custoInsumosTotal,
            custoMaoDeObra = custoMaoDeObra,
            custoEmbalagem = prod.custoEmbalagem,
            custosIndiretos = 0.0
        )

        val precoSugerido = CalculadoraFinanceira.calcularPrecoSugerido(custoTotal, prod.margemLucroPretendida)

        val lucroUnitarioReal = if (prod.precoVendaPraticado > 0.0) {
            CalculadoraFinanceira.calcularLucroUnitario(prod.precoVendaPraticado, custoTotal)
        } else {
            0.0
        }

        val produtoAtualizado = prod.copy(
            custoInsumos = custoInsumosTotal,
            custoMaoDeObra = custoMaoDeObra,
            custoTotalProducao = custoTotal,
            precoVendaSugerido = precoSugerido,
            lucroUnitario = lucroUnitarioReal
        )

        repository.atualizarProduto(produtoAtualizado)
        _produto.value = produtoAtualizado
    }
}

class FichaTecnicaViewModelFactory(
    private val repository: FaturoRepository,
    private val produtoId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FichaTecnicaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FichaTecnicaViewModel(repository, produtoId) as T
        }
        throw IllegalArgumentException("ViewModel Desconhecido")
    }
}