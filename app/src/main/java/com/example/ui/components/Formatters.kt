package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DarkPeach
import com.example.ui.theme.DarkSand
import com.example.ui.theme.DarkTerracotta
import com.example.ui.theme.DarkYellow
import com.example.ui.theme.PastelGreen
import com.example.ui.theme.PastelPeach
import com.example.ui.theme.PastelSand
import com.example.ui.theme.PastelTerracotta
import com.example.ui.theme.PastelYellow
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Formatters {
    private val symbols = DecimalFormatSymbols(Locale.FRENCH).apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }
    private val standardFormat = DecimalFormat("#,##0.00", symbols)
    private val roundFormat = DecimalFormat("#,##0", symbols)

    fun formatCurrency(amount: Double, symbol: String = "€", includeDecimals: Boolean = true): String {
        val formatted = if (includeDecimals) {
            standardFormat.format(amount)
        } else {
            roundFormat.format(amount)
        }
        return "$formatted $symbol"
    }

    fun formatSignedCurrency(amount: Double, symbol: String = "€"): String {
        val sign = if (amount > 0) "+" else ""
        return "$sign${standardFormat.format(amount)} $symbol"
    }

    @Composable
    fun getGroupHeaderColors(groupName: String): Pair<Color, Color> {
        val isDark = isSystemInDarkTheme()
        val normalized = groupName.lowercase().trim()
        return when {
            normalized.contains("maroc") -> {
                if (isDark) Pair(DarkPeach, Color(0xFFFFD8CC))
                else Pair(PastelPeach, Color(0xFF4A2518))
            }
            normalized.contains("mensuel") -> {
                if (isDark) Pair(DarkSand, Color(0xFFFCE4D6))
                else Pair(PastelSand, Color(0xFF4A3418))
            }
            normalized.contains("basic") -> {
                if (isDark) Pair(DarkTerracotta, Color(0xFFF8CBAD))
                else Pair(PastelTerracotta, Color(0xFF4E2616))
            }
            normalized.contains("income") || normalized.contains("revenu") -> {
                if (isDark) Pair(DarkGreen, Color(0xFFD5F5E3))
                else Pair(PastelGreen, Color(0xFF144D29))
            }
            normalized.contains("autre") -> {
                if (isDark) Pair(DarkYellow, Color(0xFFFFF3CD))
                else Pair(PastelYellow, Color(0xFF534515))
            }
            else -> {
                if (isDark) Pair(Color(0xFF2C3E50), Color(0xFFECF0F1))
                else Pair(Color(0xFFE2E8F0), Color(0xFF1E293B))
            }
        }
    }
}
