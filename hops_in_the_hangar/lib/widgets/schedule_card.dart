import 'package:flutter/material.dart';

class ScheduleCard extends StatelessWidget{

  final String title;
  final String time;
  final String location;

  const ScheduleCard({
    super.key,
    required this.title,
    required this.time,
    required this.location
  });

  @override
  Widget build(BuildContext context){
    return Card(
      child:
        ListTile(
          leading:
            const Icon(
              Icons.event,
              color:Color(0xffD4AF37)
            ),
            title:
              Text(title),
            subtitle:
              Text(
                "$time\n$location"
              ),
        )
    );
  }
}