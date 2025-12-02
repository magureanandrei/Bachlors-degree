package com.example.diabetesapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diabetesapp.viewmodel.CalculateBolusViewModel
import com.example.diabetesapp.viewmodel.InputMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculateBolusScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: CalculateBolusViewModel = viewModel()
) {
    val inputState by viewModel.inputState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.offset(y = (-2).dp)
                ) {
                    Text(
                        text = "Calculate Bolus",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }
            },
            navigationIcon = {
                Box(
                    modifier = Modifier.offset(y = (-2).dp)
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            modifier = Modifier.height(56.dp)
        )

        // Screen Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Time Display
            TimeDisplayCard(
                currentDate = inputState.currentDate,
                currentTime = inputState.currentTime,
                onDateChange = { viewModel.updateDate(it) },
                onTimeChange = { viewModel.updateTime(it) }
            )

            // Mode Toggle Buttons
            ModeToggleButtons(
                selectedMode = inputState.inputMode,
                onModeSelected = { viewModel.setInputMode(it) }
            )

            // Input Fields Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Input Values",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00897B)
                    )

                    // Blood Glucose
                    InputField(
                        label = "Blood Glucose (mg/dL)",
                        value = inputState.bloodGlucose,
                        onValueChange = { viewModel.updateBloodGlucose(it) },
                        isRequired = true
                    )

                    // Carbs
                    InputField(
                        label = "Carbohydrates (g)",
                        value = inputState.carbs,
                        onValueChange = { viewModel.updateCarbs(it) },
                        isRequired = true
                    )

                    // Manual Insulin - only show in MANUAL mode
                    if (inputState.inputMode == InputMode.MANUAL) {
                        InputField(
                            label = "Manual Insulin (U)",
                            value = inputState.manualInsulin,
                            onValueChange = { viewModel.updateManualInsulin(it) },
                            isRequired = false
                        )
                    }

                    // Correction Amount
                    InputField(
                        label = "Correction Amount (U)",
                        value = inputState.correctionAmount,
                        onValueChange = { viewModel.updateCorrectionAmount(it) },
                        isRequired = true
                    )

                    // Basal Rate Excess
                    InputField(
                        label = "Basal Rate Excess (U/h)",
                        value = inputState.basalRateExcess,
                        onValueChange = { viewModel.updateBasalRateExcess(it) },
                        isRequired = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calculate Button
            Button(
                onClick = { viewModel.calculateBolus() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00897B)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Calculate Bolus",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ModeToggleButtons(
    selectedMode: InputMode,
    onModeSelected: (InputMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Manual Input Button
        Button(
            onClick = { onModeSelected(InputMode.MANUAL) },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedMode == InputMode.MANUAL) 
                    Color(0xFF00897B) 
                else 
                    Color(0xFFE0E0E0),
                contentColor = if (selectedMode == InputMode.MANUAL) 
                    Color.White 
                else 
                    Color(0xFF757575)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Manual Input",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Calculate Bolus Button
        Button(
            onClick = { onModeSelected(InputMode.CALCULATE) },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedMode == InputMode.CALCULATE) 
                    Color(0xFF00897B) 
                else 
                    Color(0xFFE0E0E0),
                contentColor = if (selectedMode == InputMode.CALCULATE) 
                    Color.White 
                else 
                    Color(0xFF757575)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Calculate Bolus",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TimeDisplayCard(
    currentDate: String,
    currentTime: String,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Date & Time",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00897B)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Field
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Date",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    OutlinedTextField(
                        value = currentDate,
                        onValueChange = onDateChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("DD/MM/YYYY") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00897B),
                            unfocusedBorderColor = Color(0xFFCCCCCC)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                // Time Field
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Time",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    OutlinedTextField(
                        value = currentTime,
                        onValueChange = onTimeChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("HH:MM") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00897B),
                            unfocusedBorderColor = Color(0xFFCCCCCC)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = if (isRequired) "$label *" else label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00897B),
                unfocusedBorderColor = Color(0xFFCCCCCC)
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

