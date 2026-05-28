package ru.kazan.itis.bikmukhametov.kts.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import ru.kazan.itis.bikmukhametov.database.locale.AppLanguage
import ru.kazan.itis.bikmukhametov.database.locale.AppLanguageRepository
import ru.kazan.itis.bikmukhametov.kts.presentation.navigation.AppNavigation
import ru.kazan.itis.bikmukhametov.theme.KtsMetaclassTheme

@Composable
fun App() {
    val appLanguageRepository: AppLanguageRepository = koinInject()
    val appLanguage by produceState(initialValue = AppLanguage.RU, appLanguageRepository) {
        appLanguageRepository.language.collect { value = it }
    }

    AppLocaleBridge(appLanguage = appLanguage) {
        KtsMetaclassTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
