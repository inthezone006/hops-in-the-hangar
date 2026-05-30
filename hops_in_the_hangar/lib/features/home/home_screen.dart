import 'package:flutter/material.dart';
import '../../core/constants/app_colors.dart';
import '../map/map_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundLight,
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Hero Brand Branding Header Area
            Container(
              padding: const EdgeInsets.fromLTRB(24, 60, 24, 30),
              decoration: const BoxDecoration(
                color: AppColors.hangarDarkBlue,
                borderRadius: BorderRadius.only(
                  bottomLeft: Radius.circular(32),
                  bottomRight: Radius.circular(32),
                ),
              ),
              child: Column(
                children: [
                  // Logo Container Box
                  Container(
                    height: 100,
                    width: 100,
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: const Icon(Icons.airplanemode_active, size: 60, color: AppColors.hangarDarkBlue), 
                    // Replace with Image.asset('assets/images/logo.png') when asset is loaded
                  ),
                  const SizedBox(height: 16),
                  const Text(
                    "HOPS IN THE HANGAR",
                    style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold, letterSpacing: 1.5),
                  ),
                  const Text(
                    "Middletown Regional Airport • 2026",
                    style: TextStyle(color: AppColors.hopGold, fontSize: 14, fontWeight: FontWeight.w500),
                  ),
                ],
              ),
            ),

            // Dynamic Formatted Event Welcome Introduction Text Card
            Padding(
              padding: const EdgeInsets.all(20.0),
              child: Card(
                elevation: 2,
                color: AppColors.surfaceWhite,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                child: Padding(
                  padding: const EdgeInsets.all(20.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: const [
                      Text(
                        "Welcome to Hops in the Hangar, your ultimate Craft Beer & Airshow event app! Explore a lineup of vendors and sponsors, discover detailed venue information, find the best hotels nearby, enjoy exciting entertainment, and get to know the featured airshow performers.",
                        style: TextStyle(fontSize: 15, height: 1.5, fontWeight: FontWeight.w600, color: AppColors.hangarDarkBlue),
                      ),
                      SizedBox(height: 12),
                      Text(
                        "Craft beer, beverages, and aircraft come together to create not only a fun social event, but also an extremely unique community experience. Hops in the Hangar celebrates aviation, local businesses, and great craft beverages while bringing people together for an unforgettable evening at the Middletown Regional Airport.",
                        style: TextStyle(fontSize: 14, height: 1.5, color: Colors.black87),
                      ),
                      SizedBox(height: 12),
                      Text(
                        "Whether you're here for the thrilling air show performances, the incredible selection of breweries and beverage vendors, or simply to enjoy time with friends and family, this app will help you make the most of your experience. Stay connected with schedules, updates, event maps, and everything you need for an amazing experience at Hops in the Hangar 2026.",
                        style: TextStyle(fontSize: 14, height: 1.5, color: Colors.black87),
                      ),
                    ],
                  ),
                ),
              ),
            ),

            // Navigation Grid Options Selection
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24.0),
              child: Column(
                children: [
                  ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.hopGold,
                      foregroundColor: AppColors.hangarDarkBlue,
                      minimumSize: const Size.fromHeight(60),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      textStyle: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)
                    ),
                    onPressed: () {
                      Navigator.push(context, MaterialPageRoute(builder: (context) => const MapScreen()));
                    },
                    icon: const Icon(Icons.map_outlined),
                    label: const Text("EXPLORE INTERACTIVE MAP"),
                  ),
                  const SizedBox(height: 12),
                  // Additional placeholder menu options can safely branch off beneath here...
                ],
              ),
            ),
            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }
}