package ru.kazan.itis.bikmukhametov.network.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.kazan.itis.bikmukhametov.network.space.api.SpaceProvider

actual fun platformModules(): List<Module> = listOf(
    module {
        single<SpaceProvider> { IosSpaceProvider() }
    }
)

private class IosSpaceProvider : SpaceProvider {
    private val _cabinet = MutableStateFlow<String?>(null)
    private val _project = MutableStateFlow<String?>(null)

    override val cabinet: StateFlow<String?> get() = _cabinet
    override val project: StateFlow<String?> get() = _project

    override suspend fun setSpace(cabinet: String, project: String) {
        _cabinet.value = cabinet
        _project.value = project
    }

    override suspend fun getPersistedProjectId(): String? = _project.value
    override suspend fun getPersistedCabinetId(): String? = _cabinet.value
}
