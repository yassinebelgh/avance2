package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Planned monthly action / budget item (e.g. Dar, Savings, Loyer, Food, etc.)
 */
@Entity(tableName = "planned_actions")
data class PlannedAction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val groupName: String, // e.g. "Maroc", "Mensuel", "Basics", "Divers"
    val isSavings: Boolean = false, // If true, treated as savings in the dashboard
    val sortOrder: Int = 0,
    val groupSortOrder: Int = 0
)

/**
 * Real money in accounts or cash (e.g. BNP, Boursorama, Cash, À prévoir)
 */
@Entity(tableName = "real_accounts")
data class RealAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val balance: Double, // Can be negative or positive
    val colorTag: String = "#3498DB", // Hex color representation for UI highlight
    val sortOrder: Int = 0
)

/**
 * Provisional notes and upcoming one-off expenses (Remarques et prévisions)
 */
@Entity(tableName = "prevision_notes")
data class PrevisionNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val isSettled: Boolean = false,
    val sortOrder: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Global budget configuration (Monthly income, currency, etc.)
 */
@Entity(tableName = "budget_config")
data class BudgetConfig(
    @PrimaryKey
    val id: Int = 1,
    val monthlyIncome: Double = 5000.0,
    val currencySymbol: String = "€",
    val title: String = "Budget Mensuel"
)
