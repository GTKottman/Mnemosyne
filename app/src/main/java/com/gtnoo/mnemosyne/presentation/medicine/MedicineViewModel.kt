package com.gtnoo.mnemosyne.presentation.medicine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.domain.model.Medicine
import com.gtnoo.mnemosyne.domain.model.MedicineBottle
import com.gtnoo.mnemosyne.domain.model.MedicineDose
import com.gtnoo.mnemosyne.domain.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MedicineWithBottle(
    val medicine: Medicine,
    val currentBottle: MedicineBottle?
)

// ---- List ViewModel ----

@HiltViewModel
class MedicineListViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    val medicinesWithBottles: StateFlow<List<MedicineWithBottle>> =
        medicineRepository.observeAll()
            .map { medicines ->
                medicines.map { medicine ->
                    MedicineWithBottle(
                        medicine = medicine,
                        currentBottle = medicineRepository.getCurrentBottle(medicine.id)
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleActive(medicine: Medicine) {
        viewModelScope.launch {
            medicineRepository.update(medicine.copy(isActive = !medicine.isActive))
        }
    }

    fun deleteMedicine(id: String) {
        viewModelScope.launch { medicineRepository.delete(id) }
    }
}

// ---- Add ViewModel ----

@HiltViewModel
class AddMedicineViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _mgPerPill = MutableStateFlow("")
    val mgPerPill: StateFlow<String> = _mgPerPill.asStateFlow()

    private val _totalPills = MutableStateFlow("")
    val totalPills: StateFlow<String> = _totalPills.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var existingMedicine: Medicine? = null
    private var initialized = false

    val isEditing: Boolean get() = existingMedicine != null

    fun updateName(value: String) { _name.value = value }
    fun updateNotes(value: String) { _notes.value = value }
    fun updateMgPerPill(value: String) { _mgPerPill.value = value }
    fun updateTotalPills(value: String) { _totalPills.value = value }
    fun clearError() { _error.value = null }

    fun loadMedicine(medicineId: String) {
        if (initialized) return
        initialized = true
        viewModelScope.launch {
            val medicine = medicineRepository.getById(medicineId) ?: return@launch
            existingMedicine = medicine
            _name.value = medicine.name
            _notes.value = medicine.notes
        }
    }

    fun save() {
        if (_name.value.isBlank()) { _error.value = "Name is required"; return }

        existingMedicine?.let { medicine ->
            viewModelScope.launch {
                medicineRepository.update(
                    medicine.copy(
                        name = _name.value.trim(),
                        notes = _notes.value.trim()
                    )
                )
                _saved.value = true
            }
            return
        }

        val mg = _mgPerPill.value.toIntOrNull()
        val pills = _totalPills.value.toIntOrNull()
        if (mg == null || mg <= 0) { _error.value = "Enter a valid mg per pill"; return }
        if (pills == null || pills <= 0) { _error.value = "Enter a valid pill count"; return }

        viewModelScope.launch {
            val medicine = Medicine(name = _name.value.trim(), notes = _notes.value.trim())
            medicineRepository.save(medicine)
            medicineRepository.saveBottle(
                MedicineBottle(
                    medicineId = medicine.id,
                    mgPerPill = mg,
                    pillsTotal = pills,
                    pillsRemaining = pills,
                    openedDate = LocalDate.now(),
                    isCurrentBottle = true
                )
            )
            _saved.value = true
        }
    }
}

// ---- Detail ViewModel ----

@HiltViewModel
class MedicineDetailViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    private val _medicine = MutableStateFlow<Medicine?>(null)
    val medicine: StateFlow<Medicine?> = _medicine.asStateFlow()

    private val _currentBottle = MutableStateFlow<MedicineBottle?>(null)
    val currentBottle: StateFlow<MedicineBottle?> = _currentBottle.asStateFlow()

    private val _bottleHistory = MutableStateFlow<List<MedicineBottle>>(emptyList())
    val bottleHistory: StateFlow<List<MedicineBottle>> = _bottleHistory.asStateFlow()

    private val _doseHistory = MutableStateFlow<List<MedicineDose>>(emptyList())
    val doseHistory: StateFlow<List<MedicineDose>> = _doseHistory.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var bottleObserveJob: kotlinx.coroutines.Job? = null
    private var medicineObserveJob: kotlinx.coroutines.Job? = null

    fun load(medicineId: String) {
        if (medicineObserveJob != null) return
        medicineObserveJob = viewModelScope.launch {
            medicineRepository.observeById(medicineId).collect { _medicine.value = it }
        }
        viewModelScope.launch {
            _doseHistory.value = medicineRepository.getAllDosesForMedicine(medicineId)
        }
        bottleObserveJob = viewModelScope.launch {
            medicineRepository.observeBottlesForMedicine(medicineId).collect {
                _bottleHistory.value = it
                _currentBottle.value = it.firstOrNull { b -> b.isCurrentBottle }
            }
        }
    }

    fun refill(newPillCount: Int) {
        val med = _medicine.value ?: return
        val existing = _currentBottle.value ?: return
        viewModelScope.launch {
            medicineRepository.retireCurrentBottle(med.id)
            medicineRepository.saveBottle(
                MedicineBottle(
                    medicineId = med.id,
                    mgPerPill = existing.mgPerPill,
                    pillsTotal = newPillCount,
                    pillsRemaining = newPillCount,
                    openedDate = LocalDate.now(),
                    isCurrentBottle = true
                )
            )
        }
    }

    fun changeDosage(newMgPerPill: Int, newPillCount: Int) {
        val med = _medicine.value ?: return
        viewModelScope.launch {
            medicineRepository.retireCurrentBottle(med.id)
            medicineRepository.saveBottle(
                MedicineBottle(
                    medicineId = med.id,
                    mgPerPill = newMgPerPill,
                    pillsTotal = newPillCount,
                    pillsRemaining = newPillCount,
                    openedDate = LocalDate.now(),
                    isCurrentBottle = true
                )
            )
        }
    }

    fun toggleActive() {
        val med = _medicine.value ?: return
        viewModelScope.launch {
            val updated = med.copy(isActive = !med.isActive)
            medicineRepository.update(updated)
            _medicine.value = updated
        }
    }

    fun clearError() { _error.value = null }
}
