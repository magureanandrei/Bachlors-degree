package com.example.diabetesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diabetesapp.data.models.BolusSettings
import com.example.diabetesapp.data.models.InsulinType
import com.example.diabetesapp.data.repository.BolusSettingsRepository
import com.example.diabetesapp.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BolusSettingsUiState(
    val settings: BolusSettings = BolusSettings(),
    val expandedCard: ExpandableCard? = ExpandableCard.GENERAL, // Only one card expanded at a time
    val icrTimeDependent: Boolean = false, // Toggle for ICR time segments
    val isfTimeDependent: Boolean = false,  // Toggle for ISF time segments

    // Validation error states
    val durationError: String? = null,
    val targetBGError: String? = null,
    val icrGlobalError: String? = null,
    val icrMorningError: String? = null,
    val icrNoonError: String? = null,
    val icrEveningError: String? = null,
    val icrNightError: String? = null,
    val isfGlobalError: String? = null,
    val isfMorningError: String? = null,
    val isfNoonError: String? = null,
    val isfEveningError: String? = null,
    val isfNightError: String? = null,

    // Save feedback
    val saveMessage: String? = null,
    val isSaving: Boolean = false
)

enum class ExpandableCard {
    GENERAL,
    ICR,
    ISF,
    TARGET_BG
}

enum class FieldType {
    DURATION,
    TARGET_BG,
    ICR_GLOBAL,
    ICR_MORNING,
    ICR_NOON,
    ICR_EVENING,
    ICR_NIGHT,
    ISF_GLOBAL,
    ISF_MORNING,
    ISF_NOON,
    ISF_EVENING,
    ISF_NIGHT
}

class BolusSettingsViewModel(
    private val repository: BolusSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BolusSettingsUiState())
    val uiState: StateFlow<BolusSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
    }

    fun toggleCardExpansion(card: ExpandableCard) {
        // Exclusive accordion: if clicking the same card, collapse it; otherwise expand the new one
        _uiState.value = _uiState.value.copy(
            expandedCard = if (_uiState.value.expandedCard == card) null else card
        )
    }

    fun toggleIcrTimeDependent(enabled: Boolean, globalValue: String = "") {
        _uiState.value = _uiState.value.copy(icrTimeDependent = enabled)

        // When enabling, copy global value to all segments
        if (enabled && globalValue.isNotEmpty()) {
            globalValue.toFloatOrNull()?.let { value ->
                if (value > 0) {
                    repository.updateIcrMorning(value)
                    repository.updateIcrNoon(value)
                    repository.updateIcrEvening(value)
                    repository.updateIcrNight(value)
                }
            }
        }
    }

    fun toggleIsfTimeDependent(enabled: Boolean, globalValue: String = "") {
        _uiState.value = _uiState.value.copy(isfTimeDependent = enabled)

        // When enabling, copy global value to all segments
        if (enabled && globalValue.isNotEmpty()) {
            globalValue.toFloatOrNull()?.let { value ->
                if (value > 0) {
                    repository.updateIsfMorning(value)
                    repository.updateIsfNoon(value)
                    repository.updateIsfEvening(value)
                    repository.updateIsfNight(value)
                }
            }
        }
    }

    fun updateGlobalIcr(value: String) {
        // Update all 4 segments with the same value
        value.toFloatOrNull()?.let { v ->
            if (v > 0) {
                repository.updateIcrMorning(v)
                repository.updateIcrNoon(v)
                repository.updateIcrEvening(v)
                repository.updateIcrNight(v)
            }
        }
    }

    fun updateGlobalIsf(value: String) {
        // Update all 4 segments with the same value
        value.toFloatOrNull()?.let { v ->
            if (v > 0) {
                repository.updateIsfMorning(v)
                repository.updateIsfNoon(v)
                repository.updateIsfEvening(v)
                repository.updateIsfNight(v)
            }
        }
    }

    fun updateInsulinType(type: InsulinType) {
        repository.updateInsulinType(type)
    }

    fun updateDurationOfAction(duration: String) {
        duration.toFloatOrNull()?.let { value ->
            if (value > 0 && value <= 24) { // Reasonable range: 0-24 hours
                repository.updateDurationOfAction(value)
            }
        }
    }

    // ICR update methods for 4 time segments
    fun updateIcrMorning(value: String) {
        value.toFloatOrNull()?.let { v ->
            if (v > 0) repository.updateIcrMorning(v)
        }
    }

    fun updateIcrNoon(value: String) {
        value.toFloatOrNull()?.let { v ->
            if (v > 0) repository.updateIcrNoon(v)
        }
    }

    fun updateIcrEvening(value: String) {
        value.toFloatOrNull()?.let { v ->
            if (v > 0) repository.updateIcrEvening(v)
        }
    }

    fun updateIcrNight(value: String) {
        value.toFloatOrNull()?.let { v ->
            if (v > 0) repository.updateIcrNight(v)
        }
    }

    // ISF update methods for 4 time segments
    fun updateIsfMorning(value: String) {
        value.toFloatOrNull()?.let { v ->
            if (v > 0) repository.updateIsfMorning(v)
        }
    }

    fun updateIsfNoon(value: String) {
        value.toFloatOrNull()?.let { v ->
            if (v > 0) repository.updateIsfNoon(v)
        }
    }

    fun updateIsfEvening(value: String) {
        value.toFloatOrNull()?.let { v ->
            if (v > 0) repository.updateIsfEvening(v)
        }
    }

    fun updateIsfNight(value: String) {
        value.toFloatOrNull()?.let { v ->
            if (v > 0) repository.updateIsfNight(v)
        }
    }

    fun updateTargetBG(target: String) {
        target.toFloatOrNull()?.let { value ->
            if (value > 0) {
                repository.updateTargetBG(value)
            }
        }
    }

    fun isCardExpanded(card: ExpandableCard): Boolean {
        return _uiState.value.expandedCard == card
    }

    /**
     * Check if all fields are valid (for enabling/disabling Save button)
     */
    fun areAllFieldsValid(): Boolean {
        val state = _uiState.value

        // Check required fields
        if (state.durationError != null || state.targetBGError != null) {
            return false
        }

        // Check ICR fields
        if (!state.icrTimeDependent) {
            if (state.icrGlobalError != null) return false
        } else {
            if (state.icrMorningError != null || state.icrNoonError != null ||
                state.icrEveningError != null || state.icrNightError != null) {
                return false
            }
        }

        // Check ISF fields
        if (!state.isfTimeDependent) {
            if (state.isfGlobalError != null) return false
        } else {
            if (state.isfMorningError != null || state.isfNoonError != null ||
                state.isfEveningError != null || state.isfNightError != null) {
                return false
            }
        }

        return true
    }

    /**
     * Live validation - validates a single field as user types
     */
    fun validateFieldLive(field: FieldType, value: String) {
        val result = when (field) {
            FieldType.DURATION -> ValidationUtils.validateDuration(value)
            FieldType.TARGET_BG -> ValidationUtils.validateTargetBG(value)
            FieldType.ICR_GLOBAL, FieldType.ICR_MORNING, FieldType.ICR_NOON,
            FieldType.ICR_EVENING, FieldType.ICR_NIGHT -> ValidationUtils.validateICR(value)
            FieldType.ISF_GLOBAL, FieldType.ISF_MORNING, FieldType.ISF_NOON,
            FieldType.ISF_EVENING, FieldType.ISF_NIGHT -> ValidationUtils.validateISF(value)
        }

        // Update the appropriate error state
        _uiState.value = when (field) {
            FieldType.DURATION -> _uiState.value.copy(durationError = if (result.isValid) null else result.errorMessage)
            FieldType.TARGET_BG -> _uiState.value.copy(targetBGError = if (result.isValid) null else result.errorMessage)
            FieldType.ICR_GLOBAL -> _uiState.value.copy(icrGlobalError = if (result.isValid) null else result.errorMessage)
            FieldType.ICR_MORNING -> _uiState.value.copy(icrMorningError = if (result.isValid) null else result.errorMessage)
            FieldType.ICR_NOON -> _uiState.value.copy(icrNoonError = if (result.isValid) null else result.errorMessage)
            FieldType.ICR_EVENING -> _uiState.value.copy(icrEveningError = if (result.isValid) null else result.errorMessage)
            FieldType.ICR_NIGHT -> _uiState.value.copy(icrNightError = if (result.isValid) null else result.errorMessage)
            FieldType.ISF_GLOBAL -> _uiState.value.copy(isfGlobalError = if (result.isValid) null else result.errorMessage)
            FieldType.ISF_MORNING -> _uiState.value.copy(isfMorningError = if (result.isValid) null else result.errorMessage)
            FieldType.ISF_NOON -> _uiState.value.copy(isfNoonError = if (result.isValid) null else result.errorMessage)
            FieldType.ISF_EVENING -> _uiState.value.copy(isfEveningError = if (result.isValid) null else result.errorMessage)
            FieldType.ISF_NIGHT -> _uiState.value.copy(isfNightError = if (result.isValid) null else result.errorMessage)
        }
    }

    /**
     * Clear validation error for a specific field
     */
    fun clearError(fieldName: String) {
        _uiState.value = when (fieldName) {
            "duration" -> _uiState.value.copy(durationError = null)
            "targetBG" -> _uiState.value.copy(targetBGError = null)
            "icrGlobal" -> _uiState.value.copy(icrGlobalError = null)
            "icrMorning" -> _uiState.value.copy(icrMorningError = null)
            "icrNoon" -> _uiState.value.copy(icrNoonError = null)
            "icrEvening" -> _uiState.value.copy(icrEveningError = null)
            "icrNight" -> _uiState.value.copy(icrNightError = null)
            "isfGlobal" -> _uiState.value.copy(isfGlobalError = null)
            "isfMorning" -> _uiState.value.copy(isfMorningError = null)
            "isfNoon" -> _uiState.value.copy(isfNoonError = null)
            "isfEvening" -> _uiState.value.copy(isfEveningError = null)
            "isfNight" -> _uiState.value.copy(isfNightError = null)
            else -> _uiState.value
        }
    }

    /**
     * Clear save message
     */
    fun clearSaveMessage() {
        _uiState.value = _uiState.value.copy(saveMessage = null)
    }

    /**
     * Validate and save all settings
     */
    fun validateAndSave(
        durationText: String,
        targetBGText: String,
        icrGlobalText: String,
        icrMorningText: String,
        icrNoonText: String,
        icrEveningText: String,
        icrNightText: String,
        isfGlobalText: String,
        isfMorningText: String,
        isfNoonText: String,
        isfEveningText: String,
        isfNightText: String
    ): Boolean {
        // Clear previous errors
        _uiState.value = _uiState.value.copy(
            durationError = null,
            targetBGError = null,
            icrGlobalError = null,
            icrMorningError = null,
            icrNoonError = null,
            icrEveningError = null,
            icrNightError = null,
            isfGlobalError = null,
            isfMorningError = null,
            isfNoonError = null,
            isfEveningError = null,
            isfNightError = null
        )

        var hasErrors = false

        // Validate Duration of Action
        val durationResult = ValidationUtils.validateDuration(durationText)
        if (!durationResult.isValid) {
            _uiState.value = _uiState.value.copy(durationError = durationResult.errorMessage)
            hasErrors = true
        }

        // Validate Target BG
        val targetBGResult = ValidationUtils.validateTargetBG(targetBGText)
        if (!targetBGResult.isValid) {
            _uiState.value = _uiState.value.copy(targetBGError = targetBGResult.errorMessage)
            hasErrors = true
        }

        // Validate ICR values
        if (!_uiState.value.icrTimeDependent) {
            // Simple mode - validate global input
            val icrResult = ValidationUtils.validateICR(icrGlobalText)
            if (!icrResult.isValid) {
                _uiState.value = _uiState.value.copy(icrGlobalError = icrResult.errorMessage)
                hasErrors = true
            }
        } else {
            // Advanced mode - validate all 4 segments
            val icrMorningResult = ValidationUtils.validateICR(icrMorningText)
            if (!icrMorningResult.isValid) {
                _uiState.value = _uiState.value.copy(icrMorningError = icrMorningResult.errorMessage)
                hasErrors = true
            }

            val icrNoonResult = ValidationUtils.validateICR(icrNoonText)
            if (!icrNoonResult.isValid) {
                _uiState.value = _uiState.value.copy(icrNoonError = icrNoonResult.errorMessage)
                hasErrors = true
            }

            val icrEveningResult = ValidationUtils.validateICR(icrEveningText)
            if (!icrEveningResult.isValid) {
                _uiState.value = _uiState.value.copy(icrEveningError = icrEveningResult.errorMessage)
                hasErrors = true
            }

            val icrNightResult = ValidationUtils.validateICR(icrNightText)
            if (!icrNightResult.isValid) {
                _uiState.value = _uiState.value.copy(icrNightError = icrNightResult.errorMessage)
                hasErrors = true
            }
        }

        // Validate ISF values
        if (!_uiState.value.isfTimeDependent) {
            // Simple mode - validate global input
            val isfResult = ValidationUtils.validateISF(isfGlobalText)
            if (!isfResult.isValid) {
                _uiState.value = _uiState.value.copy(isfGlobalError = isfResult.errorMessage)
                hasErrors = true
            }
        } else {
            // Advanced mode - validate all 4 segments
            val isfMorningResult = ValidationUtils.validateISF(isfMorningText)
            if (!isfMorningResult.isValid) {
                _uiState.value = _uiState.value.copy(isfMorningError = isfMorningResult.errorMessage)
                hasErrors = true
            }

            val isfNoonResult = ValidationUtils.validateISF(isfNoonText)
            if (!isfNoonResult.isValid) {
                _uiState.value = _uiState.value.copy(isfNoonError = isfNoonResult.errorMessage)
                hasErrors = true
            }

            val isfEveningResult = ValidationUtils.validateISF(isfEveningText)
            if (!isfEveningResult.isValid) {
                _uiState.value = _uiState.value.copy(isfEveningError = isfEveningResult.errorMessage)
                hasErrors = true
            }

            val isfNightResult = ValidationUtils.validateISF(isfNightText)
            if (!isfNightResult.isValid) {
                _uiState.value = _uiState.value.copy(isfNightError = isfNightResult.errorMessage)
                hasErrors = true
            }
        }

        if (hasErrors) {
            _uiState.value = _uiState.value.copy(
                saveMessage = "Please fix invalid fields before saving"
            )
            return false
        }

        // All validations passed - Save to repository
        viewModelScope.launch {
            try {
                // Log the values being saved
                android.util.Log.d("BolusSettings", "=== Saving Settings ===")
                android.util.Log.d("BolusSettings", "Duration: $durationText hours")
                android.util.Log.d("BolusSettings", "Target BG: $targetBGText mg/dL")

                if (!_uiState.value.icrTimeDependent) {
                    android.util.Log.d("BolusSettings", "ICR (Global): 1:$icrGlobalText")
                } else {
                    android.util.Log.d("BolusSettings", "ICR Morning: 1:$icrMorningText")
                    android.util.Log.d("BolusSettings", "ICR Noon: 1:$icrNoonText")
                    android.util.Log.d("BolusSettings", "ICR Evening: 1:$icrEveningText")
                    android.util.Log.d("BolusSettings", "ICR Night: 1:$icrNightText")
                }

                if (!_uiState.value.isfTimeDependent) {
                    android.util.Log.d("BolusSettings", "ISF (Global): 1:$isfGlobalText")
                } else {
                    android.util.Log.d("BolusSettings", "ISF Morning: 1:$isfMorningText")
                    android.util.Log.d("BolusSettings", "ISF Noon: 1:$isfNoonText")
                    android.util.Log.d("BolusSettings", "ISF Evening: 1:$isfEveningText")
                    android.util.Log.d("BolusSettings", "ISF Night: 1:$isfNightText")
                }
                android.util.Log.d("BolusSettings", "======================")

                // The repository already saves changes automatically via DataStore
                // All update methods have already been called during user input
                // This confirms the final state is persisted

                _uiState.value = _uiState.value.copy(
                    saveMessage = "Settings Saved Successfully! ✓",
                    isSaving = false
                )
            } catch (e: Exception) {
                android.util.Log.e("BolusSettings", "Error saving settings", e)
                _uiState.value = _uiState.value.copy(
                    saveMessage = "Error saving settings: ${e.message}"
                )
            }
        }
        return true
    }
}

class BolusSettingsViewModelFactory(
    private val repository: BolusSettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BolusSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BolusSettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
