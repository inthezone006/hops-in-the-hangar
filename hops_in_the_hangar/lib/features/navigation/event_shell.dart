import 'package:flutter/material.dart';

import '../admin/admin_screen.dart';
import '../home/home_screen.dart';
import '../map/map_screen.dart';
import '../schedule/schedule_screen.dart';
import '../sponsors/sponsors_screen.dart';

class EventShell extends StatefulWidget {
  const EventShell({super.key});

  @override
  State<EventShell> createState() => _EventShellState();
}

class _EventShellState extends State<EventShell> {
  int _selectedIndex = 0;

  static const List<Widget> _pages = [
    HomeScreen(),
    MapScreen(),
    ScheduleScreen(),
    SponsorsScreen(),
    AdminScreen(),
  ];

  static const List<NavigationDestination> _destinations = [
    NavigationDestination(icon: Icon(Icons.home_rounded), label: 'Home'),
    NavigationDestination(icon: Icon(Icons.map_rounded), label: 'Map'),
    NavigationDestination(icon: Icon(Icons.event_note_rounded), label: 'Schedule'),
    NavigationDestination(icon: Icon(Icons.handshake_rounded), label: 'Sponsors'),
    NavigationDestination(icon: Icon(Icons.admin_panel_settings_rounded), label: 'Admin'),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(index: _selectedIndex, children: _pages),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _selectedIndex,
        destinations: _destinations,
        onDestinationSelected: (index) => setState(() => _selectedIndex = index),
      ),
    );
  }
}