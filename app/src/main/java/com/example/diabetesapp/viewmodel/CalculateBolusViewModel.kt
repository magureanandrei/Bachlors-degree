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
    val inputMode: InputMode = InputMode.MANUAL,
    val currentDate: String = "",
    val currentTime: String = "",
    val bloodGlucose: String = "",
    val carbs: String = "",
    val manualInsulin: String = "",
    val correctionAmount: String = "",
    val basalRateExcess: String = ""
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
        _inputState.value = _inputState.value.copy(bloodGlucose = value)
    }

    fun updateCarbs(value: String) {
        _inputState.value = _inputState.value.copy(carbs = value)
    }

    fun updateManualInsulin(value: String) {
        _inputState.value = _inputState.value.copy(manualInsulin = value)
    }

    fun updateCorrectionAmount(value: String) {
        _inputState.value = _inputState.value.copy(correctionAmount = value)
    }

    fun updateBasalRateExcess(value: String) {
        _inputState.value = _inputState.value.copy(basalRateExcess = value)
    }

    fun calculateBolus() {
        val state = _inputState.value
        // Placeholder function - will be implemented later
        println("Calculate Bolus called with:")
        println("Date: ${state.currentDate}")
        println("Time: ${state.currentTime}")
        println("Blood Glucose: ${state.bloodGlucose}")
        println("Carbs: ${state.carbs}")
        println("Manual Insulin: ${state.manualInsulin}")
        println("Correction Amount: ${state.correctionAmount}")
        println("Basal Rate Excess: ${state.basalRateExcess}")
    }
}

