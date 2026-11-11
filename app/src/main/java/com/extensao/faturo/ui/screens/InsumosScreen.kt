package com.extensao.faturo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.extensao.faturo.data.local.entity.InsumoEntity
import com.extensao.faturo.ui.MascaraDinheiro // IMPORTAÇÃO DA CLASSE SEPARADA
import com.extensao.faturo.ui.viewmodel.InsumosViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsumosScreen(viewModel: InsumosViewModel) {
    val insumos by viewModel.insumos.collectAsState(initial = emptyList())

    var mostrarDialogCadastro by remember { mutableStateOf(false) }
    var insumoParaEditar by remember { mutableStateOf<InsumoEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Materiais") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogCadastro = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Material")
            }
        }
    ) { paddingValues ->
        if (insumos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum material cadastrado.\nToque no + para adicionar.",
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
                items(insumos) { insumo ->
                    CartaoInsumo(
                        insumo = insumo,
                        onEdit = { insumoParaEditar = insumo },
                        onDelete = { viewModel.deletarInsumo(insumo) }
                    )
                }
            }
        }
    }

    if (mostrarDialogCadastro) {
        DialogoAdicionarInsumo(
            onDismiss = { mostrarDialogCadastro = false },
            onSalvar = { nome, precoUnitarioFixo, precoTotal, capacidadeTotal, qtdItens, capacidadeItem, unidade ->
                viewModel.salvarInsumo(nome, precoUnitarioFixo, precoTotal, capacidadeTotal, qtdItens, capacidadeItem, unidade)
                mostrarDialogCadastro = false
            }
        )
    }

    if (insumoParaEditar != null) {
        DialogoEditarInsumo(
            insumo = insumoParaEditar!!,
            onDismiss = { insumoParaEditar = null },
            onSalvar = { novoPrecoUnitarioPorPote, potesAdicionados ->
                viewModel.atualizarEstoqueInsumo(insumoParaEditar!!, novoPrecoUnitarioPorPote, potesAdicionados)
                insumoParaEditar = null
            }
        )
    }
}

@Composable
fun obterCorDoEstoqueUniversal(potesFechados: Int): Color {
    return when {
        potesFechados <= 2 -> MaterialTheme.colorScheme.error
        potesFechados == 3 -> Color(0xFFFFFF00)
        else -> Color(0xFF2E7D32)
    }
}

@Composable
fun CartaoInsumo(insumo: InsumoEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    var mostrarDialogExclusao by remember { mutableStateOf(false) }

    val ehUnidade = insumo.unidadeCompra == "un"
    val potesFechados = if (ehUnidade) insumo.quantidadeItens.toInt() else kotlin.math.floor(insumo.quantidadeItens).toInt()
    val corStatus = obterCorDoEstoqueUniversal(potesFechados)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.5.dp, corStatus)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = insumo.nome, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Text(
                    text = "Estoque: $potesFechados un",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = corStatus,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(onClick = { mostrarDialogExclusao = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (mostrarDialogExclusao) {
        AlertDialog(
            onDismissRequest = { mostrarDialogExclusao = false },
            properties = DialogProperties(dismissOnClickOutside = false),
            title = { Text("Excluir material?") },
            text = { Text("Tem certeza que deseja apagar '${insumo.nome}'? Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogExclusao = false
                        onDelete()
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogExclusao = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DialogoEditarInsumo(
    insumo: InsumoEntity,
    onDismiss: () -> Unit,
    onSalvar: (Double, Double) -> Unit
) {
    val ehUnidade = insumo.unidadeCompra == "un"
    val potesFechados = if (ehUnidade) insumo.quantidadeItens.toInt() else kotlin.math.floor(insumo.quantidadeItens).toInt()

    val precoAExibir = if (insumo.precoUnitarioFixo > 0) insumo.precoUnitarioFixo else (if (insumo.quantidadeItens > 0) insumo.precoLote / insumo.quantidadeItens else 0.0)

    val precoOriginalTexto = if (precoAExibir > 0) String.format(Locale.US, "%.2f", precoAExibir).replace(".", "") else ""
    var precoEditavel by remember { mutableStateOf(precoOriginalTexto) }

    var quantidadeAdicional by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atualizar Estoque") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = insumo.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Text(
                    text = "Você possui $potesFechados un no estoque.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                OutlinedTextField(
                    value = precoEditavel,
                    onValueChange = { novoTexto ->
                        precoEditavel = novoTexto.filter { it.isDigit() }
                    },
                    label = { Text("Preço de 1 unidade (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = MascaraDinheiro(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantidadeAdicional,
                    onValueChange = { quantidadeAdicional = it },
                    label = { Text("Reposição (+ un)") },
                    placeholder = { Text("Deixe em branco se não comprou mais") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val precoCalculado = (precoEditavel.toDoubleOrNull() ?: 0.0) / 100.0
                val novoPrecoPote = if (precoCalculado > 0) precoCalculado else precoAExibir
                val qtdAdd = quantidadeAdicional.replace(",", ".").toDoubleOrNull() ?: 0.0
                onSalvar(novoPrecoPote, qtdAdd)
            }) {
                Text("Atualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoAdicionarInsumo(
    onDismiss: () -> Unit,
    onSalvar: (String, Double, Double, Double, Double, Double, String) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var precoUnitario by remember { mutableStateOf("") }
    var quantidadeItens by remember { mutableStateOf("") }
    var tamanhoPorItem by remember { mutableStateOf("") }

    val opcoesDeMedida = listOf(
        "un" to "Unidades Simples (ex: Botões)",
        "m" to "Metros (ex: Tecido, Fita)",
        "cm" to "Centímetros",
        "kg" to "Quilos (ex: Barbante)",
        "g" to "Gramas",
        "L" to "Litros",
        "ml" to "Mililitros (ex: Tinta)"
    )
    var medidaSelecionada by remember { mutableStateOf(opcoesDeMedida[0]) }
    var menuExpandido by remember { mutableStateOf(false) }

    val ehUnidadeSimples = medidaSelecionada.first == "un"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Material") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome (ex: Tinta Acrílica)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = precoUnitario,
                    onValueChange = { novoTexto ->
                        precoUnitario = novoTexto.filter { it.isDigit() }
                    },
                    label = { Text(if (ehUnidadeSimples) "Preço de 1 unidade (R$)" else "Preço de 1 unidade (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = MascaraDinheiro(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = menuExpandido,
                    onExpandedChange = { menuExpandido = !menuExpandido }
                ) {
                    OutlinedTextField(
                        value = medidaSelecionada.second,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Medida") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuExpandido,
                        onDismissRequest = { menuExpandido = false }
                    ) {
                        opcoesDeMedida.forEach { opcao ->
                            DropdownMenuItem(
                                text = { Text(opcao.second) },
                                onClick = {
                                    medidaSelecionada = opcao
                                    menuExpandido = false
                                    quantidadeItens = ""
                                    tamanhoPorItem = ""
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantidadeItens,
                        onValueChange = { quantidadeItens = it },
                        label = { Text("Quantidade") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    if (!ehUnidadeSimples) {
                        OutlinedTextField(
                            value = tamanhoPorItem,
                            onValueChange = { tamanhoPorItem = it },
                            label = { Text("Peso un.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pUnitario = (precoUnitario.toDoubleOrNull() ?: 0.0) / 100.0
                    val qtd = quantidadeItens.replace(",", ".").toDoubleOrNull() ?: 1.0
                    val tamanho = if (ehUnidadeSimples) 1.0 else (tamanhoPorItem.replace(",", ".").toDoubleOrNull() ?: 1.0)

                    val precoTotalReal = pUnitario * qtd
                    val capacidadeTotalReal = qtd * tamanho

                    if (nome.isNotBlank()) {
                        onSalvar(nome, pUnitario, precoTotalReal, capacidadeTotalReal, qtd, tamanho, medidaSelecionada.first)
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