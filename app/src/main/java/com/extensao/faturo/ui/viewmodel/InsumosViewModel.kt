package com.extensao.faturo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.extensao.faturo.data.local.entity.InsumoEntity
import com.extensao.faturo.data.repository.FaturoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InsumosViewModel(private val repository: FaturoRepository) : ViewModel() {

    val insumos: StateFlow<List<InsumoEntity>> = repository.buscarTodosInsumos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun salvarInsumo(nome: String, precoUnitarioFixo: Double, precoTotal: Double, capacidadeTotal: Double, qtdItens: Double, capacidadeItem: Double, unidade: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val novoInsumo = InsumoEntity(
                nome = nome,
                precoLote = precoTotal,
                quantidadeLote = capacidadeTotal,
                unidadeCompra = unidade,
                quantidadeItens = qtdItens,
                capacidadePorItem = capacidadeItem,
                precoUnitarioFixo = precoUnitarioFixo
            )
            repository.salvarInsumo(novoInsumo)
        }
    }

    fun atualizarEstoqueInsumo(insumo: InsumoEntity, novoPrecoUnitarioPorPote: Double, potesAdicionados: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val capacidadePote = if (insumo.unidadeCompra == "un") 1.0 else (if (insumo.capacidadePorItem > 0) insumo.capacidadePorItem else 1.0)

            val mlAdicionais = potesAdicionados * capacidadePote
            val novaQuantidadeLote = insumo.quantidadeLote + mlAdicionais

            val novaQtdPotesTotal = insumo.quantidadeItens + potesAdicionados

            val novoPrecoLote = novaQtdPotesTotal * novoPrecoUnitarioPorPote

            val insumoAtualizado = insumo.copy(
                quantidadeLote = novaQuantidadeLote,
                quantidadeItens = novaQtdPotesTotal,
                precoLote = novoPrecoLote,
                precoUnitarioFixo = novoPrecoUnitarioPorPote
            )
            repository.atualizarInsumo(insumoAtualizado)
        }
    }

    fun deletarInsumo(insumo: InsumoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletarInsumo(insumo)
        }
    }
}

class InsumosViewModelFactory(private val repository: FaturoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InsumosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InsumosViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel Desconhecido")
    }
}