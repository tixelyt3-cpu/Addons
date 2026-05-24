package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AddonEntity
import com.example.data.AddonRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddonViewModel(private val repository: AddonRepository) : ViewModel() {

    // Active Section: "Inicio", "Explorar", "Publicar", "Mis Guardados"
    private val _activeSection = MutableStateFlow("Inicio")
    val activeSection: StateFlow<String> = _activeSection.asStateFlow()

    // Selected category for filtering in Explorar
    private val _selectedCategoryFilter = MutableStateFlow("Todos")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected item for current Detail View overlay / sheet
    private val _selectedDetailItem = MutableStateFlow<AddonEntity?>(null)
    val selectedDetailItem: StateFlow<AddonEntity?> = _selectedDetailItem.asStateFlow()

    // Download progression: mappings of itemId -> percentage (0 to 100) or -1 if finished or idle
    private val _downloadingProgress = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val downloadingProgress: StateFlow<Map<Int, Int>> = _downloadingProgress.asStateFlow()

    // Followed creators cache for fast query matching on UI
    val followedCreators: StateFlow<Set<String>> = repository.followedCreators
        .combine(MutableStateFlow<Unit>(Unit)) { list, _ ->
            list.map { it.creatorName }.toSet()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    // Main listing of addons
    val allAddons: StateFlow<List<AddonEntity>> = repository.allAddons
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Prepopulate if DB is empty
        viewModelScope.launch {
            // Wait shortly to verify if empty
            val initialList = repository.allAddons.first()
            if (initialList.isEmpty()) {
                prepopulateDatabase()
            }
        }
    }

    fun setActiveSection(section: String) {
        _activeSection.value = section
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectDetailItem(item: AddonEntity?) {
        _selectedDetailItem.value = item
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun triggerToast(msg: String) {
        _toastMessage.value = msg
    }

    // Toggle Actions
    fun toggleFavorite(item: AddonEntity) {
        viewModelScope.launch {
            val newFav = !item.isFavorite
            repository.toggleFavorite(item.id, newFav)
            // Keep matching detail state in sync
            val currentDetail = _selectedDetailItem.value
            if (currentDetail != null && currentDetail.id == item.id) {
                _selectedDetailItem.value = currentDetail.copy(isFavorite = newFav)
            }
            if (newFav) {
                _toastMessage.value = "¡Añadido a favoritos: ${item.title}!"
            } else {
                _toastMessage.value = "Quitado de favoritos"
            }
        }
    }

    fun toggleSaved(item: AddonEntity) {
        viewModelScope.launch {
            val newSaved = !item.isSaved
            repository.toggleSaved(item.id, newSaved)
            // Keep matching detail state in sync
            val currentDetail = _selectedDetailItem.value
            if (currentDetail != null && currentDetail.id == item.id) {
                _selectedDetailItem.value = currentDetail.copy(isSaved = newSaved)
            }
            if (newSaved) {
                _toastMessage.value = "¡Guardado: ${item.title}!"
            } else {
                _toastMessage.value = "Se quitó de guardados"
            }
        }
    }

    fun toggleFollowCreator(creatorName: String) {
        viewModelScope.launch {
            val followed = followedCreators.value
            if (followed.contains(creatorName)) {
                repository.unfollowCreator(creatorName)
                _toastMessage.value = "Dejaste de seguir a $creatorName"
            } else {
                repository.followCreator(creatorName)
                _toastMessage.value = "¡Ahora sigues a $creatorName!"
            }
        }
    }

    // Start download simulation
    fun startDownload(item: AddonEntity) {
        if (_downloadingProgress.value[item.id] != null) return // Already downloading

        viewModelScope.launch {
            _toastMessage.value = "Iniciando descarga de ${item.title}..."
            val progressMap = _downloadingProgress.value.toMutableMap()
            progressMap[item.id] = 0
            _downloadingProgress.value = progressMap

            // Loop mock progress from 0 to 100 with smooth updates
            for (progress in 10..100 step 15) {
                delay(300)
                val updateMap = _downloadingProgress.value.toMutableMap()
                updateMap[item.id] = progress
                _downloadingProgress.value = updateMap
            }
            delay(200)

            // Remove from downloading progress list and record download count increase
            repository.recordDownload(item.id)
            val finalMap = _downloadingProgress.value.toMutableMap()
            finalMap.remove(item.id)
            _downloadingProgress.value = finalMap

            // Update local detail state download count if it is currently displayed
            val currentDetail = _selectedDetailItem.value
            if (currentDetail != null && currentDetail.id == item.id) {
                _selectedDetailItem.value = currentDetail.copy(downloadsCount = currentDetail.downloadsCount + 1)
            }

            _toastMessage.value = "¡${item.title} descargado con éxito! Comprueba la carpeta de Minecraft."
        }
    }

    // Publish custom addon
    fun publishCustomAddon(
        title: String,
        category: String,
        description: String,
        creatorName: String,
        bannerType: String
    ): Boolean {
        if (title.isBlank() || description.isBlank() || creatorName.isBlank()) {
            _toastMessage.value = "Por favor, completa todos los campos requeridos."
            return false
        }

        viewModelScope.launch {
            val placeholderDownload = "https://minecraftaddons.example/user_published_${System.currentTimeMillis()}"
            val newAddon = AddonEntity(
                title = title,
                category = category,
                description = description,
                creatorName = creatorName,
                downloadUrl = placeholderDownload,
                bannerType = bannerType,
                isFavorite = false,
                isSaved = false,
                downloadsCount = 0
            )
            repository.publishAddon(newAddon)
            _toastMessage.value = "¡Publicado con éxito! '${title}' ya está disponible en la comunidad."
            _activeSection.value = "Inicio" // Return home after publish
        }
        return true
    }

    private suspend fun prepopulateDatabase() {
        val predefinedList = listOf(
            AddonEntity(
                title = "Dragon Mounts 2",
                category = "Add-on",
                description = "¡Domina y vuela sobre dragones legendarios en tu mundo de Minecraft! Este addon añade 8 tipos de dragones salvajes con habilidades mágicas, nidos y armaduras equipables.",
                creatorName = "DragonCraftCo",
                downloadUrl = "https://minecraftaddons.example/downloads/dragon_mounts_v2.mcaddon",
                bannerType = "creeper",
                downloadsCount = 4520
            ),
            AddonEntity(
                title = "Furniture Craft 3D",
                category = "Add-on",
                description = "Decora tu casa del árbol, búnker o mansión moderna con más de 120 tipos de muebles tridimensionales interactivos. ¡Todo en alta fidelidad y con texturas que combinan perfectamente!",
                creatorName = "InteriorPixel",
                downloadUrl = "https://minecraftaddons.example/downloads/furniture_3d.mcaddon",
                bannerType = "dirt",
                downloadsCount = 12450
            ),
            AddonEntity(
                title = "More Weapons v4",
                category = "Add-on",
                description = "Añade más de 55 armas personalizadas que varían desde catanas ninjas de obsidiana hasta descomunales martillos de batalla de netherite. ¡Cada arma tiene animaciones de ataque especiales!",
                creatorName = "ForgeEngineers",
                downloadUrl = "https://minecraftaddons.example/downloads/more_weapons_v4.mcaddon",
                bannerType = "sword",
                downloadsCount = 8900
            ),
            AddonEntity(
                title = "Pixel Faithful 64x",
                category = "Textura",
                description = "La textura Faithful definitiva adaptada para 64x64 píxeles. Conserva la esencia original del juego y sus colores clásicos pero con una nitidez espectacular y suavizado de bordes.",
                creatorName = "SmoothGamer",
                downloadUrl = "https://minecraftaddons.example/downloads/faithful_64.mcpack",
                bannerType = "gold",
                downloadsCount = 1540
            ),
            AddonEntity(
                title = "Bare Bones Retro",
                category = "Textura",
                description = "Lleva el estilo simplificado de los trailers de Minecraft oficiales directos a tu partida. Colores planos, vibrantes y sin ruido visual, óptimo para aumentar tus FPS en un 20%.",
                creatorName = "TrailerStyle",
                downloadUrl = "https://minecraftaddons.example/downloads/bare_bones.mcpack",
                bannerType = "dirt",
                downloadsCount = 3120
            ),
            AddonEntity(
                title = "Ender Warriors Pack",
                category = "Skin Pack",
                description = "Una colección de 15 skins premium inspiradas en la misteriosa dimensión del End. Diseño neon morado, cascos holográficos, ojos místicos brillantes y túnicas para asustar en multijugador.",
                creatorName = "VoidWalker",
                downloadUrl = "https://minecraftaddons.example/downloads/ender_warriors.mcpack",
                bannerType = "portal",
                downloadsCount = 540
            ),
            AddonEntity(
                title = "Vibrant Ultra Shaders",
                category = "Shader",
                description = "Shaders ultrarrealistas optimizados para móviles, con sombras dinámicas de alta definición, agua cristalina ondeante y reflejos de luz realistas en bloques metálicos.",
                creatorName = "OptiLite",
                downloadUrl = "https://minecraftaddons.example/downloads/vibrant_ultra.mcpack",
                bannerType = "water",
                downloadsCount = 6300
            ),
            AddonEntity(
                title = "RTX Light Shimmers",
                category = "Shader",
                description = "Lleva la simulación de trazado de rayos por software a tu dispositivo. Ofrece luces extra cálidas de antorchas, niebla matutina envolvente y refracción realista de agua dulce.",
                creatorName = "RayTracer2026",
                downloadUrl = "https://minecraftaddons.example/downloads/rtx_shaders.mcpack",
                bannerType = "redstone",
                downloadsCount = 9400
            )
        )

        for (item in predefinedList) {
            repository.publishAddon(item)
        }
    }
}

// Simple Factory for constructing our ViewModel with context DB connection
class AddonViewModelFactory(private val repository: AddonRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddonViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
