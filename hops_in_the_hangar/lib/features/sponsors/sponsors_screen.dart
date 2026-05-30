import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../event/event_data.dart';

class SponsorsScreen extends StatelessWidget {
  const SponsorsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: eventContentController,
      builder: (context, _) {
        return Scaffold(
          appBar: AppBar(title: const Text('Sponsors & Partners')),
          body: ListView(
            padding: const EdgeInsets.all(20),
            children: [
              Container(
                padding: const EdgeInsets.all(22),
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [AppColors.hangarDarkBlue, AppColors.hangarBlue],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  borderRadius: BorderRadius.circular(28),
                ),
                child: const Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Our 2026 sponsors',
                      style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.w800),
                    ),
                    SizedBox(height: 10),
                    Text(
                      'We are proud to partner with businesses, organizations, and individuals bringing Hops in the Hangar to life.',
                      style: TextStyle(color: Colors.white70, height: 1.45),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 18),
              ...eventContentController.sponsorCards.map(
                (sponsor) => Padding(
                  padding: const EdgeInsets.only(bottom: 12),
                  child: Card(
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundColor: AppColors.hopGold.withOpacity(0.16),
                        child: Icon(sponsor.icon, color: AppColors.hangarDarkBlue),
                      ),
                      title: Text(sponsor.name, style: const TextStyle(fontWeight: FontWeight.w800)),
                      subtitle: Text('${sponsor.tier}\n${sponsor.summary}'),
                      isThreeLine: true,
                      trailing: sponsor.featured ? const Icon(Icons.star_rounded, color: AppColors.hopGold) : null,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 8),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(18),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: const [
                      Text('Sponsor CTA', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800)),
                      SizedBox(height: 8),
                      Text(
                        'Use the sponsor website and social links from the live site for logos and click-throughs. This screen keeps the tiers readable inside the app.',
                        style: TextStyle(height: 1.45, color: AppColors.mutedText),
                      ),
                    ],
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