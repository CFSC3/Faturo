package com.extensao.faturo.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.extensao.faturo.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onCarregamentoFinalizado: () -> Unit) {
    val opacidade = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Fade-In suave (0.8s)
        opacidade.animateTo(
            targetValue = 1f,
            animationSpec = tween(800)
        )
        // Segura a tela por 2 segundos
        delay(2000)
        // Fade-Out suave antes da transição (0.4s)
        opacidade.animateTo(
            targetValue = 0f,
            animationSpec = tween(400)
        )
        // Finaliza o carregamento fake!
        onCarregamentoFinalizado()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1A30)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(opacidade.value)
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo_semfundo),
                contentDescription = "Logo Faturô",
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Faturô",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Valorizando seu trabalho",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(56.dp))

            CircularProgressIndicator(
                color = Color(0xFFFF9800), // Laranja vibrante
                strokeWidth = 3.2.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}