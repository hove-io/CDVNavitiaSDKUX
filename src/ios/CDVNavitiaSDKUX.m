#import "CDVNavitiaSDKUX.h"
#import <Cordova/CDV.h>

@implementation CDVNavitiaSDKUX

- (void)init:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)invokeJourneyResults:(CDVInvokedUrlCommand*)command;
{
    NSDictionary* params = [command.arguments objectAtIndex:0];

    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end