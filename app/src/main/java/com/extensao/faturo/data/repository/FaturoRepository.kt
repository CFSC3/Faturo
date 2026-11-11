package com.extensao.faturo.data.repository

import com.extensao.faturo.data.local.dao.*
import com.extensao.faturo.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FaturoRepository(
    private val produtoDao: ProdutoDao,
    private val insumoDao: InsumoDao,
    private val fichaTecnicaDao: FichaTecnicaDao,
    private val configuracaoDao: ConfiguracaoNegocioDao,
    private val vendaDao: VendaDao
) {
    // ================== PRODUTOS ==================
    fun buscarTodosProdutos(): Flow<List<ProdutoEntity>> = produtoDao.buscarTodosProdutos()

    suspend fun buscarProdutoPorId(id: Long): ProdutoEntity? {
        return withContext(Dispatchers.IO) { produtoDao.buscarProdutoPorId(id) }
    }

    suspend fun salvarProduto(produto: ProdutoEntity): Long {
        return withContext(Dispatchers.IO) { produtoDao.salvarProduto(produto) }
    }

    suspend fun atualizarProduto(produto: ProdutoEntity) {
        withContext(Dispatchers.IO) { produtoDao.atualizarProduto(produto) }
    }

    suspend fun deletarProduto(produto: ProdutoEntity) {
        withContext(Dispatchers.IO) { produtoDao.deletarProduto(produto) }
    }

    // ================== FICHA TÉCNICA ==================

    suspend fun salvarItemFicha(item: ItemFichaTecnicaEntity) {
        withContext(Dispatchers.IO) { fichaTecnicaDao.salvarItemFicha(item) }
    }

    suspend fun deletarItemFicha(item: ItemFichaTecnicaEntity) {
        withContext(Dispatchers.IO) { fichaTecnicaDao.deletarItemFicha(item) }
    }

    fun buscarItensDoProduto(produtoId: Long): Flow<List<ItemFichaTecnicaEntity>> {
        return fichaTecnicaDao.buscarItensDoProduto(produtoId)
    }

    suspend fun obterItensDoProduto(produtoId: Long): List<ItemFichaTecnicaEntity> {
        return withContext(Dispatchers.IO) { fichaTecnicaDao.obterItensDoProduto(produtoId) }
    }

    suspend fun atualizarFichaTecnica(produtoId: Long, itens: List<ItemFichaTecnicaEntity>) {
        withContext(Dispatchers.IO) {
            fichaTecnicaDao.limparFichaDoProduto(produtoId)
            itens.forEach { item ->
                fichaTecnicaDao.salvarItemFicha(item.copy(produtoId = produtoId))
            }
        }
    }

    // ================== INSUMOS ==================
    fun buscarTodosInsumos(): Flow<List<InsumoEntity>> = insumoDao.buscarTodosInsumos()

    suspend fun salvarInsumo(insumo: InsumoEntity) {
        withContext(Dispatchers.IO) { insumoDao.salvarInsumo(insumo) }
    }

    suspend fun deletarInsumo(insumo: InsumoEntity) {
        withContext(Dispatchers.IO) { insumoDao.deletarInsumo(insumo) }
    }

    suspend fun atualizarInsumo(insumo: InsumoEntity) {
        withContext(Dispatchers.IO) { insumoDao.atualizarInsumo(insumo) }
    }

    // ================== CONFIGURAÇÃO ==================
    fun buscarConfiguracao(): Flow<ConfiguracaoNegocioEntity?> = configuracaoDao.buscarConfiguracao()

    suspend fun salvarConfiguracao(configuracao: ConfiguracaoNegocioEntity) {
        withContext(Dispatchers.IO) { configuracaoDao.salvarConfiguracao(configuracao) }
    }

    // ================== HISTÓRICO DE VENDAS ==================
    fun buscarTodasVendas(): Flow<List<VendaEntity>> = vendaDao.obterTodasVendas()

    suspend fun salvarVenda(venda: VendaEntity) {
        withContext(Dispatchers.IO) { vendaDao.inserirVenda(venda) }
    }
}