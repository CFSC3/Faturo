package com.extensao.faturo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extensao.faturo.ui.viewmodel.ConfiguracaoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(viewModel: ConfiguracaoViewModel) {
    val configuracao by viewModel.configuracao.collectAsState()

    // Estados locais para os campos de texto
    var salario by remember(configuracao) { mutableStateOf(configuracao?.salarioDesejado?.toString() ?: "") }
    var despesas by remember(configuracao) { mutableStateOf(configuracao?.despesasFixasMensais?.toString() ?: "") }
    var horasDia by remember(configuracao) { mutableStateOf(configuracao?.horasPorDia?.toString() ?: "") }
    var diasSemana by remember(configuracao) { mutableStateOf(configuracao?.diasPorSemana?.toString() ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Negócio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Para calcularmos o preço justo dos seus produtos, precisamos saber a sua meta financeira.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = salario,
                onValueChange = { salario = it },
                label = { Text("Qual salário você deseja tirar no mês? (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = despesas,
                onValueChange = { despesas = it },
                label = { Text("Despesas fixas do mês (Luz, internet, etc) (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = horasDia,
                    onValueChange = { horasDia = it },
                    label = { Text("Horas por dia") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = diasSemana,
                    onValueChange = { diasSemana = it },
                    label = { Text("Dias na semana") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Button(
                onClick = {
                    val s = salario.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val d = despesas.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val h = horasDia.toIntOrNull() ?: 0
                    val ds = diasSemana.toIntOrNull() ?: 0
                    viewModel.salvarConfiguracao(s, d, h, ds)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Salvar e Calcular Valor da Hora")
            }

            // Mostra o resultado do cálculo
            if (configuracao != null && configuracao!!.valorHoraCalculado > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("O valor da sua hora de trabalho é:", fontSize = 14.sp)
                        Text(
                            text = "R$ ${"%.2f".format(configuracao!!.valorHoraCalculado)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}