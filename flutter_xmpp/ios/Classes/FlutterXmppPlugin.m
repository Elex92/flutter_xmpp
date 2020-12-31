#import "FlutterXmppPlugin.h"
#import "XMPPManager.h"
#import "FYResult.h"
#define WeakSelf __weak typeof(self) weakSelf = self;
@interface FlutterXmppPlugin()<FlutterStreamHandler>
@property(nonatomic,strong)XMPPManager * xmppManager;
@property(nonatomic,strong)FlutterEventSink eventSink;
@property(nonatomic,copy) NSString * userName;
@property(nonatomic,copy) NSString * password;
@end

@implementation FlutterXmppPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"flutter_xmpp"
            binaryMessenger:[registrar messenger]];
  FlutterXmppPlugin* instance = [[FlutterXmppPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
  FlutterEventChannel * eventChannel = [FlutterEventChannel eventChannelWithName:@"flutter_xmpp/event" binaryMessenger:[registrar messenger]];
  [eventChannel setStreamHandler:instance];
  
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    //初始化xmpp
  if ([@"init" isEqualToString:call.method]) {
      self.xmppManager = [XMPPManager shareInstance];
      NSDictionary *dic = call.arguments;
      NSString * hostName = dic[@"hostName"];
      NSString * port = dic[@"port"];
      NSString * domin = dic[@"domin"];
      if (hostName.length==0) {
          result([FYResult noHostName]);
      }
      if (port.length==0) {
          result([FYResult noPort]);
      }
      if ([domin isKindOfClass:[NSNull class]]) {
          domin = @"";
      }

      [self.xmppManager initXmppWithHostName:hostName andPort:port andDomin:domin withConnectResult:^(NSDictionary * _Nonnull result) {
          NSMutableDictionary * dic = [NSMutableDictionary dictionary];
          dic[@"event"]  = @"onConnect";
          [dic addEntriesFromDictionary:result];
          if (self.eventSink) {
            self.eventSink(dic);
          }
         
      }];
      WeakSelf
      [self.xmppManager addListenerReceiveMessageWithBlock:^(NSDictionary * _Nonnull message) {
           NSMutableDictionary * dic = [NSMutableDictionary dictionary];
           dic[@"event"]  = @"onMessage";
           [dic addEntriesFromDictionary:message];
           if (weakSelf.eventSink) {
             weakSelf.eventSink(dic);
           }
       }];
      result([FYResult paramsComplete]);
   //登录
  }else if([@"login" isEqualToString:call.method]){
      NSDictionary *dic = call.arguments;
      
      [self loginActionWithArguments:dic andResult:result];
  }else if([@"register" isEqualToString:call.method]){
      NSDictionary *dic = call.arguments;
      
      [self registerActionWithArguments:dic andResult:result];
     
      
  }else if([@"send" isEqualToString:call.method]){
      NSDictionary *dic = call.arguments;
      NSString * messageType= dic[@"messageType"];
      NSString * body= dic[@"body"];
      NSString * to= dic[@"to"];
      NSDictionary * args = dic[@"args"];
      [self.xmppManager sendMessageWithType:messageType andBody:body andTo:to andOtherArgs:args];
     
  }else if([@"disconnect" isEqualToString:call.method]){
     
      [self.xmppManager disconnect];
     
  }else {
    result(FlutterMethodNotImplemented);
  }
}
#pragma mark ————— 注册 —————
-(void)registerActionWithArguments:(NSDictionary*)dic andResult:(FlutterResult)result
{
    NSString * userName = dic[@"userName"];
    NSString * password = dic[@"password"];
    if (userName.length==0) {
       result([FYResult noUserName]);
    }
    if (password.length==0) {
       result([FYResult noPassword]);
    }
    [self.xmppManager registerWithUserID:userName withPassword:password withRegisterResult:^(NSString * _Nonnull RegisterResult) {
        NSMutableDictionary * dic = [NSMutableDictionary dictionary];
        if ([RegisterResult isEqualToString:@"success"]) {
           
            dic[@"event"] = @"onRegister";
            if (self.eventSink) {
                [dic addEntriesFromDictionary:[FYResult registerSuccess]];
                self.eventSink(dic);
            }
           
        }else if ([RegisterResult isEqualToString:@"exists"]){
            dic[@"event"] = @"onRegister";
            if (self.eventSink) {
                [dic addEntriesFromDictionary:[FYResult registerUserExists]];
                self.eventSink(dic);
            }
            
        }else{
            dic[@"event"] = @"onRegister";
            if (self.eventSink) {
                [dic addEntriesFromDictionary:[FYResult registerFail]];
                self.eventSink(dic);
            }
            
            
        }
    }];
    result([FYResult paramsComplete]);
}
#pragma mark ————— 登录 —————
-(void)loginActionWithArguments:(NSDictionary*)dic andResult:(FlutterResult)result
{
    NSString * userName = dic[@"userName"];
    NSString * password = dic[@"password"];
    if (userName.length==0) {
        result([FYResult noUserName]);
    }
    if (password.length==0) {
        result([FYResult noPassword]);
    }
    [self.xmppManager loginWithUserID:userName withPassword:password withLoginResult:^(NSString * _Nonnull loginResult) {
         NSMutableDictionary * dic = [NSMutableDictionary dictionary];
        if ([loginResult isEqualToString:@"success"]) {
           
            dic[@"event"] = @"onLogin";
            if (self.eventSink) {
                [dic addEntriesFromDictionary:[FYResult loginSuccess]];
                self.eventSink(dic);
            }
           
        }else{
            if (self.eventSink) {
                [dic addEntriesFromDictionary:[FYResult loginFail]];
                self.eventSink(dic);
            }
            
        }
       
    }];
    result([FYResult paramsComplete]);
}
#pragma mark ————— FlutterStreamHandler —————
- (FlutterError* _Nullable)onListenWithArguments:(id _Nullable)arguments
                                       eventSink:(FlutterEventSink)eventSink{
    self.eventSink = eventSink;
    return nil;
}

- (FlutterError* _Nullable)onCancelWithArguments:(id _Nullable)arguments {
    return nil;
}

@end
