package com.extensao.faturo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.extensao.faturo.data.local.entity.ItemFichaTecnicaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FichaTecnicaDao {

    @Query("SELECT * FROM itens_ficha_tecnica WHERE produtoId = :produtoId")
    fun buscarItensDoProduto(produtoId: Long): Flow<List<ItemFichaTecnicaEntity>>

    @Query("SELECT * FROM itens_ficha_tecnica WHERE produtoId = :produtoId")
    fun obterItensDoProduto(produtoId: Long): List<ItemFichaTecnicaEntity>

    @Query("DELETE FROM itens_ficha_tecnica WHERE produtoId = :produtoId")
    fun limparFichaDoProduto(produtoId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun salvarItemFicha(item: ItemFichaTecnicaEntity)

    @Delete
    fun deletarItemFicha(item: ItemFichaTecnicaEntity)
}