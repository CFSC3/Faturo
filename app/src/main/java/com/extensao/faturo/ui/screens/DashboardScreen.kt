package com.extensao.faturo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extensao.faturo.ui.viewmodel.DashboardViewModel
import com.extensao.faturo.ui.viewmodel.ResumoMes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val resumo by viewModel.resumoAtual.collectAsState()
    val mesesDisponiveis by viewModel.mesesDisponiveis.collectAsState()
    val mesSelecionado by viewModel.mesSelecionado.collectAsState()
    val configuracao by viewModel.configuracao.collectAsState()

    var menuExpandido by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visão Financeira") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (resumo == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(
                    text = "Nenhuma venda registrada ainda.\nVá na aba Produtos, registre vendas e os gráficos aparecerão aqui!",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ExposedDropdownMenuBox(
                        expanded = menuExpandido,
                        onExpandedChange = { menuExpandido = !menuExpandido }
                    ) {
                        OutlinedTextField(
                            value = mesSelecionado,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mês de Referência") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = menuExpandido,
                            onDismissRequest = { menuExpandido = false }
                        ) {
                            mesesDisponiveis.forEach { mes ->
                                DropdownMenuItem(
                                    text = { Text(mes) },
                                    onClick = {
                                        viewModel.selecionarMes(mes)
                                        menuExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    val salarioAlvo = configuracao?.salarioDesejado ?: 0.0
                    val ganhoAtual = resumo!!.lucroLiquido + resumo!!.valorMaoDeObra
                    val porcentagem = if (salarioAlvo > 0) (ganhoAtual / salarioAlvo).coerceIn(0.0, 1.0) else 0.0

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Progresso da sua Meta Salarial", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { porcentagem.toFloat() },
                                modifier = Modifier.fillMaxWidth().height(12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("No bolso: R$ ${"%.2f".format(ganhoAtual)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text("Meta: R$ ${"%.2f".format(salarioAlvo)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Para onde foi o seu dinheiro?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(24.dp))

                            GraficoDeRosca(
                                lucroLiquido = resumo!!.lucroLiquido,
                                custoProducao = resumo!!.custoProducao,
                                maoDeObra = resumo!!.valorMaoDeObra
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            ResumoValores(resumo!!)
                        }
                    }
                }

                item {
                    Text("Top Produtos do Mês", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
                }

                items(resumo!!.topProdutos) { produto ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically // Centraliza o texto verticalmente com o preço
                        ) {
                            // O weight(1f) empurra o texto para baixo se for muito grande, protegendo o preço
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = produto.nome,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text("${produto.quantidade} unidades vendidas", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "R$ ${"%.2f".format(produto.lucroGerado)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GraficoDeRosca(lucroLiquido: Double, custoProducao: Double, maoDeObra: Double) {
    val total = lucroLiquido + custoProducao + maoDeObra
    if (total <= 0.0) return

    val anguloCusto = (custoProducao / total) * 360f
    val anguloMaoDeObra = (maoDeObra / total) * 360f
    val anguloLucro = (lucroLiquido / total) * 360f

    val corLucro = Color(0xFF2E7D32)
    val corCusto = Color(0xFFD32F2F)
    val corMaoDeObra = Color(0xFF1976D2)

    Canvas(modifier = Modifier.size(160.dp)) {
        val strokeWidth = 32.dp.toPx()
        var startAngle = -90f

        drawArc(
            color = corCusto,
            startAngle = startAngle,
            sweepAngle = anguloCusto.toFloat(),
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        startAngle += anguloCusto.toFloat()

        drawArc(
            color = corMaoDeObra,
            startAngle = startAngle,
            sweepAngle = anguloMaoDeObra.toFloat(),
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        startAngle += anguloMaoDeObra.toFloat()

        drawArc(
            color = corLucro,
            startAngle = startAngle,
            sweepAngle = anguloLucro.toFloat(),
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
    }
}

@Composable
fun ResumoValores(resumo: ResumoMes) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LinhaResumo("Faturamento Bruto", resumo.faturamentoBruto, MaterialTheme.colorScheme.onSurface, true)
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        LinhaResumo("Custos de Produção (Material + Fixo)", resumo.custoProducao, Color(0xFFD32F2F))
        LinhaResumo("Sua Hora Trabalhada", resumo.valorMaoDeObra, Color(0xFF1976D2))
        LinhaResumo("Lucro Líquido (O que sobrou limpo)", resumo.lucroLiquido, Color(0xFF2E7D32), true)
    }
}

@Composable
fun LinhaResumo(label: String, valor: Double, cor: Color, negrito: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = if (negrito) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
        Text(
            text = "R$ ${"%.2f".format(valor)}",
            fontWeight = if (negrito) FontWeight.Bold else FontWeight.SemiBold,
            color = cor,
            fontSize = 15.sp
        )
    }
}