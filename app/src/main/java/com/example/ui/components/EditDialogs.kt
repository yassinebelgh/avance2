package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.PlannedAction
import com.example.data.model.PrevisionNote
import com.example.data.model.RealAccount

@Composable
fun EditIncomeDialog(
    currentIncome: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var textValue by remember { mutableStateOf(if (currentIncome == currentIncome.toLong().toDouble()) currentIncome.toLong().toString() else currentIncome.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le revenu mensuel") },
        text = {
            Column {
                Text(
                    "Définissez le montant total de vos revenus mensuels (Incomes) pour calculer les allocations et le reste disponible.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        isError = it.toDoubleOrNull() == null || (it.toDoubleOrNull() ?: 0.0) < 0
                    },
                    label = { Text("Revenu total (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("income_input_field")
                )
                if (isError) {
                    Text(
                        "Veuillez saisir un montant valide",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = textValue.toDoubleOrNull()
                    if (amount != null && amount >= 0) {
                        onConfirm(amount)
                    } else {
                        isError = true
                    }
                },
                modifier = Modifier.testTag("confirm_income_button")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun EditPlannedActionDialog(
    existingAction: PlannedAction? = null,
    defaultGroupName: String = "Mensuel",
    existingGroups: List<String> = listOf("Maroc", "Mensuel", "Basics"),
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onConfirm: (name: String, amount: Double, group: String, isSavings: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(existingAction?.name ?: "") }
    var amountText by remember {
        mutableStateOf(
            if (existingAction != null) {
                if (existingAction.amount == existingAction.amount.toLong().toDouble()) {
                    existingAction.amount.toLong().toString()
                } else {
                    existingAction.amount.toString()
                }
            } else ""
        )
    }
    var groupName by remember { mutableStateOf(existingAction?.groupName ?: defaultGroupName) }
    var isSavings by remember { mutableStateOf(existingAction?.isSavings ?: false) }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingAction == null) "Ajouter une action / dépense" else "Modifier l'action")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom (ex: Loyer, Food, Dar...)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_name_input")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Coût prévu (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_amount_input")
                )

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Catégorie (ex: Maroc, Mensuel, Basics)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_group_input")
                )

                // Quick chips for existing groups
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    existingGroups.distinct().take(4).forEach { grp ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (groupName.equals(grp, ignoreCase = true))
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { groupName = grp }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                grp,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (groupName.equals(grp, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Considérer comme Épargne", style = MaterialTheme.typography.bodyMedium)
                            Text("Comptabilisé dans l'Épargne totale", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isSavings,
                        onCheckedChange = { isSavings = it },
                        modifier = Modifier.testTag("action_savings_switch")
                    )
                }

                if (hasError) {
                    Text(
                        "Veuillez remplir le nom et un montant valide",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (existingAction != null && onDelete != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onDelete,
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("delete_action_btn")
                    ) {
                        Text("Supprimer cette dépense")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (name.isNotBlank() && amount != null && amount >= 0 && groupName.isNotBlank()) {
                        onConfirm(name.trim(), amount, groupName.trim(), isSavings)
                    } else {
                        hasError = true
                    }
                },
                modifier = Modifier.testTag("confirm_action_button")
            ) {
                Text(if (existingAction == null) "Ajouter" else "Enregistrer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun ManageCategoryDialog(
    groupName: String,
    onDismiss: () -> Unit,
    onRename: (newName: String) -> Unit,
    onDeleteGroup: () -> Unit,
    onAddNewActionToGroup: () -> Unit
) {
    var newName by remember { mutableStateOf(groupName) }
    var isEditingName by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gérer la catégorie « $groupName »") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (isEditingName) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nouveau nom de catégorie") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isEditingName = false }) {
                            Text("Annuler")
                        }
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    onRename(newName.trim())
                                    onDismiss()
                                }
                            }
                        ) {
                            Text("Renommer")
                        }
                    }
                } else {
                    Button(
                        onClick = onAddNewActionToGroup,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Ajouter une dépense dans « $groupName »")
                    }

                    OutlinedButton(
                        onClick = { isEditingName = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Renommer la catégorie")
                    }

                    OutlinedButton(
                        onClick = onDeleteGroup,
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Supprimer toute la catégorie")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (categoryName: String, firstActionName: String, amount: Double, isSavings: Boolean) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var firstActionName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isSavings by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Créer une nouvelle catégorie") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Nom de la catégorie (ex: Vacances, Projets...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = firstActionName,
                    onValueChange = { firstActionName = it },
                    label = { Text("Première dépense / output") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Montant (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Marquer comme Épargne", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isSavings, onCheckedChange = { isSavings = it })
                }

                if (hasError) {
                    Text(
                        "Veuillez remplir tous les champs avec des valeurs valides",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (categoryName.isNotBlank() && firstActionName.isNotBlank() && amount != null && amount >= 0) {
                        onConfirm(categoryName.trim(), firstActionName.trim(), amount, isSavings)
                    } else {
                        hasError = true
                    }
                }
            ) {
                Text("Créer catégorie")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun EditRealAccountDialog(
    existingAccount: RealAccount? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, balance: Double, colorTag: String) -> Unit
) {
    var name by remember { mutableStateOf(existingAccount?.name ?: "") }
    var balanceText by remember {
        mutableStateOf(
            if (existingAccount != null) {
                existingAccount.balance.toString()
            } else ""
        )
    }
    var selectedColor by remember { mutableStateOf(existingAccount?.colorTag ?: "#27AE60") }
    var hasError by remember { mutableStateOf(false) }

    val presetColors = listOf("#27AE60", "#E91E63", "#E67E22", "#3498DB", "#9B59B6", "#1ABC9C")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingAccount == null) "Ajouter un compte ou cash" else "Modifier le compte")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du compte (ex: BNP, Boursorama, Cash...)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_name_input")
                )

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Solde réel (€) (ex: -169.80 ou 93.20)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_balance_input")
                )

                Text("Couleur d'identification :", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    presetColors.forEach { hex ->
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .clickable { selectedColor = hex }
                                .padding(if (selectedColor == hex) 2.dp else 0.dp)
                        )
                    }
                }

                if (hasError) {
                    Text(
                        "Veuillez renseigner un nom et un solde numérique valide",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val balance = balanceText.toDoubleOrNull()
                    if (name.isNotBlank() && balance != null) {
                        onConfirm(name.trim(), balance, selectedColor)
                    } else {
                        hasError = true
                    }
                },
                modifier = Modifier.testTag("confirm_account_button")
            ) {
                Text(if (existingAccount == null) "Ajouter" else "Enregistrer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun EditPrevisionDialog(
    existingNote: PrevisionNote? = null,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, description: String) -> Unit
) {
    var description by remember { mutableStateOf(existingNote?.description ?: "") }
    var amountText by remember {
        mutableStateOf(
            if (existingNote != null) {
                if (existingNote.amount == existingNote.amount.toLong().toDouble()) {
                    existingNote.amount.toLong().toString()
                } else {
                    existingNote.amount.toString()
                }
            } else ""
        )
    }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingNote == null) "Ajouter une prévision / remarque" else "Modifier la prévision")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Montant (€) (ex: 90, 80)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prevision_amount_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (ex: assurance, plein essence...)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prevision_desc_input")
                )

                if (hasError) {
                    Text(
                        "Veuillez saisir un montant et une description",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && description.isNotBlank()) {
                        onConfirm(amount, description.trim())
                    } else {
                        hasError = true
                    }
                },
                modifier = Modifier.testTag("confirm_prevision_button")
            ) {
                Text(if (existingNote == null) "Ajouter" else "Enregistrer")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    itemTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer l'élément") },
        text = { Text("Êtes-vous sûr de vouloir supprimer « $itemTitle » ?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("Supprimer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun ResetDataDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Réinitialiser le modèle vierge ?") },
        text = {
            Text("Cela remettra le modèle de base avec 1 élément par tableau pour vous permettre de tout éditer depuis zéro.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_reset_data_button")
            ) {
                Text("Réinitialiser")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
