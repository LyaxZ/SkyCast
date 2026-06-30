package com.example.skycast.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycast.data.location.LocationClient
import com.example.skycast.data.model.CombinedWeather
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
    val combinedWeather: CombinedWeather? = null,
    val errorMessage: String? = null,
    /** 定位失败 / 权限拒绝 → 弹出城市选择器 */
    val needManualInput: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationClient: LocationClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** 定位 + 请求天气（仅在权限已授予时调用） */
    fun loadWeatherByLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, needManualInput = false) }
            val result = locationClient.getCurrentLocation()
            result.fold(
                onSuccess = { location ->
                    loadCombinedWeather(
                        province = location.province,
                        city = location.city,
                        displayPlace = "${location.province} ${location.city}",
                    )
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "定位失败：${e.message}，请选择城市",
                            needManualInput = true,
                        )
                    }
                },
            )
        }
    }

    /** 权限拒绝时，切换到城市选择器模式 */
    fun onPermissionDenied() {
        _uiState.update {
            it.copy(
                isLoading = false,
                needManualInput = true,
                errorMessage = "位置权限被拒绝，请选择城市",
            )
        }
    }

    /** 从城市选择器选择后调用 */
    fun selectCity(province: String, city: String, district: String? = null) {
        val displayPlace = if (district != null) "$province $city $district" else "$province $city"
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, needManualInput = false) }
            loadCombinedWeather(
                province = province,
                city = city,
                district = district,
                displayPlace = displayPlace,
            )
        }
    }

    private suspend fun loadCombinedWeather(
        province: String,
        city: String,
        district: String? = null,
        displayPlace: String,
    ) {
        val result = weatherRepository.getCombinedWeather(
            province = province,
            city = city,
            district = district,
            displayPlace = displayPlace,
        )
        result.fold(
            onSuccess = { combined ->
                _uiState.update {
                    it.copy(isLoading = false, combinedWeather = combined)
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
