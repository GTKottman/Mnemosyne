package com.gtnoo.mnemosyne.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.domain.model.*
import com.gtnoo.mnemosyne.domain.repository.EntryRepository
import com.gtnoo.mnemosyne.domain.repository.PersonRepository
import com.gtnoo.mnemosyne.domain.repository.SettingsRepository
import com.gtnoo.mnemosyne.domain.service.ImportantDateService
import com.gtnoo.mnemosyne.domain.service.PersonService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val personRepository: PersonRepository,
    private val personService: PersonService,
    private val importantDateService: ImportantDateService,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val todayEntries: StateFlow<List<DailyEntry>> = entryRepository.observeByDate(LocalDate.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentEntries: StateFlow<List<DailyEntry>> = entryRepository.observeAll()
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritePeople: StateFlow<List<Person>> = personRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _favoriteSummaries = MutableStateFlow<List<PersonSummary>>(emptyList())
    val favoriteSummaries: StateFlow<List<PersonSummary>> = _favoriteSummaries.asStateFlow()

    val upcomingDates: StateFlow<List<UpcomingImportantDate>> =
        importantDateService.observeUpcoming(30)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            favoritePeople.collect { people ->
                val range = DateRange.lastMonth()
                _favoriteSummaries.value = people.map { personService.getSummaryForPerson(it, range) }
            }
        }
    }
}
