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
        BIKE: "physical_mode_bike",
        BSS: "physical_mode_bss",
        BUS: "physical_mode_bus",
        CAR: "physical_mode_car",
        COACH: "physical_mode_coach",
        FERRY: "physical_mode_ferry",
        FUNICULAR: "physical_mode_funicular",
        METRO: "physical_mode_metro",
        LOCALTRAIN: "physical_mode_localtrain",
        LONGDISTANCETRAIN: "physical_mode_longdistancetrain",
        RAPIDTRANSIT: "physical_mode_rapidtransit",
        RIDESHARING: "section_mode_ridesharing",
        SHUTTLE: "physical_mode_shuttle",
        TAXI: "physical_mode_taxi",
        TRAIN: "physical_mode_train",
        TRAMWAY: "physical_mode_tramway",
    },
    TravelerType: {
        FAST: "fast_walker",
        LUGGAGE: "luggage",
        SLOW: "slow_walker",
        STANDARD: "standard",
        WHEELCHAIR: "wheelchair"
    }
};
