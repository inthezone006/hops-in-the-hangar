package com.rahul.hopsinthehangar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rahul.hopsinthehangar.ui.theme.HopsInTheHangarTheme

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val items = listOf(
        Screen.Home,
        Screen.Sponsors,
        Screen.Entertainment,
        Screen.Vendors,
        Screen.Map
    )
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Hops in the Hangar") },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label, maxLines = 1) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen },
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
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
                onClick = { /* TODO: Handle ticket click */ },
                icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                text = { Text("Tickets") },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedScreen) {
                Screen.Home -> HomeScreen()
                Screen.Sponsors -> SponsorsScreen()
                Screen.Entertainment -> EntertainmentScreen()
                Screen.Vendors -> VendorsScreen()
                Screen.Map -> MapScreen()
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
        Text(
            text = "Welcome to Hops in the Hangar!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
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

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
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
                    text = "Craft beer, beverages, and aircraft come together to create not only a fun social event, but also an extremely unique community experience. Hops in the Hangar celebrates aviation, local businesses, and great craft beverages while bringing people together for an unforgettable evening at the Middletown Regional Airport.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Hops in the Hangar 2026",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Whether you're here for the thrilling air show performances, the incredible selection of breweries and beverage vendors, or simply to enjoy time with friends and family, this app will help you make the most of your experience. Stay connected with schedules, updates, event maps, and everything you need for an amazing experience.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SponsorsScreen() {
    val sponsors = listOf(
        SponsorItem("Skyline Airways", "Diamond", "Official Flight Partner"),
        SponsorItem("Hoppy Valley Brewing", "Gold", "Providing the finest local craft beer"),
        SponsorItem("Middletown Community Bank", "Gold", "Proud supporter of local events"),
        SponsorItem("Aviation Tech Solutions", "Silver", "Innovation in the skies"),
        SponsorItem("Cloud Nine Hospitality", "Silver", "Premium event experiences"),
        SponsorItem("Hangar Refreshments", "Bronze", "Quality drinks for all guests")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Our Valued Sponsors",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sponsors) { sponsor ->
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(sponsor.name) },
                        supportingContent = { Text(sponsor.description) },
                        overlineContent = { Text(sponsor.level) },
                        leadingContent = {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (sponsor.level == "Diamond") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                        }
                    )
                }
            }
        }
    }
}

data class SponsorItem(val name: String, val level: String, val description: String)

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
                    leadingContent = {
                        Icon(Icons.Default.MusicNote, contentDescription = null)
                    }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Captain Clouds") },
                    overlineContent = { Text("Air Show Announcer") },
                    leadingContent = {
                        Icon(Icons.Default.Mic, contentDescription = null)
                    }
                )
            }
        }

        Text(
            text = "Jump Schedule",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
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
                        leadingContent = {
                            Icon(Icons.Default.Event, contentDescription = null)
                        }
                    )
                    if (index < schedule.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VendorsScreen() {
    val vendors = listOf(
        VendorItem("The Golden Hops", "Brewery", "Award-winning IPAs and Stouts"),
        VendorItem("Hangar Grill", "Food", "Gourmet burgers and aviation-themed sides"),
        VendorItem("Sky-High Crafts", "Merchandise", "Handmade aviation art and apparel"),
        VendorItem("Blue Sky Distillery", "Spirits", "Small-batch gin and vodka"),
        VendorItem("Taco Takeoff", "Food", "Authentic street tacos with a kick"),
        VendorItem("Propeller Pretzels", "Snacks", "Giant soft pretzels with beer cheese")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Event Vendors",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(vendors) { vendor ->
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(vendor.name) },
                        supportingContent = { Text(vendor.description) },
                        overlineContent = { Text(vendor.category) },
                        leadingContent = {
                            Icon(
                                when(vendor.category) {
                                    "Brewery" -> Icons.Default.ShoppingCart
                                    "Food" -> Icons.Default.ShoppingCart
                                    else -> Icons.Default.ShoppingCart
                                },
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Text("Map #$ { (vendors.indexOf(vendor) + 1) * 10 }")
                        }
                    )
                }
            }
        }
    }
}

data class VendorItem(val name: String, val category: String, val description: String)

@Composable
fun MapScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Event Map Coming Soon",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Interactive Middletown Regional Airport map",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    HopsInTheHangarTheme {
        MainScreen()
    }
}
