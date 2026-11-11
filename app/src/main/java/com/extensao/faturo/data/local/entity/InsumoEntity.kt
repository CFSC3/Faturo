package com.extensao.faturo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insumos")
data class InsumoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val precoLote: Double,
    val quantidadeLote: Double,
    val unidadeCompra: String,
    val quantidadeItens: Double = 1.0,
    val capacidadePorItem: Double = 0.0,
    val precoUnitarioFixo: Double = 0.0
)