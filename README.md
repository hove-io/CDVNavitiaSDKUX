# CDVNavitiaSDK

Cordova plugin for using Navitia SDK UX

## Installation

### iOS

    cordova plugin add cordova-plugin-cocoapod-support
    cordova plugin add cordova-navitia-sdk-ux

### Android

    cordova plugin add cordova-navitia-sdk-ux

## Usage

### Example

    var config = {
        token: 'my-token',
        colors: {
            primary: '#ff0000',
            secondary: '#00ff00',
            tertiary: '#0000ff'
        }
    }
    NavitiaSDKUX.init(config);

    var journeyParams = {
        origin: 'My home',
        destination: 'My work',
        datetime: new Date(),
    }
    NavitiaSDKUX.invokeJourneyResults(journeyParams, function(success) {
        alert(success.journeys);
    }, function(error) {
        alert("An error has occured");
    });
