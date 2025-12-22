package com.example.diabetesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diabetesapp.data.models.BolusSettings
import com.example.diabetesapp.data.models.InsulinType
import com.example.diabetesapp.data.repository.BolusSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BolusSettingsUiState(
    val settings: BolusSettings = BolusSettings(),
    val expandedCard: ExpandableCard? = ExpandableCard.GENERAL, // Only one card expanded at a time
    val icrTimeDependent: Boolean = false, // Toggle for ICR time segments
    val isfTimeDependent: Boolean = false  // Toggle for ISF time segments
)

enum class ExpandableCard {
    GENERAL,
    ICR,
    ISF,
    TARGET_BG
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
