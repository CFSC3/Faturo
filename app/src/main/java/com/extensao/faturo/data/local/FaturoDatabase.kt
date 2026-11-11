package com.extensao.faturo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.extensao.faturo.data.local.dao.*
import com.extensao.faturo.data.local.entity.*

@Database(
    entities = [
        InsumoEntity::class,
        ProdutoEntity::class,
        ItemFichaTecnicaEntity::class,
        ConfiguracaoNegocioEntity::class,
        VendaEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class FaturoDatabase : RoomDatabase() {

    abstract val insumoDao: InsumoDao
    abstract val produtoDao: ProdutoDao
    abstract val fichaTecnicaDao: FichaTecnicaDao
    abstract val configuracaoNegocioDao: ConfiguracaoNegocioDao
    abstract val vendaDao: VendaDao

    companion object {
        const val DATABASE_NAME = "faturo_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE produtos ADD COLUMN margemLucroPretendida REAL NOT NULL DEFAULT 100.0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE insumos ADD COLUMN quantidadeItens REAL NOT NULL DEFAULT 1.0")
                db.execSQL("ALTER TABLE insumos ADD COLUMN capacidadePorItem REAL NOT NULL DEFAULT 0.0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE insumos ADD COLUMN precoUnitarioFixo REAL NOT NULL DEFAULT 0.0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vendas` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `produtoId` INTEGER NOT NULL, 
                        `produtoNome` TEXT NOT NULL, 
                        `dataVenda` INTEGER NOT NULL, 
                        `faturamentoBruto` REAL NOT NULL, 
                        `custoProducao` REAL NOT NULL, 
                        `lucroLiquido` REAL NOT NULL, 
                        `valorMaoDeObra` REAL NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}