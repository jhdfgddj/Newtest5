package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.data.model.UserPreferences
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoPrimary

@Composable
fun PersonalizedFilterBar(
    userPreferences: UserPreferences,
    isMyRoutineMode: Boolean,
    onToggleMyRoutineMode: () -> Unit,
    availableTechnologies: List<String>,
    availableSemesters: List<String>,
    onUpdatePreferences: (tech: String?, sem: String?, reg: String?) -> Unit
) {
    var showPreferenceDialog by remember { mutableStateOf(false) }

    val hasPreferencesSet = !userPreferences.selectedTechnology.isNullOrBlank() || !userPreferences.selectedSemester.isNullOrBlank()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isMyRoutineMode) IndigoPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = if (isMyRoutineMode) androidx.compose.foundation.BorderStroke(1.5.dp, IndigoPrimary) else null,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isMyRoutineMode) IndigoPrimary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "My Routine",
                        tint = if (isMyRoutineMode) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isMyRoutineMode) "My Personalized Routine" else "Show Entire Board Routine",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = if (hasPreferencesSet) {
                            val tech = userPreferences.selectedTechnology ?: "All Tech"
                            val sem = userPreferences.selectedSemester ?: "All Semesters"
                            "$tech • $sem"
                        } else {
                            "Tap gear to set your Tech & Semester"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showPreferenceDialog = true },
                    modifier = Modifier.testTag("setup_my_routine_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configure My Routine",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = {
                        if (!hasPreferencesSet && !isMyRoutineMode) {
                            showPreferenceDialog = true
                        } else {
                            onToggleMyRoutineMode()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMyRoutineMode) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isMyRoutineMode) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.testTag("toggle_my_routine_mode")
                ) {
                    Text(
                        text = if (isMyRoutineMode) "Active" else "Focus Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    if (showPreferenceDialog) {
        PreferenceSetupDialog(
            currentTech = userPreferences.selectedTechnology,
            currentSem = userPreferences.selectedSemester,
            currentReg = userPreferences.selectedRegulation,
            availableTechs = availableTechnologies,
            availableSemesters = availableSemesters,
            onDismiss = { showPreferenceDialog = false },
            onSave = { tech, sem, reg ->
                onUpdatePreferences(tech, sem, reg)
                showPreferenceDialog = false
            }
        )
    }
}

@Composable
private fun PreferenceSetupDialog(
    currentTech: String?,
    currentSem: String?,
    currentReg: String?,
    availableTechs: List<String>,
    availableSemesters: List<String>,
    onDismiss: () -> Unit,
    onSave: (tech: String?, sem: String?, reg: String?) -> Unit
) {
    var selectedTech by remember { mutableStateOf(currentTech) }
    var selectedSem by remember { mutableStateOf(currentSem) }
    var selectedReg by remember { mutableStateOf(currentReg ?: "2022 Regulation") }

    var techDropdownExpanded by remember { mutableStateOf(false) }
    var semDropdownExpanded by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Personalized Routine Setup", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select your technology and semester to filter out irrelevant exams from the entire routine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Technology Selector
                Text(text = "Your Technology / Department", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { techDropdownExpanded = true }
                        .padding(12.dp)
                ) {
                    Text(text = selectedTech ?: "Select Technology (e.g. Electrical)")
                    DropdownMenu(
                        expanded = techDropdownExpanded,
                        onDismissRequest = { techDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Technologies") },
                            onClick = {
                                selectedTech = null
                                techDropdownExpanded = false
                            }
                        )
                        availableTechs.forEach { tech ->
                            DropdownMenuItem(
                                text = { Text(tech) },
                                onClick = {
                                    selectedTech = tech
                                    techDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Semester Selector
                Text(text = "Your Current Semester", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { semDropdownExpanded = true }
                        .padding(12.dp)
                ) {
                    Text(text = selectedSem ?: "Select Semester (e.g. 4th)")
                    DropdownMenu(
                        expanded = semDropdownExpanded,
                        onDismissRequest = { semDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Semesters") },
                            onClick = {
                                selectedSem = null
                                semDropdownExpanded = false
                            }
                        )
                        availableSemesters.forEach { sem ->
                            DropdownMenuItem(
                                text = { Text(sem) },
                                onClick = {
                                    selectedSem = sem
                                    semDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedTech, selectedSem, selectedReg) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save My Routine")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        }
    )
}
