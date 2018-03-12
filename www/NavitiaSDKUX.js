var exec = require('cordova/exec');

module.exports = {
    init: function(config, successCallback, errorCallback) {
        exec(
            successCallback,
            errorCallback,
            'NavitiaSDKUX',
            'init',
            [config]
        )
    },
    invokeJourneyResults: function(params, successCallback, errorCallback) {
        exec(
            successCallback,
            errorCallback,
            'NavitiaSDKUX',
            'invokeJourneyResults',
            [params]
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
        RIDESHARING: 'ridesharing'
    },
};
