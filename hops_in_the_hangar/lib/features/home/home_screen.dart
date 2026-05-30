import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../admin/admin_screen.dart';
import '../event/event_data.dart';
import '../map/map_screen.dart';
import '../schedule/schedule_screen.dart';
import '../sponsors/sponsors_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: eventContentController,
      builder: (context, _) {
        return Scaffold(
          body: Container(
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [Color(0xFFF8F4EC), Color(0xFFEAF2FB)],
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
              ),
            ),
            child: SafeArea(
              child: ListView(
                padding: const EdgeInsets.fromLTRB(20, 20, 20, 28),
                children: [
                  Container(
                    padding: const EdgeInsets.all(22),
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                        colors: [AppColors.hangarDarkBlue, AppColors.hangarBlue],
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                      ),
                      borderRadius: BorderRadius.circular(30),
                      boxShadow: const [
                        BoxShadow(color: Color(0x1F12233F), blurRadius: 20, offset: Offset(0, 10)),
                      ],
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: const [
                            _BrandMark(),
                            Spacer(),
                            Text('2026 event app', style: TextStyle(color: Colors.white70, fontSize: 12)),
                          ],
                        ),
                        const SizedBox(height: 8),
                        Align(
                          alignment: Alignment.centerRight,
                          child: Image.asset('assets/images/maf_logo.jpg', height: 28, fit: BoxFit.contain),
                        ),
                        const SizedBox(height: 6),
                        Align(
                          alignment: Alignment.centerRight,
                          child: Text(eventContentController.venueLine, style: const TextStyle(color: AppColors.hopGold, fontWeight: FontWeight.w700)),
                        ),
                        const SizedBox(height: 20),
                        Text(
                          eventContentController.heroHeadline,
                          style: const TextStyle(color: Colors.white, fontSize: 33, fontWeight: FontWeight.w900, height: 1.05),
                        ),
                        const SizedBox(height: 10),
                        Text(
                          eventContentController.heroSubtitle,
                          style: const TextStyle(color: Colors.white70, height: 1.45),
                        ),
                        const SizedBox(height: 18),
                        Wrap(
                          spacing: 10,
                          runSpacing: 10,
                          children: const [
                            _HeroChip(text: '30 breweries'),
                            _HeroChip(text: '90+ amazing brews'),
                            _HeroChip(text: '6 food trucks'),
                            _HeroChip(text: 'Tickets on sale'),
                          ],
                        ),
                        const SizedBox(height: 18),
                        Wrap(
                          spacing: 10,
                          runSpacing: 10,
                          children: [
                            FilledButton.icon(
                              onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const MapScreen())),
                              icon: const Icon(Icons.map_rounded),
                              label: const Text('Open map'),
                            ),
                            OutlinedButton.icon(
                              onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const ScheduleScreen())),
                              icon: const Icon(Icons.event_note_rounded),
                              label: const Text('View schedule'),
                            ),
                            OutlinedButton.icon(
                              onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const SponsorsScreen())),
                              icon: const Icon(Icons.handshake_rounded),
                              label: const Text('Sponsors'),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 18),
                  const _SectionTitle(
                    title: 'Plan your night',
                    subtitle: 'Everything guests need before they hit the hangar floor.',
                  ),
                  const SizedBox(height: 12),
                  Wrap(
                    spacing: 12,
                    runSpacing: 12,
                    children: [
                      _ActionCard(
                        title: 'Interactive map',
                        subtitle: 'Find breweries, food trucks, restrooms, water, merch, and show-day highlights on the custom canvas.',
                        icon: Icons.map_rounded,
                        accent: AppColors.hopGold,
                        onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const MapScreen())),
                      ),
                      _ActionCard(
                        title: 'Schedule & entertainment',
                        subtitle: 'See the live run of show, announcer notes, and the finale sequence.',
                        icon: Icons.event_note_rounded,
                        accent: AppColors.accentOrange,
                        onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const ScheduleScreen())),
                      ),
                      _ActionCard(
                        title: 'Sponsors & partners',
                        subtitle: 'Browse the 2026 sponsor tiers, brewery partners, and supporting businesses.',
                        icon: Icons.handshake_rounded,
                        accent: AppColors.hangarBlue,
                        onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const SponsorsScreen())),
                      ),
                      _ActionCard(
                        title: 'Admin console',
                        subtitle: 'Update map labels, announcement copy, and event timing from the control panel.',
                        icon: Icons.admin_panel_settings_rounded,
                        accent: AppColors.accentGreen,
                        onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const AdminScreen())),
                      ),
                    ],
                  ),
                  const SizedBox(height: 22),
                  const _SectionTitle(
                    title: 'Sponsor spotlight',
                    subtitle: 'The current sponsor set is grouped to match the website tiers.',
                  ),
                  const SizedBox(height: 12),
                  SizedBox(
                    height: 156,
                    child: ListView.separated(
                      scrollDirection: Axis.horizontal,
                      itemCount: eventContentController.sponsorCards.length,
                      separatorBuilder: (_, __) => const SizedBox(width: 12),
                      itemBuilder: (context, index) {
                        final sponsor = eventContentController.sponsorCards[index];
                        return _SponsorCard(sponsor: sponsor);
                      },
                    ),
                  ),
                  const SizedBox(height: 22),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(18),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: const [
                          Text('What this app covers', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800)),
                          SizedBox(height: 10),
                          Text('• Custom-drawn venue map for the hangar and outside tarmac', style: TextStyle(height: 1.55)),
                          Text('• Brewery, sponsor, food truck, and amenities placement', style: TextStyle(height: 1.55)),
                          Text('• Schedule, announcer notes, jump order, and fireworks finale', style: TextStyle(height: 1.55)),
                          Text('• Admin control screen for live content updates during the event', style: TextStyle(height: 1.55)),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 14),
                  TextButton(
                    onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => const AdminScreen())),
                    child: const Text('Open admin console'),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}

class _BrandMark extends StatelessWidget {
  const _BrandMark();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 84,
      height: 84,
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.14),
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: Colors.white.withOpacity(0.18)),
      ),
      child: Image.asset('assets/images/hith_logo.jpg', fit: BoxFit.contain),
    );
  }
}

class _HeroChip extends StatelessWidget {
  const _HeroChip({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 9),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.12),
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: Colors.white.withOpacity(0.12)),
      ),
      child: Text(text, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700)),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.title, required this.subtitle});

  final String title;
  final String subtitle;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w800)),
        const SizedBox(height: 6),
        Text(subtitle, style: const TextStyle(color: AppColors.mutedText, height: 1.4)),
      ],
    );
  }
}

class _ActionCard extends StatelessWidget {
  const _ActionCard({
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.accent,
    required this.onTap,
  });

  final String title;
  final String subtitle;
  final IconData icon;
  final Color accent;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final double cardWidth = MediaQuery.of(context).size.width >= 700 ? 324 : double.infinity;

    return SizedBox(
      width: cardWidth,
      child: Material(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        child: InkWell(
          borderRadius: BorderRadius.circular(24),
          onTap: onTap,
          child: Container(
            padding: const EdgeInsets.all(18),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(24),
              border: Border.all(color: AppColors.lineColor),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: accent.withOpacity(0.14),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Icon(icon, color: accent),
                ),
                const SizedBox(height: 14),
                Text(title, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w800)),
                const SizedBox(height: 8),
                Text(subtitle, style: const TextStyle(height: 1.45, color: AppColors.mutedText)),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _SponsorCard extends StatelessWidget {
  const _SponsorCard({required this.sponsor});

  final SponsorCard sponsor;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 240,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: sponsor.featured ? AppColors.hopGold : AppColors.lineColor, width: sponsor.featured ? 1.5 : 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 46,
            height: 46,
            decoration: BoxDecoration(
              color: AppColors.hopGold.withOpacity(0.15),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Icon(sponsor.icon, color: AppColors.hangarDarkBlue),
          ),
          const SizedBox(height: 12),
          Text(sponsor.name, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w800)),
          const SizedBox(height: 4),
          Text(sponsor.tier, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700, color: AppColors.hangarBlue)),
          const SizedBox(height: 8),
          Text(sponsor.summary, style: const TextStyle(height: 1.4, color: AppColors.mutedText)),
        ],
      ),
    );
  }
}