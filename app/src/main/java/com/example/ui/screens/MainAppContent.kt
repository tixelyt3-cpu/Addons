package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AddonEntity
import com.example.ui.AddonViewModel
import com.example.ui.components.AddonBanner
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: AddonViewModel,
    modifier: Modifier = Modifier
) {
    val activeSection by viewModel.activeSection.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val selectedDetailItem by viewModel.selectedDetailItem.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe Toast Messages and display them smoothly via snackbar
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Drawing a tiny pixel block icon in top bar
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .shadow(2.dp, RoundedCornerShape(4.dp))
                        ) {
                            AddonBanner(bannerType = "dirt", modifier = Modifier.fillMaxSize())
                        }
                        Text(
                            text = "MINE ADDONS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = CreeperGreenLight
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepslateSurface,
                    titleContentColor = Color.White
                ),
                actions = {
                    if (activeSection != "Inicio") {
                        IconButton(
                            onClick = { viewModel.setActiveSection("Inicio") },
                            modifier = Modifier.testTag("action_home")
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Inicio", tint = Color.White)
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DeepslateSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                val items = listOf(
                    Triple("Inicio", Icons.Default.Home, "inicio"),
                    Triple("Explorar", Icons.Default.Search, "explorar"),
                    Triple("Publicar", Icons.Default.Add, "publicar"),
                    Triple("Mis Guardados", Icons.Default.Star, "guardados")
                )

                items.forEach { (section, icon, testTagId) ->
                    val isSelected = activeSection == section
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setActiveSection(section) },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = section,
                                tint = if (isSelected) CreeperGreenLight else TextSecondaryDark
                            )
                        },
                        label = {
                            Text(
                                text = section,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CreeperGreen,
                            unselectedIconColor = TextSecondaryDark,
                            selectedTextColor = CreeperGreenLight,
                            unselectedTextColor = TextSecondaryDark,
                            indicatorColor = IronGraySurface
                        ),
                        modifier = Modifier.testTag("nav_tab_$testTagId")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ObsidianDark)
        ) {
            // Main views based on selection
            Crossfade(
                targetState = activeSection,
                animationSpec = tween(250),
                modifier = Modifier.fillMaxSize(),
                label = "navigationState"
            ) { section ->
                when (section) {
                    "Inicio" -> InicioScreen(viewModel)
                    "Explorar" -> ExplorarScreen(viewModel)
                    "Publicar" -> PublicarScreen(viewModel)
                    "Mis Guardados" -> GuardadosScreen(viewModel)
                }
            }

            // Slide-up Detail Overlay Page (Visual Pestaña)
            AnimatedVisibility(
                visible = selectedDetailItem != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                selectedDetailItem?.let { item ->
                    AddonDetailPanel(
                        item = item,
                        viewModel = viewModel,
                        onClose = { viewModel.selectDetailItem(null) }
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// SCREEN: INICIO (HOME SCREEN WITH ADD-ONS AND TEXTURAS CATEGORIES)
// --------------------------------------------------------------------------
@Composable
fun InicioScreen(viewModel: AddonViewModel) {
    val allAddons by viewModel.allAddons.collectAsStateWithLifecycle()
    
    // As per user request: "en la pantalla principal aparezcan add-ons y texturas"
    val addonList = remember(allAddons) { allAddons.filter { it.category == "Add-on" } }
    val textureList = remember(allAddons) { allAddons.filter { it.category == "Textura" } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(CreeperGreen, Color(0xFF1B5E20))
                        )
                    )
                    .drawBehind {
                        // Drawing retro diagonal stripes to mimic minecraft sky grids
                        val pSize = 40f
                        for (x in -20..90) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(x * pSize, 0f),
                                end = Offset(x * pSize + 200f, size.height),
                                strokeWidth = 10f
                            )
                        }
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text = "¡Bienvenido, Minecrafter!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Explora y publica miles de Add-ons y texturas para elevar tu aventura pixelada.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }

        // Section: Addons
        item {
            SectionHeader(
                title = "Add-ons Destacados",
                subtitle = "Modificaciones del sistema y nuevas mecánicas",
                onSeeAll = {
                    viewModel.setCategoryFilter("Add-on")
                    viewModel.setActiveSection("Explorar")
                }
            )
        }

        if (addonList.isEmpty()) {
            item {
                EmptyListPlaceholder(categoryName = "Add-ons")
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(addonList) { item ->
                        FeaturedAddonCard(
                            item = item,
                            onClick = { viewModel.selectDetailItem(item) }
                        )
                    }
                }
            }
        }

        // Section: Texturas
        item {
            SectionHeader(
                title = "Texturas Populares",
                subtitle = "Rediseña la apariencia visual de tus entornos",
                onSeeAll = {
                    viewModel.setCategoryFilter("Textura")
                    viewModel.setActiveSection("Explorar")
                }
            )
        }

        if (textureList.isEmpty()) {
            item {
                EmptyListPlaceholder(categoryName = "Texturas")
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(textureList) { item ->
                        FeaturedAddonCard(
                            item = item,
                            onClick = { viewModel.selectDetailItem(item) }
                        )
                    }
                }
            }
        }

        // Quick Publish promo card
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = IronGraySurface),
                border = BorderStroke(1.dp, CobblestoneBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setActiveSection("Publicar") }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(CreeperGreen, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Publicar", tint = Color.Black)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¿Creaste algo genial?",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GoldIngot)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Publica tus propios add-ons, skins o texturas con la comunidad.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                        )
                    }
                    Icon(Icons.Default.PlayArrow, contentDescription = "Ir", tint = CreeperGreenLight, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --------------------------------------------------------------------------
// SCREEN: EXPLORAR (BROWSE EVERYTHING WITH FILTERS AND SEARCH)
// --------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExplorarScreen(viewModel: AddonViewModel) {
    val allAddons by viewModel.allAddons.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val filteredList = remember(allAddons, selectedFilter, searchQuery) {
        allAddons.filter { item ->
            val matchesCategory = (selectedFilter == "Todos") || (item.category == selectedFilter)
            val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.creatorName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Styled search box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Buscar add-ons, texturas, creadores...", color = TextMutedDark, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CreeperGreenLight) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Check, contentDescription = "Borrar", tint = TextSecondaryDark)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DeepslateSurface,
                unfocusedContainerColor = DeepslateSurface,
                focusedBorderColor = CreeperGreen,
                unfocusedBorderColor = CobblestoneBorder,
                focusedLabelColor = CreeperGreenLight,
                unfocusedLabelColor = TextSecondaryDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_field_explore"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal Category Filter chips
        val categories = listOf("Todos", "Add-on", "Textura", "Skin Pack", "Shader")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = selectedFilter == category
                val border = if (isSelected) BorderStroke(1.5.dp, CreeperGreen) else BorderStroke(1.dp, CobblestoneBorder)
                val bg = if (isSelected) CreeperGreen.copy(alpha = 0.15f) else DeepslateSurface
                val txtColor = if (isSelected) CreeperGreenLight else TextSecondaryDark

                Box(
                    modifier = Modifier
                        .background(bg, RoundedCornerShape(20.dp))
                        .border(border, RoundedCornerShape(20.dp))
                        .clickable { viewModel.setCategoryFilter(category) }
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                        .testTag("filter_chip_$category")
                ) {
                    Text(
                        text = category,
                        color = txtColor,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Items listing grid
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Sin resultados",
                        tint = GoldIngot,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No se encontraron elementos",
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Prueba con otra categoría o término de búsqueda.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredList) { item ->
                    ExploreItemCard(
                        item = item,
                        onClick = { viewModel.selectDetailItem(item) }
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// SCREEN: PUBLICAR (PUBLISH CUSTOM ADDON FORM)
// --------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicarScreen(viewModel: AddonViewModel) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Add-on") }
    var description by remember { mutableStateOf("") }
    var creatorName by remember { mutableStateOf("") }
    var selectedBannerTheme by remember { mutableStateOf("dirt") }

    val scrollState = rememberScrollState()

    val categories = listOf("Add-on", "Textura", "Skin Pack", "Shader")
    val bannerThemes = listOf("creeper", "sword", "dirt", "portal", "water", "gold", "redstone")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp)
            .testTag("publish_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Publica tu Creación",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Comparte tus complementos y paquetes de Minecraft con creadores de todo el mundo.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
        )

        Divider(color = CobblestoneBorder, thickness = 1.dp)

        // Title Input
        PublishFormLabel("Título del Complemento *")
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Ej: Aldeanos Mecánicos 3D", color = TextMutedDark, fontSize = 14.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DeepslateSurface,
                unfocusedContainerColor = DeepslateSurface,
                focusedBorderColor = CreeperGreen,
                unfocusedBorderColor = CobblestoneBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("publish_input_title"),
            singleLine = true
        )

        // Creator Input
        PublishFormLabel("Nombre del Creador / Autor *")
        OutlinedTextField(
            value = creatorName,
            onValueChange = { creatorName = it },
            placeholder = { Text("Ej: AlexCraft_99", color = TextMutedDark, fontSize = 14.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DeepslateSurface,
                unfocusedContainerColor = DeepslateSurface,
                focusedBorderColor = CreeperGreen,
                unfocusedBorderColor = CobblestoneBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("publish_input_creator"),
            singleLine = true
        )

        // Category Choice Row
        PublishFormLabel("Categoría *")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                val border = if (isSelected) BorderStroke(1.5.dp, CreeperGreen) else BorderStroke(1.dp, CobblestoneBorder)
                val bg = if (isSelected) CreeperGreen.copy(alpha = 0.2f) else DeepslateSurface
                val txtColor = if (isSelected) CreeperGreenLight else TextSecondaryDark

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(bg, RoundedCornerShape(10.dp))
                        .border(border, RoundedCornerShape(10.dp))
                        .clickable { selectedCategory = category }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        color = txtColor,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Banner Graphic Choice (Pixel graphics)
        PublishFormLabel("Aparente del Banner (Diseño Pixelado) *")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            bannerThemes.forEach { theme ->
                val isSelected = selectedBannerTheme == theme
                val sizeVal = if (isSelected) 46.dp else 36.dp
                val border = if (isSelected) BorderStroke(2.dp, GoldIngot) else BorderStroke(1.dp, CobblestoneBorder)

                Card(
                    modifier = Modifier
                        .size(sizeVal)
                        .clickable { selectedBannerTheme = theme },
                    shape = RoundedCornerShape(6.dp),
                    border = border,
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
                ) {
                    AddonBanner(bannerType = theme, modifier = Modifier.fillMaxSize())
                }
            }
        }

        // Preview of graphics
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(BorderStroke(1.dp, CobblestoneBorder), RoundedCornerShape(8.dp))
        ) {
            AddonBanner(bannerType = selectedBannerTheme, modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (title.isBlank()) "Vista previa del banner" else title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Por $creatorName • $selectedCategory",
                        color = TextSecondaryDark,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Description Input
        PublishFormLabel("Descripción del Complemento *")
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Describe detalladamente cómo funciona, recetas de crafteo o instalación...", color = TextMutedDark, fontSize = 14.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DeepslateSurface,
                unfocusedContainerColor = DeepslateSurface,
                focusedBorderColor = CreeperGreen,
                unfocusedBorderColor = CobblestoneBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .testTag("publish_input_description"),
            maxLines = 6
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Save Button
        Button(
            onClick = {
                val success = viewModel.publishCustomAddon(
                    title = title,
                    category = selectedCategory,
                    description = description,
                    creatorName = creatorName,
                    bannerType = selectedBannerTheme
                )
                if (success) {
                    // Reset fields
                    title = ""
                    description = ""
                    creatorName = ""
                    selectedBannerTheme = "dirt"
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = CreeperGreen,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("publish_submit_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = "PublishIcon", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PUBLICAR AHORA",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun PublishFormLabel(text: String) {
    Text(
        text = text,
        color = TextSecondaryDark,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp)
    )
}

// --------------------------------------------------------------------------
// SCREEN: MIS GUARDADOS (SAVED AND FAVORITES AND FOLLOWED CREATORS)
// --------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuardadosScreen(viewModel: AddonViewModel) {
    val allAddons by viewModel.allAddons.collectAsStateWithLifecycle()
    val followedCreators by viewModel.followedCreators.collectAsStateWithLifecycle()

    val favAndSaved = remember(allAddons) {
        allAddons.filter { it.isFavorite || it.isSaved }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tu Arsenal",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Tus creadores favoritos y complementos guardados para descargar rápidamente.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
            )
        }

        // Section: Creadores Seguidos
        item {
            Text(
                text = "Creadores Seguidos",
                style = MaterialTheme.typography.titleMedium.copy(color = GoldIngot, fontWeight = FontWeight.Bold)
            )
        }

        if (followedCreators.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepslateSurface),
                    border = BorderStroke(1.dp, CobblestoneBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no sigues a ningún creador.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedDark)
                        )
                    }
                }
            }
        } else {
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    followedCreators.forEach { creatorName ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IronGraySurface,
                            border = BorderStroke(1.dp, CobblestoneBorder),
                            modifier = Modifier.clickable {
                                // Filter Explore list with creator query!
                                viewModel.setSearchQuery(creatorName)
                                viewModel.setCategoryFilter("Todos")
                                viewModel.setActiveSection("Explorar")
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(DiamondCyan, RoundedCornerShape(4.dp))
                                )
                                Text(
                                    text = creatorName,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                IconButton(
                                    onClick = { viewModel.toggleFollowCreator(creatorName) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Dejar de seguir",
                                        tint = RedstoneRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Addons and Textures Guardadas o Favoritas
        item {
            Text(
                text = "Elementos Guardados y Favoritos",
                style = MaterialTheme.typography.titleMedium.copy(color = CreeperGreenLight, fontWeight = FontWeight.Bold)
            )
        }

        if (favAndSaved.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepslateSurface),
                    border = BorderStroke(1.dp, CobblestoneBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Star, contentDescription = "Star", tint = TextMutedDark, modifier = Modifier.size(32.dp))
                            Text(
                                text = "Aún no tienes material guardado",
                                color = TextMutedDark,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Dale al icono de guardar o favorito en de los detalles de un addon.",
                                color = TextMutedDark,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        } else {
            items(favAndSaved) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepslateSurface),
                    border = BorderStroke(1.dp, CobblestoneBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectDetailItem(item) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.size(60.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            AddonBanner(bannerType = item.bannerType, modifier = Modifier.fillMaxSize())
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = item.category, fontSize = 9.sp, color = GoldIngot, fontWeight = FontWeight.Bold)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (item.isFavorite) {
                                        Icon(Icons.Default.Favorite, contentDescription = "Fav", tint = RedstoneRed, modifier = Modifier.size(14.dp))
                                    }
                                    if (item.isSaved) {
                                        Icon(Icons.Default.Star, contentDescription = "Saved", tint = GoldIngot, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Por ${item.creatorName}",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}


// --------------------------------------------------------------------------
// SLIDING/PULL_UP DETAIL PANEL ("Pestaña" where Download, favorite, save, follow are there)
// --------------------------------------------------------------------------
@Composable
fun AddonDetailPanel(
    item: AddonEntity,
    viewModel: AddonViewModel,
    onClose: () -> Unit
) {
    val downloadingProgress by viewModel.downloadingProgress.collectAsStateWithLifecycle()
    val followedCreators by viewModel.followedCreators.collectAsStateWithLifecycle()

    val isDownloading = downloadingProgress[item.id] != null
    val progressPercent = downloadingProgress[item.id] ?: 0
    val isFollowed = followedCreators.contains(item.creatorName)

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onClose() } // Close when tapping outside backdrop
            .testTag("detail_panel_overlay")
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .background(DeepslateSurface, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .border(BorderStroke(1.5.dp, CobblestoneBorder), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable(enabled = false) {} // Avoid bubble taps through click listeners
                .padding(bottom = 16.dp)
        ) {
            // Drag handle decorator
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(CobblestoneBorder, RoundedCornerShape(2.dp))
            )

            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Text(
                    text = "Aparato de Detalles",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondaryDark, letterSpacing = 1.sp)
                )
                Box(modifier = Modifier.size(48.dp)) // Equalizer spacer
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // High fidelity item header with customized pixel artwork banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    AddonBanner(bannerType = item.bannerType, modifier = Modifier.fillMaxSize())
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                )
                            )
                    )
                    // Category chip
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(CreeperGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = item.category.uppercase(), color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title and Downloads Count
                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Descargas: ${item.downloadsCount}",
                                color = GoldIngot,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Box(modifier = Modifier.size(3.dp).background(TextMutedDark, RoundedCornerShape(2.dp)))
                            Text(
                                text = "Minecraft PE/Bedrock",
                                color = TextMutedDark,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Creator and "Seguir" (Follow) section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = IronGraySurface),
                        border = BorderStroke(1.dp, CobblestoneBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GoldIngot, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = "CreatorAvatar", tint = Color.Black)
                                }
                                Column {
                                    Text(
                                        text = "Creador",
                                        color = TextMutedDark,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = item.creatorName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            // Follow Button: "Seguir"
                            Button(
                                onClick = { viewModel.toggleFollowCreator(item.creatorName) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowed) DeepslateSurface else CreeperGreen,
                                    contentColor = if (isFollowed) Color.White else Color.Black
                                ),
                                border = if (isFollowed) BorderStroke(1.dp, CobblestoneBorder) else null,
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("follow_creator_button")
                            ) {
                                if (isFollowed) {
                                    Icon(Icons.Default.Check, contentDescription = "Enseguida", tint = CreeperGreenLight, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Siguiendo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text(text = "Seguir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Description Segment
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Descripción", color = Color.White, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark, lineHeight = 20.sp)
                        )
                    }

                    Divider(color = CobblestoneBorder)

                    // Additional metadata details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "TAMAÑO", fontSize = 10.sp, color = TextMutedDark)
                            Text(text = "4.2 MB", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "VERSIÓN", fontSize = 10.sp, color = TextMutedDark)
                            Text(text = "v1.2.5", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "SOPORTE", fontSize = 10.sp, color = TextMutedDark)
                            Text(text = "1.21+", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Interactive Bottom Actions panel
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DeepslateSurface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Download progress indicator
                    if (isDownloading) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Descargando complementos...",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CreeperGreenLight, fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = "$progressPercent%",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CreeperGreenLight, fontWeight = FontWeight.Bold)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CreeperGreen,
                                trackColor = IronGraySurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Action: Descargar
                        Button(
                            onClick = { viewModel.startDownload(item) },
                            enabled = !isDownloading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CreeperGreen,
                                contentColor = Color.Black,
                                disabledContainerColor = IronGraySurface,
                                disabledContentColor = TextMutedDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .testTag("download_addon_button")
                        ) {
                            if (isDownloading) {
                                Text("Iniciando...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = "DownloadIcon", tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("DESCARGAR", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Action: Dar favorito
                        val iconColorFav = if (item.isFavorite) RedstoneRed else TextSecondaryDark
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(IronGraySurface, RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, CobblestoneBorder), RoundedCornerShape(10.dp))
                                .clickable { viewModel.toggleFavorite(item) }
                                .testTag("favorite_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = iconColorFav
                            )
                        }

                        // Action: Guardar / Bookmark
                        val iconColorSaved = if (item.isSaved) GoldIngot else TextSecondaryDark
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(IronGraySurface, RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, CobblestoneBorder), RoundedCornerShape(10.dp))
                                .clickable { viewModel.toggleSaved(item) }
                                .testTag("save_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Guardar",
                                tint = iconColorSaved
                            )
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// SHARED HELPER COMPONENTS
// --------------------------------------------------------------------------
@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondaryDark
                )
            )
        }
        Text(
            text = "VER TODOS",
            modifier = Modifier
                .clickable { onSeeAll() }
                .padding(4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CreeperGreenLight,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
fun FeaturedAddonCard(
    item: AddonEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepslateSurface),
        border = BorderStroke(1.dp, CobblestoneBorder),
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() }
            .testTag("addon_card_featured_${item.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            ) {
                AddonBanner(bannerType = item.bannerType, modifier = Modifier.fillMaxSize())
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Por ${item.creatorName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.downloadsCount} descargas",
                        fontSize = 9.sp,
                        color = GoldIngot,
                        fontWeight = FontWeight.Medium
                    )

                    Box(
                        modifier = Modifier
                            .background(CreeperGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "GET", fontSize = 9.sp, color = CreeperGreenLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreItemCard(
    item: AddonEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepslateSurface),
        border = BorderStroke(1.dp, CobblestoneBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("addon_card_explore_${item.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                AddonBanner(bannerType = item.bannerType, modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = item.category, fontSize = 8.sp, color = GoldIngot, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "Por ${item.creatorName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(GoldIngot, RoundedCornerShape(3.dp)))
                        Text(
                            text = "${item.downloadsCount}",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (item.isFavorite) {
                        Icon(Icons.Default.Favorite, contentDescription = "Fav", tint = RedstoneRed, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyListPlaceholder(categoryName: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepslateSurface),
        border = BorderStroke(1.dp, CobblestoneBorder),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "¡Pronto habrá más $categoryName!",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedDark)
            )
        }
    }
}
