#ifndef CDVNavitiaSDKUX_h
#define CDVNavitiaSDKUX_h

#import <Cordova/CDV.h>

@interface CDVNavitiaSDKUX : CDVPlugin

- (void)init:(CDVInvokedUrlCommand*)command;
- (void)invokeJourneyResults:(CDVInvokedUrlCommand*)command;

@end

#endif
