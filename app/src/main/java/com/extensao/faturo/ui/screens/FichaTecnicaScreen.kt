package com.extensao.faturo.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.extensao.faturo.data.local.entity.InsumoEntity
import com.extensao.faturo.data.local.entity.ItemFichaTecnicaEntity
import com.extensao.faturo.ui.viewmodel.FichaTecnicaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaTecnicaScreen(viewModel: FichaTecnicaViewModel) {
    val produto by viewModel.produto.collectAsState()
    val itensCompletos by viewModel.itensCompletos.collectAsState()
    val todosInsumos by viewModel.todosInsumos.collectAsState()

    var mostrarDialogAdicionar by remember { mutableStateOf(false) }
    var mostrarDialogPreco by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(produto?.nome ?: "Carregando Receita...") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogAdicionar = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Material")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            produto?.let { prod ->
                val lucroLiquido = prod.precoVendaSugerido - prod.custoTotalProducao

                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Resumo de Custos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Materiais: R$ ${"%.2f".format(prod.custoInsumos)}")
                        Text("Mão de Obra (${prod.tempoMinutos} min): R$ ${"%.2f".format(prod.custoMaoDeObra)}")

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("Custo Total da Peça: R$ ${"%.2f".format(prod.custoTotalProducao)}", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Preço Sugerido: R$ ${"%.2f".format(prod.precoVendaSugerido)}",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Lucro Real (Sugerido): R$ ${"%.2f".format(lucroLiquido)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Preço Praticado: R$ ${"%.2f".format(prod.precoVendaPraticado)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = if (prod.precoVendaPraticado > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Lucro Real: R$ ${"%.2f".format(prod.lucroUnitario)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (prod.lucroUnitario > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(onClick = { mostrarDialogPreco = true }) {
                                Text(if (prod.precoVendaPraticado > 0) "Ajustar" else "Definir")
                            }
                        }
                    }
                }
            }

            if (itensCompletos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A receita está vazia.\nToque no + para adicionar os materiais.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(itensCompletos) { (item, insumoUsado) ->
                        CartaoItemReceita(
                            item = item,
                            nomeInsumo = insumoUsado.nome,
                            onDelete = { viewModel.removerItemDaReceita(item) }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogAdicionar) {
        DialogoAdicionarNaReceita(
            insumosDisponiveis = todosInsumos,
            onDismiss = { mostrarDialogAdicionar = false },
            onSalvar = { insumoSelecionado, qtd, unidade ->
                viewModel.adicionarItemNaReceita(insumoSelecionado, qtd, unidade)
                mostrarDialogAdicionar = false
            }
        )
    }

    if (mostrarDialogPreco) {
        produto?.let { prod ->
            DialogoDefinirPreco(
                precoAtual = prod.precoVendaPraticado,
                precoSugerido = prod.precoVendaSugerido,
                onDismiss = { mostrarDialogPreco = false },
                onSalvar = { precoReal ->
                    viewModel.atualizarPrecoPraticado(precoReal)
                    mostrarDialogPreco = false
                }
            )
        }
    }
}

@Composable
fun CartaoItemReceita(
    item: ItemFichaTecnicaEntity,
    nomeInsumo: String,
    onDelete: () -> Unit
) {
    var mostrarDialogExclusao by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = nomeInsumo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "Usado: ${item.quantidadeUtilizada} ${item.unidadeUtilizada}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Custo: R$ ${"%.2f".format(item.custoCalculadoSnapshot)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(onClick = { mostrarDialogExclusao = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (mostrarDialogExclusao) {
        AlertDialog(
            onDismissRequest = { mostrarDialogExclusao = false },
            properties = DialogProperties(dismissOnClickOutside = false),
            title = { Text("Remover da receita?") },
            text = { Text("Tem certeza que deseja tirar '$nomeInsumo' deste produto?") },
            confirmButton = {
                TextButton(onClick = { mostrarDialogExclusao = false; onDelete() }) {
                    Text("Remover", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogExclusao = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun DialogoDefinirPreco(
    precoAtual: Double,
    precoSugerido: Double,
    onDismiss: () -> Unit,
    onSalvar: (Double) -> Unit
) {
    val valorInicial = if (precoAtual > 0) precoAtual else precoSugerido
    var precoInput by remember {
        mutableStateOf("%.2f".format(valorInicial).replace(",", "."))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text("Preço Praticado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Preço sugerido: R$ ${"%.2f".format(precoSugerido)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text("Qual valor final você cobrará do cliente?")
                OutlinedTextField(
                    value = precoInput,
                    onValueChange = { precoInput = it },
                    label = { Text("Valor de Venda (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val preco = precoInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                onSalvar(preco)
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoAdicionarNaReceita(
    insumosDisponiveis: List<InsumoEntity>,
    onDismiss: () -> Unit,
    onSalvar: (InsumoEntity, Double, String) -> Unit
) {
    var insumoSelecionado by remember { mutableStateOf<InsumoEntity?>(null) }
    var menuInsumosExpandido by remember { mutableStateOf(false) }
    var quantidade by remember { mutableStateOf("") }
    var medidaSelecionada by remember { mutableStateOf("un") }
    var menuMedidasExpandido by remember { mutableStateOf(false) }

    val opcoesDeMedida = listOf("un", "m", "cm", "kg", "g", "L", "ml")

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text("Adicionar na Receita") },
        text = {
            if (insumosDisponiveis.isEmpty()) {
                Text("Você ainda não tem materiais cadastrados. Vá na aba Materiais primeiro.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = menuInsumosExpandido,
                        onExpandedChange = { menuInsumosExpandido = !menuInsumosExpandido }
                    ) {
                        OutlinedTextField(
                            value = insumoSelecionado?.nome ?: "Selecione um material",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Material do Estoque") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuInsumosExpandido) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = menuInsumosExpandido,
                            onDismissRequest = { menuInsumosExpandido = false }
                        ) {
                            insumosDisponiveis.forEach { insumo ->
                                DropdownMenuItem(
                                    text = { Text(insumo.nome) },
                                    onClick = {
                                        insumoSelecionado = insumo
                                        medidaSelecionada = insumo.unidadeCompra
                                        menuInsumosExpandido = false
                                    }
                                )
                            }
                        }
                    }

                    if (insumoSelecionado != null) {
                        Text(
                            text = "Dica: O app sabe converter as medidas sozinho. Você comprou em '${insumoSelecionado!!.unidadeCompra}', mas pode informar o uso em outra medida se precisar.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 16.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantidade,
                            onValueChange = { quantidade = it },
                            label = { Text("Qtd usada") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        ExposedDropdownMenuBox(
                            expanded = menuMedidasExpandido,
                            onExpandedChange = { menuMedidasExpandido = !menuMedidasExpandido },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = medidaSelecionada,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Medida") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuMedidasExpandido) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = menuMedidasExpandido,
                                onDismissRequest = { menuMedidasExpandido = false }
                            ) {
                                opcoesDeMedida.forEach { opcao ->
                                    DropdownMenuItem(
                                        text = { Text(opcao) },
                                        onClick = {
                                            medidaSelecionada = opcao
                                            menuMedidasExpandido = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qtd = quantidade.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (insumoSelecionado != null && qtd > 0) {
                        onSalvar(insumoSelecionado!!, qtd, medidaSelecionada)
                    }
                },
                enabled = insumoSelecionado != null
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}