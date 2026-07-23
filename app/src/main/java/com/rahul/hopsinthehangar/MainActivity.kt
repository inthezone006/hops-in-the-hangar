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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!isHome) {
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
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,
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
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            "HOPS IN THE\nHANGAR",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = MaterialTheme.typography.displayLarge.lineHeight * 0.85
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "2026 EDITION",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 4.sp
                )
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Welcome to the Show",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Experience the thrill of aviation and the taste of local craft beer at the Middletown Regional Airport.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    "LATEST ANNOUNCEMENTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                AnnouncementRow(
                    icon = Icons.Default.Mic,
                    title = "Announcer",
                    value = "Steven Hanshew aka Wild Bill"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AnnouncementRow(
                    icon = Icons.Default.AirplanemodeActive,
                    title = "Featured Performance",
                    value = "Smoke on Aviation"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            "Middletown Aviation Foundation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
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
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            maxItemsInEachRow = 3
        ) {
            crew.forEach { name ->
                Text(
                    name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun AnnouncementRow(icon: ImageVector, title: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title, 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                value, 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    onClick = { onSponsorClick(sponsor.name) },
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
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = when(sponsor.level) {
                                            "Diamond" -> MaterialTheme.colorScheme.primary
                                            "Gold" -> Color(0xFFFFD700)
                                            else -> MaterialTheme.colorScheme.secondary
                                        }
                                    )
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
    onVendorClick: (String) -> Unit,
    favoriteIds: List<String>,
    onToggleFavorite: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val vendors = remember {
        listOf(
            VendorItem("High Grain Brewing", "Brewery", "Cincinnati, OH"),
            VendorItem("Dafuque beer company", "Brewery", "Local Favorite"),
            VendorItem("Streetside Brewery", "Brewery", "Cincinnati, OH"),
            VendorItem("Loose Ends Brewing", "Brewery", "Centerville, OH"),
            VendorItem("Third eye brewing", "Brewery", "Sharonville, OH"),
            VendorItem("Gravel Road Brewing Co", "Brewery", "Middletown, OH"),
            VendorItem("Depot Brewing Company", "Brewery", "Fairborn, OH"),
            VendorItem("Sonder Brewing", "Brewery", "Mason, OH"),
            VendorItem("Stevens Point Brewery", "Brewery", "Stevens Point, WI"),
            VendorItem("Heavier Than Air Brewing Co", "Brewery", "Centerville, OH"),
            VendorItem("NEW Ales", "Brewery", "Middletown, OH"),
            VendorItem("BC's Brewing Company", "Brewery", "Mason, OH")
        )
    }

    val filteredVendors = vendors.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            placeholder = { Text("Search Breweries...") },
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
            items(filteredVendors) { vendor ->
                val isFavorite = favoriteIds.contains(vendor.name)
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
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
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when(vendor.category) {
                                            "Food" -> Icons.Default.Fastfood
                                            "Brewery" -> Icons.Default.LocalBar
                                            "Spirits" -> Icons.Default.WineBar
                                            else -> Icons.Default.ShoppingCart
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
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
fun DetailScreen(type: String, id: String) {
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
                    text = "This is where detailed information about $id would go. You can edit this to include menus, full biographies, or special event offers for this specific $type.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
        
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
                
                DetailContactRow(icon = Icons.Default.Email, value = "info@$id.com")
                Spacer(modifier = Modifier.height(12.dp))
                DetailContactRow(icon = Icons.Default.Phone, value = "(555) 012-3456")
                Spacer(modifier = Modifier.height(12.dp))
                DetailContactRow(icon = Icons.Default.Language, value = "www.$id.com")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DetailContactRow(icon: ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntertainmentScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Featured Performers",
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
                    overlineContent = { Text("ANNNOUNCER", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall) },
                    leadingContent = { 
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ListItem(
                    headlineContent = { Text("Smoke on Aviation", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Out of Louisville, KY") },
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

        val schedule = listOf(
            "14:00" to "Opening Jump - Team Fastrax",
            "15:30" to "Aerobatic Display - Pitts Special",
            "16:00" to "Formation Display - Sky Knights",
            "17:30" to "Warbird Flyover - P-51 Mustang",
            "18:00" to "Sunset Finale - Evening Parachute Jump"
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                schedule.forEachIndexed { index, (time, event) ->
                    ListItem(
                        headlineContent = { Text(event, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(time, color = MaterialTheme.colorScheme.primary) },
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
fun MapScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.LocationOn, 
                    contentDescription = null, 
                    modifier = Modifier.size(64.dp), 
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = "Event Map Coming Soon", 
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            text = "Interactive Middletown Regional Airport map will be available on event day.", 
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        
        Spacer(Modifier.height(48.dp))
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Airport Address", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("1707 Run Way, Middletown, OH 45042", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
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
