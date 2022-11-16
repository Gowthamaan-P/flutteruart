import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';


class FlutterUart{

  static const MethodChannel _channel = MethodChannel('flutteruart');
  static const EventChannel _eventChannel = EventChannel('flutteruart/stream');
  static Stream<Uint8List>? _inputStream;

  final int _baudRate = 57600;
  int get baudRate => _baudRate;

  static Stream<Uint8List>? get inputStream {
    _inputStream ??= _eventChannel.receiveBroadcastStream().map<Uint8List>((dynamic value) => value);
    return _inputStream;
  }

  /// Opens the uart communication channel.
  ///
  /// returns true if successful or false if failed.
  static Future<bool> begin(int uartId) async {
    return await _channel.invokeMethod("begin", {"uartId": uartId-1});
  }

  /// Closes the uart port.
  static Future<bool> close() async {
    return await _channel.invokeMethod("close");
  }

  /// Asynchronously writes [data].
  static Future<void> write(Uint8List data) async {
    return await _channel.invokeMethod("write", {"data": data});
  }
}
