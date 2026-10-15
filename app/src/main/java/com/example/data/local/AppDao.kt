package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BudgetConfig
import com.example.data.model.PlannedAction
import com.example.data.model.PrevisionNote
import com.example.data.model.RealAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Planned Actions
    @Query("SELECT * FROM planned_actions ORDER BY groupSortOrder ASC, groupName ASC, sortOrder ASC, id ASC")
    fun getAllPlannedActions(): Flow<List<PlannedAction>>

    @Query("SELECT * FROM planned_actions ORDER BY groupSortOrder ASC, groupName ASC, sortOrder ASC, id ASC")
    suspend fun getAllPlannedActionsList(): List<PlannedAction>

    @Query("SELECT * FROM planned_actions WHERE groupName = :groupName ORDER BY sortOrder ASC, id ASC")
    suspend fun getActionsInGroup(groupName: String): List<PlannedAction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedAction(action: PlannedAction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPlannedActions(actions: List<PlannedAction>)

    @Update
    suspend fun updatePlannedAction(action: PlannedAction)

    @Update
    suspend fun updatePlannedActions(actions: List<PlannedAction>)

    @Delete
    suspend fun deletePlannedAction(action: PlannedAction)

    @Query("DELETE FROM planned_actions WHERE groupName = :groupName")
    suspend fun deleteGroupByGroupName(groupName: String)

    @Query("UPDATE planned_actions SET groupName = :newName WHERE groupName = :oldName")
    suspend fun renameGroup(oldName: String, newName: String)

    @Query("DELETE FROM planned_actions")
    suspend fun clearPlannedActions()

    // Real Accounts
    @Query("SELECT * FROM real_accounts ORDER BY sortOrder ASC, id ASC")
    fun getAllRealAccounts(): Flow<List<RealAccount>>

    @Query("SELECT * FROM real_accounts ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllRealAccountsList(): List<RealAccount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRealAccount(account: RealAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRealAccounts(accounts: List<RealAccount>)

    @Update
    suspend fun updateRealAccount(account: RealAccount)

    @Update
    suspend fun updateRealAccounts(accounts: List<RealAccount>)

    @Delete
    suspend fun deleteRealAccount(account: RealAccount)

    @Query("DELETE FROM real_accounts")
    suspend fun clearRealAccounts()

    // Prevision Notes
    @Query("SELECT * FROM prevision_notes ORDER BY sortOrder ASC, timestamp DESC, id DESC")
    fun getAllPrevisionNotes(): Flow<List<PrevisionNote>>

    @Query("SELECT * FROM prevision_notes ORDER BY sortOrder ASC, timestamp DESC, id DESC")
    suspend fun getAllPrevisionNotesList(): List<PrevisionNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrevisionNote(note: PrevisionNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPrevisionNotes(notes: List<PrevisionNote>)

    @Update
    suspend fun updatePrevisionNote(note: PrevisionNote)

    @Update
    suspend fun updatePrevisionNotes(notes: List<PrevisionNote>)

    @Delete
    suspend fun deletePrevisionNote(note: PrevisionNote)

    @Query("DELETE FROM prevision_notes")
    suspend fun clearPrevisionNotes()

    // Budget Config
    @Query("SELECT * FROM budget_config WHERE id = 1 LIMIT 1")
    fun getBudgetConfig(): Flow<BudgetConfig?>

    @Query("SELECT * FROM budget_config WHERE id = 1 LIMIT 1")
    suspend fun getBudgetConfigSync(): BudgetConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudgetConfig(config: BudgetConfig)
}
