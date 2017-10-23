#import "CDVNavitiaSDKUX.h"
#import <Cordova/CDV.h>
#import <NavitiaSDKUX/NavitiaSDKUX-swift.h>

@implementation CDVNavitiaSDKUX

- (void)init:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSDictionary* config = [command.arguments objectAtIndex:0];
    NSString* token = (NSString*)[config objectForKey:@"token"];
    if (!token.length) {
        NSString* errorMessage = @"No token specified";
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    
    [NavitiaSDKUXConfig setTokenWithToken:token];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)invokeJourneyResults:(CDVInvokedUrlCommand*)command;
{
    NSDictionary* params = [command.arguments objectAtIndex:0];

    NSBundle *bundle = [NSBundle bundleWithIdentifier:@"org.kisio.NavitiaSDKUX"];
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Journey" bundle: bundle];
    JourneySolutionsController *vc = [storyboard instantiateInitialViewController];
    [vc setPropsWithOriginId:[params objectForKey:@"initOriginId"] destinationId:[params objectForKey:@"initDestinationId"] origin:[params objectForKey:@"initOrigin"] destination:[params objectForKey:@"initDestination"]];

    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:vc];
    [self.viewController presentViewController:navigationController animated:YES completion:nil];

    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""] callbackId:command.callbackId];
}

@end
