import 'package:flutter/material.dart';
import '../widgets/sponsor_card.dart';

class SponsorsScreen extends StatelessWidget{
  const SponsorsScreen({super.key});

  @override
  Widget build(BuildContext context){
    final sponsors=[
      "Major Sponsor",
      "Hangar Sponsor",
      "Beer Sponsor",
      "Community Sponsor",
      "Aircraft Sponsor"
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text("Sponsors"),
      ),
      body:
        GridView.count(
          crossAxisCount:2,
          padding: const EdgeInsets.all(20),
          children:
            sponsors.map(
              (e) => SponsorCard(name:e)
            ).toList(),
        )
    );
  }
}