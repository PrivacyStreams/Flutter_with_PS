import 'dart:async';

import 'package:flutter/services.dart';

class FlutterWithPs {
  static const MethodChannel _channel =
      const MethodChannel("com.google.flutter/ps_method");
  static const EventChannel _stream = const EventChannel("com.google.flutter/ps_stream");

  Stream<Map<String,Object>> _onDeviceStateUpdated;

  static Future<String>  emailAddress (String fullname) =>
      _channel.invokeMethod('findEmailAddress', <String, Object>{
        'fullname': fullname});

  Stream<Map<String,Object>> get onDeviceStateUpdated {
    if (_onDeviceStateUpdated == null) {
      _onDeviceStateUpdated =
          _stream.receiveBroadcastStream();
    }
    return _onDeviceStateUpdated;
  }

}
