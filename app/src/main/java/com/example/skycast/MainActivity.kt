package com.example.skycast

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.skycast.data.repository.CityRepository
import com.example.skycast.ui.home.HomeScreen
import com.example.skycast.ui.home.HomeViewModel
import com.example.skycast.ui.theme.SkyCastTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var cityRepository: CityRepository

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.loadWeatherByLocation()
        } else {
            Toast.makeText(this, "需要位置权限才能自动定位", Toast.LENGTH_LONG).show()
            viewModel.onPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasLocationPermission()) {
            viewModel.loadWeatherByLocation()
        } else {
            requestLocationPermission()
        }

        setContent {
            SkyCastTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                    HomeScreen(
                        viewModel = viewModel,
                        cityRepository = cityRepository,
                    )
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        locationPermissionLauncher.launch(permissions)
    }
}
