var exec = require('cordova/exec');

module.exports = {
    init: function(config, successCallback, errorCallback) {
        exec(
            successCallback,
            errorCallback,
            'NavitiaSDKUI',
            'init',
            [config]
        )
    },
    invokeJourneyResults: function(params, successCallback, errorCallback) {
        exec(
            successCallback,
            errorCallback,
            'NavitiaSDKUI',
            'invokeJourneyResults',
            [params]
        )
    },
    resetPreferences: function(successCallback, errorCallback) {
        exec(
            successCallback,
            errorCallback,
            'NavitiaSDKUI',
            'resetPreferences',
            [{}]
        )
    },
    DatetimeRepresents: {
        DEPARTURE: 'departure',
        ARRIVAL: 'arrival'
    },
    SectionMode: {
        WALKING: 'walking',
        BIKE: 'bike',
        CAR: 'car',
        BSS: 'bss',
        RIDESHARING: 'ridesharing',
    },
    TransportModeIcon: {
        AIR: "air",
        BIKE: "bike",
        FERRY: "ferry",
        BSS: "bss",
        BUS: "bus",
        CAR: "car",
        COACH: "coach",
        FUNICULAR: "funicular",
        METRO: "metro",
        LOCALTRAIN: "localtrain",
        TRAIN: "train",
        RAPIDTRANSIT: "rapidtransit",
        LONGDISTANCETRAIN: "longdistancetrain",
        TRAMWAY: "tramway",
        WALKING: "walking",
        PHONE_TAD: "phone-tad",
        BUS_TAD: "bus-tad",
        TAXI_TAD: "taxi-tad",
        CAR_TAD: "car-tad",
    }
};
