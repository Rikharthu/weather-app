package sashjakk.weather.app.ui

import androidx.lifecycle.ViewModel
import sashjakk.weather.app.api.OpenWeatherClient

data class WeatherViewData(
    val city: String,
    val date: String,
    val degrees: Float,
    val windSpeed: Float,
    val humidity: Float,
    val iconUrl: String
)

class MainViewModel(
    private val apiClient: OpenWeatherClient
) : ViewModel() {

    suspend fun getWeatherData(latitude: Double, longitude: Double): WeatherViewData {
        val response = apiClient.getWeatherData(latitude, longitude)

        val icon = response.weatherData
            .first()
            .icon

        val iconUrl = apiClient.getIconUrl(icon)

        return WeatherViewData(
            city = response.cityName,
            date = response.date.toString(),
            degrees = response.mainData.degrees,
            windSpeed = response.windData.speed,
            humidity = response.mainData.humidity,
            iconUrl = iconUrl
        )
    }
}