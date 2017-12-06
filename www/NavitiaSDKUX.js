var exec = require('cordova/exec');

module.exports = {
    init: function(config, successCallback, errorCallback) {
        exec(
            successCallback,
            errorCallback,
            'NavitiaSDKUX',
            'init',
            [config],
        )
    },
    invokeJourneyResults: function(params, successCallback, errorCallback) {
        exec(
            successCallback,
            errorCallback,
            'NavitiaSDKUX',
            'invokeJourneyResults',
            [params],
        )
    },
    datetimeRepresents: {
        DEPARTURE: 'departure',
        ARRIVAL: 'arrival',
    },
    sectionMode: {
        WALKING: 'walking',
        BIKE: 'bike',
        CAR: 'car',
        BSS: 'bss',
    },
};
