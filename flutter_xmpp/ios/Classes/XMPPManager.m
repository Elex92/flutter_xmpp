//
//  XMPPManager.m
//  CocoaAsyncSocket
//
//  Created by elex on 2020/7/17.
//

#import "XMPPManager.h"
#import "FYResult.h"
#define RESOURCE @"iOS"
@interface XMPPManager()<XMPPStreamDelegate,XMPPRosterDelegate>
//连接回调
@property (nonatomic,copy)ConnectResult connectResult;
//登录回调
@property (nonatomic,copy)LoginResult loginResult;
//注册回调
@property (nonatomic,copy)RegisterResult registerResult;
//接收回调
@property (nonatomic,copy)ReceiveMessage receiveMessage;
//刷新列表block
@property (nonatomic,copy)Refresh refresh;
//登录密码
@property (nonatomic,copy)NSString * password;
//域名
@property (nonatomic,copy)NSString * kDomin;
//将枚举设成属性
@property (nonatomic,assign)ConnectToServerPurpose connectToServerPurpose;
//通信通道,用于数据传输，要引入XMPPFramework.h头文件
@property (nonatomic,strong)XMPPStream *xmppStream;
//花名册，用于获取好友
@property (nonatomic,strong)XMPPRoster *xmppRoster;
//好友列表数组
@property (nonatomic,strong)NSMutableArray *friendArr;
//信息归档对象
@property (nonatomic,strong)XMPPMessageArchiving *xmppMessageArchiving;
//数据管理器（coredata）
@property (nonatomic,strong)NSManagedObjectContext *context;
@end
@implementation XMPPManager
#pragma mark ————— 初始化单例类 —————
+ (id)shareInstance {
    static XMPPManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[XMPPManager alloc] init];
    });
    return manager;
}
#pragma mark ————— 初始化xmpp —————
- (void)initXmppWithHostName:(NSString *)hostname andPort:(NSString *)port andDomin:(NSString *)domin withConnectResult:(ConnectResult)connectResult
{
    self.connectResult = connectResult;
    //初始化通信通道
    self.xmppStream = [[XMPPStream alloc]init];
    //给通信通道设置服务IP地址
    self.xmppStream.hostName = hostname;
    if (domin.length==0) {
        self.kDomin = hostname;
    }else{
        self.kDomin = domin;
    }
    
    //给通信通道设置端口
    self.xmppStream.hostPort = [port intValue];
    //给通信通道设置代理
    [self.xmppStream addDelegate:self delegateQueue:dispatch_get_main_queue()];
    //设置心跳时间
    [self.xmppStream setKeepAliveInterval:30];
 
    
    //断开重连模块
    XMPPReconnect * xmppReconnect = [[XMPPReconnect alloc]init];
    xmppReconnect.reconnectDelay = 0.f;//一旦失去连接，立马开始自动重连
    xmppReconnect.reconnectTimerInterval = 3.f;//每个三秒自动重连一次
    [xmppReconnect setAutoReconnect:YES];
    [xmppReconnect activate:self.xmppStream];
    [xmppReconnect addDelegate:self delegateQueue:dispatch_get_main_queue()];
    
    
    //创建花名册数据储存对象
    XMPPRosterCoreDataStorage * xmppRosterStorage = [XMPPRosterCoreDataStorage sharedInstance];
    //创建花名册并指定储存对象
    self.xmppRoster = [[XMPPRoster alloc]initWithRosterStorage:xmppRosterStorage dispatchQueue:dispatch_get_main_queue()];
    //激活通信通道
    [self.xmppRoster activate:self.xmppStream];
    //添加代理
    [self.xmppRoster addDelegate:self delegateQueue:dispatch_get_main_queue()];
    
    
    //单例，信息归档存储对象
    XMPPMessageArchivingCoreDataStorage *xmppMessageArchivingCoreDataStorage= [XMPPMessageArchivingCoreDataStorage sharedInstance];
    //创建信息归档对象
    self.xmppMessageArchiving = [[XMPPMessageArchiving alloc]initWithMessageArchivingStorage:xmppMessageArchivingCoreDataStorage dispatchQueue:dispatch_get_main_queue()];
    //激活通信通道
    [self.xmppMessageArchiving activate:self.xmppStream];
    
    //创建数据管理器
    self.context = xmppMessageArchivingCoreDataStorage.mainThreadManagedObjectContext;
    
  
}
- (void)disconnect
{
    if (self.xmppStream) {
        [self.xmppStream disconnect];
    }
}
#pragma mark ————— 发送消息 —————
-(void)sendMessageWithType:(NSString *)type andBody:(NSString *)body andTo:(NSString *)to andOtherArgs:(nonnull NSDictionary *)otherArgs
{
    XMPPJID *toJID = [XMPPJID jidWithUser:to domain:self.kDomin resource:RESOURCE];
    XMPPMessage * message = [XMPPMessage messageWithType:@"chat" to:toJID];
    [message addBody:body];
    [message addAttributeWithName:@"messageType" stringValue:type];
    
    //处理额外参数
    for (NSInteger i=0; i<otherArgs.allKeys.count; i++) {
        NSString * key = otherArgs.allKeys[i];
        [message addAttributeWithName:key stringValue:otherArgs[key]];
    }
    NSLog(@"%@",[message description]);
    [self.xmppStream sendElement:message];
}
#pragma mark ————— 用户登录 —————
- (void)loginWithUserID:(NSString *)userID withPassword:(NSString *)password withLoginResult:(LoginResult)loginResult
{
    self.connectToServerPurpose = ConnectToServerPurposeLogin;
    self.loginResult = loginResult;
    self.password = password;
    [self connectToServerWithUserID:userID];
}

#pragma mark ————— 用户注册 —————
- (void)registerWithUserID:(NSString *)userID withPassword:(NSString *)password withRegisterResult:(RegisterResult )registerResult {
    self.connectToServerPurpose = ConnectToServerPurposeRegister;
    self.password = password;
    self.registerResult = registerResult;
    //连接服务器
    [self connectToServerWithUserID:userID];
}

#pragma mark ————— 添加好友 —————
- (void)addFriendActionWithFriendName:(NSString *)friendName {
    //把账号封装成JID对象
    XMPPJID *addJID = [XMPPJID jidWithUser:friendName domain:self.kDomin resource:RESOURCE];
    //发送好友请求
    [self.xmppRoster addUser:addJID withNickname:nil];
}
#pragma mark ————— 新增接收消息监听 —————
-(void)addListenerReceiveMessageWithBlock:(ReceiveMessage)receiveMessage
{
    self.receiveMessage = receiveMessage;
}
#pragma mark ————— 连接openfire服务器 —————
- (void)connectToServerWithUserID:(NSString *)userID{
    //创建一个JID对象。每个JID对象代表的就是一个封装好的用户
    XMPPJID *jidItem = [XMPPJID jidWithUser:userID domain:self.kDomin resource:RESOURCE];
    //设置通信通道的JID
    self.xmppStream.myJID = jidItem;
    
    //如果通信通道正在连接或者已经连接
    if ([self.xmppStream isConnecting] || [self.xmppStream isConnected]) {
        //1.发送下线状态
        XMPPPresence *presence = [XMPPPresence presenceWithType:@"unavailable"];
        [self.xmppStream sendElement:presence];
        //2.断开连接
        [self.xmppStream disconnect];
    }
    
    //向服务器发送请求。连接成功会走代理方法
    NSError *error = nil;
    [self.xmppStream connectWithTimeout:-1 error:&error];
    if (error != nil) {
        if (self.connectResult) {
            self.connectResult([FYResult connectFailure]);
        }
        
        NSLog(@"连接错误");
    }
}

#pragma mark ————— XMPPStreamDelegate —————
-(void)xmppStreamDidConnect:(XMPPStream *)sender
{
    NSLog(@"连接成功");
    if (self.connectResult) {
        self.connectResult([FYResult connectSuccess]);
    }
   
    if (self.connectToServerPurpose == ConnectToServerPurposeLogin) {
        //连接成功。就可以进行登录操作了，验证账号和密码是否匹配
        [self.xmppStream authenticateWithPassword:self.password error:nil];
    }
    if (self.connectToServerPurpose == ConnectToServerPurposeRegister) {
        //连接成功。就可以进行注册操作了，
        [self.xmppStream registerWithPassword:self.password error:nil];
    }
}
- (void)xmppStreamConnectDidTimeout:(XMPPStream *)sender {
   
    if (self.connectResult) {
        self.connectResult([FYResult connectTimeout]);
       }
    NSLog(@"连接超时");
}
//断开连接
-(void)xmppStreamDidDisconnect:(XMPPStream *)sender withError:(NSError *)error
{
    if (self.connectResult) {
        self.connectResult([FYResult disconnect]);
    }
}
//登录验证回调
- (void)xmppStreamDidAuthenticate:(XMPPStream *)sender {
    NSLog(@"验证成功");
    //发送一个上线状态
    XMPPPresence *presence = [XMPPPresence presenceWithType:@"available"];
    [self.xmppStream sendElement:presence];
    if (self.loginResult) {
        self.loginResult(@"success");
    }
    

}
- (void)xmppStream:(XMPPStream *)sender didNotAuthenticate:(DDXMLElement *)error {
    NSLog(@"验证失败");
    if (self.loginResult) {
        self.loginResult(@"fail");
    }
    
}
//注册回调
-(void)xmppStreamDidRegister:(XMPPStream *)sender{
    NSLog(@"注册成功");
    if (self.registerResult) {
        self.registerResult(@"success");
    }
    
}
- (void)xmppStream:(XMPPStream *)sender didNotRegister:(NSXMLElement *)error{
    NSLog(@"注册失败");
    //用户已存在
    if ([[[error elementForName:@"error"]description]containsString:@"code=\"409\""]) {
        if (self.registerResult) {
               self.registerResult(@"exists");return;
           }
    }
    
    if (self.registerResult) {
        self.registerResult(@"fail");
    }
    
}
//接受到消息
-(void)xmppStream:(XMPPStream *)sender didReceiveMessage:(nonnull XMPPMessage *)message
{
    //对方正在输入
    if (message.hasComposingChatState) {
        
    }
    //收到内容
    if (message.isChatMessageWithBody) {
        if (self.receiveMessage) {
            NSDictionary * dic = @{
                @"from":message.from.user,
                @"to":message.to.user,
                @"type":message.type,
                @"body":message.body
            };
            self.receiveMessage(dic);
        }
    }
    
}
//收到错误消息
- (void)xmppStream:(XMPPStream *)sender didReceiveError:(DDXMLElement *)error
{
    NSString * conflict = [[error elementForName:@"conflict"]stringValue];
    //被挤下线
    if (conflict) {
       
    }
}
@end
