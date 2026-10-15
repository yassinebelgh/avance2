package com.example.data.backup

import com.example.data.model.BudgetConfig
import com.example.data.model.PlannedAction
import com.example.data.model.PrevisionNote
import com.example.data.model.RealAccount
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object JsonBackupManager {

    data class BackupData(
        val config: BudgetConfig,
        val actions: List<PlannedAction>,
        val accounts: List<RealAccount>,
        val notes: List<PrevisionNote>,
        val exportDateFormatted: String = ""
    )

    fun exportToJson(
        config: BudgetConfig?,
        actions: List<PlannedAction>,
        accounts: List<RealAccount>,
        notes: List<PrevisionNote>
    ): String {
        val root = JSONObject()
        root.put("version", 3)
        root.put("appName", "Avance")
        root.put("exportedAt", System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        root.put("exportDate", dateFormat.format(Date()))

        // Config
        val configObj = JSONObject()
        configObj.put("monthlyIncome", config?.monthlyIncome ?: 0.0)
        configObj.put("currencySymbol", config?.currencySymbol ?: "€")
        configObj.put("title", config?.title ?: "Mon Budget")
        root.put("config", configObj)

        // Planned Actions
        val actionsArray = JSONArray()
        actions.forEach { action ->
            val obj = JSONObject()
            obj.put("name", action.name)
            obj.put("amount", action.amount)
            obj.put("groupName", action.groupName)
            obj.put("isSavings", action.isSavings)
            obj.put("sortOrder", action.sortOrder)
            obj.put("groupSortOrder", action.groupSortOrder)
            actionsArray.put(obj)
        }
        root.put("plannedActions", actionsArray)

        // Real Accounts
        val accountsArray = JSONArray()
        accounts.forEach { account ->
            val obj = JSONObject()
            obj.put("name", account.name)
            obj.put("balance", account.balance)
            obj.put("colorTag", account.colorTag)
            obj.put("sortOrder", account.sortOrder)
            accountsArray.put(obj)
        }
        root.put("realAccounts", accountsArray)

        // Prevision Notes
        val notesArray = JSONArray()
        notes.forEach { note ->
            val obj = JSONObject()
            obj.put("amount", note.amount)
            obj.put("description", note.description)
            obj.put("isSettled", note.isSettled)
            obj.put("sortOrder", note.sortOrder)
            obj.put("timestamp", note.timestamp)
            notesArray.put(obj)
        }
        root.put("previsionNotes", notesArray)

        return root.toString(2)
    }

    fun importFromJson(jsonString: String): BackupData {
        val root = JSONObject(jsonString)

        val configObj = root.optJSONObject("config")
        val config = BudgetConfig(
            id = 1,
            monthlyIncome = configObj?.optDouble("monthlyIncome", 0.0) ?: 0.0,
            currencySymbol = configObj?.optString("currencySymbol", "€") ?: "€",
            title = configObj?.optString("title", "Mon Budget") ?: "Mon Budget"
        )

        val actions = mutableListOf<PlannedAction>()
        val actionsArray = root.optJSONArray("plannedActions")
        if (actionsArray != null) {
            for (i in 0 until actionsArray.length()) {
                val obj = actionsArray.getJSONObject(i)
                actions.add(
                    PlannedAction(
                        id = 0,
                        name = obj.getString("name"),
                        amount = obj.getDouble("amount"),
                        groupName = obj.optString("groupName", "Divers"),
                        isSavings = obj.optBoolean("isSavings", false),
                        sortOrder = obj.optInt("sortOrder", i),
                        groupSortOrder = obj.optInt("groupSortOrder", 0)
                    )
                )
            }
        }

        val accounts = mutableListOf<RealAccount>()
        val accountsArray = root.optJSONArray("realAccounts")
        if (accountsArray != null) {
            for (i in 0 until accountsArray.length()) {
                val obj = accountsArray.getJSONObject(i)
                accounts.add(
                    RealAccount(
                        id = 0,
                        name = obj.getString("name"),
                        balance = obj.getDouble("balance"),
                        colorTag = obj.optString("colorTag", "#27AE60"),
                        sortOrder = obj.optInt("sortOrder", i)
                    )
                )
            }
        }

        val notes = mutableListOf<PrevisionNote>()
        val notesArray = root.optJSONArray("previsionNotes")
        if (notesArray != null) {
            for (i in 0 until notesArray.length()) {
                val obj = notesArray.getJSONObject(i)
                notes.add(
                    PrevisionNote(
                        id = 0,
                        amount = obj.getDouble("amount"),
                        description = obj.getString("description"),
                        isSettled = obj.optBoolean("isSettled", false),
                        sortOrder = obj.optInt("sortOrder", i),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        val exportDate = root.optString("exportDate", "")

        return BackupData(
            config = config,
            actions = actions,
            accounts = accounts,
            notes = notes,
            exportDateFormatted = exportDate
        )
    }
}
