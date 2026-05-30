import 'package:flutter/material.dart';
import 'core/constants/app_colors.dart';
import 'features/home/home_screen.dart';

class HopsHangarApp extends StatelessWidget {
  const HopsHangarApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Hops In The Hangar',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterialDesign: true,
        primaryColor: AppColors.hangarDarkBlue,
        scaffoldBackgroundColor: AppColors.backgroundLight,
      ),
      home: const HomeScreen(),
    );
  }
}