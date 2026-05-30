import 'package:flutter/material.dart';

import 'app.dart';
import 'features/event/event_data.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await eventContentController.initialize();
  runApp(const HopsHangarApp());
}
