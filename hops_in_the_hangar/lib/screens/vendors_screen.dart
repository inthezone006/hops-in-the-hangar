import 'package:flutter/material.dart';

class VendorsScreen extends StatelessWidget{
  const VendorsScreen({super.key});

  @override
  Widget build(BuildContext context){
    return Scaffold(
      appBar: AppBar(
        title: const Text("Vendors"),
      ),
      body:
        const Center(
          child:
            Text(
              "30 Indoor Vendors\n\n8 Food Trucks\n\n2 Outdoor Vendors",
              textAlign: TextAlign.center,
              style: TextStyle(fontSize:22),
            ),
        )
    );
  }
}