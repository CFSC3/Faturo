package com.extensao.faturo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracao_negocio")
data class ConfiguracaoNegocioEntity(
    @PrimaryKey
    val id: Int = 1,
    val salarioDesejado: Double = 0.0,
    val horasPorDia: Int = 0,
    val diasPorSemana: Int = 0,
    val despesasFixasMensais: Double = 0.0,
    val valorHoraCalculado: Double = 0.0
)