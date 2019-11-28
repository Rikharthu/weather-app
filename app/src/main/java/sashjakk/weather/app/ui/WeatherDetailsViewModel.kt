package sashjakk.weather.app.ui

import android.location.LocationManager.GPS_PROVIDER
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import sashjakk.weather.app.api.OpenWeatherClient
import sashjakk.weather.app.api.OpenWeatherResponse
import sashjakk.weather.app.location.LocationProvider
import sashjakk.weather.app.location.toPair
import sashjakk.weather.app.tools.Failure
import sashjakk.weather.app.tools.Result
import sashjakk.weather.app.tools.Success
import java.text.SimpleDateFormat
import java.util.*

data class WeatherViewData(
    val city: String,
    val date: String,
    val degrees: Float,
    val windSpeed: Float,
    val humidity: Float,
    val iconUrl: String
)

fun OpenWeatherResponse.toWeatherViewData(): WeatherViewData {
    val dateFormatter = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())

    return WeatherViewData(
        city = cityName,
        date = dateFormatter.format(date * 1000L),
        degrees = mainData.degrees,
        windSpeed = windData.speed,
        humidity = mainData.humidity,
        iconUrl = weatherData.firstOrNull()?.icon ?: ""
    )
}

@ExperimentalCoroutinesApi
class WeatherDetailsViewModel(
    private val apiClient: OpenWeatherClient,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val location = ConflatedBroadcastChannel<Result<OpenWeatherResponse>>()

    val weatherData: LiveData<Result<WeatherViewData>> = liveData {
        location.consumeEach {
            val viewData = when (it) {
                is Success -> Success(it.value.toWeatherViewData())
                is Failure -> Failure<WeatherViewData>(it.error)
            }

            emit(viewData)
        }
    }

    fun fetchWeatherData() {
        viewModelScope.launch {
            val (latitude, longitude) = locationProvider.getLastKnownLocation(GPS_PROVIDER)?.toPair()
                ?: run {
                    location.send(Failure("No location data available"))
                    return@launch
                }

            location.send(
                apiClient.getWeatherData(latitude, longitude)
            )
        }
    }

    fun fetchWeatherData(city: String? = null) {
        viewModelScope.launch {
            val target = city
                ?: run {
                    location.send(Failure("No location data available"))
                    return@launch
                }

            location.send(
                apiClient.getWeatherData(target)
            )
        }
    }

}
