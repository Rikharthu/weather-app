package sashjakk.weather.app

import android.app.Application
import org.koin.core.context.startKoin
import sashjakk.weather.app.modules.apiModule
import sashjakk.weather.app.modules.httpModule
import sashjakk.weather.app.modules.uiModule

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(
                listOf(httpModule, apiModule, uiModule)
            )

            properties(
                mapOf("OPENAPI_BASE_URL" to "https://api.openweathermap.org/data/2.5")
            )
        }
    }
}