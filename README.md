# CDVNavitiaSDK

Cordova plugin for using Navitia SDK UX

## Installation

    cordova plugin add cordova-plugin-navitia-sdk-ux

### ios

In file `platforms/ios/HelloWorld/Classes/AppDelegate.m`
Disable default Cordova Screen to use internal storyboard with NavigationController

    - (BOOL)application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions
    {
        /*
        self.viewController = [[MainViewController alloc] init];
        return [super application:application didFinishLaunchingWithOptions:launchOptions];
        */
        return YES;
    }

## Usage

### NavitiaSDKUX.init(config, success, failure)

| Parameters | Type | Required | Description | Example |
| --- | --- |:---:| --- | --- |
| config | Object | ✓ | Configuration | |
| config.token | String | ✓ | Token navitia | 0de19ce5-e0eb-4524-a074-bda3c6894c19 |
| success | Function | ✓ | Success callback function | function() {} |
| failure | Function | ✓ | Failure callback function | function(error) {} |

### NavitiaSDKUX.invokeJourneyResults(params, success, failure)

| Parameters | Type | Required | Description | Example |
| --- | --- |:---:| --- | --- |
| params | Object | ✓ | Parameters of the screen | |
| params.initOrigin | String | ✗ | Origin label, if not set the address will be display | Home |
| params.initOriginId | String | ✓ | Origin coordinates, following the format `lon;lat` | 2.3665844;48.8465337 |
| params.initDestination | String | ✗ | Destination label, if not set the address will be display | Work |
| params.initDestinationId | String | ✓ | Destination coordinates, following the format `lon;lat` | 2.2979169;48.8848719 |
| success | Function | ✓ | Success callback function | function() {} |
| failure | Function | ✓ | Failure callback function | function(error) {} |

### Example

    var config = {
        token: 'my-token',
    }

    NavitiaSDKUX.init(config, function() {}, function(error) {
        console.log(error);
    });

    var journeyParams = {
        initOrigin: 'My Home',
        initOriginId: '2.3665844;48.8465337',
        initDestinationId: '2.2979169;48.8848719',
    };

    NavitiaSDKUX.invokeJourneyResults(journeyParams, function() {}, function(error) {
        console.log(error);
    });
