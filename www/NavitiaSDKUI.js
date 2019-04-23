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
    PhysicalMode: {
        METRO: 'physical_mode:Metro',
        TRAMWAY: 'physical_mode:Tramway',
        BUS: 'physical_mode:Bus',
        COACH: 'physical_mode:Coach',
        SHUTTLE: 'physical_mode:Shuttle',
        BIKE: 'physical_mode:Bike',
        BSS: 'physical_mode:BikeSharingService',
        CAR: 'physical_mode:Car',
        TAXI: 'physical_mode:Taxi',
        CARPOOLING: 'physical_mode:Carpooling',
        TRAIN: 'physical_mode:Train',
        RAPID_TRANSIT: 'physical_mode:RapidTransit',
        LOCAL_TRAIN: 'physical_mode:LocalTrain',
        LONG_DISTANCE_TRAIN: 'physical_mode:LongDistanceTrain',
        FUNICULAR: 'physical_mode:Funicular',
        FERRY: 'physical_mode:Ferry',
    }
};
