import 'package:flutter/material.dart';
import '../widgets/schedule_card.dart';

class EntertainmentScreen extends StatelessWidget{
  const EntertainmentScreen({super.key});
  
  @override
  Widget build(BuildContext context){
    return Scaffold(
      appBar:
        AppBar(
        title:
          const Text("Entertainment"),
        ),
      body:
        ListView(
          padding: const EdgeInsets.all(15),
            children:[
              const ScheduleCard(
                title:"DJ Performance",
                time:"6:00 PM",
                location:"Main Hangar Stage"
              ),
              const ScheduleCard(
                title:"Air Show",
                time:"7:30 PM",
                location:"Runway"
              ),
              const ScheduleCard(
                title:"Still Drums",
                time:"8:15 PM",
                location:"Entertainment Area"
              ),
              const ScheduleCard(
                title:"Fast Track Skydiving Team",
                time:"10:00 PM",
                location:"Fireworks Finale"
              ),
            ]
        )
    );
  }
}