package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.JsonBackupManager
import com.example.data.local.AppDatabase
import com.example.data.model.BudgetConfig
import com.example.data.model.PlannedAction
import com.example.data.model.PrevisionNote
import com.example.data.model.RealAccount
import com.example.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlannedGroupSummary(
    val groupName: String,
    val items: List<PlannedAction>,
    val subtotal: Double
)

data class BudgetUiState(
    val isLoading: Boolean = false,
    val monthlyIncome: Double = 0.0,
    val currencySymbol: String = "€",
    val budgetMonthTitle: String = "Mon Budget",
    val plannedActions: List<PlannedAction> = emptyList(),
    val groups: List<PlannedGroupSummary> = emptyList(),
    val totalPlanned: Double = 0.0,
    val totalSavings: Double = 0.0,
    val totalPlannedExpenses: Double = 0.0,
    val remainingAutres: Double = 0.0,
    val realAccounts: List<RealAccount> = emptyList(),
    val totalRealMoney: Double = 0.0,
    val previsionNotes: List<PrevisionNote> = emptyList(),
    val totalPrevisions: Double = 0.0,
    val totalUpcomingExpenses: Double = 0.0,
    val savingsRate: Double = 0.0
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = BudgetRepository(db.appDao())
    }

    val uiState: StateFlow<BudgetUiState> = combine(
        repository.budgetConfig,
        repository.plannedActions,
        repository.realAccounts,
        repository.previsionNotes
    ) { config, actions, accounts, notes ->
        val income = config?.monthlyIncome ?: 0.0
        val currency = config?.currencySymbol ?: "€"
        val title = config?.title ?: "Mon Budget"

        // Group actions by groupName while maintaining query order
        val groupedMap = actions.groupBy { it.groupName }
        val groups = groupedMap.map { (groupName, items) ->
            PlannedGroupSummary(
                groupName = groupName,
                items = items,
                subtotal = items.sumOf { it.amount }
            )
        }

        val totalPlanned = actions.sumOf { it.amount }
        val totalSavings = actions.filter { it.isSavings }.sumOf { it.amount }
        val totalPlannedExpenses = totalPlanned - totalSavings
        val remainingAutres = income - totalPlanned

        val totalReal = accounts.sumOf { it.balance }
        val totalPrev = notes.filter { !it.isSettled }.sumOf { it.amount }
        val upcomingExpenses = totalPlannedExpenses + totalPrev
        val savingsRate = if (income > 0) (totalSavings / income) * 100 else 0.0

        BudgetUiState(
            isLoading = false,
            monthlyIncome = income,
            currencySymbol = currency,
            budgetMonthTitle = title,
            plannedActions = actions,
            groups = groups,
            totalPlanned = totalPlanned,
            totalSavings = totalSavings,
            totalPlannedExpenses = totalPlannedExpenses,
            remainingAutres = remainingAutres,
            realAccounts = accounts,
            totalRealMoney = totalReal,
            previsionNotes = notes,
            totalPrevisions = totalPrev,
            totalUpcomingExpenses = upcomingExpenses,
            savingsRate = savingsRate
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        BudgetUiState(isLoading = true)
    )

    // Planned Actions
    fun addOrUpdatePlannedAction(
        id: Long = 0,
        name: String,
        amount: Double,
        groupName: String,
        isSavings: Boolean
    ) {
        viewModelScope.launch {
            val action = PlannedAction(
                id = id,
                name = name,
                amount = amount,
                groupName = groupName.trim(),
                isSavings = isSavings
            )
            if (id == 0L) {
                repository.insertPlannedAction(action)
            } else {
                repository.updatePlannedAction(action)
            }
        }
    }

    fun deletePlannedAction(action: PlannedAction) {
        viewModelScope.launch {
            repository.deletePlannedAction(action)
        }
    }

    fun movePlannedAction(action: PlannedAction, direction: Int) {
        viewModelScope.launch {
            repository.movePlannedAction(action, direction)
        }
    }

    fun moveGroup(groupName: String, direction: Int) {
        viewModelScope.launch {
            repository.moveGroup(groupName, direction)
        }
    }

    fun deleteGroup(groupName: String) {
        viewModelScope.launch {
            repository.deleteGroupByGroupName(groupName)
        }
    }

    fun renameGroup(oldName: String, newName: String) {
        viewModelScope.launch {
            if (newName.isNotBlank() && oldName != newName) {
                repository.renameGroup(oldName.trim(), newName.trim())
            }
        }
    }

    // Income
    fun updateMonthlyIncome(income: Double) {
        viewModelScope.launch {
            repository.updateIncome(income)
        }
    }

    // Real Accounts
    fun addOrUpdateRealAccount(
        id: Long = 0,
        name: String,
        balance: Double,
        colorTag: String = "#3498DB"
    ) {
        viewModelScope.launch {
            val account = RealAccount(
                id = id,
                name = name.trim(),
                balance = balance,
                colorTag = colorTag
            )
            if (id == 0L) {
                repository.insertRealAccount(account)
            } else {
                repository.updateRealAccount(account)
            }
        }
    }

    fun deleteRealAccount(account: RealAccount) {
        viewModelScope.launch {
            repository.deleteRealAccount(account)
        }
    }

    fun moveRealAccount(account: RealAccount, direction: Int) {
        viewModelScope.launch {
            repository.moveRealAccount(account, direction)
        }
    }

    // Prevision Notes
    fun addOrUpdatePrevisionNote(
        id: Long = 0,
        amount: Double,
        description: String,
        isSettled: Boolean = false
    ) {
        viewModelScope.launch {
            val note = PrevisionNote(
                id = id,
                amount = amount,
                description = description.trim(),
                isSettled = isSettled
            )
            if (id == 0L) {
                repository.insertPrevisionNote(note)
            } else {
                repository.updatePrevisionNote(note)
            }
        }
    }

    fun togglePrevisionSettled(note: PrevisionNote) {
        viewModelScope.launch {
            repository.updatePrevisionNote(note.copy(isSettled = !note.isSettled))
        }
    }

    fun deletePrevisionNote(note: PrevisionNote) {
        viewModelScope.launch {
            repository.deletePrevisionNote(note)
        }
    }

    fun movePrevisionNote(note: PrevisionNote, direction: Int) {
        viewModelScope.launch {
            repository.movePrevisionNote(note, direction)
        }
    }

    // JSON Export & Restore
    suspend fun generateBackupJson(): String {
        val backupData = repository.getFullBackupData()
        return JsonBackupManager.exportToJson(
            config = backupData.config,
            actions = backupData.actions,
            accounts = backupData.accounts,
            notes = backupData.notes
        )
    }

    suspend fun restoreFromJson(jsonString: String): Result<Int> {
        return try {
            val backup = JsonBackupManager.importFromJson(jsonString)
            repository.restoreFromBackup(backup)
            val totalItems = backup.actions.size + backup.accounts.size + backup.notes.size
            Result.success(totalItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reset default
    fun resetToSpreadsheetDefault() {
        viewModelScope.launch {
            repository.resetToDefaultSpreadsheet()
        }
    }
}
