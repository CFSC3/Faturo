package com.extensao.faturo.data.local.dao

import androidx.room.*
import com.extensao.faturo.data.local.entity.InsumoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InsumoDao {
    @Query("SELECT * FROM insumos ORDER BY nome ASC")
    fun buscarTodosInsumos(): Flow<List<InsumoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun salvarInsumo(insumo: InsumoEntity): Long

    @Update
    fun atualizarInsumo(insumo: InsumoEntity)

    @Delete
    fun deletarInsumo(insumo: InsumoEntity)
}