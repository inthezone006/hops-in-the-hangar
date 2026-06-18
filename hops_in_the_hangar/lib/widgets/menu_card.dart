import 'package:flutter/material.dart';

class MenuCard extends StatelessWidget{
  final String title;
  final IconData icon;

  const MenuCard({
    super.key,
    required this.title,
    required this.icon
  });

  @override
  Widget build(BuildContext context){
    return Card(
      margin:
      const EdgeInsets.all(10),
      child: Column(
        mainAxisAlignment:
        MainAxisAlignment.center,
        children:[
          Icon(
            icon,
            size:45,
            color: const Color(0xffD4AF37),
          ),
          const SizedBox(height:15),
          Text(
            title,
            style: const TextStyle(
              fontSize:16,
              fontWeight: FontWeight.bold
            ),
          )
        ]
      )
    );
  }
}