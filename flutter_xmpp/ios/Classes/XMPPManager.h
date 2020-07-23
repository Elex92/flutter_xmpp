//
//  XMPPManager.h
//  CocoaAsyncSocket
//
//  Created by elex on 2020/7/17.
//

#import <Foundation/Foundation.h>
#import "XMPPFramework.h"
NS_ASSUME_NONNULL_BEGIN
//枚举，用于区别登陆还是注册
//用ConnectToServerPurpose类型 去代替 NSInteger类型
typedef NS_ENUM(NSInteger , ConnectToServerPurpose) {
    //枚举值
    ConnectToServerPurposeLogin ,
    ConnectToServerPurposeRegister
};
typedef void (^ConnectResult)(NSDictionary * result);
typedef void(^LoginResult)(NSString *loginResult);
typedef void(^RegisterResult)(NSString *RegisterResult);
typedef void(^Refresh)(void);
typedef void (^ReceiveMessage)(NSDictionary * message);

@interface XMPPManager : NSObject

/// 初始单例
+ (id)shareInstance;


/// 实例化xmpp
/// @param hostname 服务地址
/// @param port 服务端口
/// @param domin 服务器域名 默认为hostname
-(void)initXmppWithHostName:(NSString*)hostname andPort:(NSString*)port andDomin:(NSString*)domin withConnectResult:(ConnectResult)connectResult;


/// 用户登录
/// @param userID 用户名
/// @param password 用户密码
/// @param loginResult 登录回调
- (void)loginWithUserID:(NSString *)userID withPassword:(NSString *)password withLoginResult:(LoginResult)loginResult ;


/// 用户注册
/// @param userID 用户名
/// @param password 用用户密码
/// @param registerResult 注册回调
- (void)registerWithUserID:(NSString *)userID withPassword:(NSString *)password withRegisterResult:(RegisterResult )registerResult;


/// 添加好友
/// @param friendName 好友名字
- (void)addFriendActionWithFriendName:(NSString *)friendName;

/// 断开连接
-(void)disconnect;


/// 添加接受消息监听
/// @param receiveMessage 收到消息回调
-(void)addListenerReceiveMessageWithBlock:(ReceiveMessage)receiveMessage;


/// 发送消息
/// @param type 消息类型
/// @param body 消息内容
/// @param to 接受者
/// @param otherArgs 扩展参数
-(void)sendMessageWithType:(NSString*)type andBody:(NSString*)body andTo:(NSString*)to andOtherArgs:(NSDictionary*)otherArgs;
@end

NS_ASSUME_NONNULL_END
