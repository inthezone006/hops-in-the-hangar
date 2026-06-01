import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../event/event_data.dart';

class MapScreen extends StatefulWidget {
  const MapScreen({super.key});

  @override
  State<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends State<MapScreen> {
  final TransformationController _transformationController = TransformationController();
  MapCategory _activeCategory = MapCategory.all;
  MapSpot? _selectedSpot;

  static const Size _canvasSize = Size(1800, 1180);

  List<MapSpot> get _visibleSpots {
    final spots = eventContentController.mapSpots;
    if (_activeCategory == MapCategory.all) {
      return spots;
    }
    return spots.where((spot) => spot.category == _activeCategory).toList();
  }

  void _handleTap(TapDownDetails details) {
    final Offset scenePoint = _transformationController.toScene(details.localPosition);
    MapSpot? tappedSpot;
    double closestDistance = 42.0;

    for (final spot in _visibleSpots) {
      final Offset center = Offset(spot.left * _canvasSize.width, spot.top * _canvasSize.height);
      final Rect spotRect = Rect.fromCenter(
        center: center,
        width: spot.width * _canvasSize.width,
        height: spot.height * _canvasSize.height,
      );
      final double distance = spotRect.contains(scenePoint) ? 0 : (scenePoint - center).distance;
      if (distance < closestDistance) {
        closestDistance = distance;
        tappedSpot = spot;
      }
    }

    setState(() => _selectedSpot = tappedSpot);
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: eventContentController,
      builder: (context, _) {
        return Scaffold(
          appBar: AppBar(
        title: const Text('Interactive Venue Map'),
        actions: [
          IconButton(
            onPressed: () => setState(() => _selectedSpot = null),
            icon: const Icon(Icons.clear_rounded),
            tooltip: 'Clear selection',
          ),
        ],
      ),
          body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 10),
            child: Wrap(
              spacing: 10,
              runSpacing: 10,
              children: [
                _CategoryChip(
                  label: 'All',
                  selected: _activeCategory == MapCategory.all,
                  color: AppColors.hangarDarkBlue,
                  onTap: () => setState(() => _activeCategory = MapCategory.all),
                ),
                _CategoryChip(
                  label: 'Sponsors',
                  selected: _activeCategory == MapCategory.sponsors,
                  color: AppColors.hopGold,
                  onTap: () => setState(() => _activeCategory = MapCategory.sponsors),
                ),
                _CategoryChip(
                  label: 'Vendors',
                  selected: _activeCategory == MapCategory.vendors,
                  color: AppColors.hangarBlue,
                  onTap: () => setState(() => _activeCategory = MapCategory.vendors),
                ),
                _CategoryChip(
                  label: 'Food trucks',
                  selected: _activeCategory == MapCategory.foodTrucks,
                  color: AppColors.accentOrange,
                  onTap: () => setState(() => _activeCategory = MapCategory.foodTrucks),
                ),
                _CategoryChip(
                  label: 'Amenities',
                  selected: _activeCategory == MapCategory.amenities,
                  color: AppColors.accentGreen,
                  onTap: () => setState(() => _activeCategory = MapCategory.amenities),
                ),
                _CategoryChip(
                  label: 'Entertainment',
                  selected: _activeCategory == MapCategory.entertainment,
                  color: const Color(0xFF7D57C1),
                  onTap: () => setState(() => _activeCategory = MapCategory.entertainment),
                ),
              ],
            ),
          ),
          Expanded(
            child: GestureDetector(
              behavior: HitTestBehavior.opaque,
              onTapDown: _handleTap,
              child: InteractiveViewer(
                transformationController: _transformationController,
                minScale: 0.62,
                maxScale: 3.2,
                boundaryMargin: const EdgeInsets.all(240),
                child: Center(
                  child: SizedBox(
                    width: _canvasSize.width,
                    height: _canvasSize.height,
                    child: Stack(
                      children: [
                        Positioned.fill(
                          child: CustomPaint(
                            painter: HangarMapPainter(activeCategory: _activeCategory),
                          ),
                        ),
                        ..._visibleSpots.map((spot) {
                          final double width = spot.width * _canvasSize.width;
                          final double height = spot.height * _canvasSize.height;
                          final double left = (spot.left * _canvasSize.width) - width / 2;
                          final double top = (spot.top * _canvasSize.height) - height / 2;
                          final bool selected = _selectedSpot?.id == spot.id;

                          return Positioned(
                            left: left,
                            top: top,
                            child: GestureDetector(
                              onTap: () => setState(() => _selectedSpot = spot),
                              child: AnimatedContainer(
                                duration: const Duration(milliseconds: 180),
                                width: width,
                                height: height,
                                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                                decoration: BoxDecoration(
                                  color: spot.color.withValues(alpha: selected ? 0.98 : 0.94),
                                  borderRadius: BorderRadius.circular(18),
                                  border: Border.all(
                                    color: selected ? Colors.white : Colors.white.withValues(alpha: 0.38),
                                    width: selected ? 2.2 : 1,
                                  ),
                                  boxShadow: [
                                    BoxShadow(
                                      color: spot.color.withValues(alpha: 0.28),
                                      blurRadius: selected ? 18 : 10,
                                      offset: const Offset(0, 8),
                                    ),
                                  ],
                                ),
                                child: Row(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    Icon(spot.icon, size: 18, color: Colors.white),
                                    const SizedBox(width: 8),
                                    Expanded(
                                      child: Text(
                                        spot.name,
                                        overflow: TextOverflow.ellipsis,
                                        style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800, fontSize: 13),
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                          );
                        }),
                        const Positioned(left: 90, top: 66, child: _RegionLabel(label: 'Sponsor tents')),
                        const Positioned(left: 90, bottom: 154, child: _RegionLabel(label: 'Food truck lane')),
                        const Positioned(right: 100, top: 180, child: _RegionLabel(label: 'Amenities / merch / water')),
                        const Positioned(left: 690, top: 420, child: _RegionLabel(label: 'Entertainment court')),
                        const Positioned(left: 740, bottom: 92, child: _RegionLabel(label: 'Entrance')),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
          AnimatedContainer(
            duration: const Duration(milliseconds: 240),
            curve: Curves.easeOut,
            padding: const EdgeInsets.fromLTRB(16, 10, 16, 18),
            child: _selectedSpot == null
                ? Card(
                    child: Padding(
                      padding: const EdgeInsets.all(18),
                      child: Row(
                        children: const [
                          Icon(Icons.touch_app_rounded, color: AppColors.hangarBlue),
                          SizedBox(width: 12),
                          Expanded(
                            child: Text(
                              'Tap any marker to inspect a sponsor, food truck, restroom, or entertainment stop.',
                              style: TextStyle(height: 1.45),
                            ),
                          ),
                        ],
                      ),
                    ),
                  )
                : Card(
                    color: AppColors.hangarDarkBlue,
                    child: Padding(
                      padding: const EdgeInsets.all(18),
                      child: Row(
                        children: [
                          Container(
                            width: 56,
                            height: 56,
                            decoration: BoxDecoration(
                              color: Colors.white.withValues(alpha: 0.12),
                              borderRadius: BorderRadius.circular(18),
                            ),
                            child: Icon(_selectedSpot!.icon, color: Colors.white),
                          ),
                          const SizedBox(width: 14),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Text(
                                  _selectedSpot!.name,
                                  style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w800),
                                ),
                                const SizedBox(height: 4),
                                Text(
                                  _selectedSpot!.description ?? 'Interactive venue marker',
                                  style: TextStyle(color: Colors.white.withValues(alpha: 0.75), height: 1.35),
                                ),
                              ],
                            ),
                          ),
                          IconButton(
                            onPressed: () => setState(() => _selectedSpot = null),
                            icon: const Icon(Icons.close_rounded, color: Colors.white),
                          ),
                        ],
                      ),
                    ),
                  ),
          ),
        ],
      ),
        );
      },
    );
  }
}

class HangarMapPainter extends CustomPainter {
  HangarMapPainter({required this.activeCategory});

  final MapCategory activeCategory;

  @override
  void paint(Canvas canvas, Size size) {
    final Rect background = Rect.fromLTWH(0, 0, size.width, size.height);
    final Paint backgroundPaint = Paint()
      ..shader = LinearGradient(
        colors: [
          const Color(0xFFF8F1E4),
          const Color(0xFFF4EFE7),
          const Color(0xFFE6EEF8),
        ],
        begin: Alignment.topLeft,
        end: Alignment.bottomRight,
      ).createShader(background);

    canvas.drawRect(background, backgroundPaint);

    final Paint hangarFill = Paint()..color = const Color(0xFFFDFCF8);
    final Paint outline = Paint()
      ..color = AppColors.hangarDarkBlue.withValues(alpha: 0.55)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 4;

    final RRect hangar = RRect.fromRectAndRadius(
      Rect.fromLTWH(size.width * 0.05, size.height * 0.06, size.width * 0.82, size.height * 0.56),
      const Radius.circular(28),
    );
    canvas.drawRRect(hangar, hangarFill);
    canvas.drawRRect(hangar, outline);

    final RRect outside = RRect.fromRectAndRadius(
      Rect.fromLTWH(size.width * 0.05, size.height * 0.66, size.width * 0.90, size.height * 0.26),
      const Radius.circular(28),
    );
    canvas.drawRRect(outside, Paint()..color = const Color(0xFFF8EFD9));
    canvas.drawRRect(outside, outline);

    final Paint aislePaint = Paint()
      ..color = AppColors.hopGold.withValues(alpha: 0.12)
      ..style = PaintingStyle.fill;
    canvas.drawRRect(
      RRect.fromRectAndRadius(
        Rect.fromLTWH(size.width * 0.13, size.height * 0.19, size.width * 0.64, size.height * 0.26),
        const Radius.circular(22),
      ),
      aislePaint,
    );

    canvas.drawLine(
      Offset(size.width * 0.06, size.height * 0.90),
      Offset(size.width * 0.95, size.height * 0.90),
      Paint()
        ..color = AppColors.hangarDarkBlue.withValues(alpha: 0.55)
        ..strokeWidth = 3,
    );

    _drawText(canvas, 'HANGAR FLOOR', Offset(size.width * 0.08, size.height * 0.08), fontSize: 26, color: AppColors.hangarDarkBlue);
    _drawText(canvas, 'SPONSOR TENTS', Offset(size.width * 0.08, size.height * 0.68), fontSize: 22, color: AppColors.hangarBlue);
    _drawText(canvas, 'FOOD TRUCK LANE', Offset(size.width * 0.08, size.height * 0.93), fontSize: 22, color: AppColors.hangarBlue);
    _drawText(canvas, 'ENTERTAINMENT / ANNOUNCER', Offset(size.width * 0.57, size.height * 0.43), fontSize: 20, color: const Color(0xFF7D57C1));
    _drawText(canvas, 'RESTROOMS / WATER / MERCH', Offset(size.width * 0.78, size.height * 0.26), fontSize: 18, color: AppColors.accentGreen);

    _drawAircraft(canvas, Offset(size.width * 0.48, size.height * 0.34), size.width * 0.22, size.height * 0.10);
    _drawTables(canvas, size);
    _drawTentGrid(canvas, size);
    _drawUtilityBlocks(canvas, size);
  }

  void _drawTables(Canvas canvas, Size size) {
    final Paint tablePaint = Paint()
      ..color = AppColors.hangarDarkBlue.withValues(alpha: 0.72)
      ..strokeWidth = 4
      ..style = PaintingStyle.stroke;

    const List<Offset> tables = [
      Offset(0.22, 0.31),
      Offset(0.32, 0.35),
      Offset(0.40, 0.30),
      Offset(0.52, 0.34),
      Offset(0.62, 0.31),
      Offset(0.72, 0.36),
      Offset(0.26, 0.45),
      Offset(0.37, 0.49),
      Offset(0.49, 0.45),
      Offset(0.60, 0.49),
      Offset(0.71, 0.44),
    ];

    for (final table in tables) {
      final Offset center = Offset(size.width * table.dx, size.height * table.dy);
      final Rect top = Rect.fromCenter(center: center, width: 34, height: 10);
      final Rect seat1 = Rect.fromCenter(center: center + const Offset(-16, 15), width: 28, height: 8);
      final Rect seat2 = Rect.fromCenter(center: center + const Offset(16, 15), width: 28, height: 8);
      canvas.drawRect(top, tablePaint);
      canvas.drawRect(seat1, tablePaint);
      canvas.drawRect(seat2, tablePaint);
    }
  }

  void _drawTentGrid(Canvas canvas, Size size) {
    final Paint tentPaint = Paint()
      ..color = const Color(0xFFB0C2D8)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3;

    for (var row = 0; row < 3; row++) {
      for (var col = 0; col < 10; col++) {
        final double x = size.width * (0.09 + col * 0.081);
        final double y = size.height * (0.70 + row * 0.06);
        final Rect rect = Rect.fromLTWH(x, y, 40, 28);
        canvas.drawRRect(RRect.fromRectAndRadius(rect, const Radius.circular(6)), tentPaint);
      }
    }

    final Rect outsideTent = Rect.fromLTWH(size.width * 0.77, size.height * 0.11, 120, 60);
    canvas.drawRRect(RRect.fromRectAndRadius(outsideTent, const Radius.circular(8)), tentPaint);
  }

  void _drawUtilityBlocks(Canvas canvas, Size size) {
    final Paint utility = Paint()
      ..color = AppColors.accentGreen.withValues(alpha: 0.14)
      ..style = PaintingStyle.fill;
    final Paint outline = Paint()
      ..color = AppColors.accentGreen.withValues(alpha: 0.55)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.5;

    final Rect restroom = Rect.fromLTWH(size.width * 0.89, size.height * 0.70, size.width * 0.06, size.height * 0.08);
    final Rect water = Rect.fromLTWH(size.width * 0.77, size.height * 0.57, size.width * 0.06, size.height * 0.06);
    final Rect merch = Rect.fromLTWH(size.width * 0.83, size.height * 0.48, size.width * 0.06, size.height * 0.06);

    canvas.drawRRect(RRect.fromRectAndRadius(restroom, const Radius.circular(12)), utility);
    canvas.drawRRect(RRect.fromRectAndRadius(restroom, const Radius.circular(12)), outline);
    canvas.drawRRect(RRect.fromRectAndRadius(water, const Radius.circular(12)), utility);
    canvas.drawRRect(RRect.fromRectAndRadius(water, const Radius.circular(12)), outline);
    canvas.drawRRect(RRect.fromRectAndRadius(merch, const Radius.circular(12)), utility);
    canvas.drawRRect(RRect.fromRectAndRadius(merch, const Radius.circular(12)), outline);
  }

  void _drawAircraft(Canvas canvas, Offset center, double width, double height) {
    final Paint body = Paint()
      ..color = const Color(0xFFD95B63)
      ..style = PaintingStyle.fill;
    final Paint outline = Paint()
      ..color = Colors.white.withValues(alpha: 0.86)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 6;

    final Path fuselage = Path()
      ..moveTo(center.dx - width * 0.42, center.dy)
      ..lineTo(center.dx, center.dy - height * 0.36)
      ..lineTo(center.dx + width * 0.42, center.dy)
      ..lineTo(center.dx, center.dy + height * 0.36)
      ..close();

    final Path wing = Path()
      ..moveTo(center.dx - width * 0.28, center.dy - height * 0.12)
      ..lineTo(center.dx + width * 0.12, center.dy - height * 0.12)
      ..lineTo(center.dx + width * 0.28, center.dy + height * 0.18)
      ..lineTo(center.dx - width * 0.12, center.dy + height * 0.18)
      ..close();

    canvas.drawPath(fuselage, body);
    canvas.drawPath(wing, Paint()..color = const Color(0xFFF6F1EA));
    canvas.drawPath(fuselage, outline);
    canvas.drawPath(wing, outline);
  }

  void _drawText(Canvas canvas, String text, Offset offset, {required double fontSize, required Color color}) {
    final TextPainter painter = TextPainter(
      text: TextSpan(
        text: text,
        style: TextStyle(
          color: color,
          fontSize: fontSize,
          fontWeight: FontWeight.w900,
          letterSpacing: 0.8,
        ),
      ),
      textDirection: TextDirection.ltr,
    )..layout();
    painter.paint(canvas, offset);
  }

  @override
  bool shouldRepaint(covariant HangarMapPainter oldDelegate) => oldDelegate.activeCategory != activeCategory;
}

class _CategoryChip extends StatelessWidget {
  const _CategoryChip({required this.label, required this.selected, required this.color, required this.onTap});

  final String label;
  final bool selected;
  final Color color;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return ChoiceChip(
      label: Text(label),
      selected: selected,
      selectedColor: color.withValues(alpha: 0.18),
      backgroundColor: Colors.white,
      labelStyle: TextStyle(
        color: selected ? AppColors.hangarDarkBlue : AppColors.mutedText,
        fontWeight: FontWeight.w700,
      ),
      side: BorderSide(color: selected ? color : AppColors.lineColor),
      onSelected: (_) => onTap(),
    );
  }
}

class _RegionLabel extends StatelessWidget {
  const _RegionLabel({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 7),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.82),
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: AppColors.lineColor),
      ),
      child: Text(label, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w800, color: AppColors.hangarDarkBlue)),
    );
  }
}