package com.rahul.hopsinthehangar

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlin.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val isHome = currentRoute == Screen.Home.route

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
    
    // State for favorites
    val favoriteIds = remember { mutableStateListOf<String>() }

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Sponsors,
        Screen.Entertainment,
        Screen.Vendors,
        Screen.Map
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = if (isHome) Color.Transparent else MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!isHome) {
                TopAppBar(
                    title = { Text(bottomNavItems.find { it.route == currentRoute }?.label ?: "Hops in the Hangar") },
                    navigationIcon = {
                        if (currentRoute?.startsWith("detail") == true) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (isHome) Color.Transparent else MaterialTheme.colorScheme.surface,
                contentColor = if (isHome) Color.White else MaterialTheme.colorScheme.onSurface,
                tonalElevation = if (isHome) 0.dp else 8.dp
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label, maxLines = 1) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = if (isHome) {
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                selectedTextColor = Color.White,
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                indicatorColor = Color.White.copy(alpha = 0.2f)
                            )
                        } else {
                            NavigationBarItemDefaults.colors()
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(if (isHome) PaddingValues(0.dp) else innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Sponsors.route) { 
                SponsorsScreen(onSponsorClick = { id -> 
                    navController.navigate("detail/sponsor/$id")
                }) 
            }
            composable(Screen.Entertainment.route) { EntertainmentScreen() }
            composable(Screen.Vendors.route) { 
                VendorsScreen(
                    onVendorClick = { id -> 
                        analytics?.logEvent("vendor_detail_view") {
                            param("vendor_id", id)
                        }
                        navController.navigate("detail/vendor/$id") 
                    },
                    favoriteIds = favoriteIds,
                    onToggleFavorite = { id -> 
                        val isAdding = !favoriteIds.contains(id)
                        if (isAdding) favoriteIds.add(id) else favoriteIds.remove(id)
                    }
                ) 
            }
            composable(Screen.Map.route) { MapScreen() }
            composable(Screen.Detail.route) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: ""
                val id = backStackEntry.arguments?.getString("id") ?: ""
                DetailScreen(type, id)
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
            val uri = Uri.parse("android.resource://${context.packageName}/$videoResId")
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
        modifier = Modifier
            .fillMaxSize()
            .blur(15.dp) // Apply blur as requested
    )
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val videoIds = remember {
        val ids = mutableListOf<Int>()
        // Look for bg_video_1, bg_video_2, etc.
        for (i in 1..10) {
            val id = context.resources.getIdentifier("bg_video_$i", "raw", context.packageName)
            if (id != 0) ids.add(id)
        }
        // Fallback to existing background_video if no numbered ones exist
        if (ids.isEmpty()) {
            val defaultId = context.resources.getIdentifier("background_video", "raw", context.packageName)
            if (defaultId != 0) ids.add(defaultId)
        }
        ids
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (videoIds.isNotEmpty()) {
            VideoBackground(videoResIds = videoIds)
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f)) // Slightly darker for readability with blur
        )

        // Title centered
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "HOPS IN THE\nHANGAR",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = MaterialTheme.typography.displayLarge.lineHeight * 0.8
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "2026 EDITION",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Light,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 4.sp
                    )
                )
            }
        }

        // Circular App Icon in Top Left
        Surface(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .size(48.dp),
            shape = CircleShape,
            color = Color.White,
            tonalElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Using ic_launcher_foreground as it is a supported vector type
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Icon",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
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
fun SponsorsScreen(onSponsorClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val sponsors = remember {
        listOf(
            SponsorItem("Skyline Airways", "Diamond", "Official Flight Partner"),
            SponsorItem("Hoppy Valley Brewing", "Gold", "Providing the finest local craft beer"),
            SponsorItem("Middletown Community Bank", "Gold", "Proud supporter of local events"),
            SponsorItem("Aviation Tech Solutions", "Silver", "Innovation in the skies"),
            SponsorItem("Cloud Nine Hospitality", "Silver", "Premium event experiences"),
            SponsorItem("Hangar Refreshments", "Bronze", "Quality drinks for all guests")
        )
    }

    val filteredSponsors = sponsors.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.level.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            placeholder = { Text("Search Sponsors...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary
            )
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredSponsors) { sponsor ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSponsorClick(sponsor.name) },
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
                ) {
                    ListItem(
                        headlineContent = { Text(sponsor.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(sponsor.description) },
                        overlineContent = { Text(sponsor.level, color = MaterialTheme.colorScheme.secondary) },
                        leadingContent = {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = when(sponsor.level) {
                                    "Diamond" -> MaterialTheme.colorScheme.primary
                                    "Gold" -> Color(0xFFFFD700)
                                    else -> MaterialTheme.colorScheme.tertiary
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorsScreen(
    onVendorClick: (String) -> Unit,
    favoriteIds: List<String>,
    onToggleFavorite: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val vendors = remember {
        listOf(
            VendorItem("The Golden Hops", "Brewery", "Award-winning IPAs and Stouts"),
            VendorItem("Hangar Grill", "Food", "Gourmet burgers and aviation-themed sides"),
            VendorItem("Sky-High Crafts", "Merchandise", "Handmade aviation art and apparel"),
            VendorItem("Blue Sky Distillery", "Spirits", "Small-batch gin and vodka"),
            VendorItem("Taco Takeoff", "Food", "Authentic street tacos with a kick"),
            VendorItem("Propeller Pretzels", "Snacks", "Giant soft pretzels with beer cheese")
        )
    }

    val filteredVendors = vendors.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            placeholder = { Text("Search Vendors...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary
            )
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredVendors) { vendor ->
                val isFavorite = favoriteIds.contains(vendor.name)
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onVendorClick(vendor.name) },
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
                        )
                    )
                ) {
                    ListItem(
                        headlineContent = { Text(vendor.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(vendor.description) },
                        overlineContent = { Text(vendor.category, color = MaterialTheme.colorScheme.primary) },
                        leadingContent = {
                            Icon(
                                imageVector = when(vendor.category) {
                                    "Food" -> Icons.Default.Fastfood
                                    "Brewery" -> Icons.Default.LocalBar
                                    "Spirits" -> Icons.Default.WineBar
                                    else -> Icons.Default.ShoppingCart
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { onToggleFavorite(vendor.name) }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailScreen(type: String, id: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1532634896-26909d0d4b89?q=80&w=1000", // Generic detail image
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        Text(text = id, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(text = "Category: $type", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
        
        HorizontalDivider()
        
        Text(
            text = "This is where detailed information about $id would go. You can edit this to include menus, full biographies, or special event offers for this specific $type.",
            style = MaterialTheme.typography.bodyLarge
        )
        
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Contact Information", fontWeight = FontWeight.Bold)
                Text("Email: info@$id.com")
                Text("Phone: (555) 012-3456")
                Text("Website: www.$id.com")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntertainmentScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Entertainment Schedule",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                ListItem(
                    headlineContent = { Text("Sky High Beats") },
                    overlineContent = { Text("DJ") },
                    leadingContent = { Icon(Icons.Default.MusicNote, contentDescription = null) }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Captain Clouds") },
                    overlineContent = { Text("Air Show Announcer") },
                    leadingContent = { Icon(Icons.Default.Mic, contentDescription = null) }
                )
            }
        }

        Text(
            text = "Jump Schedule",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        val schedule = listOf(
            "14:00" to "Opening Jump - Team Fastrax",
            "15:30" to "Aerobatic Display - Pitts Special",
            "16:00" to "Formation Display - Sky Knights",
            "17:30" to "Warbird Flyover - P-51 Mustang",
            "18:00" to "Sunset Finale - Evening Parachute Jump"
        )

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                schedule.forEachIndexed { index, (time, event) ->
                    ListItem(
                        headlineContent = { Text(event) },
                        supportingContent = { Text(time) },
                        leadingContent = { Icon(Icons.Default.Event, contentDescription = null) }
                    )
                    if (index < schedule.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun MapScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(16.dp))
        Text(text = "Event Map Coming Soon", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "Interactive Middletown Regional Airport map", style = MaterialTheme.typography.bodyMedium)
    }
}

data class SponsorItem(val name: String, val level: String, val description: String)
data class VendorItem(val name: String, val category: String, val description: String)

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    HopsInTheHangarTheme {
        // Pass null for analytics in preview to avoid "FirebaseApp is not initialized" error
        MainScreen(analytics = null)
    }
}
