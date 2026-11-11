package com.extensao.faturo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vendas")
data class VendaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val produtoId: Long,
    val produtoNome: String,
    val dataVenda: Long,
    val faturamentoBruto: Double,
    val custoProducao: Double,
    val lucroLiquido: Double,
    val valorMaoDeObra: Double
)