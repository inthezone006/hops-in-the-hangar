package com.rahul.hopsinthehangar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
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
fun MainScreen() {
    val analytics = remember { Firebase.analytics }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Log screen views
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Hops in the Hangar") },
                navigationIcon = {
                    if (currentRoute?.startsWith("detail") == true) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label, maxLines = 1) },
                        selected = currentRoute == screen.route,
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
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    analytics.logEvent("ticket_click", null)
                    scope.launch {
                        snackbarHostState.showSnackbar("Tickets are all sold out for now!")
                    }
                },
                icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                text = { Text("Tickets") },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
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
                        analytics.logEvent("vendor_detail_view") {
                            param("vendor_id", id)
                        }
                        navController.navigate("detail/vendor/$id") 
                    },
                    favoriteIds = favoriteIds,
                    onToggleFavorite = { id -> 
                        val isAdding = !favoriteIds.contains(id)
                        analytics.logEvent("favorite_toggle") {
                            param("item_id", id)
                            param("is_favorite", isAdding.toString())
                        }
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

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.BottomStart
        ) {
            AsyncImage(
                model = "https://storage.googleapis.com/jb-chat-images/691d5755-e408-4171-be93-366a7b74f7be.png",
                contentDescription = "Event Hero Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 300f
                        )
                    )
            )
            Text(
                "Hops in the Hangar 2026",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        Text(
            text = "Welcome to Hops in the Hangar!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your ultimate Craft Beer & Airshow event app!",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Explore a lineup of vendors and sponsors, discover detailed venue information, find the best hotels nearby, enjoy exciting entertainment, and get to know the featured airshow performers.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "About the Event",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Craft beer, beverages, and aircraft come together to create not only a fun social event, but also an extremely unique community experience.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

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
        MainScreen()
    }
}
