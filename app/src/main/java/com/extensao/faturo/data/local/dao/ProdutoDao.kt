package com.extensao.faturo.data.local.dao

import androidx.room.*
import com.extensao.faturo.data.local.entity.ProdutoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProdutoDao {
    @Query("SELECT * FROM produtos ORDER BY dataCriacao DESC")
    fun buscarTodosProdutos(): Flow<List<ProdutoEntity>>

    @Query("SELECT * FROM produtos WHERE id = :produtoId LIMIT 1")
    fun buscarProdutoPorId(produtoId: Long): ProdutoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun salvarProduto(produto: ProdutoEntity): Long

    @Update
    fun atualizarProduto(produto: ProdutoEntity)

    @Delete
    fun deletarProduto(produto: ProdutoEntity)
}