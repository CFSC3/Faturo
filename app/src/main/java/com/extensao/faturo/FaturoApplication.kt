package com.extensao.faturo

import android.app.Application
import androidx.room.Room
import com.extensao.faturo.data.local.FaturoDatabase
import com.extensao.faturo.data.repository.FaturoRepository

class FaturoApplication : Application() {

    val database by lazy {
        Room.databaseBuilder(
            this,
            FaturoDatabase::class.java,
            FaturoDatabase.DATABASE_NAME
        )
            .addMigrations(
                FaturoDatabase.MIGRATION_1_2,
                FaturoDatabase.MIGRATION_2_3,
                FaturoDatabase.MIGRATION_3_4,
                FaturoDatabase.MIGRATION_4_5
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    val repository by lazy {
        FaturoRepository(
            produtoDao = database.produtoDao,
            insumoDao = database.insumoDao,
            fichaTecnicaDao = database.fichaTecnicaDao,
            configuracaoDao = database.configuracaoNegocioDao,
            vendaDao = database.vendaDao
        )
    }
}