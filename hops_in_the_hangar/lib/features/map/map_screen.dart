import 'package:flutter/material.dart';
import '../vendors/vendor_model.dart';
import '../../core/constants/app_colors.dart';

class MapScreen extends StatefulWidget {
  const MapScreen({super.key});

  @override
  State<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends State<MapScreen> {
  final TransformationController _transformationController = TransformationController();
  EventItem? _selectedItem;

  // Mocking layout coordinates based on 1.jpg (Hangar) and 2.jpg (Tarmac)
  final List<EventItem> mapItems = [
    // --- 1.jpg: Indoor Hangar Top Row ---
    EventItem(id: 'b1', name: 'New Ales', type: VendorType.beerIndoor, relativePosition: const Offset(0.10, 0.12), isVIP: true, hasPower: true),
    EventItem(id: 'b2', name: 'Street Side', type: VendorType.beerIndoor, relativePosition: const Offset(0.18, 0.12)),
    EventItem(id: 'b3', name: 'Loose Ends', type: VendorType.beerIndoor, relativePosition: const Offset(0.26, 0.12)),
    EventItem(id: 'b4', name: '50 W', type: VendorType.beerIndoor, relativePosition: const Offset(0.34, 0.12)),
    EventItem(id: 'b5', name: 'DBC', type: VendorType.beerIndoor, relativePosition: const Offset(0.42, 0.12)),
    EventItem(id: 'b6', name: '3rd Eye', type: VendorType.beerIndoor, relativePosition: const Offset(0.50, 0.12)),
    
    // --- 1.jpg: Center Elements ---
    EventItem(id: 'c1', name: 'War Birds Plane Display', type: VendorType.entertainment, relativePosition: const Offset(0.50, 0.45), details: 'Featured Aircraft Display'),
    EventItem(id: 'c2', name: 'Photo Op by Dog', type: VendorType.entertainment, relativePosition: const Offset(0.25, 0.42)),
    EventItem(id: 'c3', name: 'Pretzel Necklaces', type: VendorType.amenity, relativePosition: const Offset(0.70, 0.48)),

    // --- 2.jpg: Outdoor Food Trucks & Amenities ---
    EventItem(id: 'f1', name: "Schmidt's", type: VendorType.foodTruck, relativePosition: const Offset(0.20, 0.88)),
    EventItem(id: 'f2', name: "Mamma Bears", type: VendorType.foodTruck, relativePosition: const Offset(0.32, 0.88)),
    EventItem(id: 'f3', name: "Fatto a Mano", type: VendorType.foodTruck, relativePosition: const Offset(0.55, 0.78), hasPower: true),
    EventItem(id: 'f4', name: "El Diablo", type: VendorType.foodTruck, relativePosition: const Offset(0.68, 0.78)),
    EventItem(id: 'f5', name: "Little Chef Netty", type: VendorType.foodTruck, relativePosition: const Offset(0.58, 0.88)),
    EventItem(id: 'f6', name: "Graeters", type: VendorType.foodTruck, relativePosition: const Offset(0.70, 0.88)),
    EventItem(id: 'f7', name: "Hamburger Wagon", type: VendorType.foodTruck, relativePosition: const Offset(0.82, 0.88)),
    
    EventItem(id: 'a1', name: 'Porta Trailer / ADA', type: VendorType.amenity, relativePosition: const Offset(0.90, 0.85), details: 'Restroom Stations'),
    EventItem(id: 'e1', name: 'DJ Area', type: VendorType.entertainment, relativePosition: const Offset(0.72, 0.68), hasPower: true),
  ];

  void _handleTap(TapUpDetails details, Size canvasSize) {
    // Convert screen coordinate taps back into canvas relative coordinates
    final Offset localTap = details.localPosition;
    
    EventItem? tappedItem;
    double closestDistance = 25.0; // Hitbox radius threshold in pixels

    for (var item in mapItems) {
      final Offset itemPixelPos = Offset(
        item.relativePosition.dx * canvasSize.width,
        item.relativePosition.dy * canvasSize.height,
      );
      final double distance = (localTap - itemPixelPos).distance;
      if (distance < closestDistance) {
        closestDistance = distance;
        tappedItem = item;
      }
    }

    setState(() {
      _selectedItem = tappedItem;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Interactive Event Map'),
        backgroundColor: AppColors.hangarDarkBlue,
        foregroundColor: Colors.white,
      ),
      body: Stack(
        children: [
          // The Zoomable Canvas Interface
          GestureDetector(
            onTapUp: (details) => _handleTap(details, const Size(1000, 1200)),
            child: InteractiveViewer(
              transformationController: _transformationController,
              maxScale: 4.0,
              minScale: 0.5,
              child: Center(
                child: SizedBox(
                  width: 1000,
                  height: 1200,
                  child: CustomPaint(
                    painter: HangarMapPainter(items: mapItems),
                  ),
                ),
              ),
            ),
          ),
          
          // Selection Details HUD Banner Overlay
          if (_selectedItem != null)
            Positioned(
              bottom: 20,
              left: 20,
              right: 20,
              child: Card(
                color: AppColors.hangarDarkBlue,
                elevation: 8,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Row(
                    children: [
                      Icon(
                        _selectedItem!.type == VendorType.foodTruck ? Icons.fastfood : Icons.sports_bar,
                        color: AppColors.hopGold,
                        size: 36,
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Text(
                              _selectedItem!.name,
                              style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
                            ),
                            Text(
                              _selectedItem!.isVIP ? "👑 VIP Sponsor Location" : "Vendor Zone",
                              style: TextStyle(color: AppColors.hopGold.withValues(alpha: 0.9), fontSize: 14),
                            ),
                          ],
                        ),
                      ),
                      IconButton(
                        icon: const Icon(Icons.close, color: Colors.white),
                        onPressed: () => setState(() => _selectedItem = null),
                      )
                    ],
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

// Custom Painter drawing structural boundaries and plot points
class HangarMapPainter extends CustomPainter {
  final List<EventItem> items;
  HangarMapPainter({required this.items});

  @override
  void paint(Canvas canvas, Size size) {
    final Paint paintStructure = Paint()
      ..color = Colors.grey.shade400
      ..style = PaintingStyle.stroke
      ..strokeWidth = 4.0;

    final Paint paintHangarZone = Paint()
      ..color = Colors.blueGrey.shade50
      ..style = PaintingStyle.fill;

    // Draw Hangar Structural Boundary Wall (Upper Canvas Rectangle matching 1.jpg)
    final Rect hangarRect = Rect.fromLTWH(40, 40, size.width - 80, size.height * 0.55);
    canvas.drawRect(hangarRect, paintHangarZone);
    canvas.drawRect(hangarRect, paintStructure);

    // Render Pin Markers on Top of Floorplan
    for (var item in items) {
      final Offset position = Offset(item.relativePosition.dx * size.width, item.relativePosition.dy * size.height);
      
      Paint pinPaint = Paint()..style = PaintingStyle.fill;
      switch (item.type) {
        case VendorType.beerIndoor:
          pinPaint.color = AppColors.hopGold;
          break;
        case VendorType.foodTruck:
          pinPaint.color = AppColors.accentOrange;
          break;
        case VendorType.amenity:
          pinPaint.color = Colors.blue;
          break;
        case VendorType.entertainment:
          pinPaint.color = Colors.purple;
          break;
        default:
          pinPaint.color = Colors.grey;
      }

      // Base marker drop shadow accent
      canvas.drawCircle(position, 14.0, Paint()..color = Colors.black26);
      // Main node core
      canvas.drawCircle(position, 12.0, pinPaint);
    }
  }

  @override
  bool shouldRepaint(covariant HangarMapPainter oldDelegate) => false;
}