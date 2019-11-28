package sashjakk.weather.app.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_weather_details.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel
import sashjakk.weather.app.GlideApp
import sashjakk.weather.app.R
import sashjakk.weather.app.tools.*

@ExperimentalCoroutinesApi
class WeatherDetailsFragment : Fragment() {

    private val viewModel by viewModel<WeatherDetailsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_weather_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()

        lifecycleScope.launch {
            val context = requireContext()
            val granted = parentFragmentManager
                .requestPermissionAsync(context, ACCESS_FINE_LOCATION)

            if (!granted.await()) {
                context.toast("No permission to access location")
                return@launch
            }

            handleLocationUpdates()
            handleDataRefresh()

            viewModel.fetchWeatherData()
        }
    }

    private fun setupToolbar() = with(toolbar) {
        inflateMenu(R.menu.menu_weather_details)

        setOnMenuItemClickListener {
            if (it.itemId == R.id.weather_list) {
                findNavController().navigate(R.id.details_to_list)
            }

            true
        }
    }

    private fun handleLocationUpdates() {
        viewModel.weatherData
            .observe(viewLifecycleOwner) {
                refresher.isRefreshing = false

                val context = requireContext()
                when (it) {
                    is Success -> bindWeatherData(it.value)
                    is Failure -> context.toast(it.error.message ?: "Unknown error")
                }
            }
    }

    private fun handleDataRefresh() {
        refresher.onRefresh
            .onEach { viewModel.fetchWeatherData() }
            .launchIn(lifecycleScope)
    }

    private fun bindWeatherData(data: WeatherViewData) {
        cityName.text = data.city
        date.text = data.date
        windSpeed.text = "${data.windSpeed} m/s"
        humidity.text = "${data.humidity} %"
        degrees.text = "${data.degrees.toInt()}˚C"

        GlideApp.with(this)
            .load(data.iconUrl)
            .into(weatherIcon)
    }
}
