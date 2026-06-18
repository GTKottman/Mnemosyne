package com.gtnoo.mnemosyne.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.domain.model.DailyEntry
import com.gtnoo.mnemosyne.domain.model.FilterCriteria
import com.gtnoo.mnemosyne.domain.repository.EntryRepository
import com.gtnoo.mnemosyne.domain.service.EntryService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EntryHistoryViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val entryService: EntryService
) : ViewModel() {

    val allEntries: StateFlow<List<DailyEntry>> = entryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filter = MutableStateFlow(FilterCriteria())
    val filter: StateFlow<FilterCriteria> = _filter.asStateFlow()

    val filteredEntries: StateFlow<List<DailyEntry>> = combine(allEntries, _filter) { entries, criteria ->
        entries.filter { entry ->
            (criteria.startDate == null || !entry.entryDate.isBefore(criteria.startDate)) &&
            (criteria.endDate == null || !entry.entryDate.isAfter(criteria.endDate)) &&
            (criteria.personId == null || entry.interactions.any { it.person.id == criteria.personId }) &&
            (criteria.placeId == null || entry.placesVisited.any { it.id == criteria.placeId }) &&
            (criteria.tags.isEmpty() || criteria.tags.any { it in entry.tags }) &&
            (criteria.maxHappiness == null || entry.emotionData.happiness <= criteria.maxHappiness) &&
            (criteria.minConnection == null || entry.emotionData.connection >= criteria.minConnection) &&
            (!criteria.onlyDaysWithInteractions || entry.interactions.isNotEmpty()) &&
            (!criteria.onlyHighRuminationDays || entry.thoughtPatternData.ruminationLevel >= 7) &&
            (!criteria.onlyWeatherAffectedDays || entry.weatherSnapshots.isNotEmpty())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entriesByDate: StateFlow<Map<LocalDate, List<DailyEntry>>> = filteredEntries.map { entries ->
        entries.groupBy { it.entryDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun updateFilter(criteria: FilterCriteria) {
        _filter.value = criteria
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch { entryService.deleteEntry(id) }
    }
}
