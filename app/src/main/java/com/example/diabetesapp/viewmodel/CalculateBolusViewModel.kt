package com.example.diabetesapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class InputMode {
    MANUAL,
    CALCULATE
}

data class BolusInputState(
    val inputMode: InputMode = InputMode.CALCULATE,
    val currentDate: String = "",
    val currentTime: String = "",
    val bloodGlucose: String = "",
    val carbs: String = "",
    val manualInsulin: String = "",
    val correctionAmount: String = "",
    val basalRateExcess: String = "",
    val activeInsulin: String = "",
    val notes: String = "",
    val isAdvancedExpanded: Boolean = false,
    val calculatedDose: Double? = null,
    val showResult: Boolean = false,
    // Validation state
    val bloodGlucoseError: String? = null,
    val carbsError: String? = null,
    val manualInsulinError: String? = null,
    val warningMessage: String? = null,
    // Modal dialogs state
    val showAdvancedConfirmationDialog: Boolean = false,
    val showResultDialog: Boolean = false
)

class CalculateBolusViewModel : ViewModel() {
    private val _inputState = MutableStateFlow(BolusInputState())
    val inputState: StateFlow<BolusInputState> = _inputState.asStateFlow()

    init {
        updateCurrentDateTime()
    }

    private fun updateCurrentDateTime() {
        val now = LocalDateTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val currentTime = now.format(timeFormatter)
        val currentDate = now.format(dateFormatter)
        _inputState.value = _inputState.value.copy(
            currentTime = currentTime,
            currentDate = currentDate
        )
    }

    fun setInputMode(mode: InputMode) {
        _inputState.value = _inputState.value.copy(inputMode = mode)
    }

    fun updateDate(value: String) {
        _inputState.value = _inputState.value.copy(currentDate = value)
    }

    fun updateTime(value: String) {
        _inputState.value = _inputState.value.copy(currentTime = value)
    }

    fun updateBloodGlucose(value: String) {
        _inputState.value = _inputState.value.copy(
            bloodGlucose = value,
            bloodGlucoseError = null,
            warningMessage = null
        )
    }

    fun updateCarbs(value: String) {
        _inputState.value = _inputState.value.copy(
            carbs = value,
            carbsError = null,
            warningMessage = null
        )
    }

    fun updateManualInsulin(value: String) {
        _inputState.value = _inputState.value.copy(
            manualInsulin = value,
            manualInsulinError = null,
            warningMessage = null
        )
    }

    fun updateCorrectionAmount(value: String) {
        _inputState.value = _inputState.value.copy(correctionAmount = value)
    }

    fun updateBasalRateExcess(value: String) {
        _inputState.value = _inputState.value.copy(basalRateExcess = value)
    }

    fun updateActiveInsulin(value: String) {
        _inputState.value = _inputState.value.copy(activeInsulin = value)
    }

    fun updateNotes(value: String) {
        _inputState.value = _inputState.value.copy(notes = value)
    }

    fun toggleAdvancedSection() {
        _inputState.value = _inputState.value.copy(
            isAdvancedExpanded = !_inputState.value.isAdvancedExpanded
        )
    }

    fun dismissAdvancedConfirmationDialog() {
        _inputState.value = _inputState.value.copy(showAdvancedConfirmationDialog = false)
    }

    fun dismissResultDialog() {
        _inputState.value = _inputState.value.copy(showResultDialog = false)
    }

    fun proceedWithCalculation() {
        _inputState.value = _inputState.value.copy(showAdvancedConfirmationDialog = false)
        performCalculation()
    }

    fun calculateBolus() {
        val state = _inputState.value

        // Clear previous results and warnings
        _inputState.value = state.copy(
            showResult = false,
            calculatedDose = null,
            warningMessage = null,
            bloodGlucoseError = null,
            carbsError = null
        )

        // Validation: Check required fields
        var hasError = false

        if (state.bloodGlucose.isBlank()) {
            _inputState.value = _inputState.value.copy(
                bloodGlucoseError = "Blood glucose is required"
            )
            hasError = true
        }

        if (state.carbs.isBlank()) {
            _inputState.value = _inputState.value.copy(
                carbsError = "Carbohydrates is required"
            )
            hasError = true
        }

        if (hasError) {
            return
        }

        // Parse values
        val bg = state.bloodGlucose.toDoubleOrNull()
        val carbs = state.carbs.toDoubleOrNull()

        // Validation: Check numeric values
        if (bg == null) {
            _inputState.value = _inputState.value.copy(
                bloodGlucoseError = "Please enter a valid number"
            )
            return
        }

        if (carbs == null) {
            _inputState.value = _inputState.value.copy(
                carbsError = "Please enter a valid number"
            )
            return
        }

        // Safety validation: Check BG range
        if (bg < 40) {
            _inputState.value = _inputState.value.copy(
                warningMessage = "⚠️ Critical: Blood glucose is dangerously low (< 40 mg/dL). Please treat hypoglycemia immediately."
            )
            return
        }

        if (bg < 70) {
            _inputState.value = _inputState.value.copy(
                warningMessage = "⚠️ Warning: Blood glucose is low (< 70 mg/dL). Consider treating hypoglycemia before bolusing."
            )
            return
        }

        if (bg > 400) {
            _inputState.value = _inputState.value.copy(
                warningMessage = "⚠️ Warning: Blood glucose is very high (> 400 mg/dL). Please consult with your healthcare provider."
            )
            return
        }

        // Safety validation: Check carbs range
        if (carbs < 0) {
            _inputState.value = _inputState.value.copy(
                carbsError = "Carbs cannot be negative"
            )
            return
        }

        if (carbs > 300) {
            _inputState.value = _inputState.value.copy(
                warningMessage = "⚠️ Warning: Carb amount is unusually high (> 300g). Please verify your entry."
            )
            return
        }

        // Check if advanced adjustments are empty
        val hasAdvancedInputs = state.correctionAmount.isNotBlank() ||
                state.basalRateExcess.isNotBlank() ||
                state.activeInsulin.isNotBlank()

        if (!hasAdvancedInputs) {
            // Show confirmation dialog asking if they want to add advanced adjustments
            _inputState.value = _inputState.value.copy(showAdvancedConfirmationDialog = true)
        } else {
            // Proceed directly with calculation
            performCalculation()
        }
    }

    private fun performCalculation() {
        val state = _inputState.value

        val bg = state.bloodGlucose.toDoubleOrNull() ?: return
        val carbs = state.carbs.toDoubleOrNull() ?: return

        // Parse advanced fields
        val correction = state.correctionAmount.toDoubleOrNull() ?: 0.0
        val basalExcess = state.basalRateExcess.toDoubleOrNull() ?: 0.0
        val activeInsulin = state.activeInsulin.toDoubleOrNull() ?: 0.0

        // Calculation logic (customize these ratios based on user settings)
        // TODO: Replace with personalized insulin-to-carb ratio and correction factor
        val carbRatio = 15.0 // 1 unit per 15g carbs
        val correctionFactor = 50.0 // 1 unit per 50 mg/dL above target
        val targetBg = 100.0 // Target blood glucose

        val carbDose = carbs / carbRatio
        val correctionDose = if (bg > targetBg) (bg - targetBg) / correctionFactor else 0.0
        val totalDose = carbDose + correctionDose + correction + basalExcess - activeInsulin

        // Ensure dose is not negative
        val finalDose = if (totalDose > 0) totalDose else 0.0

        _inputState.value = _inputState.value.copy(
            calculatedDose = finalDose,
            showResult = true,
            showResultDialog = true
        )
    }

    fun logEntry() {
        val state = _inputState.value

        // Validation for Manual Input mode
        if (state.inputMode == InputMode.MANUAL) {
            if (state.manualInsulin.isBlank()) {
                _inputState.value = _inputState.value.copy(
                    manualInsulinError = "Insulin dose is required"
                )
                return
            }

            val dose = state.manualInsulin.toDoubleOrNull()
            if (dose == null) {
                _inputState.value = _inputState.value.copy(
                    manualInsulinError = "Please enter a valid number"
                )
                return
            }

            if (dose < 0) {
                _inputState.value = _inputState.value.copy(
                    manualInsulinError = "Dose cannot be negative"
                )
                return
            }

            if (dose > 100) {
                _inputState.value = _inputState.value.copy(
                    warningMessage = "⚠️ Warning: Insulin dose is unusually high (> 100 U). Please verify your entry."
                )
                return
            }
        }

        // Validation for Calculate mode - ensure calculation was performed
        if (state.inputMode == InputMode.CALCULATE && state.calculatedDose == null) {
            _inputState.value = _inputState.value.copy(
                warningMessage = "Please calculate dose before logging"
            )
            return
        }

        // Placeholder for logging - will be implemented later with repository/database
        println("Logging Entry:")
        println("Mode: ${state.inputMode}")
        println("Date: ${state.currentDate}")
        println("Time: ${state.currentTime}")

        if (state.inputMode == InputMode.CALCULATE) {
            println("Calculated Dose: ${state.calculatedDose} U")
            println("Blood Glucose: ${state.bloodGlucose} mg/dL")
            println("Carbs: ${state.carbs} g")
        } else {
            println("Manual Insulin: ${state.manualInsulin} U")
        }
        println("Notes: ${state.notes}")

        // Reset form after logging
        resetForm()
    }

    private fun resetForm() {
        updateCurrentDateTime()
        _inputState.value = _inputState.value.copy(
            bloodGlucose = "",
            carbs = "",
            manualInsulin = "",
            correctionAmount = "",
            basalRateExcess = "",
            activeInsulin = "",
            notes = "",
            isAdvancedExpanded = false,
            calculatedDose = null,
            showResult = false,
            bloodGlucoseError = null,
            carbsError = null,
            manualInsulinError = null,
            warningMessage = null
        )
    }
}
