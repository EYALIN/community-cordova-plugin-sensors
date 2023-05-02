[![NPM version](https://img.shields.io/npm/v/community-cordova-plugin-sensors)](https://www.npmjs.com/package/community-cordova-plugin-sensors)


# community-cordova-plugin-sensors


I dedicate a considerable amount of my free time to developing and maintaining many cordova plugins for the community ([See the list with all my maintained plugins][community_plugins]).
To help ensure this plugin is kept updated,
new features are added and bugfixes are implemented quickly,
please donate a couple of dollars (or a little more if you can stretch) as this will help me to afford to dedicate time to its maintenance.
Please consider donating if you're using this plugin in an app that makes you money, 
or if you're asking for new features or priority bug fixes. Thank you!

[![](https://img.shields.io/static/v1?label=Sponsor%20Me&style=for-the-badge&message=%E2%9D%A4&logo=GitHub&color=%23fe8e86)](https://github.com/sponsors/eyalin)



## Installation

    cordova plugin add community-cordova-plugin-sensors

## Examples

    declare var SensorPlugin: SensorManager;
    const sm: ISensor[] = await SensorPlugin.getSensorList()

## Functions

#### getSensorList:Promise<ISensor[]>:

## Interfaces
### ISensor
- __name__ - name string of the sensor. The name is guaranteed to be unique for a particular sensor type.
- __fifoMaxEventCount__ - Maximum number of events of this sensor that could be batched. If this value is zero it indicates that batch mode is not supported for this sensor. If other applications registered to batched sensors, the actual number of events that can be batched might be smaller because the hardware FiFo will be partially used to batch the other sensors.
- __fifoReservedEventCount__ - Number of events reserved for this sensor in the batch mode FIFO. This gives a guarantee on the minimum number of events that can be batched.
- __highestDirectReportRateLevel__ - Get the highest supported direct report mode rate level of the sensor.
- __id__ - The sensor id that will be unique for the same app unless the device is factory reset. Return value of 0 means this sensor does not support this function; return value of -1 means this sensor can be uniquely identified in system by combination of its type and name.
- __maxDelay__ - The max delay for this sensor in microseconds.
- __maximumRange__ - maximum range of the sensor in the sensor's unit. 
- __minDelay__ - the minimum delay allowed between two events in microseconds or zero if this sensor only returns a value when the data it's measuring changes. Note that if the app does not have the Manifest.permission.HIGH_SAMPLING_RATE_SENSORS permission, the minimum delay is capped at 5000 microseconds (200 Hz).
- __power__ - the power in mA used by this sensor while in use
- __reportingMode__ - Reporting mode for the input sensor, one of REPORTING_MODE_* constants.
- __resolution__ - resolution of the sensor in the sensor's unit.
- __stringType__ - The type of this sensor as a string
- __type__ - generic type of this sensor.
- __vendor__ - vendor string of this sensor.
- __version__ - version of the sensor's module.
- __isAdditionalInfoSupported__ - Returns true if the sensor supports sensor additional information API .
- __isDynamicSensor__ - true if the sensor is a dynamic sensor (sensor added at runtime).
- __isWakeUpSensor__ - Returns true if the sensor is a wake-up sensor.






### Supported Platforms


- Android


[w3c_spec]: https://www.w3.org/TR/battery-status/
[status_object]: #status-object
[community_plugins]: https://github.com/EYALIN?tab=repositories&q=community&type=&language=&sort=
