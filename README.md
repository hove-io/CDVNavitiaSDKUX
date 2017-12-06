# CDVNavitiaSDK

Cordova plugin for using Navitia SDK UX

## Installation

    cordova plugin add cordova-plugin-navitia-sdk-ux

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
| params.originLabel | String | ✗ | Origin label, if not set the address will be displayed | Home |
| params.originId | String | ✓ | Origin coordinates, following the format `lon;lat` | 2.3665844;48.8465337 |
| params.destinationLabel | String | ✗ | Destination label, if not set the address will be displayed | Work |
| params.destinationId | String | ✓ | Destination coordinates, following the format `lon;lat` | 2.2979169;48.8848719 |
| params.datetime | Date | ✗ | Requested date and time for journey results | new Date() |
| params.datetimeRepresents | String | ✗ | Can be `departure` (journeys after datetime) or `arrival` (journeys before datetime). | departure |
| params.forbiddenUris | [String] | ✗ | List of navitia uris | ['commercial_mode:Bus', 'line:1'] |
| params.firstSectionModes | [String] | ✗ | List of modes to use at the begining of the journey | ['car'] |
| params.lastSectionModes | [String] | ✗ | List of modes to use at the end of the journey | ['bike', 'bss'] |
| params.count | Integer | ✗ | The number of journeys that will be displayed | 3 |
| params.minNbJourneys | Integer | ✗ | The minimum number of journeys that will be displayed | 3 |
| params.maxNbJourneys | Integer | ✗ | The maximum number of journeys that will be displayed | 10 |
| success | Function | ✓ | Success callback function | function() {} |
| failure | Function | ✓ | Failure callback function | function(error) {} |

### Example

    var config = {
        token: 'my-token',
    };

    NavitiaSDKUX.init(config, function() {}, function(error) {
        console.log(error);
    });

    var journeyParams = {
        originLabel: 'My Home',
        originId: '2.3665844;48.8465337',
        destinationId: '2.2979169;48.8848719',
    };

    NavitiaSDKUX.invokeJourneyResults(journeyParams, function() {}, function(error) {
        console.log(error);
    });

## Known issues

- Color configuration not available yet (but soon!)