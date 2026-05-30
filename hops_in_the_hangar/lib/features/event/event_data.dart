import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

enum MapCategory { all, sponsors, vendors, foodTrucks, amenities, entertainment }

class MapSpot {
  const MapSpot({
    required this.id,
    required this.name,
    required this.category,
    required this.left,
    required this.top,
    required this.icon,
    required this.color,
    this.description,
    this.isFeatured = false,
    this.width = 0.125,
    this.height = 0.064,
  });

  final String id;
  final String name;
  final MapCategory category;
  final double left;
  final double top;
  final IconData icon;
  final Color color;
  final String? description;
  final bool isFeatured;
  final double width;
  final double height;

  MapSpot copyWith({
    String? id,
    String? name,
    MapCategory? category,
    double? left,
    double? top,
    IconData? icon,
    Color? color,
    String? description,
    bool? isFeatured,
    double? width,
    double? height,
  }) {
    return MapSpot(
      id: id ?? this.id,
      name: name ?? this.name,
      category: category ?? this.category,
      left: left ?? this.left,
      top: top ?? this.top,
      icon: icon ?? this.icon,
      color: color ?? this.color,
      description: description ?? this.description,
      isFeatured: isFeatured ?? this.isFeatured,
      width: width ?? this.width,
      height: height ?? this.height,
    );
  }

  Map<String, Object?> toJson() {
    return {
      'id': id,
      'name': name,
      'category': category.name,
      'left': left,
      'top': top,
      'icon': icon.codePoint,
      'color': color.value,
      'description': description,
      'isFeatured': isFeatured,
      'width': width,
      'height': height,
    };
  }

  factory MapSpot.fromJson(Map<String, Object?> json) {
    return MapSpot(
      id: json['id'] as String,
      name: json['name'] as String,
      category: MapCategory.values.byName(json['category'] as String),
      left: (json['left'] as num).toDouble(),
      top: (json['top'] as num).toDouble(),
      icon: iconFromCodePoint(json['icon'] as int),
      color: Color(json['color'] as int),
      description: json['description'] as String?,
      isFeatured: json['isFeatured'] as bool? ?? false,
      width: (json['width'] as num?)?.toDouble() ?? 0.125,
      height: (json['height'] as num?)?.toDouble() ?? 0.064,
    );
  }
}

class ScheduleItem {
  const ScheduleItem({
    required this.time,
    required this.title,
    required this.description,
    required this.icon,
    this.location,
    this.featured = false,
  });

  final String time;
  final String title;
  final String description;
  final String? location;
  final IconData icon;
  final bool featured;

  ScheduleItem copyWith({
    String? time,
    String? title,
    String? description,
    String? location,
    IconData? icon,
    bool? featured,
  }) {
    return ScheduleItem(
      time: time ?? this.time,
      title: title ?? this.title,
      description: description ?? this.description,
      icon: icon ?? this.icon,
      location: location ?? this.location,
      featured: featured ?? this.featured,
    );
  }

  Map<String, Object?> toJson() {
    return {
      'time': time,
      'title': title,
      'description': description,
      'location': location,
      'icon': icon.codePoint,
      'featured': featured,
    };
  }

  factory ScheduleItem.fromJson(Map<String, Object?> json) {
    return ScheduleItem(
      time: json['time'] as String,
      title: json['title'] as String,
      description: json['description'] as String,
      location: json['location'] as String?,
      icon: iconFromCodePoint(json['icon'] as int),
      featured: json['featured'] as bool? ?? false,
    );
  }
}

class SponsorCard {
  const SponsorCard({
    required this.name,
    required this.tier,
    required this.summary,
    required this.icon,
    this.featured = false,
  });

  final String name;
  final String tier;
  final String summary;
  final IconData icon;
  final bool featured;

  SponsorCard copyWith({
    String? name,
    String? tier,
    String? summary,
    IconData? icon,
    bool? featured,
  }) {
    return SponsorCard(
      name: name ?? this.name,
      tier: tier ?? this.tier,
      summary: summary ?? this.summary,
      icon: icon ?? this.icon,
      featured: featured ?? this.featured,
    );
  }

  Map<String, Object?> toJson() {
    return {
      'name': name,
      'tier': tier,
      'summary': summary,
      'icon': icon.codePoint,
      'featured': featured,
    };
  }

  factory SponsorCard.fromJson(Map<String, Object?> json) {
    return SponsorCard(
      name: json['name'] as String,
      tier: json['tier'] as String,
      summary: json['summary'] as String,
      icon: iconFromCodePoint(json['icon'] as int),
      featured: json['featured'] as bool? ?? false,
    );
  }
}

class AdminMetric {
  const AdminMetric({
    required this.label,
    required this.value,
    required this.icon,
  });

  final String label;
  final String value;
  final IconData icon;

  AdminMetric copyWith({
    String? label,
    String? value,
    IconData? icon,
  }) {
    return AdminMetric(
      label: label ?? this.label,
      value: value ?? this.value,
      icon: icon ?? this.icon,
    );
  }

  Map<String, Object?> toJson() {
    return {
      'label': label,
      'value': value,
      'icon': icon.codePoint,
    };
  }

  factory AdminMetric.fromJson(Map<String, Object?> json) {
    return AdminMetric(
      label: json['label'] as String,
      value: json['value'] as String,
      icon: iconFromCodePoint(json['icon'] as int),
    );
  }
}

const List<ScheduleItem> eventSchedule = [
  ScheduleItem(
    time: '4:30 PM',
    title: 'Doors open and check-in begins',
    description: 'Guests arrive, tickets scan in, and the hangar opens for the first wave of visitors.',
    icon: Icons.warehouse_rounded,
    location: 'Main hangar',
    featured: true,
  ),
  ScheduleItem(
    time: '5:00 PM',
    title: 'First pours and food trucks',
    description: 'Beer tents, food vendors, and the first round of crowd favorites are ready to go.',
    icon: Icons.queue_music_rounded,
    location: 'Hangar floor',
  ),
  ScheduleItem(
    time: '5:30 PM',
    title: 'Live entertainment starts',
    description: 'Music and announcements pick up as the evening crowd fills the hangar.',
    icon: Icons.surround_sound_rounded,
    location: 'Center stage',
  ),
  ScheduleItem(
    time: '6:00 PM',
    title: 'Air show introductions',
    description: 'The announcer welcomes guests and tees up the aircraft and flight team highlights.',
    icon: Icons.mic_rounded,
    location: 'Announcer stand',
  ),
  ScheduleItem(
    time: '6:30 PM',
    title: 'Jump order updates',
    description: 'The jump board updates throughout the evening so attendees can track each drop.',
    icon: Icons.paragliding_rounded,
    location: 'Event board',
  ),
  ScheduleItem(
    time: '7:15 PM',
    title: 'Airshow feature pass',
    description: 'Fast passes, aerial rolls, and crowd-favorite fly-bys over the hangar.',
    icon: Icons.flight_rounded,
    location: 'Show line',
    featured: true,
  ),
  ScheduleItem(
    time: '8:15 PM',
    title: 'Team Fast Track jump',
    description: 'The evening finale jump sequence lands right before the fireworks moment.',
    icon: Icons.rocket_launch_rounded,
    location: 'Jump zone',
    featured: true,
  ),
  ScheduleItem(
    time: '8:45 PM',
    title: 'Fireworks finale',
    description: 'Final thank-you and fireworks wrap the event with the last call for food and drinks.',
    icon: Icons.local_fire_department_rounded,
    location: 'West field',
    featured: true,
  ),
];

const List<SponsorCard> sponsorHighlights = [
  SponsorCard(
    name: 'Start Skydiving',
    tier: 'Top Flight Airshow Sponsor',
    summary: 'Lead sponsor of the 2026 event and the headline airshow experience.',
    icon: Icons.flight_takeoff_rounded,
    featured: true,
  ),
  SponsorCard(
    name: 'Team Fastrax',
    tier: 'First Class Sponsor',
    summary: 'One of the premium partners supporting the main show floor.',
    icon: Icons.paragliding_rounded,
  ),
  SponsorCard(
    name: 'Safe Skies Parts',
    tier: 'First Class Sponsor',
    summary: 'Premium sponsor helping bring the 2026 event to Middletown.',
    icon: Icons.shield_rounded,
  ),
  SponsorCard(
    name: 'Balloon Dog Events',
    tier: 'Business Class Sponsor',
    summary: 'Event experience partner supporting the guest journey.',
    icon: Icons.event_rounded,
  ),
  SponsorCard(
    name: 'Passport Sponsors',
    tier: 'Coach / Passport Class',
    summary: 'Logo group covering boarding passes, check-in, and guest materials.',
    icon: Icons.confirmation_number_rounded,
  ),
  SponsorCard(
    name: 'New Ales Brewing',
    tier: 'Sponsoring Brewery',
    summary: 'Brewing partner highlighted with the 2026 beer lineup.',
    icon: Icons.local_bar_rounded,
    featured: true,
  ),
];

const List<AdminMetric> adminMetrics = [
  AdminMetric(label: 'Breweries', value: '30', icon: Icons.emoji_food_beverage_rounded),
  AdminMetric(label: 'Amazing brews', value: '90+', icon: Icons.local_bar_rounded),
  AdminMetric(label: 'Food trucks', value: '6', icon: Icons.local_dining_rounded),
  AdminMetric(label: 'Event year', value: '2026', icon: Icons.event_rounded),
];

const List<MapSpot> venueSpots = [
  MapSpot(
    id: 'vip-sponsor',
    name: 'VIP Sponsor Tent',
    category: MapCategory.sponsors,
    left: 0.74,
    top: 0.10,
    icon: Icons.star_rounded,
    color: Color(0xFFF3B23A),
    description: 'Premium sponsor placement with visibility from the center aisle.',
    isFeatured: true,
    width: 0.15,
  ),
  MapSpot(
    id: 'pilot-tent',
    name: 'Pilot\'s Tent',
    category: MapCategory.sponsors,
    left: 0.42,
    top: 0.07,
    icon: Icons.airplanemode_active_rounded,
    color: Color(0xFF7AA6D8),
    description: 'Pilot check-in and pre-show gathering point.',
    width: 0.12,
  ),
  MapSpot(
    id: 'anderson',
    name: 'Anderson',
    category: MapCategory.sponsors,
    left: 0.34,
    top: 0.14,
    icon: Icons.local_convenience_store_rounded,
    color: Color(0xFF94A3B8),
    width: 0.10,
  ),
  MapSpot(
    id: 'grainworks',
    name: 'Grainworks',
    category: MapCategory.vendors,
    left: 0.67,
    top: 0.18,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFEDC15A),
    description: 'Beer vendor and sponsor highlight near the VIP corridor.',
    width: 0.13,
  ),
  MapSpot(
    id: 'new-ales',
    name: 'New Ales',
    category: MapCategory.vendors,
    left: 0.10,
    top: 0.12,
    icon: Icons.star_rounded,
    color: Color(0xFFF3B23A),
    description: 'Primary beer sponsor in the upper hangar row.',
    isFeatured: true,
  ),
  MapSpot(
    id: 'street-side',
    name: 'Street Side',
    category: MapCategory.vendors,
    left: 0.18,
    top: 0.12,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
  ),
  MapSpot(
    id: 'loose-ends',
    name: 'Loose Ends',
    category: MapCategory.vendors,
    left: 0.27,
    top: 0.12,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
  ),
  MapSpot(
    id: 'fifty-w',
    name: '50 W',
    category: MapCategory.vendors,
    left: 0.36,
    top: 0.12,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
  ),
  MapSpot(
    id: 'dbc',
    name: 'DBC',
    category: MapCategory.vendors,
    left: 0.45,
    top: 0.12,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
  ),
  MapSpot(
    id: 'third-eye',
    name: '3rd Eye',
    category: MapCategory.vendors,
    left: 0.54,
    top: 0.12,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
  ),
  MapSpot(
    id: 'surfside',
    name: 'Surfside',
    category: MapCategory.vendors,
    left: 0.18,
    top: 0.24,
    icon: Icons.beach_access_rounded,
    color: Color(0xFF7BD2F2),
  ),
  MapSpot(
    id: 'march-first',
    name: 'March First',
    category: MapCategory.vendors,
    left: 0.27,
    top: 0.24,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
  ),
  MapSpot(
    id: 'fat-head',
    name: 'Fat Head',
    category: MapCategory.vendors,
    left: 0.36,
    top: 0.24,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
  ),
  MapSpot(
    id: 'skimmers',
    name: 'Skimmers',
    category: MapCategory.vendors,
    left: 0.45,
    top: 0.24,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
  ),
  MapSpot(
    id: 'brewers-assoc',
    name: 'Brewers Assoc',
    category: MapCategory.vendors,
    left: 0.54,
    top: 0.24,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
  ),
  MapSpot(
    id: 'municipal',
    name: 'Municipal',
    category: MapCategory.vendors,
    left: 0.64,
    top: 0.12,
    icon: Icons.location_city_rounded,
    color: Color(0xFF8B9BB4),
    width: 0.11,
  ),
  MapSpot(
    id: 'country-boy',
    name: 'Country Boy',
    category: MapCategory.vendors,
    left: 0.89,
    top: 0.14,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
    width: 0.12,
  ),
  MapSpot(
    id: 'heavier-than-air',
    name: 'Heavier Than Air',
    category: MapCategory.vendors,
    left: 0.89,
    top: 0.24,
    icon: Icons.flight_takeoff_rounded,
    color: Color(0xFF7AA6D8),
    width: 0.13,
  ),
  MapSpot(
    id: 'warped-wing',
    name: 'Warped Wing',
    category: MapCategory.vendors,
    left: 0.89,
    top: 0.34,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
    width: 0.12,
  ),
  MapSpot(
    id: 'bock',
    name: 'Bock',
    category: MapCategory.vendors,
    left: 0.89,
    top: 0.44,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
    width: 0.10,
  ),
  MapSpot(
    id: 'sonder',
    name: 'Sonder',
    category: MapCategory.vendors,
    left: 0.89,
    top: 0.54,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
    width: 0.11,
  ),
  MapSpot(
    id: 'carbliss',
    name: 'Carbliss',
    category: MapCategory.vendors,
    left: 0.89,
    top: 0.64,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
    width: 0.11,
  ),
  MapSpot(
    id: 'brink',
    name: 'Brink',
    category: MapCategory.vendors,
    left: 0.89,
    top: 0.74,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
    width: 0.10,
  ),
  MapSpot(
    id: 'crafted-culture',
    name: 'Crafted Culture',
    category: MapCategory.sponsors,
    left: 0.07,
    top: 0.84,
    icon: Icons.local_drink_rounded,
    color: Color(0xFFEDC15A),
    width: 0.14,
  ),
  MapSpot(
    id: 'toxic',
    name: 'Toxic',
    category: MapCategory.sponsors,
    left: 0.16,
    top: 0.84,
    icon: Icons.local_drink_rounded,
    color: Color(0xFFEDC15A),
    width: 0.08,
  ),
  MapSpot(
    id: 'gravel-road',
    name: 'Gravel Road',
    category: MapCategory.sponsors,
    left: 0.27,
    top: 0.84,
    icon: Icons.route_rounded,
    color: Color(0xFF8B9BB4),
    width: 0.12,
  ),
  MapSpot(
    id: 'mom-dad-water',
    name: 'Mom / Dad Water',
    category: MapCategory.amenities,
    left: 0.35,
    top: 0.84,
    icon: Icons.water_drop_rounded,
    color: Color(0xFF48A3D4),
    description: 'Water station for guests staying hydrated through the evening.',
    width: 0.14,
  ),
  MapSpot(
    id: 'high-esoteric-grain',
    name: 'High Esoteric Grain',
    category: MapCategory.sponsors,
    left: 0.47,
    top: 0.84,
    icon: Icons.wb_twilight_rounded,
    color: Color(0xFFEDC15A),
    width: 0.15,
  ),
  MapSpot(
    id: '21-barrels',
    name: '21 Barrels',
    category: MapCategory.vendors,
    left: 0.57,
    top: 0.84,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
    width: 0.10,
  ),
  MapSpot(
    id: 'dead-low',
    name: 'Dead Low',
    category: MapCategory.vendors,
    left: 0.69,
    top: 0.84,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
    width: 0.11,
  ),
  MapSpot(
    id: 'nutrl',
    name: 'Nutrl',
    category: MapCategory.vendors,
    left: 0.79,
    top: 0.84,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
    width: 0.09,
  ),
  MapSpot(
    id: 'mad-tree',
    name: 'Mad Tree',
    category: MapCategory.vendors,
    left: 0.89,
    top: 0.84,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
    width: 0.10,
  ),
  MapSpot(
    id: 'cut-water',
    name: 'Cut Water',
    category: MapCategory.vendors,
    left: 0.89,
    top: 0.92,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFF3B23A),
    width: 0.11,
  ),
  MapSpot(
    id: 'schmidts',
    name: "Schmidt's",
    category: MapCategory.foodTrucks,
    left: 0.14,
    top: 0.92,
    icon: Icons.local_dining_rounded,
    color: Color(0xFFE56B2E),
    description: 'Food truck along the lower outside lane.',
    width: 0.13,
  ),
  MapSpot(
    id: 'mamma-bears',
    name: 'Mamma Bears',
    category: MapCategory.foodTrucks,
    left: 0.23,
    top: 0.92,
    icon: Icons.local_dining_rounded,
    color: Color(0xFFE56B2E),
    width: 0.13,
  ),
  MapSpot(
    id: 'fatto-a-mano',
    name: 'Fatto a Mano',
    category: MapCategory.foodTrucks,
    left: 0.55,
    top: 0.08,
    icon: Icons.local_pizza_rounded,
    color: Color(0xFFE56B2E),
    description: 'Featured food truck near the pilot tent.',
    width: 0.13,
  ),
  MapSpot(
    id: 'el-diablo',
    name: 'El Diablo',
    category: MapCategory.foodTrucks,
    left: 0.67,
    top: 0.08,
    icon: Icons.local_dining_rounded,
    color: Color(0xFFE56B2E),
    width: 0.11,
  ),
  MapSpot(
    id: 'little-chef-netty',
    name: 'Little Chef Netty',
    category: MapCategory.foodTrucks,
    left: 0.57,
    top: 0.92,
    icon: Icons.fastfood_rounded,
    color: Color(0xFFE56B2E),
    width: 0.15,
  ),
  MapSpot(
    id: 'graeters',
    name: 'Graeters',
    category: MapCategory.foodTrucks,
    left: 0.72,
    top: 0.92,
    icon: Icons.icecream_rounded,
    color: Color(0xFFE56B2E),
    width: 0.11,
  ),
  MapSpot(
    id: 'hamburger-wagon',
    name: 'Hamburger Wagon',
    category: MapCategory.foodTrucks,
    left: 0.86,
    top: 0.92,
    icon: Icons.lunch_dining_rounded,
    color: Color(0xFFE56B2E),
    width: 0.16,
  ),
  MapSpot(
    id: 'porta-trailer',
    name: 'Porta Trailer',
    category: MapCategory.amenities,
    left: 0.95,
    top: 0.74,
    icon: Icons.wc_rounded,
    color: Color(0xFF48A3D4),
    description: 'Portable restroom and ADA access zone.',
    width: 0.10,
  ),
  MapSpot(
    id: 'ada-porta',
    name: 'ADA Porta',
    category: MapCategory.amenities,
    left: 0.95,
    top: 0.82,
    icon: Icons.accessible_rounded,
    color: Color(0xFF48A3D4),
    width: 0.10,
  ),
  MapSpot(
    id: 'water-station',
    name: 'Water',
    category: MapCategory.amenities,
    left: 0.79,
    top: 0.62,
    icon: Icons.water_drop_rounded,
    color: Color(0xFF48A3D4),
    width: 0.10,
  ),
  MapSpot(
    id: 'merch',
    name: 'Merch',
    category: MapCategory.amenities,
    left: 0.84,
    top: 0.52,
    icon: Icons.shopping_bag_rounded,
    color: Color(0xFF48A3D4),
    width: 0.10,
  ),
  MapSpot(
    id: 'dj-booth',
    name: 'DJ Booth',
    category: MapCategory.entertainment,
    left: 0.72,
    top: 0.68,
    icon: Icons.queue_music_rounded,
    color: Color(0xFF7D57C1),
    description: 'Live music and evening transitions.',
    isFeatured: true,
    width: 0.12,
  ),
  MapSpot(
    id: 'airshow-announcer',
    name: 'Air Show Announcer',
    category: MapCategory.entertainment,
    left: 0.49,
    top: 0.47,
    icon: Icons.mic_rounded,
    color: Color(0xFF7D57C1),
    description: 'Live commentary over the main display floor.',
    isFeatured: true,
    width: 0.16,
  ),
  MapSpot(
    id: 'still-drums',
    name: 'Still Drums',
    category: MapCategory.entertainment,
    left: 0.60,
    top: 0.36,
    icon: Icons.surround_sound_rounded,
    color: Color(0xFF7D57C1),
    width: 0.11,
  ),
  MapSpot(
    id: 'fast-track-jump',
    name: 'Team Fast Track Jump',
    category: MapCategory.entertainment,
    left: 0.51,
    top: 0.25,
    icon: Icons.paragliding_rounded,
    color: Color(0xFF7D57C1),
    description: 'Final jump team spot before the fireworks finale.',
    isFeatured: true,
    width: 0.17,
  ),
  MapSpot(
    id: 'fireworks-finale',
    name: 'Fireworks Finale',
    category: MapCategory.entertainment,
    left: 0.86,
    top: 0.20,
    icon: Icons.local_fire_department_rounded,
    color: Color(0xFF7D57C1),
    description: 'End-of-night celebration by the west field.',
    isFeatured: true,
    width: 0.14,
  ),
  MapSpot(
    id: 'photo-op',
    name: 'Photo Op',
    category: MapCategory.entertainment,
    left: 0.29,
    top: 0.42,
    icon: Icons.photo_camera_rounded,
    color: Color(0xFF7D57C1),
    width: 0.10,
  ),
  MapSpot(
    id: 'safe-skies',
    name: 'Safe Skies',
    category: MapCategory.sponsors,
    left: 0.11,
    top: 0.84,
    icon: Icons.verified_rounded,
    color: Color(0xFFEDC15A),
    width: 0.11,
  ),
  MapSpot(
    id: 'dafuque',
    name: 'Dafuque',
    category: MapCategory.sponsors,
    left: 0.42,
    top: 0.84,
    icon: Icons.local_bar_rounded,
    color: Color(0xFFEDC15A),
    width: 0.10,
  ),
];

class EventContentController extends ChangeNotifier {
  EventContentController._();

  static final EventContentController instance = EventContentController._();
  static const String _storageKey = 'event_content_v1';
  bool _isLoaded = false;

  String heroHeadline = 'Hops in the Hangar';
  String heroSubtitle = 'The epic craft beer and aircraft event in Southwest Ohio. Explore the hangar, breweries, food trucks, sponsors, and the live event schedule.';
  String venueLine = 'August 22, 2026 · Middletown Regional Airport';
  String announcement = 'Tickets are on sale now. Use the map to find breweries, food trucks, restrooms, merch, and the show line.';

  List<ScheduleItem> scheduleItems = List<ScheduleItem>.from(eventSchedule);
  List<SponsorCard> sponsorCards = List<SponsorCard>.from(sponsorHighlights);
  List<AdminMetric> metrics = List<AdminMetric>.from(adminMetrics);
  List<MapSpot> mapSpots = List<MapSpot>.from(venueSpots);

  Future<void> initialize() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final String? raw = prefs.getString(_storageKey);
    if (raw != null && raw.isNotEmpty) {
      _applyJson(jsonDecode(raw) as Map<String, Object?>);
    }
    _isLoaded = true;
  }

  void updateHero({String? headline, String? subtitle, String? venue}) {
    if (headline != null) {
      heroHeadline = headline;
    }
    if (subtitle != null) {
      heroSubtitle = subtitle;
    }
    if (venue != null) {
      venueLine = venue;
    }
    notifyListeners();
    unawaited(_save());
  }

  void updateAnnouncement(String value) {
    announcement = value;
    notifyListeners();
    unawaited(_save());
  }

  void updateSpot(String id, MapSpot updated) {
    mapSpots = mapSpots.map((spot) => spot.id == id ? updated : spot).toList(growable: false);
    notifyListeners();
    unawaited(_save());
  }

  void addSpot(MapSpot spot) {
    mapSpots = [...mapSpots, spot];
    notifyListeners();
    unawaited(_save());
  }

  void updateScheduleItem(String id, ScheduleItem updated) {
    scheduleItems = scheduleItems.map((item) => item.time == id ? updated : item).toList(growable: false);
    notifyListeners();
    unawaited(_save());
  }

  void updateSponsorCard(String name, SponsorCard updated) {
    sponsorCards = sponsorCards.map((card) => card.name == name ? updated : card).toList(growable: false);
    notifyListeners();
    unawaited(_save());
  }

  void updateMetric(String label, AdminMetric updated) {
    metrics = metrics.map((metric) => metric.label == label ? updated : metric).toList(growable: false);
    notifyListeners();
    unawaited(_save());
  }

  MapSpot? spotById(String id) => mapSpots.where((spot) => spot.id == id).cast<MapSpot?>().firstOrNull;
  ScheduleItem? scheduleByTime(String time) => scheduleItems.where((item) => item.time == time).cast<ScheduleItem?>().firstOrNull;
  SponsorCard? sponsorByName(String name) => sponsorCards.where((card) => card.name == name).cast<SponsorCard?>().firstOrNull;

  Map<String, Object?> _toJson() {
    return {
      'heroHeadline': heroHeadline,
      'heroSubtitle': heroSubtitle,
      'venueLine': venueLine,
      'announcement': announcement,
      'scheduleItems': scheduleItems.map((item) => item.toJson()).toList(),
      'sponsorCards': sponsorCards.map((card) => card.toJson()).toList(),
      'metrics': metrics.map((metric) => metric.toJson()).toList(),
      'mapSpots': mapSpots.map((spot) => spot.toJson()).toList(),
    };
  }

  void _applyJson(Map<String, Object?> json) {
    heroHeadline = json['heroHeadline'] as String? ?? heroHeadline;
    heroSubtitle = json['heroSubtitle'] as String? ?? heroSubtitle;
    venueLine = json['venueLine'] as String? ?? venueLine;
    announcement = json['announcement'] as String? ?? announcement;

    final List<Object?> rawSchedule = json['scheduleItems'] as List<Object?>? ?? const [];
    final List<Object?> rawSponsors = json['sponsorCards'] as List<Object?>? ?? const [];
    final List<Object?> rawMetrics = json['metrics'] as List<Object?>? ?? const [];
    final List<Object?> rawSpots = json['mapSpots'] as List<Object?>? ?? const [];

    scheduleItems = rawSchedule
        .whereType<Map>()
        .map((value) => ScheduleItem.fromJson(Map<String, Object?>.from(value.cast<String, Object?>())))
        .toList(growable: false);
    sponsorCards = rawSponsors
        .whereType<Map>()
        .map((value) => SponsorCard.fromJson(Map<String, Object?>.from(value.cast<String, Object?>())))
        .toList(growable: false);
    metrics = rawMetrics
        .whereType<Map>()
        .map((value) => AdminMetric.fromJson(Map<String, Object?>.from(value.cast<String, Object?>())))
        .toList(growable: false);
    mapSpots = rawSpots
        .whereType<Map>()
        .map((value) => MapSpot.fromJson(Map<String, Object?>.from(value.cast<String, Object?>())))
        .toList(growable: false);
  }

  Future<void> _save() async {
    if (!_isLoaded) {
      return;
    }
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    await prefs.setString(_storageKey, jsonEncode(_toJson()));
  }
}

final EventContentController eventContentController = EventContentController.instance;

extension<T> on Iterable<T> {
  T? get firstOrNull => isEmpty ? null : first;
}

IconData iconFromCodePoint(int codePoint) => IconData(codePoint, fontFamily: 'MaterialIcons');
