package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.JsonBackupManager
import com.example.data.model.BudgetConfig
import com.example.data.model.PlannedAction
import com.example.data.model.PrevisionNote
import com.example.data.model.RealAccount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Avance", appName)
  }

  @Test
  fun testJsonBackupExportAndImport() {
    val config = BudgetConfig(monthlyIncome = 4500.0, currencySymbol = "€", title = "Novembre")
    val actions = listOf(
      PlannedAction(name = "Loyer", amount = 850.0, groupName = "Fixe", isSavings = false, sortOrder = 0),
      PlannedAction(name = "Livret A", amount = 500.0, groupName = "Épargne", isSavings = true, sortOrder = 1)
    )
    val accounts = listOf(
      RealAccount(name = "BNP", balance = 1250.50, colorTag = "#27AE60", sortOrder = 0)
    )
    val notes = listOf(
      PrevisionNote(amount = 120.0, description = "Assurance auto", isSettled = false, sortOrder = 0)
    )

    val exportedJson = JsonBackupManager.exportToJson(config, actions, accounts, notes)
    assertNotNull(exportedJson)
    assertTrue(exportedJson.contains("Loyer"))
    assertTrue(exportedJson.contains("Livret A"))
    assertTrue(exportedJson.contains("BNP"))
    assertTrue(exportedJson.contains("Assurance auto"))

    val restored = JsonBackupManager.importFromJson(exportedJson)
    assertEquals(4500.0, restored.config.monthlyIncome, 0.001)
    assertEquals(2, restored.actions.size)
    assertEquals("Loyer", restored.actions[0].name)
    assertEquals(850.0, restored.actions[0].amount, 0.001)
    assertEquals(true, restored.actions[1].isSavings)
    assertEquals(1, restored.accounts.size)
    assertEquals("BNP", restored.accounts[0].name)
    assertEquals(1, restored.notes.size)
    assertEquals("Assurance auto", restored.notes[0].description)
  }
}
