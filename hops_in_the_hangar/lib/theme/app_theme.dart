import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';


class AppTheme{
  static ThemeData darkTheme = ThemeData(
    brightness:Brightness.dark,
    scaffoldBackgroundColor: const Color(0xff0B0B0B),
    primaryColor: const Color(0xffD4AF37),
    textTheme:GoogleFonts.poppinsTextTheme(
      ThemeData.dark().textTheme,
    ),
    
    appBarTheme: const AppBarTheme(
      backgroundColor: Color(0xff0B0B0B),
      elevation:0,
      centerTitle:true,
    ),

    cardTheme: CardThemeData(
      color: const Color(0xff171717),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20)
      )
    ),
  );
}