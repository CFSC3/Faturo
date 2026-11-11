package com.extensao.faturo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.extensao.faturo.data.local.entity.ProdutoEntity
import com.extensao.faturo.data.local.entity.VendaEntity
import com.extensao.faturo.data.repository.FaturoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProdutosViewModel(private val repository: FaturoRepository) : ViewModel() {

    val produtos: StateFlow<List<ProdutoEntity>> = repository.buscarTodosProdutos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _statusEstoque = MutableStateFlow(emptyMap<Long, Boolean>())
    val statusEstoque = _statusEstoque.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                repository.buscarTodosProdutos(),
                repository.buscarTodosInsumos()
            ) { listaProdutos, listaInsumos ->
                val mapStatus = mutableMapOf<Long, Boolean>()

                for (prod in listaProdutos) {
                    val itens = repository.obterItensDoProduto(prod.id)
                    var podeVender = true

                    // Se a receita estiver vazia, o produto não pode ser vendido.
                    if (itens.isEmpty()) {
                        podeVender = false
                    } else {
                        for (item in itens) {
                            val insumoNoEstoque = listaInsumos.find { it.id == item.insumoId }
                            if (insumoNoEstoque == null) {
                                podeVender = false
                                break
                            }

                            val qtdNecessaria = calcularQtdDeducao(item.quantidadeUtilizada, item.unidadeUtilizada, insumoNoEstoque.unidadeCompra)

                            if (insumoNoEstoque.quantidadeLote < (qtdNecessaria - 0.001)) {
                                podeVender = false
                                break
                            }
                        }
                    }
                    mapStatus[prod.id] = podeVender
                }
                mapStatus.toMap()
            }.collect { mapStatus ->
                _statusEstoque.value = mapStatus
            }
        }
    }

    private fun calcularQtdDeducao(qtdUsada: Double, unUsada: String, unCompra: String): Double {
        var qtd = qtdUsada
        if (unCompra == "kg" && unUsada == "g") qtd /= 1000.0
        else if (unCompra == "L" && unUsada == "ml") qtd /= 1000.0
        else if (unCompra == "m" && unUsada == "cm") qtd /= 100.0
        else if (unCompra == "g" && unUsada == "kg") qtd *= 1000.0
        else if (unCompra == "ml" && unUsada == "L") qtd *= 1000.0
        else if (unCompra == "cm" && unUsada == "m") qtd *= 100.0
        return qtd
    }

    fun salvarProduto(nome: String, tempoMinutos: Int, margemLucro: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val novoProduto = ProdutoEntity(
                nome = nome,
                tempoMinutos = tempoMinutos,
                margemLucroPretendida = margemLucro
            )
            repository.salvarProduto(novoProduto)
        }
    }

    fun deletarProduto(produto: ProdutoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletarProduto(produto)
        }
    }

    fun registrarVenda(produto: ProdutoEntity, onSucesso: () -> Unit, onErro: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val itens = repository.obterItensDoProduto(produto.id)

            // NOVO: Barreira de Segurança
            if (itens.isEmpty()) {
                launch(Dispatchers.Main) { onErro("Receita Vazia! Monte a Ficha Técnica antes de vender.") }
                return@launch
            }

            val insumosAtuais = repository.buscarTodosInsumos().firstOrNull() ?: emptyList()

            var estoqueSuficiente = true
            for (item in itens) {
                val insumo = insumosAtuais.find { it.id == item.insumoId }
                if (insumo == null) {
                    estoqueSuficiente = false; break
                }
                val qtdNecessaria = calcularQtdDeducao(item.quantidadeUtilizada, item.unidadeUtilizada, insumo.unidadeCompra)
                if (insumo.quantidadeLote < (qtdNecessaria - 0.001)) {
                    estoqueSuficiente = false; break
                }
            }

            if (!estoqueSuficiente) {
                launch(Dispatchers.Main) { onErro("Estoque Insuficiente!") }
                return@launch
            }

            // Deduz o estoque
            itens.forEach { item ->
                val insumo = insumosAtuais.find { it.id == item.insumoId }!!
                val qtdDeducao = calcularQtdDeducao(item.quantidadeUtilizada, item.unidadeUtilizada, insumo.unidadeCompra)
                val novoEstoqueLote = insumo.quantidadeLote - qtdDeducao

                val estoqueFinalLote = if (novoEstoqueLote < 0) 0.0 else novoEstoqueLote

                val novaQtdItens = if (insumo.unidadeCompra == "un") {
                    estoqueFinalLote
                } else {
                    if (insumo.capacidadePorItem > 0) estoqueFinalLote / insumo.capacidadePorItem else estoqueFinalLote
                }

                repository.atualizarInsumo(insumo.copy(
                    quantidadeLote = estoqueFinalLote,
                    quantidadeItens = novaQtdItens
                ))
            }

            val faturamento = if (produto.precoVendaPraticado > 0) produto.precoVendaPraticado else produto.precoVendaSugerido
            val lucroRealDaPeca = if (produto.lucroUnitario > 0) {
                produto.lucroUnitario
            } else {
                faturamento - produto.custoTotalProducao
            }

            val reciboDeVenda = VendaEntity(
                produtoId = produto.id,
                produtoNome = produto.nome,
                dataVenda = System.currentTimeMillis(),
                faturamentoBruto = faturamento,
                custoProducao = produto.custoTotalProducao,
                lucroLiquido = lucroRealDaPeca,
                valorMaoDeObra = produto.custoMaoDeObra
            )
            repository.salvarVenda(reciboDeVenda)

            val produtoAtualizado = produto.copy(totalVendas = produto.totalVendas + 1)
            repository.atualizarProduto(produtoAtualizado)

            launch(Dispatchers.Main) { onSucesso() }
        }
    }
}

class ProdutosViewModelFactory(private val repository: FaturoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProdutosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProdutosViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel Desconhecido")
    }
}