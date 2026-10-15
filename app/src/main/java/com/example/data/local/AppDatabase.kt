package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BudgetConfig
import com.example.data.model.PlannedAction
import com.example.data.model.PrevisionNote
import com.example.data.model.RealAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PlannedAction::class,
        RealAccount::class,
        PrevisionNote::class,
        BudgetConfig::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE prevision_notes ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE planned_actions ADD COLUMN groupSortOrder INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_app_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Clean template with 1 item per table for complete editing freedom from scratch
         */
        suspend fun populateInitialData(dao: AppDao) {
            dao.setBudgetConfig(
                BudgetConfig(
                    id = 1,
                    monthlyIncome = 0.0,
                    currencySymbol = "€",
                    title = "Mon Budget"
                )
            )

            // Table 1: Incomes & Actions (1 template item)
            val initialActions = listOf(
                PlannedAction(
                    name = "Exemple de dépense",
                    amount = 0.0,
                    groupName = "Fixe",
                    isSavings = false,
                    sortOrder = 0,
                    groupSortOrder = 0
                )
            )
            dao.insertAllPlannedActions(initialActions)

            // Table 2: Comptes réels & Cash (1 template item)
            val initialAccounts = listOf(
                RealAccount(
                    name = "Compte principal",
                    balance = 0.0,
                    colorTag = "#27AE60",
                    sortOrder = 0
                )
            )
            dao.insertAllRealAccounts(initialAccounts)

            // Table 3: À prévoir (1 template item)
            val initialPrevisions = listOf(
                PrevisionNote(
                    amount = 0.0,
                    description = "Dépense à prévoir",
                    isSettled = false,
                    sortOrder = 0
                )
            )
            dao.insertAllPrevisionNotes(initialPrevisions)
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.appDao())
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.appDao())
                    }
                }
            }
        }
    }
}
