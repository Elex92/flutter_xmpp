package com.elex.flutter_xmpp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.flutter.app.FlutterActivity;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterXmppPlugin */
public class FlutterXmppPlugin extends FlutterActivity implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
  private static final String TAG ="flutter_xmpp";
  private MethodChannel channel;
  private EventChannel eventChannel;
  private BroadcastReceiver mBroadcastReceiver = null;
  private Context context;
  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    context = flutterPluginBinding.getApplicationContext();
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_xmpp");
    channel.setMethodCallHandler(this);

    eventChannel = new EventChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(),"flutter_xmpp/event");
    eventChannel.setStreamHandler(this);

  }



  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("init")) {

      String hostName = call.argument("hostName").toString();
      String domin = "";
      if (call.argument("domin")!=null){
        domin  = call.argument("domin").toString();
      }
      Integer port =  Integer.parseInt(call.argument("port").toString());

      Intent intent = new Intent(context,FlutterXmppConnectionService.class);
      intent.putExtra("hostName",hostName);
      intent.putExtra("domin",domin);
      intent.putExtra("port",port);
      context.startService(intent);

      result.success(FYResult.paramsComplete());

    }else if (call.method.equals("login")){
      Map<String,String> map = (Map) call.arguments;
      String userName = call.argument("userName").toString();
      String password = call.argument("password").toString();
      login(userName,password);
      result.success(FYResult.paramsComplete());
    }else if (call.method.equals("send")){
      Map<String,String> map = (Map) call.arguments;

      String messageType = call.argument("messageType").toString();
      String body = call.argument("body").toString();
      String to = call.argument("to").toString();
      Map args = call.argument("args");
      send_message(messageType,body,to,args);
      result.success(FYResult.paramsComplete());
    }else if (call.method.equals("disconnect")){

      Intent intent = new Intent(FlutterXmppConnectionService.DISCONNECT);
      context.sendBroadcast(intent);
      result.success(FYResult.paramsComplete());
    }else if (call.method.equals("register")){

      Map<String,String> map = (Map) call.arguments;
      String userName = call.argument("userName").toString();
      String password = call.argument("password").toString();
      register(userName,password);
      result.success(FYResult.paramsComplete());
    } else {
      result.notImplemented();
    }
  }

  /**
   *发送消息
   * @param messageType  消息类型
   * @param body 内容
   * @param to 接受者
   * @param args 扩展参数
   */
  private void send_message(String messageType, String body, String to, Map<String,String> args) {
    Log.d(TAG, "Current Status : " + FlutterXmppConnectionService.getState().toString());
    if (FlutterXmppConnectionService.getState().equals(FlutterXmppConnection.ConnectionState.CONNECTED)) {
      Bundle bundle = new Bundle();
      for (String key:args.keySet()) {
        bundle.putString(key,args.get(key));
      }
      Intent intent = new Intent(FlutterXmppConnectionService.SEND_MESSAGE);
      intent.putExtra(FlutterXmppConnectionService.BUNDLE_MESSAGE_TYPE, messageType);
      intent.putExtra(FlutterXmppConnectionService.BUNDLE_MESSAGE_BODY, body);
      intent.putExtra(FlutterXmppConnectionService.BUNDLE_TO, to);
      intent.putExtra(FlutterXmppConnectionService.BUNDLE_MESSAGE_PARAMS, bundle);
      context.sendBroadcast(intent);
    } else {

    }
  }
  /**
   * 登录
   * @param userName 用户名
   * @param password 密码
   */
  private void login(String userName, String password) {

    Intent intent = new Intent(FlutterXmppConnectionService.LOGIN);
    intent.putExtra(FlutterXmppConnectionService.USERNAME, userName);
    intent.putExtra(FlutterXmppConnectionService.PASSWORD, password);
    context.sendBroadcast(intent);
  }

  /**
   * 注册
   * @param userName 用户名
   * @param password 密码
   */
  private void register(String userName, String password) {

    Intent intent = new Intent(FlutterXmppConnectionService.REGISTER);
    intent.putExtra(FlutterXmppConnectionService.USERNAME, userName);
    intent.putExtra(FlutterXmppConnectionService.PASSWORD, password);
    context.sendBroadcast(intent);
  }

  private static BroadcastReceiver get_message(final EventChannel.EventSink events) {
    return new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
          case FlutterXmppConnectionService.RECEIVE_MESSAGE:
            Bundle bundle = intent.getExtras();
            String body = bundle.getString(FlutterXmppConnectionService.BUNDLE_MESSAGE_BODY);
            String to = bundle.getString(FlutterXmppConnectionService.BUNDLE_TO);
            String from = bundle.getString(FlutterXmppConnectionService.BUNDLE_FROM_JID);
            String type = bundle.getString(FlutterXmppConnectionService.BUNDLE_MESSAGE_TYPE);
            Log.d(TAG,intent.getExtras().toString());
            Map<String, Object> build = new HashMap<>();
            build.put("event","onMessage");
            build.put("type", type);
            build.put("to", to);
            build.put("from", from);
            build.put("body", body);
            events.success(build);
            break;
          case FlutterXmppConnectionService.CONNECT_STATUS:
            String code = intent.getStringExtra(FlutterXmppConnectionService.CODE);
            String message = intent.getStringExtra(FlutterXmppConnectionService.MESSAGE);
            Map<String, Object> connect = new HashMap<>();
            connect.put("event","onConnect");
            connect.put("code",code);
            connect.put("message",message);
            events.success(connect);
            break;
          case FlutterXmppConnectionService.LOGIN_STATUS:
            String logincode = intent.getStringExtra(FlutterXmppConnectionService.CODE);
            String loginmessage = intent.getStringExtra(FlutterXmppConnectionService.MESSAGE);
            Map<String, Object> loginStatus = new HashMap<>();
            loginStatus.put("event","onLogin");
            loginStatus.put("code",logincode);
            loginStatus.put("message",loginmessage);
            events.success(loginStatus);
            break;
          case FlutterXmppConnectionService.REGISTER_STATUS:
            String registercode = intent.getStringExtra(FlutterXmppConnectionService.CODE);
            String registermessage = intent.getStringExtra(FlutterXmppConnectionService.MESSAGE);
            Map<String, Object> registerStatus = new HashMap<>();
            registerStatus.put("event","onRegister");
            registerStatus.put("code",registercode);
            registerStatus.put("message",registermessage);
            events.success(registerStatus);
            break;

        }
      }
    };
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    eventChannel.setStreamHandler(null);
  }

  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {
    if (mBroadcastReceiver == null){
      mBroadcastReceiver = get_message(events);
      IntentFilter filter = new IntentFilter();
      filter.addAction(FlutterXmppConnectionService.RECEIVE_MESSAGE);
      filter.addAction(FlutterXmppConnectionService.CONNECT_STATUS);
      filter.addAction(FlutterXmppConnectionService.LOGIN_STATUS);
      context.registerReceiver(mBroadcastReceiver,filter);
    }
  }

  @Override
  public void onCancel(Object arguments) {
   if (mBroadcastReceiver!=null){
     context.unregisterReceiver(mBroadcastReceiver);
     mBroadcastReceiver = null;
   }
  }

}
