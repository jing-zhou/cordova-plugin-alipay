#import <Cordova/CDV.h>

@interface Alipay : CDVPlugin 
    @property NSString *appId;
    @property NSString *callbackId;

    - (void)pay:(CDVInvokedUrlCommand*)command;
@end