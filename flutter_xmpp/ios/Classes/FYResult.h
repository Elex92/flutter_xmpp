//
//  FYResult.h
//  CocoaAsyncSocket
//
//  Created by elex on 2020/7/17.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface FYResult : NSObject
+(NSDictionary*)paramsComplete;
+(NSDictionary*)connectSuccess;
+(NSDictionary*)connectFailure;
+(NSDictionary*)connectTimeout;
+(NSDictionary*)conflict;
+(NSDictionary*)disconnect;
+(NSDictionary*)registerSuccess;
+(NSDictionary*)registerFail;
+(NSDictionary*)registerUserExists;
+(NSDictionary*)loginSuccess;
+(NSDictionary*)loginFail;
+(NSDictionary*)noHostName;
+(NSDictionary*)noPort;
+(NSDictionary*)noUserName;
+(NSDictionary*)noPassword;
@end

NS_ASSUME_NONNULL_END
