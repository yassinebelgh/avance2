package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IncomePositive
import com.example.ui.viewmodel.BudgetUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupExportDialog(
    uiState: BudgetUiState,
    onGenerateJson: suspend () -> String,
    onRestoreJson: suspend (String) -> Result<Int>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var jsonContent by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var showConfirmRestoreFile by remember { mutableStateOf<String?>(null) }

    // Prepare JSON
    LaunchedEffect(Unit) {
        jsonContent = onGenerateJson()
    }

    // SAF File Creator (Export to JSON file)
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val data = jsonContent ?: ""
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(data.toByteArray(Charsets.UTF_8))
                }
                statusMessage = "Fichier JSON exporté avec succès !"
                isError = false
            } catch (e: Exception) {
                statusMessage = "Erreur lors de l'enregistrement : ${e.localizedMessage}"
                isError = true
            }
        }
    }

    // SAF File Picker (Import from JSON file)
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    showConfirmRestoreFile = content
                } else {
                    statusMessage = "Le fichier sélectionné est vide."
                    isError = true
                }
            } catch (e: Exception) {
                statusMessage = "Impossible de lire le fichier : ${e.localizedMessage}"
                isError = true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sauvegarde & Export JSON", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Exportez vos données pour les conserver en lieu sûr ou changez de version sans risquer de perdre votre budget.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Current data summary badge
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Données actuelles enregistrées :",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Revenu mensuel : ${Formatters.formatCurrency(uiState.monthlyIncome, uiState.currencySymbol, false)}\n" +
                                   "• Dépenses prévues : ${uiState.plannedActions.size} éléments (${uiState.groups.size} catégories)\n" +
                                   "• Comptes bancaires / Cash : ${uiState.realAccounts.size} comptes\n" +
                                   "• À prévoir : ${uiState.previsionNotes.size} éléments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status banner if any
                statusMessage?.let { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else IncomePositive.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isError) MaterialTheme.colorScheme.error else IncomePositive,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isError) MaterialTheme.colorScheme.onErrorContainer else IncomePositive,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Text(
                    text = "1. Exporter vos données",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Action 1: Save as JSON file
                Button(
                    onClick = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        exportFileLauncher.launch("avance_budget_$timestamp.json")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_json_file_button")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enregistrer le fichier JSON")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action 2: Share JSON (Email, Drive, WhatsApp)
                    OutlinedButton(
                        onClick = {
                            val data = jsonContent ?: ""
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, data)
                                putExtra(Intent.EXTRA_TITLE, "Sauvegarde Avance Budget JSON")
                                type = "application/json"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Partager la sauvegarde JSON")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Partager", fontSize = 12.sp)
                    }

                    // Action 3: Copy to clipboard
                    OutlinedButton(
                        onClick = {
                            val data = jsonContent ?: ""
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Avance Budget JSON Backup", data)
                            clipboard.setPrimaryClip(clip)
                            statusMessage = "JSON copié dans le presse-papiers !"
                            isError = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copier", fontSize = 12.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "2. Restaurer vos données",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Action 4: Import JSON file
                OutlinedButton(
                    onClick = {
                        importFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_json_file_button")
                ) {
                    Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importer un fichier JSON")
                }

                // Action 5: Paste JSON directly
                TextButton(
                    onClick = { showPasteDialog = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ou coller le texte JSON manuellement")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )

    // Confirm restore dialog for file import
    showConfirmRestoreFile?.let { jsonToRestore ->
        AlertDialog(
            onDismissRequest = { showConfirmRestoreFile = null },
            title = { Text("Confirmer la restauration") },
            text = {
                Text("Attention : Cette action va remplacer les données actuelles de l'application par celles du fichier JSON importé. Souhaitez-vous continuer ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val result = onRestoreJson(jsonToRestore)
                            result.onSuccess { count ->
                                statusMessage = "Restauration réussie ! $count éléments restaurés."
                                isError = false
                                jsonContent = onGenerateJson()
                            }.onFailure { err ->
                                statusMessage = "Erreur de restauration : ${err.localizedMessage ?: "JSON invalide"}"
                                isError = true
                            }
                            showConfirmRestoreFile = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Oui, restaurer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestoreFile = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog for pasting raw JSON
    if (showPasteDialog) {
        var pastedText by remember { mutableStateOf("") }
        var pasteError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPasteDialog = false },
            title = { Text("Coller la sauvegarde JSON") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Collez ici le contenu texte d'une sauvegarde JSON précédemment copiée :", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = {
                            pastedText = it
                            pasteError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("{\n  \"appName\": \"Avance\", ...\n}") },
                        maxLines = 10
                    )
                    pasteError?.let { err ->
                        Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pastedText.isBlank()) {
                            pasteError = "Veuillez coller un texte JSON"
                            return@Button
                        }
                        scope.launch {
                            val result = onRestoreJson(pastedText)
                            result.onSuccess { count ->
                                statusMessage = "Restauration réussie ! $count éléments restaurés."
                                isError = false
                                showPasteDialog = false
                                jsonContent = onGenerateJson()
                            }.onFailure { err ->
                                pasteError = "Format invalide : ${err.localizedMessage ?: "Erreur inconnue"}"
                            }
                        }
                    }
                ) {
                    Text("Restaurer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
