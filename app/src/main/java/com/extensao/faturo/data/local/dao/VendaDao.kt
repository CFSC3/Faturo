package com.extensao.faturo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.extensao.faturo.data.local.entity.VendaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VendaDao {
    @Insert
    fun inserirVenda(venda: VendaEntity)

    @Query("SELECT * FROM vendas ORDER BY dataVenda DESC")
    fun obterTodasVendas(): Flow<List<VendaEntity>>
}