package com.example.diabetesapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diabetesapp.data.models.InsulinType
import com.example.diabetesapp.data.repository.BolusSettingsRepository
import com.example.diabetesapp.ui.components.ExpandableSettingsCard
import com.example.diabetesapp.viewmodel.BolusSettingsViewModel
import com.example.diabetesapp.viewmodel.BolusSettingsViewModelFactory
import com.example.diabetesapp.viewmodel.ExpandableCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BolusSettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { BolusSettingsRepository.getInstance(context) }
    val viewModel: BolusSettingsViewModel = viewModel(
        factory = BolusSettingsViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF00897B)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bolus Calculator Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Content Area with ScrollView
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: General Configuration
            val generalValueText = if (!viewModel.isCardExpanded(ExpandableCard.GENERAL)) {
                "${settings.insulinType.displayName}, ${settings.durationDisplay}"
            } else null

            ExpandableSettingsCard(
                title = "General",
                valueText = generalValueText,
                isExpanded = viewModel.isCardExpanded(ExpandableCard.GENERAL),
                onToggleExpand = { viewModel.toggleCardExpansion(ExpandableCard.GENERAL) }
            ) {
                // Insulin Type Dropdown
                var insulinTypeExpanded by remember { mutableStateOf(false) }

                Column {
                    Text(
                        text = "Insulin Type",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = insulinTypeExpanded,
                        onExpandedChange = { insulinTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = settings.insulinType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = insulinTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00897B),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = insulinTypeExpanded,
                            onDismissRequest = { insulinTypeExpanded = false }
                        ) {
                            InsulinType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName) },
                                    onClick = {
                                        viewModel.updateInsulinType(type)
                                        insulinTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Duration of Action - Number Input (Decimal)
                var durationText by remember(settings.durationOfAction) {
                    mutableStateOf(settings.durationOfAction.toString())
                }

                Column {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = {
                            durationText = it
                            viewModel.updateDurationOfAction(it)
                        },
                        label = { Text("Duration (hours)") },
                        placeholder = { Text("e.g., 4.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00897B),
                            focusedLabelColor = Color(0xFF00897B),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        supportingText = {
                            Text(
                                text = "Time until insulin is fully gone (e.g., 4.0).",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    )
                }
            }

            // Card 2: Insulin-to-Carb Ratio (ICR) - Simple/Advanced Mode
            ExpandableSettingsCard(
                title = "Insulin to Carb Ratio",
                valueText = if (!viewModel.isCardExpanded(ExpandableCard.ICR)) settings.icrSummary else null,
                isExpanded = viewModel.isCardExpanded(ExpandableCard.ICR),
                onToggleExpand = { viewModel.toggleCardExpansion(ExpandableCard.ICR) }
            ) {
                // Global Input (Simple Mode)
                var globalIcrText by remember(settings.icrMorning) {
                    mutableStateOf(settings.icrMorning.toInt().toString())
                }

                AnimatedVisibility(
                    visible = !uiState.icrTimeDependent,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        OutlinedTextField(
                            value = globalIcrText,
                            onValueChange = {
                                globalIcrText = it
                                viewModel.updateGlobalIcr(it)
                            },
                            label = { Text("1 Unit covers __ g carbs") },
                            suffix = { Text("g/unit", fontSize = 12.sp, color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00897B),
                                focusedLabelColor = Color(0xFF00897B),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            supportingText = {
                                Text(
                                    text = "Standard ratio for all times of day.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Time Dependent Toggle Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Time Dependent Settings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Switch(
                        checked = uiState.icrTimeDependent,
                        onCheckedChange = { enabled ->
                            viewModel.toggleIcrTimeDependent(enabled, globalIcrText)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00897B),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }

                // 4-Segment Inputs (Advanced Mode)
                AnimatedVisibility(
                    visible = uiState.icrTimeDependent,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Morning (06-11)
                        var icrMorningText by remember(settings.icrMorning) {
                            mutableStateOf(settings.icrMorning.toInt().toString())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Morning",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "06:00 - 11:00",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            OutlinedTextField(
                                value = icrMorningText,
                                onValueChange = {
                                    icrMorningText = it
                                    viewModel.updateIcrMorning(it)
                                },
                                suffix = { Text("g/unit", fontSize = 11.sp, color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00897B),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                singleLine = true
                            )
                        }

                        // Noon (11-16)
                        var icrNoonText by remember(settings.icrNoon) {
                            mutableStateOf(settings.icrNoon.toInt().toString())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Noon",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "11:00 - 16:00",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            OutlinedTextField(
                                value = icrNoonText,
                                onValueChange = {
                                    icrNoonText = it
                                    viewModel.updateIcrNoon(it)
                                },
                                suffix = { Text("g/unit", fontSize = 11.sp, color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00897B),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                singleLine = true
                            )
                        }

                        // Evening (16-23)
                        var icrEveningText by remember(settings.icrEvening) {
                            mutableStateOf(settings.icrEvening.toInt().toString())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Evening",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "16:00 - 23:00",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            OutlinedTextField(
                                value = icrEveningText,
                                onValueChange = {
                                    icrEveningText = it
                                    viewModel.updateIcrEvening(it)
                                },
                                suffix = { Text("g/unit", fontSize = 11.sp, color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00897B),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                singleLine = true
                            )
                        }

                        // Night (23-06)
                        var icrNightText by remember(settings.icrNight) {
                            mutableStateOf(settings.icrNight.toInt().toString())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Night",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "23:00 - 06:00",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            OutlinedTextField(
                                value = icrNightText,
                                onValueChange = {
                                    icrNightText = it
                                    viewModel.updateIcrNight(it)
                                },
                                suffix = { Text("g/unit", fontSize = 11.sp, color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00897B),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                singleLine = true
                            )
                        }

                        Text(
                            text = "Different carb ratios for each time period.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Card 3: Sensitivity Factor (ISF) - Simple/Advanced Mode
            ExpandableSettingsCard(
                title = "Correction Factor",
                valueText = if (!viewModel.isCardExpanded(ExpandableCard.ISF)) settings.isfSummary else null,
                isExpanded = viewModel.isCardExpanded(ExpandableCard.ISF),
                onToggleExpand = { viewModel.toggleCardExpansion(ExpandableCard.ISF) }
            ) {
                // Global Input (Simple Mode)
                var globalIsfText by remember(settings.isfMorning) {
                    mutableStateOf(settings.isfMorning.toInt().toString())
                }

                AnimatedVisibility(
                    visible = !uiState.isfTimeDependent,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        OutlinedTextField(
                            value = globalIsfText,
                            onValueChange = {
                                globalIsfText = it
                                viewModel.updateGlobalIsf(it)
                            },
                            label = { Text("1 Unit lowers BG by __ mg/dL") },
                            suffix = { Text("mg/dL", fontSize = 12.sp, color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00897B),
                                focusedLabelColor = Color(0xFF00897B),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            supportingText = {
                                Text(
                                    text = "How much 1 unit lowers blood glucose for all times.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Time Dependent Toggle Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Time Dependent Settings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Switch(
                        checked = uiState.isfTimeDependent,
                        onCheckedChange = { enabled ->
                            viewModel.toggleIsfTimeDependent(enabled, globalIsfText)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00897B),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }

                // 4-Segment Inputs (Advanced Mode)
                AnimatedVisibility(
                    visible = uiState.isfTimeDependent,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Morning (06-11)
                        var isfMorningText by remember(settings.isfMorning) {
                            mutableStateOf(settings.isfMorning.toInt().toString())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Morning",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "06:00 - 11:00",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            OutlinedTextField(
                                value = isfMorningText,
                                onValueChange = {
                                    isfMorningText = it
                                    viewModel.updateIsfMorning(it)
                                },
                                suffix = { Text("mg/dL", fontSize = 11.sp, color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00897B),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                singleLine = true
                            )
                        }

                        // Noon (11-16)
                        var isfNoonText by remember(settings.isfNoon) {
                            mutableStateOf(settings.isfNoon.toInt().toString())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Noon",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "11:00 - 16:00",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            OutlinedTextField(
                                value = isfNoonText,
                                onValueChange = {
                                    isfNoonText = it
                                    viewModel.updateIsfNoon(it)
                                },
                                suffix = { Text("mg/dL", fontSize = 11.sp, color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00897B),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                singleLine = true
                            )
                        }

                        // Evening (16-23)
                        var isfEveningText by remember(settings.isfEvening) {
                            mutableStateOf(settings.isfEvening.toInt().toString())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Evening",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "16:00 - 23:00",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            OutlinedTextField(
                                value = isfEveningText,
                                onValueChange = {
                                    isfEveningText = it
                                    viewModel.updateIsfEvening(it)
                                },
                                suffix = { Text("mg/dL", fontSize = 11.sp, color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00897B),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                singleLine = true
                            )
                        }

                        // Night (23-06)
                        var isfNightText by remember(settings.isfNight) {
                            mutableStateOf(settings.isfNight.toInt().toString())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Night",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "23:00 - 06:00",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            OutlinedTextField(
                                value = isfNightText,
                                onValueChange = {
                                    isfNightText = it
                                    viewModel.updateIsfNight(it)
                                },
                                suffix = { Text("mg/dL", fontSize = 11.sp, color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00897B),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                singleLine = true
                            )
                        }

                        Text(
                            text = "Different sensitivity factors for each time period.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Card 4: Blood Glucose Target
            ExpandableSettingsCard(
                title = "Target BG",
                valueText = if (!viewModel.isCardExpanded(ExpandableCard.TARGET_BG)) settings.targetBGDisplay else null,
                isExpanded = viewModel.isCardExpanded(ExpandableCard.TARGET_BG),
                onToggleExpand = { viewModel.toggleCardExpansion(ExpandableCard.TARGET_BG) }
            ) {
                var targetText by remember(settings.targetBG) {
                    mutableStateOf(if (settings.targetBG == 0f) "100" else settings.targetBG.toInt().toString())
                }

                OutlinedTextField(
                    value = targetText,
                    onValueChange = {
                        targetText = it
                        viewModel.updateTargetBG(it)
                    },
                    label = { Text("Target Value (mg/dL)") },
                    placeholder = { Text("100") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00897B),
                        focusedLabelColor = Color(0xFF00897B),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    supportingText = {
                        Text(
                            text = "Your desired blood glucose level. Required for correction formula.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                )
            }

            // Bottom spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

