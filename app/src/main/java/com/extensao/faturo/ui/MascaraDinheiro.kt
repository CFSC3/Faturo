package com.extensao.faturo.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class MascaraDinheiro : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text.filter { it.isDigit() }

        if (originalText.isEmpty()) {
            return TransformedText(
                text = AnnotatedString(""),
                offsetMapping = OffsetMapping.Identity
            )
        }

        // Transforma '560' em 5.60
        val valorDouble = (originalText.toDoubleOrNull() ?: 0.0) / 100

        // Formata para o padrão Brasileiro (5,60) - sem o R$
        val formatter = NumberFormat.getNumberInstance(Locale("pt", "BR"))
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        val formattedText = formatter.format(valorDouble)

        return TransformedText(
            text = AnnotatedString(formattedText),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return formattedText.length
                }
                override fun transformedToOriginal(offset: Int): Int {
                    return text.length
                }
            }
        )
    }
}