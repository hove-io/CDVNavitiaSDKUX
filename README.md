# Journeys UI for Cordova
[![npm version](https://badge.fury.io/js/cordova-plugin-navitia-sdk-ui.svg)](https://badge.fury.io/js/cordova-plugin-navitia-sdk-ui)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Cordova plugin for using NavitiaSDK UI.
This plugin uses the native SDK [Android](https://github.com/CanalTP/NavitiaSDKUX_android) and [iOS](https://github.com/CanalTP/NavitiaSDKUX_ios).

Actually, it's not possible to use NavitiaSDK UI for Cordova with a Tab Bar (iOS) or a Bottom Navigation (Android)

## Installation

This plugin uses Carthage to build dependencies for iOS, please install it first:
https://github.com/Carthage/Carthage

Then use this command to install the plugin

    cordova plugin add cordova-plugin-navitia-sdk-ui

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

| Parameters | Type | Required | Description | Example |
| --- | --- |:---:| --- | --- |
| config | Object | ✓ | Configuration | |
| config.token | String | ✓ | Navitia token (generate a token on [navitia.io](https://www.navitia.io/))| 0de19ce5-e0eb-4524-a074-bda3c6894c19 |
| config.mainColor | String | ✗ | To set the background and the journey's duration colors  | by default #2a968f |
| config.originColor | String | ✗ | To set the color of the origin icon and the roadmap departure bloc | by default #00b981 |
| config.destinationColor | String | ✗ | To set the color of the destination icon and the roadmap arrival bloc  | by default #b90054 |
| config.multiNetwork | Boolean | ✗ | To set the display of the network name in the roadmap  | by default false |
| config.formJourney | Boolean | ✗ | To set the display of search form | by default false |
| config.modeForm | Object | ✗ | To customize the search form |  |
| success | Function | ✓ | Success callback function | function() {} |
| failure | Function | ✓ | Failure callback function | function(error) {} |

#### Example

```js****
var config = {
    token: 'my-token',
    mainColor: '#e67e22',
    originColor: '#2980b9',
    destinationColor: '#d35400',
};

NavitiaSDKUI.init(config, function() {}, function(error) {
    console.log(error);
});
```

#### Example with custom form

| Parameters | Type | Required | Description | Example |
| --- | --- |:---:| --- | --- |
| modeForm | Object | ✓ | Configuration | |
| modeForm.title | String | ✓ | To set the button title | NavitiaSDKUI.TransportModeIcon.METRO |
| modeForm.icon | String | ✓ | To set the button icon | 'metro' |
| modeForm.selected | Boolean | ✓ | To set the button is selected by default | false |
| modeForm.firstSectionMode | [String] | ✓ | To set mode to use at the begining by the button | NavitiaSDKUI.SectionMode.WALKING |
| modeForm.lastSectionMode | [String] | ✓ | To set mode to use at the end by the button | NavitiaSDKUI.SectionMode.WALKING |
| modeForm.physicalMode | [String] | ✗ | To set physical modes use by the button | ['physical_mode:Metro'] |
| modeForm.realTime | Boolean | ✗ | To set the display of the availability in real time | true |

```js****
var modeForm = [{
      title: 'Metro',
      icon: NavitiaSDKUI.TransportModeIcon.METRO,
      selected: true,
      firstSectionMode: [ NavitiaSDKUI.SectionMode.WALKING ],
      lastSectionMode: [ NavitiaSDKUI.SectionMode.WALKING ],
      physicalMode: ['physical_mode:Metro'],
    },{
      title: 'Bus',
      icon: NavitiaSDKUI.TransportModeIcon.BUS,
      selected: true,
      firstSectionMode: [ NavitiaSDKUI.SectionMode.WALKING ],
      lastSectionMode: [ NavitiaSDKUI.SectionMode.WALKING ],
      physicalMode: ['physical_mode:Bus'],
    },{
      title: 'Train',
      icon: NavitiaSDKUI.TransportModeIcon.TRAIN,
      selected: true,
      firstSectionMode: [ NavitiaSDKUI.SectionMode.WALKING ],
      lastSectionMode: [ NavitiaSDKUI.SectionMode.WALKING ],
      physicalMode: ['physical_mode:RapidTransit', 'physical_mode:LocalTrain', 'physical_mode:Train', 'physical_mode:Shuttle'],
    },{
      title: 'Bike',
      icon: NavitiaSDKUI.TransportModeIcon.BIKE,
      selected: false,
      firstSectionMode: [ NavitiaSDKUI.SectionMode.BIKE ],
      lastSectionMode: [ NavitiaSDKUI.SectionMode.BIKE ],
      physicalMode: ['physical_mode:Bike'],
    },{
      title: 'BSS',
      icon: NavitiaSDKUI.TransportModeIcon.BSS,
      selected: false,
      firstSectionMode: [ NavitiaSDKUI.SectionMode.BSS ],
      lastSectionMode: [ NavitiaSDKUI.SectionMode.BSS ],
      physicalMode: ['physical_mode:Bss'],
      realTime: true,
    },{
      title: 'Car',
      icon: NavitiaSDKUI.TransportModeIcon.CAR,
      selected: false,
      firstSectionMode: [ NavitiaSDKUI.SectionMode.CAR ],
      lastSectionMode: [ NavitiaSDKUI.SectionMode.CAR ],
      physicalMode: ['physical_mode:Car'],
      realTime: true,
    }];

var config = {
    token: 'my-token',
    mainColor: '#e67e22',
    originColor: '#2980b9',
    destinationColor: '#d35400',
    modeForm: modeForm,
};

NavitiaSDKUI.init(config, function() {}, function(error) {
    console.log(error);
});
```

### Journeys request - NavitiaSDKUI.invokeJourneyResults(params, success, failure)

| Parameters | Type | Required | Description | Example |
| --- | --- |:---:| --- | --- |
| params | Object | ✓ | Parameters of the screen | |
| params.coverage | String | ✓ | Name of search area | "fr-idf" |
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
	coverage: 'fr-idf',
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
	coverage: 'fr-idf',
    originId: '2.3665844;48.8465337',
    destinationId: '2.2979169;48.8848719',
};
```

##### Bike

```js
var journeyParams = {
	coverage: 'fr-idf',
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
	coverage: 'fr-idf',
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
	coverage: 'fr-idf',
    originId: '2.3665844;48.8465337',
    destinationId: '2.2979169;48.8848719',
    firstSectionModes: [NavitiaSDKUI.SectionMode.CAR],
    addPoiInfos: ['car_park'],
};
```

##### Ridesharing

```js
var journeyParams = {
	coverage: 'fr-idf',
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
Rename your customized icon with the same resource name as the default icon.
Simply register your custom icons in `config.xml`:
```
<platform name="android">
  <resource-file src="www/res/drawable/yourImage.xml" target="res/drawable/yourImage.xml" />
</platform>
```

#### Departure Icon
In order to customize the `departure` icon, rename your resource as `ic_departure.xml`

#### Arrival Icon
In order to customize the `arrival` icon, rename your resource as `ic_arrival.xml`

#### Transport Mode Icons
Here is the list of default icon name:
`ic_transport_mode_air.xml`
`ic_transport_mode_bike.xml`
`ic_transport_mode_bss.xml`
`ic_transport_mode_bus.xml`
`ic_transport_mode_car.xml`
`ic_transport_mode_coach.xml`
`ic_transport_mode_ferry.xml`
`ic_transport_mode_funicular.xml`
`ic_transport_mode_metro.xml`
`ic_transport_mode_ridesharing.xml`
`ic_transport_mode_shuttle.xml`
`ic_transport_mode_taxi.xml`
`ic_transport_mode_train.xml`
`ic_transport_mode_tramway.xml`
`ic_transport_mode_walking.xml`


## Troubleshooting
### Specific android tools version : 26
In case you are having problems building and getting this kind of problems :
```
platforms/android/build/intermediates/res/merged/debug/values-v24/values-v24.xml:3: AAPT: Error retrieving parent for item: No resource found that matches the given name ...
```

You may try to override your android compiler environment variables :

```
export ORG_GRADLE_PROJECT_cdvCompileSdkVersion=android-26
export ORG_GRADLE_PROJECT_cdvBuildToolsVersion=28.0.0
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
