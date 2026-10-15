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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.PrevisionNote
import com.example.data.model.RealAccount
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EditPrevisionDialog
import com.example.ui.components.EditRealAccountDialog
import com.example.ui.components.Formatters
import com.example.ui.theme.ExpenseNegative
import com.example.ui.theme.IncomePositive
import com.example.ui.viewmodel.BudgetUiState

@Composable
fun ComptesEtPrevisionsScreen(
    uiState: BudgetUiState,
    onAddAccount: (name: String, balance: Double, colorTag: String) -> Unit,
    onUpdateAccount: (id: Long, name: String, balance: Double, colorTag: String) -> Unit,
    onDeleteAccount: (RealAccount) -> Unit,
    onMoveAccountUp: (RealAccount) -> Unit,
    onMoveAccountDown: (RealAccount) -> Unit,
    onAddNote: (amount: Double, description: String) -> Unit,
    onUpdateNote: (id: Long, amount: Double, description: String) -> Unit,
    onToggleSettled: (PrevisionNote) -> Unit,
    onDeleteNote: (PrevisionNote) -> Unit,
    onMoveNoteUp: (PrevisionNote) -> Unit,
    onMoveNoteDown: (PrevisionNote) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<RealAccount?>(null) }
    var accountToDelete by remember { mutableStateOf<RealAccount?>(null) }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<PrevisionNote?>(null) }
    var noteToDelete by remember { mutableStateOf<PrevisionNote?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // SECTION 1 : COMPTES & ARGENT RÉEL (En haut)
        // ==========================================
        item(key = "comptes_total_card") {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("comptes_total_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.totalRealMoney >= 0) Color(0xFFD5F5E3) else Color(0xFFFADBD8)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.totalRealMoney >= 0) Color(0xFF27AE60) else Color(0xFFC0392B)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Solde Total Réel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.totalRealMoney >= 0) Color(0xFF144D29) else Color(0xFF641E16)
                            )
                            Text(
                                text = "Comptes & liquidités combinés",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.totalRealMoney >= 0) Color(0xFF144D29).copy(alpha = 0.8f) else Color(0xFF641E16).copy(alpha = 0.8f)
                            )
                        }
                    }

                    Text(
                        text = Formatters.formatCurrency(uiState.totalRealMoney, uiState.currencySymbol),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (uiState.totalRealMoney >= 0) Color(0xFF144D29) else Color(0xFF922B21)
                    )
                }
            }
        }

        item(key = "header_comptes_section") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comptes bancaires & Cash",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = { showAddAccountDialog = true },
                    modifier = Modifier.testTag("add_account_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter compte")
                }
            }
        }

        // List of Real Accounts with Up and Down arrows
        itemsIndexed(uiState.realAccounts, key = { _, account -> "account_${account.id}" }) { index, account ->
            val badgeColor = try {
                Color(android.graphics.Color.parseColor(account.colorTag))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { accountToEdit = account }
                    .testTag("account_item_${account.name}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Toucher pour modifier",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Up/Down arrows
                        IconButton(
                            onClick = { onMoveAccountUp(account) },
                            enabled = index > 0,
                            modifier = Modifier.size(26.dp).testTag("account_up_${account.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Monter compte",
                                tint = if (index > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onMoveAccountDown(account) },
                            enabled = index < uiState.realAccounts.size - 1,
                            modifier = Modifier.size(26.dp).testTag("account_down_${account.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Descendre compte",
                                tint = if (index < uiState.realAccounts.size - 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Formatters.formatCurrency(account.balance, uiState.currencySymbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (account.balance < 0) ExpenseNegative else IncomePositive
                        )
                        IconButton(
                            onClick = { accountToEdit = account },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                        }
                        IconButton(
                            onClick = { accountToDelete = account },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }

        // Section Divider
        item(key = "divider_accounts_previsions") {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ==========================================
        // SECTION 2 : REMARQUES ET PRÉVISIONS (À prévoir)
        // ==========================================
        item(key = "previsions_total_header_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("previsions_total_header_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD6DBDF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PendingActions,
                            contentDescription = null,
                            tint = Color(0xFF1C2833),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "À prévoir",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C2833)
                        )
                    }

                    Text(
                        text = Formatters.formatCurrency(uiState.totalPrevisions, uiState.currencySymbol, false),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1C2833)
                    )
                }
            }
        }

        item(key = "header_previsions_section") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "À prévoir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = { showAddNoteDialog = true },
                    modifier = Modifier.testTag("add_prevision_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter")
                }
            }
        }

        // List of Previsions with Up and Down arrows
        itemsIndexed(uiState.previsionNotes, key = { _, note -> "note_${note.id}" }) { index, note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { noteToEdit = note }
                    .testTag("prevision_card_${note.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (note.isSettled) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    } else {
                        Color(0xFFFDEBD0)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = note.isSettled,
                            onCheckedChange = { onToggleSettled(note) }
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Column {
                            Text(
                                text = note.description,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (note.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF5D4037),
                                textDecoration = if (note.isSettled) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Text(
                                text = if (note.isSettled) "Payé / Réglé" else "À prévoir prochainement",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (note.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF8D6E63)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Up/Down arrows
                        IconButton(
                            onClick = { onMoveNoteUp(note) },
                            enabled = index > 0,
                            modifier = Modifier.size(26.dp).testTag("note_up_${note.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Monter note",
                                tint = if (index > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onMoveNoteDown(note) },
                            enabled = index < uiState.previsionNotes.size - 1,
                            modifier = Modifier.size(26.dp).testTag("note_down_${note.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Descendre note",
                                tint = if (index < uiState.previsionNotes.size - 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Formatters.formatCurrency(note.amount, uiState.currencySymbol, false),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (note.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF3E2723),
                            textDecoration = if (note.isSettled) TextDecoration.LineThrough else TextDecoration.None
                        )
                        IconButton(
                            onClick = { noteToEdit = note },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                        }
                        IconButton(
                            onClick = { noteToDelete = note },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }

        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // ==========================================
    // DIALOGS
    // ==========================================
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

    accountToDelete?.let { acc ->
        ConfirmDeleteDialog(
            itemTitle = acc.name,
            onDismiss = { accountToDelete = null },
            onConfirm = {
                onDeleteAccount(acc)
                accountToDelete = null
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

    noteToDelete?.let { note ->
        ConfirmDeleteDialog(
            itemTitle = note.description,
            onDismiss = { noteToDelete = null },
            onConfirm = {
                onDeleteNote(note)
                noteToDelete = null
            }
        )
    }
}
