import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';


class FlutterUart{

  static const MethodChannel _channel = MethodChannel('flutteruart');
  static const EventChannel _eventChannel = EventChannel('flutteruart/stream');
  static Stream<Uint8List>? _inputStream;

  static int? _baudRate;
  static int get currentBaudRate => _baudRate??115200;
  static set currentBaudRate(newBaudRate) => _baudRate = newBaudRate;

  static int? _uartId;
  static int get currentUartId => _uartId??1;
  static set currentUartId(newUartId) => _uartId = newUartId;

  static Stream<Uint8List>? get inputStream {
    _inputStream ??= _eventChannel.receiveBroadcastStream().map<Uint8List>((dynamic value) => value);
    return _inputStream;
  }

  /// Opens the uart communication channel.
  ///
  /// returns true if successful or false if failed.
  static Future<bool> begin({int? uartId, int? baudRate}) async {
    baudRate ??= currentBaudRate;
    currentBaudRate = baudRate;

    uartId ??= currentUartId;
    currentUartId = uartId;
    return await _channel.invokeMethod("begin", {"uartId": "/dev/ttyMSM$uartId", "baudRate": baudRate });
  }

  /// Closes the uart port.
  static Future<bool> close() async {
    return await _channel.invokeMethod("close");
  }

  /// Asynchronously writes [data].
  static Future<void> write(Uint8List data) async {
    return await _channel.invokeMethod("write", {"data": data});
  }

  static Future<void> writeString(String data) async {
    return await _channel.invokeMethod("write", {"data": Uint8List.fromList(data.codeUnits)});
  }
}
