package com.extensao.faturo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produtos")
data class ProdutoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val tempoMinutos: Int = 0,
    val margemLucroPretendida: Double = 100.0,
    val custoEmbalagem: Double = 0.0,
    val estoqueAtual: Int = 0,
    val custoInsumos: Double = 0.0,
    val custoMaoDeObra: Double = 0.0,
    val custosIndiretosAplicados: Double = 0.0,
    val custoTotalProducao: Double = 0.0,
    val precoVendaSugerido: Double = 0.0,
    val precoVendaPraticado: Double = 0.0,
    val lucroUnitario: Double = 0.0,
    val totalVendas: Int = 0,
    val dataCriacao: Long = System.currentTimeMillis()
)