package com.extensao.faturo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "itens_ficha_tecnica",
    foreignKeys = [
        ForeignKey(
            entity = ProdutoEntity::class,
            parentColumns = ["id"],
            childColumns = ["produtoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InsumoEntity::class,
            parentColumns = ["id"],
            childColumns = ["insumoId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["produtoId"]), Index(value = ["insumoId"])]
)
data class ItemFichaTecnicaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val produtoId: Long,
    val insumoId: Long,
    val quantidadeUtilizada: Double,
    val unidadeUtilizada: String,
    val custoCalculadoSnapshot: Double
)