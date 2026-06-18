package com.gtnoo.mnemosyne.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gtnoo.mnemosyne.domain.model.DateRange
import com.gtnoo.mnemosyne.domain.service.ChartPoint
import com.gtnoo.mnemosyne.domain.service.ScatterPoint
import com.gtnoo.mnemosyne.domain.service.VisualizationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class VisualizationDashboardViewModel @Inject constructor(
    private val vizService: VisualizationService
) : ViewModel() {

    private val _calmTrend = MutableStateFlow<List<ChartPoint>>(emptyList())
    val calmTrend: StateFlow<List<ChartPoint>> = _calmTrend.asStateFlow()

    private val _happinessTrend = MutableStateFlow<List<ChartPoint>>(emptyList())
    val happinessTrend: StateFlow<List<ChartPoint>> = _happinessTrend.asStateFlow()

    private val _energyTrend = MutableStateFlow<List<ChartPoint>>(emptyList())
    val energyTrend: StateFlow<List<ChartPoint>> = _energyTrend.asStateFlow()

    private val _sleepVsCalm = MutableStateFlow<List<ScatterPoint>>(emptyList())
    val sleepVsCalm: StateFlow<List<ScatterPoint>> = _sleepVsCalm.asStateFlow()

    private val _weatherVsMood = MutableStateFlow<List<ScatterPoint>>(emptyList())
    val weatherVsMood: StateFlow<List<ScatterPoint>> = _weatherVsMood.asStateFlow()

    private val _outcomeSummary = MutableStateFlow<Map<String, Int>>(emptyMap())
    val outcomeSummary: StateFlow<Map<String, Int>> = _outcomeSummary.asStateFlow()

    private val _moodByPlace = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val moodByPlace: StateFlow<List<Pair<String, Double>>> = _moodByPlace.asStateFlow()

    private val _selectedRange = MutableStateFlow(DateRange.lastMonth())
    val selectedRange: StateFlow<DateRange> = _selectedRange.asStateFlow()

    init { loadAll() }

    fun setRange(range: DateRange) {
        _selectedRange.value = range
        loadAll()
    }

    private fun loadAll() {
        val range = _selectedRange.value
        viewModelScope.launch {
            _calmTrend.value = vizService.buildEmotionTrend("calm", range)
            _happinessTrend.value = vizService.buildEmotionTrend("happiness", range)
            _energyTrend.value = vizService.buildEmotionTrend("energy", range)
            _sleepVsCalm.value = vizService.buildSleepVsCalm(range)
            _weatherVsMood.value = vizService.buildWeatherVsMood(range)
            _outcomeSummary.value = vizService.buildOutcomeSummary(range)
            _moodByPlace.value = vizService.buildMoodByPlaceBreakdown(range)
        }
    }
}
