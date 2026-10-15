package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.PrevisionNote
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EditPrevisionDialog
import com.example.ui.components.Formatters
import com.example.ui.viewmodel.BudgetUiState

@Composable
fun NotesScreen(
    uiState: BudgetUiState,
    onAddNote: (amount: Double, description: String) -> Unit,
    onUpdateNote: (id: Long, amount: Double, description: String) -> Unit,
    onToggleSettled: (PrevisionNote) -> Unit,
    onDeleteNote: (PrevisionNote) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<PrevisionNote?>(null) }
    var noteToDelete by remember { mutableStateOf<PrevisionNote?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_prevision_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter une prévision")
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
                // Spreadsheet Header Style: "Remarques et prévisions:"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notes_header_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD6DBDF) // Spreadsheet light slate grey
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
                            Icon(
                                imageVector = Icons.Default.PendingActions,
                                contentDescription = null,
                                tint = Color(0xFF1C2833),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Remarques et prévisions :",
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

            items(uiState.previsionNotes, key = { "note_${it.id}" }) { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { noteToEdit = note }
                        .testTag("prevision_item_${note.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (note.isSettled) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        } else {
                            Color(0xFFFDEBD0) // Spreadsheet warm tone
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = note.isSettled,
                                onCheckedChange = { onToggleSettled(note) },
                                modifier = Modifier.testTag("prevision_checkbox_${note.id}")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = note.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (note.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF5D4037),
                                    textDecoration = if (note.isSettled) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Text(
                                    text = if (note.isSettled) "Réglé" else "À payer prochainement",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (note.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF8D6E63)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = Formatters.formatCurrency(note.amount, uiState.currencySymbol, false),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (note.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF3E2723),
                                textDecoration = if (note.isSettled) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { noteToEdit = note },
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
                                onClick = { noteToDelete = note },
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

            if (uiState.previsionNotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucune prévision pour le moment. Cliquez sur + pour ajouter (ex: assurance, plein essence...)",
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
        EditPrevisionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, description ->
                onAddNote(amount, description)
                showAddDialog = false
            }
        )
    }

    noteToEdit?.let { note ->
        EditPrevisionDialog(
            existingNote = note,
            onDismiss = { noteToEdit = null },
            onConfirm = { amount, description ->
                onUpdateNote(note.id, amount, description)
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
