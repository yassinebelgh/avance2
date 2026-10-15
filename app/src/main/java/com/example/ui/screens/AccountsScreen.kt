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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.RealAccount
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EditRealAccountDialog
import com.example.ui.components.Formatters
import com.example.ui.theme.ExpenseNegative
import com.example.ui.theme.IncomePositive
import com.example.ui.viewmodel.BudgetUiState

@Composable
fun AccountsScreen(
    uiState: BudgetUiState,
    onAddAccount: (name: String, balance: Double, colorTag: String) -> Unit,
    onUpdateAccount: (id: Long, name: String, balance: Double, colorTag: String) -> Unit,
    onDeleteAccount: (RealAccount) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<RealAccount?>(null) }
    var accountToDelete by remember { mutableStateOf<RealAccount?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_real_account_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter un compte")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Summary Total Card matching the spreadsheet Total row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("accounts_total_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.totalRealMoney >= 0) {
                            Color(0xFFD5F5E3)
                        } else {
                            Color(0xFFFADBD8)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (uiState.totalRealMoney >= 0) Color(0xFF27AE60)
                                            else Color(0xFFC0392B)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
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
                                        text = "Tous comptes & cash combinés",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (uiState.totalRealMoney >= 0) Color(0xFF144D29).copy(alpha = 0.8f) else Color(0xFF641E16).copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Text(
                                text = Formatters.formatCurrency(uiState.totalRealMoney, uiState.currencySymbol),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (uiState.totalRealMoney >= 0) Color(0xFF144D29) else Color(0xFF922B21)
                            )
                        }
                    }
                }
            }

            // List of Accounts (Spreadsheet style)
            item {
                Text(
                    text = "Détail des comptes (Tableau d'argent réel)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(uiState.realAccounts, key = { "acc_${it.id}" }) { account ->
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
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
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
                            val isNegative = account.balance < 0
                            Text(
                                text = Formatters.formatCurrency(account.balance, uiState.currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isNegative) ExpenseNegative else IncomePositive
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { accountToEdit = account },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modifier",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { accountToDelete = account },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Supprimer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.realAccounts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun compte renseigné. Utilisez le bouton + pour en ajouter un.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddDialog) {
        EditRealAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, balance, colorTag ->
                onAddAccount(name, balance, colorTag)
                showAddDialog = false
            }
        )
    }

    accountToEdit?.let { account ->
        EditRealAccountDialog(
            existingAccount = account,
            onDismiss = { accountToEdit = null },
            onConfirm = { name, balance, colorTag ->
                onUpdateAccount(account.id, name, balance, colorTag)
                accountToEdit = null
            }
        )
    }

    accountToDelete?.let { account ->
        ConfirmDeleteDialog(
            itemTitle = account.name,
            onDismiss = { accountToDelete = null },
            onConfirm = {
                onDeleteAccount(account)
                accountToDelete = null
            }
        )
    }
}
