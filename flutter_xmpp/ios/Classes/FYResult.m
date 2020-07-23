//
//  FYResult.m
//  CocoaAsyncSocket
//
//  Created by elex on 2020/7/17.
//

#import "FYResult.h"

@implementation FYResult
+(NSDictionary*)paramsComplete
{
    return @{@"code":@"200",@"message":@"参数齐全"};
}
+(NSDictionary*)loginSuccess
{
    return @{@"code":@"200",@"message":@"登录成功"};
}
+(NSDictionary*)loginFail
{
    return @{@"code":@"205",@"message":@"登录失败"};
}
+(NSDictionary*)registerSuccess
{
    return @{@"code":@"200",@"message":@"注册成功"};
}
+(NSDictionary*)registerFail
{
    return @{@"code":@"206",@"message":@"注册失败"};
}
+(NSDictionary*)registerUserExists
{
    return @{@"code":@"409",@"message":@"用户已存在"};
}
+(NSDictionary*)connectSuccess
{
    return @{@"code":@"200",@"message":@"连接成功"};
}
+(NSDictionary*)connectTimeout
{
    return @{@"code":@"400",@"message":@"连接超时"};
}
+ (NSDictionary *)disconnect
{
    return @{@"code":@"401",@"message":@"连接断开"};
}
+(NSDictionary*)conflict
{
    return @{@"code":@"402",@"message":@"被挤下线"};
}
+(NSDictionary*)connectFailure
{
    return @{@"code":@"404",@"message":@"连接失败"};
}
+(NSDictionary*)noHostName
{
    return @{@"code":@"201",@"message":@"缺少hostname参数"};
}
+(NSDictionary*)noPort
{
    return @{@"code":@"202",@"message":@"缺少port参数"};
}
+(NSDictionary*)noUserName
{
    return @{@"code":@"203",@"message":@"缺少userName参数"};
}
+(NSDictionary*)noPassword
{
    return @{@"code":@"204",@"message":@"缺少password参数"};
}

@end
