import 'package:flutter/material.dart';

import 'theme/app_theme.dart';

import 'screens/home_screen.dart';
import 'screens/map_screen.dart';
import 'screens/entertainment_screen.dart';
import 'screens/sponsors_screen.dart';
import 'screens/vendors_screen.dart';


void main(){
  runApp(
    const HopsHangarApp()
  );
}


class HopsHangarApp extends StatelessWidget {
  const HopsHangarApp({super.key});

  @override
  Widget build(BuildContext context){
    return MaterialApp(
      debugShowCheckedModeBanner:false,
      title:"Hops In The Hangar",
      theme:AppTheme.darkTheme,
      home:const MainNavigation(),
    );
  }
}

class MainNavigation extends StatefulWidget{
  const MainNavigation({super.key});

  @override
  State<MainNavigation> createState()=>_MainNavigationState();
}

class _MainNavigationState extends State<MainNavigation>{
  int currentIndex=0;
  final pages=[
    const HomeScreen(),
    const MapScreen(),
    const EntertainmentScreen(),
    const SponsorsScreen(), 
    const VendorsScreen(),
  ];

  @override
  Widget build(BuildContext context){
    return Scaffold(
      body:pages[currentIndex],
      bottomNavigationBar:
      BottomNavigationBar(
        currentIndex:currentIndex,
        type:BottomNavigationBarType.fixed,
        onTap:(index){
          setState((){
            currentIndex=index;
          });
        },

        items: const[
          BottomNavigationBarItem(
            icon:Icon(Icons.home),
            label:"Home"
          ),

          BottomNavigationBarItem(
            icon:Icon(Icons.map),
            label:"Map"
          ),

          BottomNavigationBarItem(
            icon:Icon(Icons.music_note),
            label:"Events"
          ),

          BottomNavigationBarItem(
            icon:Icon(Icons.handshake),
            label:"Sponsors"
          ),

          BottomNavigationBarItem(
            icon:Icon(Icons.store),
            label:"Vendors"
          ),
        ],
      ),
    );
  }
}
