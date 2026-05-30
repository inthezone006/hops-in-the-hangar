import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:hops_in_the_hangar/app.dart';

void main() {
  testWidgets('loads the Hops in the Hangar home screen', (WidgetTester tester) async {
    await tester.pumpWidget(const HopsHangarApp());

    expect(find.text('Hops in the Hangar'), findsOneWidget);
    expect(find.text('Open map'), findsOneWidget);
    expect(find.text('Plan your night'), findsOneWidget);
  });
}
