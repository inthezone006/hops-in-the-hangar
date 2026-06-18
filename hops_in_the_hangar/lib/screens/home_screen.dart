import 'package:flutter/material.dart';
import '../widgets/menu_card.dart';

class HomeScreen extends StatelessWidget{
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context){
    return Scaffold(
      body: SafeArea(
        child:
          SingleChildScrollView(
            child: Column(
              children:[
                Container(
                  height:280,
                  width:double.infinity,
                  decoration: const BoxDecoration(
                    gradient: LinearGradient(
                      colors:[
                        Colors.black,
                        Color(0xff333333)
                      ],
                    )
                  ),
                  child: Center(
                    child: Text(
                      "HOPS\nIN THE\nHANGAR",
                      textAlign:TextAlign.center,
                      style: TextStyle(
                        fontSize:45,
                        fontWeight: FontWeight.bold
                      ),
                    ),
                  ),
                ),
                const SizedBox(height:20),
                Text(
                  "Indiana's Premier Aviation & Craft Beer Experience",
                  style: TextStyle(
                    fontSize:16,
                    color:Colors.grey
                  ),
                ),
                const SizedBox(height:30),
                  GridView.count(
                    shrinkWrap:true,
                    physics: const NeverScrollableScrollPhysics(),
                    crossAxisCount:2,
                    padding: const EdgeInsets.all(20),
                    children:[
                      MenuCard(
                        title:"Event Map",
                        icon:Icons.map,
                      ),
                      MenuCard(
                        title:"Entertainment",
                        icon:Icons.music_note,
                      ),
                      MenuCard(
                        title:"Sponsors",
                        icon:Icons.handshake,
                      ),
                      MenuCard(
                        title:"VIP Breweries",
                        icon:Icons.local_drink,
                      ),
                    ],
                  )
              ]
            )
          )
        )
    );
  }
}