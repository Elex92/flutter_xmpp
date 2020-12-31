import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class FlutterXmpp {
  static const MethodChannel _channel =
      const MethodChannel('flutter_xmpp');


  /**
   * 初始化xmpp数据
   * hostName:服务器地址
   * port:端口
   * domin:服务器域名
   */
  static Future<Map> init(String hostName,String port,{String domin}) async {
    Map map = Map();
    map["hostName"] = hostName;
    map["port"] = port;
    map["domin"] = domin;
    final Map result = await _channel.invokeMethod('init',map);
    return result;
  }

  /**
   * 登录
   * userName:用户名
   * password:密码
   */
  static Future<Map> login(String userName,String password) async{
    Map map = Map();
    map["userName"] = userName;
    map["password"] = password;
    final Map result = await _channel.invokeMethod('login',map);
    return result;
  }

  /**
   * 注册
   * userName:用户名
   * password:密码
   */
  static Future<Map> register(String userName,String password) async{
    Map map = Map();
    map["userName"] = userName;
    map["password"] = password;
    final Map result = await _channel.invokeMethod('register',map);
    return result;
  }

  /**
   * 断开连接
   */
  static void disconnect(){
    _channel.invokeMethod('disconnect');
  }

  /**
   * 发送消息
   * type:消息类型
   * body:消息内容
   * to:接受者
   * args:扩展参数
   */
  static Future<Map> send({String type="text",@required String body,@required String to,Map args}) async{
    Map map = Map();
    map["messageType"] = type;
    map["body"] = body;
    map["to"] = to;
    map["args"] = args;
    final Map result = await _channel.invokeMethod('send',map);
    return result;
  }

  /**
   * 添加监听
   * onConnect:连接回调
   * onLogin：登录回调
   * onRegister：注册回调
   * onReceiveMessage：收到消息回调
   */
  static addListener({Function onConnect,Function onLogin,Function onRegister,Function onReceiveMessage}){
    EventChannel eventChannel = EventChannel("flutter_xmpp/event");
    eventChannel.receiveBroadcastStream().listen((data) {
        var event = data["event"];
        if(event == "onConnect"){
          onConnect(data);
        }else if(event == "onMessage"){
          onReceiveMessage(data);
        }else if(event == "onLogin"){
          onLogin(data);
        }else if(event == "onRegister"){
          onRegister(data);
        }
    });
  }


}
