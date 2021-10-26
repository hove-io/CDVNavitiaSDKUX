# Journey for Cordova
[![npm version](https://badge.fury.io/js/cordova-plugin-navitia-sdk-ui.svg)](https://badge.fury.io/js/cordova-plugin-navitia-sdk-ui)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Cordova plugin for using Journey.
This plugin uses the native SDK [Android](https://github.com/CanalTP/NavitiaSDKUX_android) and [iOS](https://github.com/CanalTP/NavitiaSDKUX_ios).

Actually, it's not possible to use Journey for Cordova with a Tab Bar (iOS) or a Bottom Navigation (Android)

## Requirements

This plugin uses native SDKs. Since those SDKs are private, you will need to get access credentials to our [artifactory](https://kisiodigital.jfrog.io). This plugin uses Cocoapods to manage dependencies for iOS, please install it first: https://cocoapods.org.

## Credentials configuration

Once you have credentials to access our [artifactory](https://kisiodigital.jfrog.io), one further step is required before installing the plugin. Please follow one of these steps to configure the credentials properly. 
The `<YOUR_ARTIFACTORY_USERNAME>` and `<YOUR_ARTIFACTORY_PASSWORD>` should be replaced with your username and password!

#### Using Config.xml preferences

In the Config.xml file of your project, add these lines:

```xml
<widget>
	.
	.
	<preference name="KISIO_ARTIFACTORY_USERNAME" value="<YOUR_ARTIFACTORY_USERNAME>" />
    	<preference name="KISIO_ARTIFACTORY_PASSWORD" value="<YOUR_ARTIFACTORY_PASSWORD>" />
</widget>
```

#### Using environment variables

Define two global environment variables as follows:
```
KISIO_ARTIFACTORY_USERNAME=<YOUR_ARTIFACTORY_USERNAME>
KISIO_ARTIFACTORY_PASSWORD=<YOUR_ARTIFACTORY_PASSWORD>
```

#### Using global properties files (MacOS users only)

##### iOS

In the Home directory, open `.netrc` file (if not found, create a new file) and add this line:
```
machine kisiodigital.jfrog.io login <YOUR_ARTIFACTORY_USERNAME> password <YOUR_ARTIFACTORY_PASSWORD>
``````

##### Android

In the `~/.gradle` directory, open `gradle.properties`file (if not found, create a new file) andd these lines:
```
kisio_artifactory_username=<YOUR_ARTIFACTORY_USERNAME>
kisio_artifactory_password=<YOUR_ARTIFACTORY_PASSWORD>
```

## Installation

Use this command to install the plugin `cordova plugin add cordova-plugin-navitia-sdk-ui`

## Setup for Android platform

This plugin uses Google Maps and requires a Google API key in case you're targeting the Android platform with your cordova application. You can get your own API key using this link: https://developers.google.com/maps/documentation/android-api/signup

You need to update your config.xml file as follows:

    <widget ......... xmlns:android="http://schemas.android.com/apk/res/android">
        .
        .
        <platform name="android">
            .
            .
            <config-file parent="/manifest/application" target="AndroidManifest.xml">
                <meta-data android:name="com.google.android.geo.API_KEY" android:value="YOUR_API_KEY" />
            </config-file>
        </platform>
        .
        .
    </widget>
Note that you have to change YOUR_API_KEY with your own API key!

## Usage

### Configuration - NavitiaSDKUI.init(config, success, failure)

| Parameters | Type | Required | Description | Default |
| --- | --- |:---:| --- | --- |
| config | `Object` | ✓ | Configuration | ✗ |
| config.token | `String` | ✓ | Navitia token (generate a token on [navitia.io](https://www.navitia.io/))| ✗ |
| config.coverage | `String` | ✓ | Name of search area | ✗ |
| config.environment | `String` | ✗ | Navitia environment | "PROD" |
| config.primaryColor | `String` | ✗ | To set the main color of the module | ✗ |
| config.secondaryColor | `String` | ✗ | To set the color of some components | `primaryColor` |
| config.originColor | `String` | ✗ | To set the color of the origin at the roadmap departure bloc | ✗ |
| config.originBackgroundColor | `String` | ✗ | To set the color of the origin at the roadmap departure bloc | `originColor` |
| config.originIconColor | `String` | ✗ | To set the color of the origin icon | `transparent` (no color filter) |
| config.destinationColor | `String` | ✗ | To set the color of the destination at the roadmap arrival bloc | ✗ |
| config.destinationBackgroundColor | `String` | ✗ | To set the color of the destination at the roadmap arrival bloc | `destinationColor` |
| config.destinationIconColor | `String` | ✗ | To set the color of the destination icon | `transparent` (no color filter) |
| config.isFormEnabled | `Boolean` | ✗ | To set the display of search form | false |
| config.isMultiNetworkEnabled | `Boolean` | ✗ | To set the display of the network name in the roadmap  | false |
| config.isEarlierLaterFeatureEnabled | `Boolean` | ✗ | To set the display of "Earlier" and "Later" buttons | false |
| config.isNextDeparturesFeatureEnabled | `Boolean` | ✗ | To set the display of next departures | by default false |
| config.maxHistory | `Int` | ✗ | To set the maximum number of autocomplete history inputs | 10 |
| config.transportModes | `Object` | ✗ | To customize the search form | ✗ |
| config.disruptionContributor | `String` | ✗ | To filter traffic disruptions | ✗ |
| config.customTitles | `Object` | ✗ | To set screen titles | ✗ |
| success | Function | ✓ | Success callback function | function() {} |
| failure | Function | ✓ | Failure callback function | function(error) {} |

##### Environments

For the supported environments, please check the table below:

| Environment | Value |
| --- | --- |
| Prod | "PROD" |
| Customer | "CUSTOMER" |
| Dev | "DEV" |
| Internal | "INTERNAL" |

##### Custom titles

You can customize the screens titles. A string ressource ID is required and should be passed within the CustomTitles Object.

| Parameters | Type | Required | Description | Example |
| --- | --- |:---:| --- | --- |
| customTitle | Object | ✗ | CustomTitles | |
| customTitle.form | String | ✗ | To set the form screen title | 'form_screen_title' |
| customTitle.journeys | String | ✗ | To set the journeys list screen title | 'journeys_screen_title' |
| customTitle.roadmap | String | ✗ | To set the roadmap screen title | 'roadmap_screen_title' |
| customTitle.ridesharing | String | ✗ | To set the ridesharing offers screen title | 'ridesharing_screen_title' |
| customTitle.autocomplete | String | ✗ | To set the autocomplete screen title | 'autocomplete_screen_title' |

#### Example

```js****
var config = {
    environment: 'CUSTOMER',
    token: 'my-token',
    coverage: 'my-coverage',
    backgroundColor: '#e67e22',
    originColor: '#2980b9',
    destinationColor: '#d35400',
};

NavitiaSDKUI.init(config, function() {}, function(error) {
    console.log(error);
});
```

#### Example with custom form and custom titles

| Parameters | Type | Required | Description | Example |
| --- | --- |:---:| --- | --- |
| Transport Mode | Object | ✓ | Configuration | |
| transportMode.title | String | ✓ | To set the button title | NavitiaSDKUI.TransportModeIcon.METRO |
| transportMode.type | String | ✓ | To set the button type | 'metro' |
| transportMode.icon | String | ✓ | To set the button icon | 'metro' |
| transportMode.selected | Boolean | ✓ | To set the button is selected by default | false |
| transportMode.firstSectionMode | [String] | ✓ | To set mode to use at the begining by the button | NavitiaSDKUI.SectionMode.WALKING |
| transportMode.lastSectionMode | [String] | ✓ | To set mode to use at the end by the button | NavitiaSDKUI.SectionMode.WALKING |
| transportMode.physicalMode | [String] | ✗ | To set physical modes use by the button | ['physical_mode:Metro'] |
| transportMode.realTime | Boolean | ✗ | To set the display of the availability in real time | true |

```js****
var transportModes = [{
      title: 'Metro',
      type: 'metro',
      icon: NavitiaSDKUI.TransportModeIcon.METRO,
      selected: true,
      firstSectionMode: [ NavitiaSDKUI.SectionMode.WALKING ],
      lastSectionMode: [ NavitiaSDKUI.SectionMode.WALKING ],
      physicalMode: ['physical_mode:Metro'],
    },{
      title: 'Bus',
      type: 'bus',
      icon: NavitiaSDKUI.TransportModeIcon.BUS,
      selected: true,
      firstSectionMode: [ NavitiaSDKUI.SectionMode.WALKING ],
      lastSectionMode: [ NavitiaSDKUI.SectionMode.WALKING ],
      physicalMode: ['physical_mode:Bus'],
    },{
      title: 'Train',
      type: 'train',
      icon: NavitiaSDKUI.TransportModeIcon.TRAIN,
      selected: true,
      firstSectionMode: [ NavitiaSDKUI.SectionMode.WALKING ],
      lastSectionMode: [ NavitiaSDKUI.SectionMode.WALKING ],
      physicalMode: ['physical_mode:RapidTransit', 'physical_mode:LocalTrain', 'physical_mode:Train', 'physical_mode:Shuttle'],
    },{
      title: 'Bike',
      type: 'bike',
      icon: NavitiaSDKUI.TransportModeIcon.BIKE,
      selected: false,
      firstSectionMode: [ NavitiaSDKUI.SectionMode.BIKE ],
      lastSectionMode: [ NavitiaSDKUI.SectionMode.BIKE ],
      physicalMode: ['physical_mode:Bike'],
    },{
      title: 'BSS',
      type: 'bss',
      icon: NavitiaSDKUI.TransportModeIcon.BSS,
      selected: false,
      firstSectionMode: [ NavitiaSDKUI.SectionMode.BSS ],
      lastSectionMode: [ NavitiaSDKUI.SectionMode.BSS ],
      physicalMode: ['physical_mode:Bss'],
      realTime: true,
    },{
      title: 'Car',
      type: 'car',
      icon: NavitiaSDKUI.TransportModeIcon.CAR,
      selected: false,
      firstSectionMode: [ NavitiaSDKUI.SectionMode.CAR ],
      lastSectionMode: [ NavitiaSDKUI.SectionMode.CAR ],
      physicalMode: ['physical_mode:Car'],
      realTime: true,
    }];
    
var customTitles = {
     form: "form_screen_title",
     journeys: "journeys_screen_title",
     roadmap: "roadmap_screen_title",
     ridesharing: "ridesharing_screen_title",
     autocomplete: "autocomplete_screen_title"
};

var config = {
    token: 'my-token',
    primaryColor: '#e67e22',
    secondaryColor: '#2980b9',
    destinationColor: '#d35400',
    transportModes: transportModes,
    customTitles: customTitles
};

NavitiaSDKUI.init(config, function() {}, function(error) {
    console.log(error);
});
```

### Journeys request - NavitiaSDKUI.invokeJourneyResults(params, success, failure)

| Parameters | Type | Required | Description | Example |
| --- | --- |:---:| --- | --- |
| params | Object | ✓ | Parameters of the screen | |
| params.originId | String | ✗ | Origin coordinates, following the format `lon;lat` | "2.3665844;48.8465337" |
| params.destinationId | String | ✗ | Destination coordinates, following the format `lon;lat` | "2.2979169;48.8848719" |
| params.originLabel | String | ✗ | Origin label, if not set the address will be displayed | "Home" |
| params.destinationLabel | String | ✗ | Destination label, if not set the address will be displayed | "Work" |
| params.datetime | String | ✗ | Requested date and time (in UTC Timezone) for journey results | new Date().toISOString() |
| params.datetimeRepresents | String | ✗ | Can be `NavitiaSDKUI.DatetimeRepresents.DEPARTURE` (journeys after datetime) or `NavitiaSDKUI.DatetimeRepresents.ARRIVAL` (journeys before datetime). | NavitiaSDKUI.DatetimeRepresents.DEPARTURE |
| params.forbiddenUris | [String] | ✗ | Used to avoid lines, modes, networks, etc in the Journey search (List of navitia uris) | ['commercial_mode:Bus', 'line:1'] |
| params.allowedId | [String] | ✗ | If you want to use only a small subset of the public transport objects in the Journey search (List of navitia uris) | ['commercial_mode:Bus', 'line:1'] |
| params.firstSectionModes | [String] | ✗ | List of modes to use at the begining of the journey | [NavitiaSDKUI.SectionMode.CAR, NavitiaSDKUI.SectionMode.RIDESHARING] |
| params.lastSectionModes | [String] | ✗ | List of modes to use at the end of the journey | [NavitiaSDKUI.SectionMode.BIKE, NavitiaSDKUI.SectionMode.BSS] |
| params.count | Integer | ✗ | The number of journeys that will be displayed | 3 |
| params.minNbJourneys | Integer | ✗ | The minimum number of journeys that will be displayed | 3 |
| params.maxNbJourneys | Integer | ✗ | The maximum number of journeys that will be displayed | 10 |
| params.addPoiInfos | [String] | ✗ | Allow the display of the availability in real time for bike share and car park | ['bss\_stands', 'car\_park'] |
| params.directPath | String | ✗ | To indicate if the journey is direct | "only" |
| success | Function | ✓ | Success callback function | function() {} |
| failure | Function | ✓ | Failure callback function | function(error) {} |

#### Example

```js
var journeyParams = {
    originId: '2.3665844;48.8465337',
    destinationId: '2.2979169;48.8848719',
    originLabel: 'My Home',
    firstSectionModes: [NavitiaSDKUI.SectionMode.WALKING, NavitiaSDKUI.SectionMode.CAR, NavitiaSDKUI.SectionMode.BIKE, NavitiaSDKUI.SectionMode.BSS, NavitiaSDKUI.SectionMode.RIDESHARING],
    addPoiInfos: ['bss_stand', 'car_park'],
    count: 5,
};

NavitiaSDKUI.invokeJourneyResults(journeyParams, function() {}, function(error) {
    console.log(error);
});
```

##### Public transport 

```js
var journeyParams = {
    originId: '2.3665844;48.8465337',
    destinationId: '2.2979169;48.8848719',
};
```

##### Bike

```js
var journeyParams = {
    originId: '2.3665844;48.8465337',
    destinationId: '2.2979169;48.8848719',
    forbiddenUris: ['physical_mode:Bus', ‘physical_mode:Tramway’, ‘physical_mode:Metro’]
    firstSectionModes: [NavitiaSDKUI.SectionMode.BIKE],
    lastSectionModes: [NavitiaSDKUI.SectionMode.BIKE],
};
```

##### BSS

```js
var journeyParams = {
    originId: '2.3665844;48.8465337',
    destinationId: '2.2979169;48.8848719',
    forbiddenUris: ['physical_mode:Bus', ‘physical_mode:Tramway’, ‘physical_mode:Metro’]
    firstSectionModes: [NavitiaSDKUI.SectionMode.BSS],
    lastSectionModes: [NavitiaSDKUI.SectionMode.BSS],
    addPoiInfos: ['bss_stand'],
};
```

##### Car

```js
var journeyParams = {
    originId: '2.3665844;48.8465337',
    destinationId: '2.2979169;48.8848719',
    firstSectionModes: [NavitiaSDKUI.SectionMode.CAR],
    addPoiInfos: ['car_park'],
};
```

##### Ridesharing

```js
var journeyParams = {
    originId: '2.3665844;48.8465337',
    destinationId: '2.2979169;48.8848719',
    firstSectionModes: [NavitiaSDKUI.SectionMode.RIDESHARING],
    lastSectionModes: [NavitiaSDKUI.SectionMode.RIDESHARING],
};
```

### Colors configuration

Actually, three color parameters can be customized for the SDK.
These parameters must be added to the "config" object as indicated in the table above.

### Customize icons

Some module icons can be customized using a JSON configuration. Please note that all ressources should be added to the platform main assets/bundle folder.

#### Transport

##### Lines

| Object | Type | Required | Description |
| --- | --- | --- | --- |
| code | String | ✓ | Navitia line code |
| icon_res | String | ✓ | Icon ressource name |
| commercial | Commercial | ✓ | Navitia commercial mode |

##### Modes

| Object | Type | Required | Description |
| --- | --- | --- | --- |
| icon_res | String | ✓ | Icon ressource name |
| commercial | Commercial | ✓ | Navitia commercial mode |

##### Commercial

| Object | Type | Required | Description |
| --- | --- | --- | --- |
| name | String | ✓ | Commercial mode name |
| id | String | ✓ | Commercial mode ID |

##### Providers

| Object | Type | Required | Description |
| --- | --- | --- | --- |
| id | String | ✓ | Navitia provider ID |
| icon_res | String | ✓ | Icon ressource name |

##### Example

```js
var transportConfiguration = `{
    "lines": [
      {
        "code": "6",
        "icon_res": "ic_metro_6",
        "commercial": {
          "name": "Métro",
          "id": "commercial_mode:Metro"
        }
      }
    ],
    "modes": [
      {
        "icon_res": "ic_metro",
        "commercial": {
          "name": "Métro",
          "id": "commercial_mode:Metro"
        }
      }
    ],
    "providers": [
        {
          "id": "ridesharing_provider",
          "icon_res": "ic_ridesharing"
        }
    ]
}`

var config = {
    token: 'my-token',
    primaryColor: '#e67e22',
    secondaryColor: '#2980b9',
    destinationColor: '#d35400',
    transportConfiguration: transportConfiguration
};

NavitiaSDKUI.init(config, function() {}, function(error) {
    console.log(error);
});
```

## Troubleshooting

### Force gradle wrapper version before build

In terminal, before building :
```
export CORDOVA_ANDROID_GRADLE_DISTRIBUTION_URL=https://services.gradle.org/distributions/gradle-6.0.1-all.zip
```

### Specific android tools version : 29

In case you are having problems building and getting this kind of problems :
```
platforms/android/build/intermediates/res/merged/debug/values-v24/values-v24.xml:3: AAPT: Error retrieving parent for item: No resource found that matches the given name ...
```

You may try to override your android compiler environment variables :

```
export ORG_GRADLE_PROJECT_cdvCompileSdkVersion=android-28
export ORG_GRADLE_PROJECT_cdvBuildToolsVersion=29.0.0
```

More information on [Cordova website](https://cordova.apache.org/docs/en/7.x/guide/platforms/android/index.html#setting-gradle-properties) 

### Manifest merger issue

This usually happens if you change the API key in the config.xml file. The build fails and you're getting this kind of error:
```
Element meta-data#com.google.android.geo.API_KEY at AndroidManifest.xml:xx:xx-xx duplicated with element declared at AndroidManifest.xml:xx:xx-xx
```
You may try to remove the Android platform and add it back again.

### Android building problem : Cannot read property ‘replace’ of undefined

In the file /platforms/android/cordova/lib/emulator.js, replace :
```
var num = target.split(’(API level ‘)[1].replace(’)’, ‘’);
```
By :
```
var num = target.match(/\d+/)[0];
```

## License 

Check out the Cordova plugin [License](https://github.com/CanalTP/CDVNavitiaSDKUX/blob/master/LICENSE) here.
