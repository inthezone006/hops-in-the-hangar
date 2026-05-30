import 'package:flutter/material.dart';

import 'package:shared_preferences/shared_preferences.dart';

import '../../core/constants/app_colors.dart';
import '../event/event_data.dart';

class AdminScreen extends StatefulWidget {
  const AdminScreen({super.key});

  @override
  State<AdminScreen> createState() => _AdminScreenState();
}

class _AdminScreenState extends State<AdminScreen> {
  final TextEditingController _headlineController = TextEditingController();
  final TextEditingController _subtitleController = TextEditingController();
  final TextEditingController _venueController = TextEditingController();
  final TextEditingController _announcementController = TextEditingController();

  final TextEditingController _spotNameController = TextEditingController();
  final TextEditingController _spotDescriptionController = TextEditingController();
  final TextEditingController _scheduleTimeController = TextEditingController();
  final TextEditingController _scheduleTitleController = TextEditingController();
  final TextEditingController _scheduleDescriptionController = TextEditingController();
  final TextEditingController _scheduleLocationController = TextEditingController();
  final TextEditingController _sponsorNameController = TextEditingController();
  final TextEditingController _sponsorTierController = TextEditingController();
  final TextEditingController _sponsorSummaryController = TextEditingController();

  String _selectedSpotId = '';
  String _selectedScheduleKey = '';
  String _selectedSponsorName = '';
  MapCategory _spotCategory = MapCategory.sponsors;
  IconData _spotIcon = Icons.star_rounded;
  Color _spotColor = AppColors.hopGold;
  double _spotLeft = 0.5;
  double _spotTop = 0.5;
  bool _spotFeatured = false;
  bool _scheduleFeatured = false;
  IconData _scheduleIcon = Icons.event_note_rounded;
  IconData _sponsorIcon = Icons.star_rounded;
  bool _sponsorFeatured = false;

  @override
  void initState() {
    super.initState();
    _syncFromController();
    WidgetsBinding.instance.addPostFrameCallback((_) => _maybePromptLock());
  }

  @override
  void dispose() {
    _headlineController.dispose();
    _subtitleController.dispose();
    _venueController.dispose();
    _announcementController.dispose();
    _spotNameController.dispose();
    _spotDescriptionController.dispose();
    _scheduleTimeController.dispose();
    _scheduleTitleController.dispose();
    _scheduleDescriptionController.dispose();
    _scheduleLocationController.dispose();
    _sponsorNameController.dispose();
    _sponsorTierController.dispose();
    _sponsorSummaryController.dispose();
    super.dispose();
  }

  void _syncFromController() {
    _headlineController.text = eventContentController.heroHeadline;
    _subtitleController.text = eventContentController.heroSubtitle;
    _venueController.text = eventContentController.venueLine;
    _announcementController.text = eventContentController.announcement;

    final MapSpot spot = eventContentController.mapSpots.first;
    _loadSpot(spot);

    final ScheduleItem schedule = eventContentController.scheduleItems.first;
    _loadSchedule(schedule);

    final SponsorCard sponsor = eventContentController.sponsorCards.first;
    _loadSponsor(sponsor);
  }

  void _loadSpot(MapSpot spot) {
    _selectedSpotId = spot.id;
    _spotNameController.text = spot.name;
    _spotDescriptionController.text = spot.description ?? '';
    _spotCategory = spot.category;
    _spotIcon = spot.icon;
    _spotColor = spot.color;
    _spotLeft = spot.left;
    _spotTop = spot.top;
    _spotFeatured = spot.isFeatured;
  }

  void _loadSchedule(ScheduleItem item) {
    _selectedScheduleKey = item.time;
    _scheduleTimeController.text = item.time;
    _scheduleTitleController.text = item.title;
    _scheduleDescriptionController.text = item.description;
    _scheduleLocationController.text = item.location ?? '';
    _scheduleFeatured = item.featured;
    _scheduleIcon = item.icon;
  }

  void _loadSponsor(SponsorCard sponsor) {
    _selectedSponsorName = sponsor.name;
    _sponsorNameController.text = sponsor.name;
    _sponsorTierController.text = sponsor.tier;
    _sponsorSummaryController.text = sponsor.summary;
    _sponsorIcon = sponsor.icon;
    _sponsorFeatured = sponsor.featured;
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: eventContentController,
      builder: (context, _) {
        return Scaffold(
          appBar: AppBar(title: const Text('Admin Console')),
          body: ListView(
            padding: const EdgeInsets.all(20),
            children: [
              Card(
                color: AppColors.hangarDarkBlue,
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: const [
                      Text(
                        'Event control center',
                        style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.w800),
                      ),
                      SizedBox(height: 8),
                      Text(
                        'Edit the hero copy, venue map, schedule, and sponsor highlights from one live screen.',
                        style: TextStyle(color: Colors.white70, height: 1.45),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 18),
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: eventContentController.metrics
                    .map((metric) => _MetricCard(metric: metric))
                    .toList(),
              ),
              const SizedBox(height: 20),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(18),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text('Hero copy', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w700)),
                      const SizedBox(height: 14),
                      TextField(
                        controller: _headlineController,
                        decoration: const InputDecoration(labelText: 'Headline', border: OutlineInputBorder()),
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _subtitleController,
                        maxLines: 3,
                        decoration: const InputDecoration(labelText: 'Subtitle', border: OutlineInputBorder()),
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _venueController,
                        decoration: const InputDecoration(labelText: 'Venue line', border: OutlineInputBorder()),
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _announcementController,
                        maxLines: 3,
                        decoration: const InputDecoration(labelText: 'Live announcement', border: OutlineInputBorder()),
                      ),
                      const SizedBox(height: 14),
                      FilledButton.icon(
                        onPressed: () {
                          eventContentController.updateHero(
                            headline: _headlineController.text.trim(),
                            subtitle: _subtitleController.text.trim(),
                            venue: _venueController.text.trim(),
                          );
                          eventContentController.updateAnnouncement(_announcementController.text.trim());
                          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Hero copy updated')));
                        },
                        icon: const Icon(Icons.save_rounded),
                        label: const Text('Save hero copy'),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 18),
              _EditorCard(
                title: 'Map spot editor',
                subtitle: 'Adjust the canvas labels and positions that the venue map renders.',
                child: Column(
                  children: [
                    DropdownButtonFormField<String>(
                      value: _selectedSpotId,
                      decoration: const InputDecoration(labelText: 'Spot', border: OutlineInputBorder()),
                      items: eventContentController.mapSpots
                          .map((spot) => DropdownMenuItem(value: spot.id, child: Text(spot.name)))
                          .toList(),
                      onChanged: (value) {
                        if (value == null) return;
                        final MapSpot spot = eventContentController.mapSpots.firstWhere((item) => item.id == value);
                        setState(() => _loadSpot(spot));
                      },
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _spotNameController,
                      decoration: const InputDecoration(labelText: 'Name', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _spotDescriptionController,
                      maxLines: 2,
                      decoration: const InputDecoration(labelText: 'Description', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 12),
                    DropdownButtonFormField<MapCategory>(
                      value: _spotCategory,
                      decoration: const InputDecoration(labelText: 'Category', border: OutlineInputBorder()),
                      items: MapCategory.values
                          .map((category) => DropdownMenuItem(value: category, child: Text(category.name)))
                          .toList(),
                      onChanged: (value) => setState(() => _spotCategory = value ?? _spotCategory),
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text('Left'),
                              Slider(
                                value: _spotLeft,
                                onChanged: (value) => setState(() => _spotLeft = value),
                              ),
                            ],
                          ),
                        ),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text('Top'),
                              Slider(
                                value: _spotTop,
                                onChanged: (value) => setState(() => _spotTop = value),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                    SwitchListTile(
                      value: _spotFeatured,
                      contentPadding: EdgeInsets.zero,
                      title: const Text('Featured marker'),
                      onChanged: (value) => setState(() => _spotFeatured = value),
                    ),
                    const SizedBox(height: 6),
                    FilledButton.icon(
                      onPressed: () {
                        final MapSpot updated = MapSpot(
                          id: _selectedSpotId,
                          name: _spotNameController.text.trim(),
                          category: _spotCategory,
                          left: _spotLeft,
                          top: _spotTop,
                          icon: _spotIcon,
                          color: _spotColor,
                          description: _spotDescriptionController.text.trim(),
                          isFeatured: _spotFeatured,
                          width: 0.12,
                          height: 0.064,
                        );
                        eventContentController.updateSpot(_selectedSpotId, updated);
                        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Map spot updated')));
                      },
                      icon: const Icon(Icons.map_rounded),
                      label: const Text('Save map spot'),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 18),
              _EditorCard(
                title: 'Schedule editor',
                subtitle: 'Update announcer timing, jump order, and finale details.',
                child: Column(
                  children: [
                    DropdownButtonFormField<String>(
                      value: _selectedScheduleKey,
                      decoration: const InputDecoration(labelText: 'Schedule item', border: OutlineInputBorder()),
                      items: eventContentController.scheduleItems
                          .map((item) => DropdownMenuItem(value: item.time, child: Text('${item.time} - ${item.title}')))
                          .toList(),
                      onChanged: (value) {
                        if (value == null) return;
                        final ScheduleItem item = eventContentController.scheduleItems.firstWhere((entry) => entry.time == value);
                        setState(() => _loadSchedule(item));
                      },
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _scheduleTimeController,
                      decoration: const InputDecoration(labelText: 'Time', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _scheduleTitleController,
                      decoration: const InputDecoration(labelText: 'Title', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _scheduleDescriptionController,
                      maxLines: 3,
                      decoration: const InputDecoration(labelText: 'Description', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _scheduleLocationController,
                      decoration: const InputDecoration(labelText: 'Location', border: OutlineInputBorder()),
                    ),
                    SwitchListTile(
                      value: _scheduleFeatured,
                      contentPadding: EdgeInsets.zero,
                      title: const Text('Featured schedule item'),
                      onChanged: (value) => setState(() => _scheduleFeatured = value),
                    ),
                    FilledButton.icon(
                      onPressed: () {
                        final ScheduleItem updated = ScheduleItem(
                          time: _scheduleTimeController.text.trim(),
                          title: _scheduleTitleController.text.trim(),
                          description: _scheduleDescriptionController.text.trim(),
                          icon: _scheduleIcon,
                          location: _scheduleLocationController.text.trim(),
                          featured: _scheduleFeatured,
                        );
                        eventContentController.updateScheduleItem(_selectedScheduleKey, updated);
                        _selectedScheduleKey = updated.time;
                        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Schedule item updated')));
                      },
                      icon: const Icon(Icons.schedule_rounded),
                      label: const Text('Save schedule item'),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 18),
              _EditorCard(
                title: 'Sponsor editor',
                subtitle: 'Keep sponsor acknowledgements and spotlight cards in sync with the map.',
                child: Column(
                  children: [
                    DropdownButtonFormField<String>(
                      value: _selectedSponsorName,
                      decoration: const InputDecoration(labelText: 'Sponsor', border: OutlineInputBorder()),
                      items: eventContentController.sponsorCards
                          .map((sponsor) => DropdownMenuItem(value: sponsor.name, child: Text(sponsor.name)))
                          .toList(),
                      onChanged: (value) {
                        if (value == null) return;
                        final SponsorCard sponsor = eventContentController.sponsorCards.firstWhere((entry) => entry.name == value);
                        setState(() => _loadSponsor(sponsor));
                      },
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _sponsorNameController,
                      decoration: const InputDecoration(labelText: 'Name', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _sponsorTierController,
                      decoration: const InputDecoration(labelText: 'Tier', border: OutlineInputBorder()),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: _sponsorSummaryController,
                      maxLines: 3,
                      decoration: const InputDecoration(labelText: 'Summary', border: OutlineInputBorder()),
                    ),
                    SwitchListTile(
                      value: _sponsorFeatured,
                      contentPadding: EdgeInsets.zero,
                      title: const Text('Featured sponsor'),
                      onChanged: (value) => setState(() => _sponsorFeatured = value),
                    ),
                    FilledButton.icon(
                      onPressed: () {
                        final SponsorCard updated = SponsorCard(
                          name: _sponsorNameController.text.trim(),
                          tier: _sponsorTierController.text.trim(),
                          summary: _sponsorSummaryController.text.trim(),
                          icon: _sponsorIcon,
                          featured: _sponsorFeatured,
                        );
                        eventContentController.updateSponsorCard(_selectedSponsorName, updated);
                        _selectedSponsorName = updated.name;
                        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Sponsor updated')));
                      },
                      icon: const Icon(Icons.star_rounded),
                      label: const Text('Save sponsor'),
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
        },
      );
    }

    Future<void> _maybePromptLock() async {
      final SharedPreferences prefs = await SharedPreferences.getInstance();
      final String? pin = prefs.getString('admin_pin');
      if (pin == null) {
        await prefs.setString('admin_pin', '1234');
        return;
      }

      final TextEditingController entry = TextEditingController();
      final bool ok = await showDialog<bool>(
            context: context,
            barrierDismissible: false,
            builder: (context) => AlertDialog(
              title: const Text('Enter Admin PIN'),
              content: TextField(controller: entry, keyboardType: TextInputType.number, decoration: const InputDecoration(hintText: 'PIN')),
              actions: [
                TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Cancel')),
                FilledButton(onPressed: () => Navigator.of(context).pop(entry.text.trim() == pin), child: const Text('Unlock')),
              ],
            ),
          ) ?? false;

      if (!ok && mounted) {
        Navigator.of(context).maybePop();
      }
    }
}

class _MetricCard extends StatelessWidget {
  const _MetricCard({required this.metric});

  final AdminMetric metric;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 160,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.lineColor),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 42,
            height: 42,
            decoration: BoxDecoration(
              color: AppColors.hopGold.withOpacity(0.15),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(metric.icon, color: AppColors.hangarDarkBlue),
          ),
          const SizedBox(height: 10),
          Text(metric.value, style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w800)),
          Text(metric.label, style: const TextStyle(color: AppColors.mutedText)),
        ],
      ),
    );
  }
}

class _EditorCard extends StatelessWidget {
  const _EditorCard({required this.title, required this.subtitle, required this.child});

  final String title;
  final String subtitle;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w700)),
            const SizedBox(height: 6),
            Text(subtitle, style: const TextStyle(color: AppColors.mutedText, height: 1.4)),
            const SizedBox(height: 14),
            child,
          ],
        ),
      ),
    );
  }
}