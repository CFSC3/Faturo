package com.extensao.faturo.domain

enum class TipoGrandeza(val rotulo: String) {
    MASSA("Peso"),
    COMPRIMENTO("Comprimento / Tecido"),
    VOLUME("Líquido / Volume"),
    UNIDADE("Unidade Simples")
}

enum class UnidadeMedida(
    val simbolo: String,
    val grandeza: TipoGrandeza,
    val fatorConversaoBase: Double // Fator para normalizar tudo para: g, cm, ml ou unidade
) {
    KG("kg", TipoGrandeza.MASSA, 1000.0),
    G("g", TipoGrandeza.MASSA, 1.0),
    METRO("m", TipoGrandeza.COMPRIMENTO, 100.0),
    CENTIMETRO("cm", TipoGrandeza.COMPRIMENTO, 1.0),
    LITRO("L", TipoGrandeza.VOLUME, 1000.0),
    MILILITRO("ml", TipoGrandeza.VOLUME, 1.0),
    UNIDADE("un", TipoGrandeza.UNIDADE, 1.0);

    companion object {
        fun fromSimbolo(simbolo: String): UnidadeMedida {
            return entries.find { it.simbolo == simbolo } ?: UNIDADE
        }
    }
}

object ConversorDeMedidas {

    fun calcularCustoFracionado(
        precoLote: Double,
        quantidadeLote: Double,
        unidadeCompraSimbolo: String,
        quantidadeUsada: Double,
        unidadeUsoSimbolo: String
    ): Double {
        val unidadeCompra = UnidadeMedida.fromSimbolo(unidadeCompraSimbolo)
        val unidadeUso = UnidadeMedida.fromSimbolo(unidadeUsoSimbolo)

        // Trava de segurança: se o usuário tentar misturar Kg com Litros, retorna 0
        if (unidadeCompra.grandeza != unidadeUso.grandeza) return 0.0

        // Descobre o custo de 1 unidade base
        val quantidadeLoteBase = quantidadeLote * unidadeCompra.fatorConversaoBase
        val custoPorUnidadeBase = precoLote / quantidadeLoteBase

        // Converte a quantidade usada na receita para a unidade base
        val quantidadeUsadaBase = quantidadeUsada * unidadeUso.fatorConversaoBase

        // Multiplica o custo unitário pelo total usado
        return custoPorUnidadeBase * quantidadeUsadaBase
    }
}