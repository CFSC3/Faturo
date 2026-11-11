package com.extensao.faturo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.extensao.faturo.data.local.entity.ProdutoEntity
import com.extensao.faturo.ui.viewmodel.ProdutosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdutosScreen(
    viewModel: ProdutosViewModel,
    onProdutoClick: (Long) -> Unit,
    onNavigateToSobre: () -> Unit
) {
    val produtos by viewModel.produtos.collectAsState(initial = emptyList())
    val statusEstoque by viewModel.statusEstoque.collectAsState()

    var mostrarDialogCadastro by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Produtos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onNavigateToSobre) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogCadastro = true }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Produto")
            }
        }
    ) { paddingValues ->
        if (produtos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum produto cadastrado.\nToque no + para criar sua primeira peça.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(produtos) { produto ->
                    val podeVender = statusEstoque[produto.id] ?: true

                    CartaoProduto(
                        produto = produto,
                        podeVender = podeVender,
                        onClick = { onProdutoClick(produto.id) },
                        onVender = {
                            viewModel.registrarVenda(
                                produto = produto,
                                onSucesso = { Toast.makeText(context, "Venda registrada! Insumos deduzidos.", Toast.LENGTH_SHORT).show() },
                                onErro = { mensagem -> Toast.makeText(context, "Atenção: $mensagem", Toast.LENGTH_LONG).show() }
                            )
                        },
                        onDelete = { viewModel.deletarProduto(produto) }
                    )
                }
            }
        }
    }

    if (mostrarDialogCadastro) {
        DialogoAdicionarProduto(
            onDismiss = { mostrarDialogCadastro = false },
            onSalvar = { nome, tempo, margem ->
                viewModel.salvarProduto(nome, tempo, margem)
                mostrarDialogCadastro = false
            }
        )
    }
}

@Composable
fun CartaoProduto(
    produto: ProdutoEntity,
    podeVender: Boolean,
    onClick: () -> Unit,
    onVender: () -> Unit,
    onDelete: () -> Unit
) {
    var mostrarDialogExclusao by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = produto.nome,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Tempo: ${produto.tempoMinutos} min | Lucro alvo: ${"%.0f".format(produto.margemLucroPretendida)}%",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (produto.precoVendaPraticado > 0.0) {
                        Text(
                            text = "Valor de Venda: R$ ${"%.2f".format(produto.precoVendaPraticado)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Seu Lucro: R$ ${"%.2f".format(produto.lucroUnitario)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    } else {
                        val lucroSugerido = produto.precoVendaSugerido - produto.custoTotalProducao
                        Text(
                            text = "Preço Sugerido: R$ ${"%.2f".format(produto.precoVendaSugerido)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        if (lucroSugerido > 0) {
                            Text(
                                text = "Lucro Previsto: R$ ${"%.2f".format(lucroSugerido)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                IconButton(onClick = { mostrarDialogExclusao = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vendidos: ${produto.totalVendas}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!podeVender) {
                        val motivo = if (produto.custoTotalProducao == 0.0) "Receita Vazia!" else "Sem materiais suficientes!"
                        Text(
                            text = motivo,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = { onVender() },
                    enabled = podeVender,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                        disabledContentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (podeVender) "Registrar Venda" else "Indisponível")
                }
            }
        }
    }

    if (mostrarDialogExclusao) {
        AlertDialog(
            onDismissRequest = { mostrarDialogExclusao = false },
            properties = DialogProperties(dismissOnClickOutside = false),
            title = { Text("Excluir produto?") },
            text = { Text("Tem certeza que deseja apagar '${produto.nome}'? Isso apagará também a ficha técnica dele.") },
            confirmButton = {
                TextButton(onClick = { mostrarDialogExclusao = false; onDelete() }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogExclusao = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun DialogoAdicionarProduto(
    onDismiss: () -> Unit,
    onSalvar: (String, Int, Double) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var tempo by remember { mutableStateOf("") }
    var margem by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Peça") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Peça (ex: Pano Decorado)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempo,
                        onValueChange = { tempo = it },
                        label = { Text("Tempo (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = margem,
                        onValueChange = { margem = it },
                        label = { Text("Lucro (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val t = tempo.toIntOrNull() ?: 0
                    val m = margem.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (nome.isNotBlank()) {
                        onSalvar(nome, t, m)
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}