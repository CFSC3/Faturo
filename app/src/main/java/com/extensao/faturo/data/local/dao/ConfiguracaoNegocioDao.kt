package com.extensao.faturo.data.local.dao

import androidx.room.*
import com.extensao.faturo.data.local.entity.ConfiguracaoNegocioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracaoNegocioDao {
    @Query("SELECT * FROM configuracao_negocio WHERE id = 1 LIMIT 1")
    fun buscarConfiguracao(): Flow<ConfiguracaoNegocioEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun salvarConfiguracao(configuracao: ConfiguracaoNegocioEntity)
}