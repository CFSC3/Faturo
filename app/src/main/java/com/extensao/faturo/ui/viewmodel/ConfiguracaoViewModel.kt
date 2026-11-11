package com.extensao.faturo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.extensao.faturo.data.local.entity.ConfiguracaoNegocioEntity
import com.extensao.faturo.data.repository.FaturoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConfiguracaoViewModel(private val repository: FaturoRepository) : ViewModel() {

    val configuracao: StateFlow<ConfiguracaoNegocioEntity?> = repository.buscarConfiguracao()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun salvarConfiguracao(salario: Double, despesas: Double, horasDia: Int, diasSemana: Int) {

        val horasNoMes = (diasSemana * 4.33) * horasDia
        val valorHora = if (horasNoMes > 0) (salario + despesas) / horasNoMes else 0.0

        viewModelScope.launch {
            val config = ConfiguracaoNegocioEntity(
                id = 1,
                salarioDesejado = salario,
                despesasFixasMensais = despesas,
                horasPorDia = horasDia,
                diasPorSemana = diasSemana,
                valorHoraCalculado = valorHora
            )
            repository.salvarConfiguracao(config)
        }
    }
}

class ConfiguracaoViewModelFactory(private val repository: FaturoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfiguracaoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConfiguracaoViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel Desconhecido")
    }
}