import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_xmpp/flutter_xmpp.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _status = '未连接';

  @override
  void initState() {
    super.initState();
    FlutterXmpp.init("192.168.0.127", "5222");
    FlutterXmpp.addListener(
        onConnect: (event){
          setState(() {
            _status = event["message"];
          });
        },
        onReceiveMessage: (event){
          print(event);
        },
        onLogin: (event){
          print(event);
        },
        onRegister: (event){
          print(event);
        }
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('xmpp'),
        ),
        body: Container(
          width: double.infinity,
          height: double.infinity,
          child: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Text("当前状态：${_status}"),
                RaisedButton(
                  child: Text("断开连接"),
                  onPressed: (){
                    FlutterXmpp.disconnect();
                  },
                ),
                RaisedButton(
                  child: Text("注册"),
                  onPressed: (){
                    register();
                  },
                ),
                RaisedButton(
                  child: Text("登录"),
                  onPressed: (){
                    login();
                  },
                ),
                RaisedButton(
                  child: Text("发送消息"),
                  onPressed: (){
                    sendMessage();
                  },
                )
              ],
            ),
          ),
        ),
      ),
    );
  }



  login(){
     FlutterXmpp.login("elex", "123456");

  }

  register() {
     FlutterXmpp.register("elex1", "123456");
  }

  sendMessage() {
    FlutterXmpp.send(body: "我是flutter", to: "test",type: "text",args: {"duration":"1111"});
  }
}
