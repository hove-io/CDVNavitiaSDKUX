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
        ARRIVAL: 'arrival',
        DEPARTURE: 'departure',
    },
    SectionMode: {
        BIKE: 'bike',
        BSS: 'bss',
        CAR: 'car',
        RIDESHARING: 'ridesharing',
        WALKING: 'walking',
    },
    TransportModeIcon: {
        BIKE: "bike",
        BSS: "bss",
        BUS: "bus",
        CAR: "car",
        CARPOOLING: "carpooling",
        COACH: "coach",
        FERRY: "ferry",
        FUNICULAR: "funicular",
        METRO: "metro",
        LOCALTRAIN: "localtrain",
        LONGDISTANCETRAIN: "longdistancetrain",
        RAPIDTRANSIT: "rapidtransit",
        SHUTTLE: "shuttle",
        TAXI: "taxi",
        TRAIN: "train",
        TRAMWAY: "tramway",
    }
};
