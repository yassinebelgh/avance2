package com.example.data.repository

import com.example.data.backup.JsonBackupManager
import com.example.data.local.AppDao
import com.example.data.local.AppDatabase
import com.example.data.model.BudgetConfig
import com.example.data.model.PlannedAction
import com.example.data.model.PrevisionNote
import com.example.data.model.RealAccount
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val dao: AppDao) {

    val plannedActions: Flow<List<PlannedAction>> = dao.getAllPlannedActions()
    val realAccounts: Flow<List<RealAccount>> = dao.getAllRealAccounts()
    val previsionNotes: Flow<List<PrevisionNote>> = dao.getAllPrevisionNotes()
    val budgetConfig: Flow<BudgetConfig?> = dao.getBudgetConfig()

    suspend fun insertPlannedAction(action: PlannedAction) = dao.insertPlannedAction(action)
    suspend fun updatePlannedAction(action: PlannedAction) = dao.updatePlannedAction(action)
    suspend fun deletePlannedAction(action: PlannedAction) = dao.deletePlannedAction(action)
    suspend fun deleteGroupByGroupName(groupName: String) = dao.deleteGroupByGroupName(groupName)
    suspend fun renameGroup(oldName: String, newName: String) = dao.renameGroup(oldName, newName)

    suspend fun insertRealAccount(account: RealAccount) = dao.insertRealAccount(account)
    suspend fun updateRealAccount(account: RealAccount) = dao.updateRealAccount(account)
    suspend fun deleteRealAccount(account: RealAccount) = dao.deleteRealAccount(account)

    suspend fun insertPrevisionNote(note: PrevisionNote) = dao.insertPrevisionNote(note)
    suspend fun updatePrevisionNote(note: PrevisionNote) = dao.updatePrevisionNote(note)
    suspend fun deletePrevisionNote(note: PrevisionNote) = dao.deletePrevisionNote(note)

    suspend fun updateIncome(amount: Double) {
        val currentConfig = dao.getBudgetConfigSync()
        dao.setBudgetConfig(
            BudgetConfig(
                id = 1,
                monthlyIncome = amount,
                currencySymbol = currentConfig?.currencySymbol ?: "€",
                title = currentConfig?.title ?: "Budget Mensuel"
            )
        )
    }

    suspend fun updateConfig(config: BudgetConfig) = dao.setBudgetConfig(config)

    // Reordering actions within their group
    suspend fun movePlannedAction(action: PlannedAction, direction: Int) { // -1 = up, +1 = down
        val actionsInGroup = dao.getActionsInGroup(action.groupName)
        val currentIndex = actionsInGroup.indexOfFirst { it.id == action.id }
        if (currentIndex == -1) return
        val targetIndex = currentIndex + direction
        if (targetIndex !in actionsInGroup.indices) return

        val mutable = actionsInGroup.toMutableList()
        val item = mutable.removeAt(currentIndex)
        mutable.add(targetIndex, item)

        val updated = mutable.mapIndexed { idx, act -> act.copy(sortOrder = idx) }
        dao.updatePlannedActions(updated)
    }

    // Reordering whole groups
    suspend fun moveGroup(groupName: String, direction: Int) {
        val allActions = dao.getAllPlannedActionsList()
        // Distinct groups in their current order
        val groupNames = allActions.map { it.groupName }.distinct()
        val currentIndex = groupNames.indexOf(groupName)
        if (currentIndex == -1) return
        val targetIndex = currentIndex + direction
        if (targetIndex !in groupNames.indices) return

        val mutableGroups = groupNames.toMutableList()
        val item = mutableGroups.removeAt(currentIndex)
        mutableGroups.add(targetIndex, item)

        // Assign groupSortOrder based on new group list
        val updatedActions = allActions.map { action ->
            val newGroupSort = mutableGroups.indexOf(action.groupName)
            action.copy(groupSortOrder = if (newGroupSort != -1) newGroupSort else 0)
        }
        dao.updatePlannedActions(updatedActions)
    }

    // Reordering real accounts
    suspend fun moveRealAccount(account: RealAccount, direction: Int) {
        val accounts = dao.getAllRealAccountsList()
        val currentIndex = accounts.indexOfFirst { it.id == account.id }
        if (currentIndex == -1) return
        val targetIndex = currentIndex + direction
        if (targetIndex !in accounts.indices) return

        val mutable = accounts.toMutableList()
        val item = mutable.removeAt(currentIndex)
        mutable.add(targetIndex, item)

        val updated = mutable.mapIndexed { idx, acc -> acc.copy(sortOrder = idx) }
        dao.updateRealAccounts(updated)
    }

    // Reordering prevision notes
    suspend fun movePrevisionNote(note: PrevisionNote, direction: Int) {
        val notes = dao.getAllPrevisionNotesList()
        val currentIndex = notes.indexOfFirst { it.id == note.id }
        if (currentIndex == -1) return
        val targetIndex = currentIndex + direction
        if (targetIndex !in notes.indices) return

        val mutable = notes.toMutableList()
        val item = mutable.removeAt(currentIndex)
        mutable.add(targetIndex, item)

        val updated = mutable.mapIndexed { idx, n -> n.copy(sortOrder = idx) }
        dao.updatePrevisionNotes(updated)
    }

    // Backup & Restore
    suspend fun getFullBackupData(): JsonBackupManager.BackupData {
        val config = dao.getBudgetConfigSync() ?: BudgetConfig()
        val actions = dao.getAllPlannedActionsList()
        val accounts = dao.getAllRealAccountsList()
        val notes = dao.getAllPrevisionNotesList()
        return JsonBackupManager.BackupData(config, actions, accounts, notes)
    }

    suspend fun restoreFromBackup(backup: JsonBackupManager.BackupData) {
        dao.clearPlannedActions()
        dao.clearRealAccounts()
        dao.clearPrevisionNotes()

        dao.setBudgetConfig(backup.config)
        dao.insertAllPlannedActions(backup.actions)
        dao.insertAllRealAccounts(backup.accounts)
        dao.insertAllPrevisionNotes(backup.notes)
    }

    suspend fun resetToDefaultSpreadsheet() {
        dao.clearPlannedActions()
        dao.clearRealAccounts()
        dao.clearPrevisionNotes()
        AppDatabase.populateInitialData(dao)
    }
}
