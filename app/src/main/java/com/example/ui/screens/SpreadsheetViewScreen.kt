package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlannedAction
import com.example.data.model.PrevisionNote
import com.example.data.model.RealAccount
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EditIncomeDialog
import com.example.ui.components.EditPlannedActionDialog
import com.example.ui.components.EditPrevisionDialog
import com.example.ui.components.EditRealAccountDialog
import com.example.ui.components.Formatters
import com.example.ui.components.ManageCategoryDialog
import com.example.ui.theme.ExpenseNegative
import com.example.ui.theme.IncomePositive
import com.example.ui.viewmodel.BudgetUiState

@Composable
fun SpreadsheetViewScreen(
    uiState: BudgetUiState,
    onUpdateIncome: (Double) -> Unit,
    onAddAction: (name: String, amount: Double, group: String, isSavings: Boolean) -> Unit,
    onUpdateAction: (id: Long, name: String, amount: Double, group: String, isSavings: Boolean) -> Unit,
    onDeleteAction: (PlannedAction) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onRenameGroup: (oldName: String, newName: String) -> Unit,
    onAddAccount: (name: String, balance: Double, colorTag: String) -> Unit,
    onUpdateAccount: (id: Long, name: String, balance: Double, colorTag: String) -> Unit,
    onDeleteAccount: (RealAccount) -> Unit,
    onAddNote: (amount: Double, description: String) -> Unit,
    onUpdateNote: (id: Long, amount: Double, description: String) -> Unit,
    onDeleteNote: (PrevisionNote) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditIncomeDialog by remember { mutableStateOf(false) }
    var actionToEdit by remember { mutableStateOf<PlannedAction?>(null) }
    var showAddActionDialog by remember { mutableStateOf(false) }
    var preselectedGroupForNewAction by remember { mutableStateOf("Mensuel") }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategoryToManage by remember { mutableStateOf<String?>(null) }

    var accountToEdit by remember { mutableStateOf<RealAccount?>(null) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<RealAccount?>(null) }

    var noteToEdit by remember { mutableStateOf<PrevisionNote?>(null) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<PrevisionNote?>(null) }

    var groupToDelete by remember { mutableStateOf<String?>(null) }

    val tableBorderColor = Color(0xFF2C3E50)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // SUMMARY DASHBOARD MINI-BANNER (Savings vs Upcoming Expenses)
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_savings_vs_expenses_banner"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Total Savings
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(IncomePositive.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = IncomePositive,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Épargne totale",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.formatCurrency(uiState.totalSavings, uiState.currencySymbol, false),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IncomePositive
                            )
                        }
                    }

                    // Right: Upcoming expenses
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Dépenses prévues",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.formatCurrency(uiState.totalUpcomingExpenses, uiState.currencySymbol, false),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseNegative
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ExpenseNegative.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = ExpenseNegative,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Small ratio indicator
                val totalFlow = uiState.totalSavings + uiState.totalUpcomingExpenses
                val ratio = if (totalFlow > 0) (uiState.totalSavings / totalFlow).toFloat() else 0.5f

                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = IncomePositive,
                    trackColor = ExpenseNegative.copy(alpha = 0.35f)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Épargne : ${String.format("%.0f", ratio * 100)}% | Reste (Autres) : ${Formatters.formatCurrency(uiState.remainingAutres, uiState.currencySymbol, false)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Argent réel : ${Formatters.formatCurrency(uiState.totalRealMoney, uiState.currencySymbol)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.totalRealMoney >= 0) IncomePositive else ExpenseNegative
                    )
                }
            }
        }

        // Action Toolbar to Add/Edit Categories and Outputs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    preselectedGroupForNewAction = uiState.groups.firstOrNull()?.groupName ?: "Mensuel"
                    showAddActionDialog = true
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_output_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Dépense / Output", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { showAddCategoryDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_category_btn")
            ) {
                Icon(imageVector = Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Catégorie", fontSize = 12.sp)
            }
        }

        // ==========================================
        // TABLE 1: INCOMES & PLANNED ACTIONS (Dynamic Editable Spreadsheet)
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tableur_main_budget_table"),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, tableBorderColor)
            ) {
                // Incomes Header Row (green)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD5F5E3))
                        .clickable { showEditIncomeDialog = true }
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(2f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Incomes",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier",
                            tint = Color(0xFF144D29),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .height(24.dp)
                            .background(tableBorderColor)
                    )
                    Text(
                        text = Formatters.formatCurrency(uiState.monthlyIncome, uiState.currencySymbol, false),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(tableBorderColor)
                )

                // Category Groups (Maroc, Mensuel, Basics, custom...)
                uiState.groups.forEach { group ->
                    val (groupBg, groupText) = Formatters.getGroupHeaderColors(group.groupName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        // Left Column: Category Name & Subtotal (tap to manage/rename/delete category)
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .background(groupBg)
                                .clickable { selectedCategoryToManage = group.groupName }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${group.groupName} : ${Formatters.formatCurrency(group.subtotal, uiState.currencySymbol, false)}",
                                    fontWeight = FontWeight.Bold,
                                    color = groupText,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "(gérer)",
                                    fontSize = 10.sp,
                                    color = groupText.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Vertical separator
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .background(tableBorderColor)
                        )

                        // Right Column: Outputs (actions) and their costs
                        Column(modifier = Modifier.weight(2f)) {
                            group.items.forEachIndexed { itemIdx, action ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { actionToEdit = action }
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1.2f),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = action.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )
                                        if (action.isSavings) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Savings,
                                                contentDescription = "Épargne",
                                                tint = IncomePositive,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(18.dp)
                                            .background(Color.LightGray)
                                    )
                                    Text(
                                        text = Formatters.formatCurrency(action.amount, uiState.currencySymbol, false),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                if (itemIdx < group.items.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFE2E8F0))
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(tableBorderColor)
                    )
                }

                // Autres (Remaining discretionary buffer)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3CD))
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Autres",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(2f),
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .height(20.dp)
                            .background(tableBorderColor)
                    )
                    Text(
                        text = Formatters.formatCurrency(uiState.remainingAutres, uiState.currencySymbol, false),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (uiState.remainingAutres >= 0) Color.Black else ExpenseNegative,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ==========================================
        // TABLE 2: REAL MONEY IN ACCOUNTS & CASH
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Argent Réel (Comptes & Cash)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { showAddAccountDialog = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter compte", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, tableBorderColor)
            ) {
                uiState.realAccounts.forEachIndexed { idx, acc ->
                    val rowBg = when {
                        acc.name.contains("BNP", ignoreCase = true) -> Color(0xFFD5F5E3)
                        acc.name.contains("Boursorama", ignoreCase = true) -> Color(0xFFFF80DF)
                        else -> Color(0xFFFCE4D6)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accountToEdit = acc }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = acc.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(22.dp)
                                .background(tableBorderColor)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(rowBg)
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = Formatters.formatCurrency(acc.balance, uiState.currencySymbol),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(tableBorderColor)
                    )
                }

                // Total Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(tableBorderColor)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFBDD7EE))
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Formatters.formatCurrency(uiState.totalRealMoney, uiState.currencySymbol),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // ==========================================
        // TABLE 3: REMARQUES ET PRÉVISIONS
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Remarques et Prévisions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { showAddNoteDialog = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter prévision", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, tableBorderColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD6DBDF))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Remarques et prévisions : ${Formatters.formatCurrency(uiState.totalPrevisions, uiState.currencySymbol, false)}",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(tableBorderColor)
                )

                uiState.previsionNotes.forEachIndexed { idx, note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFCE4D6))
                            .clickable { noteToEdit = note }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Formatters.formatCurrency(note.amount, uiState.currencySymbol, false),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(tableBorderColor)
                        )
                        Text(
                            text = note.description,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color.Black,
                            modifier = Modifier
                                .weight(1.5f)
                                .padding(horizontal = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (idx < uiState.previsionNotes.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(tableBorderColor)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // ==========================================
    // MODAL DIALOGS FOR DYNAMIC EDITING
    // ==========================================
    if (showEditIncomeDialog) {
        EditIncomeDialog(
            currentIncome = uiState.monthlyIncome,
            onDismiss = { showEditIncomeDialog = false },
            onConfirm = {
                onUpdateIncome(it)
                showEditIncomeDialog = false
            }
        )
    }

    if (showAddActionDialog) {
        val groups = uiState.groups.map { it.groupName }
        EditPlannedActionDialog(
            defaultGroupName = preselectedGroupForNewAction,
            existingGroups = if (groups.isNotEmpty()) groups else listOf("Maroc", "Mensuel", "Basics"),
            onDismiss = { showAddActionDialog = false },
            onConfirm = { name, amount, group, isSavings ->
                onAddAction(name, amount, group, isSavings)
                showAddActionDialog = false
            }
        )
    }

    actionToEdit?.let { action ->
        EditPlannedActionDialog(
            existingAction = action,
            existingGroups = uiState.groups.map { it.groupName },
            onDismiss = { actionToEdit = null },
            onDelete = {
                onDeleteAction(action)
                actionToEdit = null
            },
            onConfirm = { name, amount, group, isSavings ->
                onUpdateAction(action.id, name, amount, group, isSavings)
                actionToEdit = null
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { catName, firstActionName, amount, isSavings ->
                onAddAction(firstActionName, amount, catName, isSavings)
                showAddCategoryDialog = false
            }
        )
    }

    selectedCategoryToManage?.let { catName ->
        ManageCategoryDialog(
            groupName = catName,
            onDismiss = { selectedCategoryToManage = null },
            onRename = { newName ->
                onRenameGroup(catName, newName)
                selectedCategoryToManage = null
            },
            onDeleteGroup = {
                groupToDelete = catName
                selectedCategoryToManage = null
            },
            onAddNewActionToGroup = {
                preselectedGroupForNewAction = catName
                selectedCategoryToManage = null
                showAddActionDialog = true
            }
        )
    }

    groupToDelete?.let { grp ->
        ConfirmDeleteDialog(
            itemTitle = "toute la catégorie « $grp » et ses dépenses associées",
            onDismiss = { groupToDelete = null },
            onConfirm = {
                onDeleteGroup(grp)
                groupToDelete = null
            }
        )
    }

    if (showAddAccountDialog) {
        EditRealAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { name, balance, color ->
                onAddAccount(name, balance, color)
                showAddAccountDialog = false
            }
        )
    }

    accountToEdit?.let { acc ->
        EditRealAccountDialog(
            existingAccount = acc,
            onDismiss = { accountToEdit = null },
            onConfirm = { name, balance, color ->
                onUpdateAccount(acc.id, name, balance, color)
                accountToEdit = null
            }
        )
    }

    if (showAddNoteDialog) {
        EditPrevisionDialog(
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { amount, desc ->
                onAddNote(amount, desc)
                showAddNoteDialog = false
            }
        )
    }

    noteToEdit?.let { note ->
        EditPrevisionDialog(
            existingNote = note,
            onDismiss = { noteToEdit = null },
            onConfirm = { amount, desc ->
                onUpdateNote(note.id, amount, desc)
                noteToEdit = null
            }
        )
    }
}
