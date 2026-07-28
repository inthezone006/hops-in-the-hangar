package com.rahul.hopsinthehangar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlin.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.rahul.hopsinthehangar.ui.theme.HopsInTheHangarTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

class FavoritesRepository(private val dataStore: DataStore<Preferences>) {
    private val favoritesKey = stringSetPreferencesKey("favorite_ids")

    val favoriteIds: Flow<Set<String>> = dataStore.data
        .map { preferences ->
            preferences[favoritesKey] ?: emptySet()
        }

    suspend fun toggleFavorite(id: String) {
        dataStore.edit { preferences ->
            val current = preferences[favoritesKey] ?: emptySet()
            if (current.contains(id)) {
                preferences[favoritesKey] = current - id
            } else {
                preferences[favoritesKey] = current + id
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            HopsInTheHangarTheme {
                MainScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Sponsors : Screen("sponsors", "Sponsors", Icons.Default.Star)
    object Entertainment : Screen("entertainment", "Events", Icons.AutoMirrored.Filled.List)
    object Vendors : Screen("vendors", "Vendors", Icons.Default.ShoppingCart)
    object Map : Screen("map", "Map", Icons.Default.LocationOn)
    object Detail : Screen("detail/{type}/{id}", "Detail", Icons.Default.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(analytics: FirebaseAnalytics? = Firebase.analytics) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Data Management
    val repository = remember { FavoritesRepository(context.dataStore) }
    val favoriteIds by repository.favoriteIds.collectAsState(initial = emptySet())
    var eventData by remember { mutableStateOf<EventData?>(null) }

    LaunchedEffect(Unit) {
        eventData = loadEventData(context)
    }

    // Log screen views
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, route)
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Sponsors,
        Screen.Entertainment,
        Screen.Vendors,
        Screen.Map
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = bottomNavItems.find { it.route == currentRoute }?.label?.uppercase() ?: "HOPS IN THE HANGAR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                navigationIcon = {
                    if (currentRoute?.startsWith("detail") == true) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(eventData) }
            composable(Screen.Sponsors.route) { 
                SponsorsScreen(
                    sponsors = eventData?.sponsors ?: emptyList(),
                    onSponsorClick = { id -> 
                        navController.navigate("detail/sponsor/$id")
                    }
                ) 
            }
            composable(Screen.Entertainment.route) { 
                EntertainmentScreen(
                    schedule = eventData?.schedule ?: emptyList()
                ) 
            }
            composable(Screen.Vendors.route) { 
                VendorsScreen(
                    vendors = eventData?.vendors ?: emptyList(),
                    onVendorClick = { id -> 
                        analytics?.logEvent("vendor_detail_view") {
                            param("vendor_id", id)
                        }
                        navController.navigate("detail/vendor/$id") 
                    },
                    favoriteIds = favoriteIds.toList(),
                    onToggleFavorite = { id -> 
                        scope.launch { repository.toggleFavorite(id) }
                    }
                ) 
            }
            composable(Screen.Map.route) { MapScreen(eventData, favoriteIds) }
            composable(Screen.Detail.route) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: ""
                val id = backStackEntry.arguments?.getString("id") ?: ""
                val item = when(type) {
                    "vendor" -> eventData?.vendors?.find { it.name == id }
                    "sponsor" -> eventData?.sponsors?.find { it.name == id }
                    else -> null
                }
                DetailScreen(type, id, item)
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoBackground(videoResIds: List<Int>) {
    val context = LocalContext.current
    var currentVideoIndex by remember { mutableIntStateOf(0) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Setup the player
            repeatMode = Player.REPEAT_MODE_OFF // We'll handle looping/cycling manually
            playWhenReady = true
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        // Cycle to next video
                        currentVideoIndex = (currentVideoIndex + 1) % videoResIds.size
                    }
                }
            })
        }
    }

    // Effect to update media item when index changes
    LaunchedEffect(currentVideoIndex, videoResIds) {
        if (videoResIds.isNotEmpty()) {
            val videoResId = videoResIds[currentVideoIndex]
            val uri = "android.resource://${context.packageName}/$videoResId".toUri()
            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
            
            // Set clipping to 7 seconds (7,000,000 microseconds)
            // Note: Media3 clipping is done via MediaItem.ClippingConfiguration
            val clippedItem = MediaItem.Builder()
                .setUri(uri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setEndPositionMs(7000) // Cut to 7 seconds
                        .build()
                )
                .build()
            
            exoPlayer.setMediaItem(clippedItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Serializable
data class EventData(
    val sponsors: List<SponsorItem>,
    val vendors: List<VendorItem>,
    val schedule: List<ScheduleItem>,
    val info: GeneralInfo
)

@Serializable
data class SponsorLink(val label: String, val url: String)

@Serializable
data class SponsorItem(
    val name: String,
    val level: String,
    val description: String,
    val website: String? = null,
    val links: List<SponsorLink>? = null
)

@Serializable
data class VendorItem(
    val name: String,
    val category: String,
    val description: String,
    val email: String? = null,
    val phone: String? = null,
    val website: String? = null,
    val mapId: String? = null
)

@Serializable
data class ScheduleItem(val time: String, val event: String)

@Serializable
data class GeneralInfo(
    val parking: String,
    val rules: String,
    val hotels: List<HotelItem>
)

@Serializable
data class HotelItem(val name: String, val link: String)

suspend fun loadEventData(context: Context): EventData? = withContext(Dispatchers.IO) {
    try {
        val jsonString = context.assets.open("event_data.json").bufferedReader().use { it.readText() }
        Json.decodeFromString<EventData>(jsonString)
    } catch (e: Exception) {
        Log.e("DataLoader", "Error loading event data", e)
        null
    }
}

@Composable
fun HomeScreen(eventData: EventData?) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Surface(
            modifier = Modifier.size(140.dp),
            shape = CircleShape,
            color = Color.White, // Use white background for the circle
            border = BorderStroke(3.dp, Color.White)
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = R.mipmap.ic_launcher,
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Welcome to the Show",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                val fullText = "Welcome to Hops in the Hangar, your Craft Beer & Airshow event app! Explore a lineup of vendors and sponsors, discover detailed venue information, find the best hotels nearby, enjoy exciting entertainment, and get to know the featured airshow performers.\n\nCraft beer, beverages, and aircraft come together to create not only a fun social event, but also an extremely unique community experience. Hops in the Hangar celebrates aviation, local businesses, and great craft beverages while bringing people together for an unforgettable evening at the Middletown Regional Airport.\n\nWhether you're here for the thrilling air show performances, the incredible selection of breweries and beverage vendors, or simply to enjoy time with friends and family, this app will help you make the most of your experience. Stay connected with schedules, updates, event maps, and everything you need for an amazing experience at Hops in the Hangar 2026."
                val firstParagraph = fullText.substringBefore("\n\n")
                
                Text(
                    if (expanded) fullText else firstParagraph,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Show Less" else "Show More"
                    )
                }
            }
        }
        
        if (eventData != null) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "VENUE & LOGISTICS",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Parking", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(eventData.info.parking, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Event Rules", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(eventData.info.rules, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Nearby Hotels", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val context = LocalContext.current
                    eventData.info.hotels.forEach { hotel ->
                        ElevatedCard(
                            onClick = { 
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(hotel.link))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Color.Transparent
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Hotel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(hotel.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "OUR TEAM",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Middletown Aviation Foundation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Your Hops in the Hangar Crew",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val crew = listOf(
                    "Rich Bevis", "Kurt Yearout", "Sara Yearout", "Tom Spielmann",
                    "Sean Askren", "Mica Jones", "Missy Lawwill", "Jamie Murphy"
                )
                
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    maxItemsInEachRow = 3
                ) {
                    crew.forEach { name ->
                        Surface(
                            modifier = Modifier.padding(4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        ) {
                            Text(
                                name,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        val context = LocalContext.current
        val versionName = remember {
            try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                "Unknown"
            }
        }
        
        Text(
            text = "v$versionName",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun GlassCard(title: String, description: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White.copy(alpha = 0.15f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorsScreen(sponsors: List<SponsorItem>, onSponsorClick: (String) -> Unit) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val filteredSponsors = sponsors.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.level.contains(searchQuery, ignoreCase = true)
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedSponsor by remember { mutableStateOf<SponsorItem?>(null) }
    val sheetState = rememberModalBottomSheetState()

    if (showBottomSheet && selectedSponsor != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = selectedSponsor!!.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Which website would you like to visit?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                selectedSponsor!!.links?.forEach { link ->
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("SponsorsScreen", "Error opening website: ${link.url}", e)
                            }
                            showBottomSheet = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(link.label)
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            placeholder = { Text("Search Sponsors...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            )
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(filteredSponsors) { sponsor ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val links = sponsor.links
                            if (links != null && links.size > 1) {
                                selectedSponsor = sponsor
                                showBottomSheet = true
                            } else {
                                val url = links?.firstOrNull()?.url ?: sponsor.website
                                url?.let {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Log.e("SponsorsScreen", "Error opening website: $it", e)
                                    }
                                }
                            }
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    ListItem(
                        headlineContent = { Text(sponsor.name, fontWeight = FontWeight.ExtraBold) },
                        supportingContent = { Text(sponsor.description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                        overlineContent = { 
                            Text(
                                sponsor.level.uppercase(), 
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            ) 
                        },
                        leadingContent = {
                            val names = sponsor.name.split("&").map { it.trim() }
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp) // Move down to center visually
                                    .width(if (names.size > 1) 72.dp else 48.dp)
                                    .height(48.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                names.forEachIndexed { index, name ->
                                    val resourceName = name.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
                                    val context = LocalContext.current
                                    val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
                                    
                                    Surface(
                                        modifier = Modifier
                                            .padding(start = (index * 24).dp)
                                            .size(48.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (resourceId != 0) {
                                                AsyncImage(
                                                    model = resourceId,
                                                    contentDescription = name,
                                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = when(sponsor.level) {
                                                        "Top Flight" -> MaterialTheme.colorScheme.primary
                                                        "First Class" -> Color(0xFFFFD700) // Gold
                                                        "Business Class" -> Color(0xFFC0C0C0) // Silver
                                                        "Coach Class" -> Color(0xFFCD7F32) // Bronze
                                                        "Brewery" -> MaterialTheme.colorScheme.secondary
                                                        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorsScreen(
    vendors: List<VendorItem>,
    onVendorClick: (String) -> Unit,
    favoriteIds: List<String>,
    onToggleFavorite: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(setOf("Brewery", "Food Truck")) }
    
    val filteredVendors = vendors.filter {
        (it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)) &&
        selectedCategories.contains(it.category)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search Vendors...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                )
            )
            
            var showFilterMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showFilterMenu = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
                DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                    listOf("Brewery", "Food Truck").forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedCategories.contains(category),
                                        onCheckedChange = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(category)
                                }
                            },
                            onClick = {
                                selectedCategories = if (selectedCategories.contains(category)) {
                                    selectedCategories - category
                                } else {
                                    selectedCategories + category
                                }
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(filteredVendors) { vendor ->
                val isFavorite = favoriteIds.contains(vendor.name)
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    onClick = { onVendorClick(vendor.name) },
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    ListItem(
                        headlineContent = { Text(vendor.name, fontWeight = FontWeight.ExtraBold) },
                        supportingContent = { Text(vendor.description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                        overlineContent = { 
                            Text(
                                vendor.category.uppercase(), 
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            ) 
                        },
                        leadingContent = {
                            val context = LocalContext.current
                            val resourceName = vendor.name.lowercase().replace(" ", "_").replace(Regex("[^a-z0-9_]"), "")
                            val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)

                            Box(modifier = Modifier.padding(top = 8.dp)) { // Move down to center visually
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (resourceId != 0) {
                                            AsyncImage(
                                                model = resourceId,
                                                contentDescription = vendor.name,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = when(vendor.category) {
                                                    "Food", "Food Truck" -> Icons.Default.Fastfood
                                                    "Brewery" -> Icons.Default.LocalBar
                                                    "Spirits" -> Icons.Default.WineBar
                                                    else -> Icons.Default.ShoppingCart
                                                },
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = { onToggleFavorite(vendor.name) }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun DetailScreen(type: String, id: String, item: Any?) {
    val context = LocalContext.current
    
    val description = when (item) {
        is VendorItem -> item.description
        is SponsorItem -> item.description
        else -> "Detailed information for $id"
    }

    val email = if (item is VendorItem) item.email else null
    val phone = if (item is VendorItem) item.phone else null
    val website = if (item is VendorItem) item.website else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1532634896-26909d0d4b89?q=80&w=1000",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(32.dp)),
                contentScale = ContentScale.Crop
            )
            
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp
            ) {
                Text(
                    text = type.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = id, 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "About", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
        
        if (email != null || phone != null || website != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Contact Information", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    email?.let { 
                        DetailContactRow(
                            icon = Icons.Default.Email, 
                            value = it,
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$it")
                                }
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    phone?.let { 
                        DetailContactRow(
                            icon = Icons.Default.Phone, 
                            value = it,
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$it")
                                }
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    website?.let { 
                        DetailContactRow(
                            icon = Icons.Default.Language, 
                            value = it,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DetailContactRow(icon: ImageVector, value: String, onClick: () -> Unit = {}) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntertainmentScreen(schedule: List<ScheduleItem>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Ground Entertainment",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ListItem(
                    headlineContent = { Text("Jane Doe", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Entertainment Host") },
                    overlineContent = { Text("HOST", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall) },
                    leadingContent = { 
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ListItem(
                    headlineContent = { Text("DJ Mixmaster", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Live Music DJ") },
                    overlineContent = { Text("MUSIC", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall) },
                    leadingContent = { 
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ListItem(
                    headlineContent = { Text("John Smith", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("National Anthem Singer") },
                    overlineContent = { Text("ANTHEM", color = Color.Red, style = MaterialTheme.typography.labelSmall) },
                    leadingContent = { 
                        Surface(shape = CircleShape, color = Color.Red.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp)) }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }

        Text(
            text = "In Flight Performers",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ListItem(
                    headlineContent = { Text("Wild Bill", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Steven Hanshew") },
                    overlineContent = { Text("ANNOUNCER", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall) },
                    leadingContent = { 
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ListItem(
                    headlineContent = { Text("Team Fastrax", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Opening Jump") },
                    overlineContent = { Text("PERFORMANCE", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall) },
                    leadingContent = { 
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AirplanemodeActive, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp)) }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }

        Text(
            text = "Event Schedule",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                schedule.forEachIndexed { index, item ->
                    ListItem(
                        headlineContent = { Text(item.event, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(item.time, color = MaterialTheme.colorScheme.primary) },
                        leadingContent = { 
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    if (index < schedule.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MapScreen(eventData: EventData?, favoriteIds: Set<String>) {
    val context = LocalContext.current
    var regions by remember { mutableStateOf<List<MapRegion>>(emptyList()) }
    var selectedRegionId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Transformation state
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Constants for SVG viewport
    val svgWidth = 2000f
    val svgHeight = 2000f

    val heartedMapIds = remember(eventData, favoriteIds) {
        eventData?.vendors?.filter { favoriteIds.contains(it.name) }?.mapNotNull { it.mapId }?.toSet() ?: emptySet()
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val parsedRegions = parseSvg(context, "map.svg")
                regions = parsedRegions
                isLoading = false
            } catch (e: Exception) {
                Log.e("MapScreen", "Error parsing SVG", e)
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).clipToBounds()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().clipToBounds()) {
                val canvasWidth = constraints.maxWidth.toFloat()
                val canvasHeight = constraints.maxHeight.toFloat()
                
                // Base scale to fit SVG to screen
                val baseScaleX = canvasWidth / svgWidth
                val baseScaleY = canvasHeight / svgHeight
                val baseScale = minOf(baseScaleX, baseScaleY)
                
                // Centering offsets
                val baseOffsetX = (canvasWidth - (svgWidth * baseScale)) / 2f
                val baseOffsetY = (canvasHeight - (svgHeight * baseScale)) / 2f
                
                val primaryColor = MaterialTheme.colorScheme.primary
                val favoriteColor = Color(0xFFFF4081) // Pink/Red for favorites

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds() // Ensure map doesn't draw outside the canvas area
                        .pointerInput(regions) {
                            detectTransformGestures { centroid, pan, zoom, _ ->
                                val oldScale = zoomScale
                                val newScale = (oldScale * zoom).coerceIn(1f, 10f)
                                val scaleFactor = newScale / oldScale
                                
                                // Centric zoom: panOffset' = pan + panOffset * scaleFactor + (centroid - baseOffset) * (1 - scaleFactor)
                                val baseOffset = Offset(baseOffsetX, baseOffsetY)
                                panOffset = (panOffset * scaleFactor) + (centroid - baseOffset) * (1f - scaleFactor) + pan
                                zoomScale = newScale
                            }
                        }
                        .pointerInput(regions, zoomScale, panOffset) {
                            detectTapGestures { offset ->
                                // Calculate coordinate in SVG space
                                val svgX = (offset.x - baseOffsetX - panOffset.x) / (baseScale * zoomScale)
                                val svgY = (offset.y - baseOffsetY - panOffset.y) / (baseScale * zoomScale)
                                
                                // Hit test clickable regions only
                                val clickedRegion = regions.findLast { region ->
                                    region.isClickable && hitTest(region.path, svgX, svgY)
                                }
                                
                                selectedRegionId = clickedRegion?.id
                            }
                        }
                ) {
                    drawIntoCanvas { canvas ->
                        canvas.save()
                        
                        // Apply transformations
                        canvas.translate(baseOffsetX + panOffset.x, baseOffsetY + panOffset.y)
                        canvas.scale(baseScale * zoomScale, baseScale * zoomScale)
                        
                        regions.forEach { region ->
                            val isSelected = region.id == selectedRegionId
                            val isHearted = heartedMapIds.contains(region.id)
                            
                            drawPath(
                                path = region.path,
                                color = when {
                                    isSelected -> primaryColor
                                    isHearted -> favoriteColor
                                    else -> region.color
                                },
                                style = Fill
                            )
                            // Draw outline for selected or hearted
                            if (isSelected || isHearted) {
                                drawPath(
                                    path = region.path,
                                    color = Color.Black,
                                    style = Stroke(width = 2f / (baseScale * zoomScale))
                                )
                            }
                        }
                        canvas.restore()
                    }
                }
                
                // Overlay Info Header
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedRegionId ?: "Interactive Event Map",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedRegionId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (selectedRegionId != null) "Middletown Regional Airport" else "Pinch to zoom • Drag to pan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (zoomScale != 1f || panOffset != Offset.Zero) {
                            IconButton(onClick = {
                                zoomScale = 1f
                                panOffset = Offset.Zero
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset View")
                            }
                        }
                    }
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

data class MapRegion(
    val id: String,
    val path: Path,
    val color: Color,
    val isClickable: Boolean = true
)

fun parseSvg(context: android.content.Context, fileName: String): List<MapRegion> {
    val regionsMap = mutableMapOf<String, MutableList<Pair<Path, Color>>>()
    val factory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()
    val inputStream = context.assets.open(fileName)
    parser.setInput(inputStream, null)

    var eventType = parser.eventType
    val groupIds = mutableListOf<String>()
    
    // Non-clickable layer IDs
    val backgroundIds = setOf("Event Map Base", "Full Event Map")

    while (eventType != XmlPullParser.END_DOCUMENT) {
        val tagName = parser.name
        when (eventType) {
            XmlPullParser.START_TAG -> {
                val id = parser.getAttributeValue(null, "id")
                val transform = parser.getAttributeValue(null, "transform")
                
                if (tagName == "g") {
                    groupIds.add(id ?: "")
                } else {
                    val fill = parser.getAttributeValue(null, "fill") ?: "#000000"
                    val fillOpacity = parser.getAttributeValue(null, "fill-opacity")?.toFloatOrNull() ?: 1f
                    
                    val color = if (fill == "none") {
                        Color.Transparent
                    } else {
                        try {
                            val baseColor = android.graphics.Color.parseColor(fill)
                            Color(baseColor).copy(alpha = fillOpacity)
                        } catch (_: Exception) {
                            Color.Gray.copy(alpha = fillOpacity)
                        }
                    }

                    val finalId = id ?: groupIds.lastOrNull { it.isNotEmpty() }

                    // Skip drawing the very base white rectangle if it's "Event Map Base"
                    val shouldSkip = finalId == "Event Map Base" && tagName == "rect"

                    if (!shouldSkip && finalId != null) {
                        val androidPath = android.graphics.Path()
                        var pathFound = false

                        when (tagName) {
                            "path" -> {
                                val d = parser.getAttributeValue(null, "d")
                                if (d != null) {
                                    try {
                                        val p = PathParser().parsePathString(d).toPath().asAndroidPath()
                                        androidPath.set(p)
                                        pathFound = true
                                    } catch (_: Exception) { Log.e("MapParser", "Error parsing path") }
                                }
                            }
                            "rect" -> {
                                val x = parser.getAttributeValue(null, "x")?.toFloat() ?: 0f
                                val y = parser.getAttributeValue(null, "y")?.toFloat() ?: 0f
                                val width = parser.getAttributeValue(null, "width")?.toFloat() ?: 0f
                                val height = parser.getAttributeValue(null, "height")?.toFloat() ?: 0f
                                androidPath.addRect(x, y, x + width, y + height, android.graphics.Path.Direction.CW)
                                pathFound = true
                            }
                            "ellipse", "circle" -> {
                                val cx = parser.getAttributeValue(null, "cx")?.toFloat() ?: 0f
                                val cy = parser.getAttributeValue(null, "cy")?.toFloat() ?: 0f
                                val rx = if (tagName == "circle") parser.getAttributeValue(null, "r")?.toFloat() ?: 0f else parser.getAttributeValue(null, "rx")?.toFloat() ?: 0f
                                val ry = if (tagName == "circle") rx else parser.getAttributeValue(null, "ry")?.toFloat() ?: 0f
                                androidPath.addOval(cx - rx, cy - ry, cx + rx, cy + ry, android.graphics.Path.Direction.CW)
                                pathFound = true
                            }
                        }

                        if (pathFound) {
                            applySvgTransform(androidPath, transform)
                            val list = regionsMap.getOrPut(finalId) { mutableListOf() }
                            list.add(androidPath.asComposePath() to color)
                        }
                    }
                }
            }
            XmlPullParser.END_TAG -> {
                if (tagName == "g" && groupIds.isNotEmpty()) {
                    groupIds.removeAt(groupIds.size - 1)
                }
            }
        }
        eventType = parser.next()
    }
    inputStream.close()

    return regionsMap.map { (id, paths) ->
        val combinedPath = Path()
        paths.forEach { (path, _) -> combinedPath.addPath(path) }
        
        // Find the "best" color (non-transparent if possible)
        val regionColor = paths.find { it.second != Color.Transparent }?.second ?: Color.Transparent
        
        MapRegion(id, combinedPath, regionColor, !backgroundIds.contains(id))
    }
}

private fun applySvgTransform(path: android.graphics.Path, transform: String?) {
    if (transform == null) return
    val matrix = android.graphics.Matrix()
    
    // Robust parsing for rotate(angle [cx cy])
    if (transform.contains("rotate")) {
        val content = transform.substringAfter("rotate(").substringBefore(")")
        val values = content.split(Regex("[ ,]+")).filter { it.isNotEmpty() }
        try {
            when (values.size) {
                1 -> matrix.postRotate(values[0].toFloat())
                3 -> matrix.postRotate(values[0].toFloat(), values[1].toFloat(), values[2].toFloat())
            }
        } catch (_: Exception) {}
    }
    
    // Support for basic translate(x [y])
    if (transform.contains("translate")) {
        val content = transform.substringAfter("translate(").substringBefore(")")
        val values = content.split(Regex("[ ,]+")).filter { it.isNotEmpty() }
        try {
            when (values.size) {
                1 -> matrix.postTranslate(values[0].toFloat(), 0f)
                2 -> matrix.postTranslate(values[0].toFloat(), values[1].toFloat())
            }
        } catch (_: Exception) {}
    }
    
    path.transform(matrix)
}

fun hitTest(path: Path, x: Float, y: Float): Boolean {
    val androidPath = path.asAndroidPath()
    val bounds = android.graphics.RectF()
    androidPath.computeBounds(bounds, true)
    
    // Add a small tolerance for clicking thin lines/strokes
    val tolerance = 5f
    val region = android.graphics.Region()
    region.setPath(androidPath, android.graphics.Region(
        (bounds.left - tolerance).toInt(),
        (bounds.top - tolerance).toInt(),
        (bounds.right + tolerance).toInt(),
        (bounds.bottom + tolerance).toInt()
    ))
    
    return region.contains(x.toInt(), y.toInt())
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    HopsInTheHangarTheme {
        // Pass null for analytics in preview to avoid "FirebaseApp is not initialized" error
        MainScreen(analytics = null)
    }
}
