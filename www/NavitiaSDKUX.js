var exec = require('cordova/exec');

module.exports = {
    init: function() {
        exec(
            function(success) { },
            function(error) { },
            'NavitiaSDKUX',
            'init',
            []
        )
    }

    invokeJourneyResults: function() {
        exec(
            function(success) { },
            function(error) { },
            'NavitiaSDKUX',
            'invokeJourneyResults',
            []
        )
    }
}