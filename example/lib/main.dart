import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutteruart/flutteruart.dart';
import 'package:flutteruart/transaction.dart';


void main() => runApp(const MyApp());

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  MyAppState createState() => MyAppState();
}

class MyAppState extends State<MyApp> {

  late List<Widget> _serialData;

  StreamSubscription<Uint8List>? _subscription;
  Transaction<Uint8List>? _transaction;


  final TextEditingController _textController = TextEditingController();

  void initiateCommunicationPorts() async {
    _transaction = Transaction.magicHeader(FlutterUart.inputStream as Stream<Uint8List>, [0X52, 0X50]);

    _subscription = _transaction!.stream.listen((Uint8List line) {
      setState(() {
        _serialData.add(Text("$line"));
        if (_serialData.length > 20) {
          _serialData.removeAt(0);
        }
      });
    });
  }

  void disposeCommunicationPorts(){
    _serialData.clear();

    if (_subscription != null) {
      _subscription!.cancel();
      _subscription = null;
    }

    if (_transaction != null) {
      _transaction!.dispose();
      _transaction = null;
    }
  }

  void beginUartCommunication()async{
    await FlutterUart.begin(uartId: 1, baudRate: 115200).then((value){
      if(value){
        initiateCommunicationPorts();
      }
    });
  }

  void closeUartCommunication()async{
    await FlutterUart.close();
  }

  @override
  void initState() {
    super.initState();
    _serialData = [];
    beginUartCommunication();
  }

  @override
  void dispose() {
    super.dispose();
    disposeCommunicationPorts();
    closeUartCommunication();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        home: Scaffold(
          appBar: AppBar(
            title: const Text('Uart Plugin Example App'),
          ),
          body: Center(
              child: Column(children: <Widget>[

                ListTile(
                  title: TextField(
                    controller: _textController,
                    decoration: const InputDecoration(
                      border: OutlineInputBorder(),
                      labelText: 'Text To Send',
                    ),
                  ),
                  trailing: ElevatedButton(
                    onPressed: () async {
                      String data = "${_textController.text}\r\n";
                      await FlutterUart.write(Uint8List.fromList(data.codeUnits));
                      _textController.text = "";
                    },
                    child: const Text("Send"),
                  ),
                ),
                Text("Result Data", style: Theme.of(context).textTheme.headline6),
                ..._serialData,
              ])),
        ));
  }
}
