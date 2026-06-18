import 'package:flutter/material.dart';

class SponsorCard extends StatelessWidget{
  final String name;

  const SponsorCard({
    super.key,
    required this.name
  });

  @override
  Widget build(BuildContext context){
    return Card(
      child:
        Center(
          child:
            Text(
              name,
              textAlign:TextAlign.center,
              style: const TextStyle(
                fontWeight: FontWeight.bold
              )
            ),
        )
    );
  }
}