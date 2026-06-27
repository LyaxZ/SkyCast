package com.example.skycast.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycast.data.location.LocationClient
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val weather: WeatherResponse? = null,
    val locationName: String = "",
    val errorMessage: String? = null,
    /** 定位失败 → 需要用户手动输入城市名 */
    val needManualInput: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationClient: LocationClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadWeatherByLocation()
    }

    /** 定位 + 请求天气 */
    fun loadWeatherByLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, needManualInput = false) }
            val result = locationClient.getCurrentLocation()
            result.fold(
                onSuccess = { location ->
                    fetchWeather(
                        sheng = location.province,
                        place = location.city,
                        locationName = "${location.province} ${location.city}",
                    )
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "定位失败：${e.message}，请手动输入城市名",
                            needManualInput = true,
                        )
                    }
                },
            )
        }
    }

    /** 权限拒绝时，切换到手动输入模式 */
    fun onPermissionDenied() {
        _uiState.update {
            it.copy(
                isLoading = false,
                needManualInput = true,
                errorMessage = "位置权限被拒绝，请手动输入城市名",
            )
        }
    }

    /** 手动输入城市名查询 */
    fun searchByPlace(place: String, sheng: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            fetchWeather(sheng = sheng, place = place, locationName = place)
        }
    }

    private suspend fun fetchWeather(sheng: String, place: String, locationName: String) {
        weatherRepository.getWeather(sheng = sheng, place = place).collect { result ->
            result.fold(
                onSuccess = { weather ->
                    _uiState.update {
                        it.copy(isLoading = false, weather = weather, locationName = locationName)
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "请求天气失败：${e.message}")
                    }
                },
            )
        }
    }
}
