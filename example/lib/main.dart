import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_with_ps/flutter_with_ps.dart';

void main() => runApp(new MyApp());
class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}


class _MyAppState extends State<MyApp> {
  Future<String> _emailAddress;
  String get fullname => _fullname.text;
  String _currentDeviceStateString = "";

  static final TextEditingController _fullname = new TextEditingController();


  FlutterWithPs _deviceState = new FlutterWithPs();

  String mapToString(map) {
    String string = "";
    for (String key in map.keys) {
      string += key + ":" + map[key].toString() +"\n";
    }
    return string;
  }


  @override
  initState() {
    super.initState();
    _deviceState.onDeviceStateUpdated.listen((Map<String,Object> result) {
          setState(() {
            _currentDeviceStateString = mapToString(result);
          });
        });
  }


  @override
  Widget build(BuildContext context) {
    String fullnameString;
    Widget textSection = new Container(
      padding: const EdgeInsets.all(32.0),
      child: new Text(
        _currentDeviceStateString,
        softWrap: true,
      ),
    );

    Widget searchSection = new Container(

      child: new Column(
        children: [
          new FutureBuilder<String>(
              future: _emailAddress,
              builder: (BuildContext context,
                  AsyncSnapshot<String> snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting)
                  return const Text('Searching local contact list...');

                else if(snapshot.connectionState == ConnectionState.done){
                  return new Text('${snapshot.data}',
                    style: new TextStyle(
                      fontWeight: FontWeight.bold,
                    ),
                  );
                }
                else {
                  return new Text("");
                }
              }),
          new TextField(
              keyboardType: TextInputType.text,
              decoration: new InputDecoration(
                hintText: 'Type in a fullname for a email address match..',
              ),
              onChanged: (newValue) => fullnameString = newValue
          ),
          new RaisedButton(
            onPressed: () {
              setState(() {
                _emailAddress = FlutterWithPs.emailAddress(fullnameString);
              });

            },
            child: new Text("Search"),
          ),

        ],
      ),
    );

    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar( title: new Text('Flutter with PrivacyStreams')),
        body: new ListView(
    children: [
    searchSection,
    textSection,
    ],
    ),
      )
    );
  }
}
