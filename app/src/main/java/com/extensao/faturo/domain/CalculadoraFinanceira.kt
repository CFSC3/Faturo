package com.extensao.faturo.domain

import com.extensao.faturo.data.local.entity.ConfiguracaoNegocioEntity

object CalculadoraFinanceira {

    // Descobre quanto vale 1 hora de trabalho do artesão
    fun calcularValorHora(config: ConfiguracaoNegocioEntity): Double {
        val horasPorSemana = config.horasPorDia * config.diasPorSemana
        val semanasPorMes = 4.33 // Média universal de semanas em um mês
        val horasPorMes = horasPorSemana * semanasPorMes

        // Adicionado o .0 para evitar o erro de tipagem no compareTo
        if (horasPorMes <= 0.0) return 0.0

        // O valor da hora precisa cobrir o salário desejado e as despesas fixas do ateliê
        return (config.salarioDesejado + config.despesasFixasMensais) / horasPorMes
    }

    // Calcula o custo do tempo gasto para fazer um produto
    fun calcularCustoMaoDeObra(tempoMinutos: Int, valorHora: Double): Double {
        val horasTrabalhadas = tempoMinutos / 60.0
        return horasTrabalhadas * valorHora
    }

    // Rateia despesas invisíveis (água, luz, internet) sobre a peça
    fun calcularCustosIndiretos(
        custoInsumos: Double,
        custoMaoDeObra: Double,
        percentualCustosIndiretos: Double
    ): Double {
        val baseCalculo = custoInsumos + custoMaoDeObra
        return baseCalculo * (percentualCustosIndiretos / 100.0)
    }

    // Soma todos os gastos envolvidos na peça
    fun calcularCustoTotal(
        custoInsumos: Double,
        custoMaoDeObra: Double,
        custoEmbalagem: Double,
        custosIndiretos: Double
    ): Double {
        return custoInsumos + custoMaoDeObra + custoEmbalagem + custosIndiretos
    }

    // Sugere um preço de venda acrescentando a margem de lucro desejada
    fun calcularPrecoSugerido(custoTotal: Double, margemLucroPercentual: Double): Double {
        return custoTotal + (custoTotal * (margemLucroPercentual / 100.0))
    }

    // Calcula o lucro limpo baseado no preço real cobrado pelo artesão
    fun calcularLucroUnitario(precoPraticado: Double, custoTotal: Double): Double {
        return precoPraticado - custoTotal
    }
}