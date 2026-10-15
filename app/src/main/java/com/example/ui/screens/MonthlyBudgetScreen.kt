package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlannedAction
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EditIncomeDialog
import com.example.ui.components.EditPlannedActionDialog
import com.example.ui.components.Formatters
import com.example.ui.components.ManageCategoryDialog
import com.example.ui.theme.ExpenseNegative
import com.example.ui.theme.IncomePositive
import com.example.ui.viewmodel.BudgetUiState

@Composable
fun MonthlyBudgetScreen(
    uiState: BudgetUiState,
    onAddAction: (name: String, amount: Double, group: String, isSavings: Boolean) -> Unit,
    onUpdateAction: (id: Long, name: String, amount: Double, group: String, isSavings: Boolean) -> Unit,
    onDeleteAction: (PlannedAction) -> Unit,
    onMoveActionUp: (PlannedAction) -> Unit,
    onMoveActionDown: (PlannedAction) -> Unit,
    onMoveGroupUp: (String) -> Unit,
    onMoveGroupDown: (String) -> Unit,
    onUpdateIncome: (Double) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onRenameGroup: (oldName: String, newName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var actionToEdit by remember { mutableStateOf<PlannedAction?>(null) }
    var actionToDelete by remember { mutableStateOf<PlannedAction?>(null) }
    var showEditIncomeDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategoryToManage by remember { mutableStateOf<String?>(null) }
    var groupToDelete by remember { mutableStateOf<String?>(null) }
    var preselectedGroupForNewAction by remember { mutableStateOf("Fixe") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    preselectedGroupForNewAction = uiState.groups.firstOrNull()?.groupName ?: "Fixe"
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_planned_action_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter une dépense")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "incomes_header_item") {
                Spacer(modifier = Modifier.height(4.dp))
                // Spreadsheet Header Row: "Incomes | 5000"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEditIncomeDialog = true }
                        .testTag("incomes_table_header"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD5F5E3) // Spreadsheet green tone
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Incomes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF144D29)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier revenus",
                                tint = Color(0xFF144D29),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = Formatters.formatCurrency(uiState.monthlyIncome, uiState.currencySymbol, false),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF144D29)
                        )
                    }
                }
            }

            // Budget Groups (e.g. Maroc, Mensuel, Basics, Fixe)
            itemsIndexed(uiState.groups, key = { _, group -> "group_${group.groupName}" }) { groupIndex, groupSummary ->
                val (groupHeaderBg, groupHeaderTextColor) = Formatters.getGroupHeaderColors(groupSummary.groupName)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_group_${groupSummary.groupName}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Group Header with subtotal & order arrows
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(groupHeaderBg)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCategoryToManage = groupSummary.groupName }
                            ) {
                                Text(
                                    text = "${groupSummary.groupName} : ${Formatters.formatCurrency(groupSummary.subtotal, uiState.currencySymbol, false)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = groupHeaderTextColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${groupSummary.items.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = groupHeaderTextColor.copy(alpha = 0.8f)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onMoveGroupUp(groupSummary.groupName) },
                                    enabled = groupIndex > 0,
                                    modifier = Modifier.size(28.dp).testTag("group_up_${groupSummary.groupName}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Monter catégorie",
                                        tint = if (groupIndex > 0) groupHeaderTextColor else groupHeaderTextColor.copy(alpha = 0.25f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onMoveGroupDown(groupSummary.groupName) },
                                    enabled = groupIndex < uiState.groups.size - 1,
                                    modifier = Modifier.size(28.dp).testTag("group_down_${groupSummary.groupName}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Descendre catégorie",
                                        tint = if (groupIndex < uiState.groups.size - 1) groupHeaderTextColor else groupHeaderTextColor.copy(alpha = 0.25f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { selectedCategoryToManage = groupSummary.groupName },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Gérer catégorie",
                                        tint = groupHeaderTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Table Rows inside this group
                        groupSummary.items.forEachIndexed { index, action ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { actionToEdit = action }
                                    .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = action.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (action.isSavings) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(IncomePositive.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Savings,
                                                    contentDescription = null,
                                                    tint = IncomePositive,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "Épargne",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = IncomePositive
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Up/Down Arrows for items
                                    IconButton(
                                        onClick = { onMoveActionUp(action) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(26.dp).testTag("action_up_${action.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Monter dépense",
                                            tint = if (index > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onMoveActionDown(action) },
                                        enabled = index < groupSummary.items.size - 1,
                                        modifier = Modifier.size(26.dp).testTag("action_down_${action.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Descendre dépense",
                                            tint = if (index < groupSummary.items.size - 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = Formatters.formatCurrency(action.amount, uiState.currencySymbol, false),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { actionToEdit = action },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Modifier",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { actionToDelete = action },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Supprimer",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            if (index < groupSummary.items.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Spreadsheet Footer Row: "Autres | 103"
            item(key = "autres_footer_item") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("autres_table_footer"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3CD) // Spreadsheet yellow tone
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Autres (Reste disponible)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF534515)
                            )
                            Text(
                                text = "Incomes - Actions prévues",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF534515).copy(alpha = 0.7f)
                            )
                        }

                        Text(
                            text = Formatters.formatCurrency(uiState.remainingAutres, uiState.currencySymbol, false),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (uiState.remainingAutres >= 0) Color(0xFF144D29) else ExpenseNegative
                        )
                    }
                }
            }

            // Action buttons below the table
            item(key = "actions_row_item") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            preselectedGroupForNewAction = uiState.groups.firstOrNull()?.groupName ?: "Fixe"
                            showAddDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter dépense", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { showAddCategoryDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter catégorie", fontSize = 12.sp)
                    }
                }
            }

            item(key = "bottom_spacer_item") {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        val existingGroupNames = uiState.groups.map { it.groupName }
        EditPlannedActionDialog(
            defaultGroupName = preselectedGroupForNewAction,
            existingGroups = if (existingGroupNames.isNotEmpty()) existingGroupNames else listOf("Fixe", "Maroc", "Mensuel"),
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, group, isSavings ->
                onAddAction(name, amount, group, isSavings)
                showAddDialog = false
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

    actionToDelete?.let { action ->
        ConfirmDeleteDialog(
            itemTitle = action.name,
            onDismiss = { actionToDelete = null },
            onConfirm = {
                onDeleteAction(action)
                actionToDelete = null
            }
        )
    }

    if (showEditIncomeDialog) {
        EditIncomeDialog(
            currentIncome = uiState.monthlyIncome,
            onDismiss = { showEditIncomeDialog = false },
            onConfirm = { newIncome ->
                onUpdateIncome(newIncome)
                showEditIncomeDialog = false
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
                showAddDialog = true
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
}
