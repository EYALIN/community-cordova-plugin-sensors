var PLUGIN_NAME = 'SensorPlugin';

var SensorPlugin = {
    getSensorList: function(phrase) {
        return new Promise(function (resolve, reject) {
            cordova.exec(resolve, reject, PLUGIN_NAME, 'getSensorList', [phrase]);
        });
    },

    /**
     * watchAccelerometer(options, onSample, onError) - streams accelerometer readings until
     * clearWatchAccelerometer(). options.frequency: milliseconds between samples (default 40).
     * onSample({x, y, z, timestamp}): m/s^2 including gravity, Android axes on both platforms.
     * A new watch replaces the running one (the replaced watch's onSample simply stops being called).
     * onError(message) when the device has no accelerometer or the sensor cannot be started.
     * The sensor is released while the app is in the background and restarts when it returns.
     */
    watchAccelerometer: function(options, onSample, onError) {
        var frequency = options && Number(options.frequency) > 0 ? Number(options.frequency) : 40;
        cordova.exec(
            function (sample) {
                if (sample && typeof onSample === 'function') {
                    onSample(sample);
                }
            },
            function (error) {
                if (typeof onError === 'function') {
                    onError(error);
                }
            },
            PLUGIN_NAME, 'watchAccelerometer', [{ frequency: frequency }]);
    },

    /** clearWatchAccelerometer() - stops the running accelerometer watch. Resolves also when none runs. */
    clearWatchAccelerometer: function() {
        return new Promise(function (resolve, reject) {
            cordova.exec(resolve, reject, PLUGIN_NAME, 'clearWatchAccelerometer', []);
        });
    },
};

module.exports = SensorPlugin;
