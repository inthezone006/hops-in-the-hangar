import 'package:flutter/material.dart';

enum VendorType { beerIndoor, beerOutdoor, foodTruck, amenity, entertainment }

class EventItem {
  final String id;
  final String name;
  final VendorType type;
  final Offset relativePosition;
  final String? details;
  final bool isVIP;
  final bool hasPower;

  const EventItem({
    required this.id,
    required this.name,
    required this.type,
    required this.relativePosition,
    this.details,
    this.isVIP = false,
    this.hasPower = false,
  });
}