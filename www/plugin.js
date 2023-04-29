var PLUGIN_NAME = 'SensorPlugin';

var SensorPlugin = {
    getSensorList: function(phrase) {
        return new Promise(function (resolve, reject) {
            cordova.exec(resolve, reject, PLUGIN_NAME, 'getSensorList', [phrase]);
        });
    },
};

module.exports = SensorPlugin;
