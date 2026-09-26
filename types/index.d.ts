export interface ISensor  {
    /* name string of the sensor. The name is guaranteed to be unique for a particular sensor type. */
    name: string;
    /* Maximum number of events of this sensor that could be batched. If this value is zero it indicates that batch mode is not supported for this sensor. If other applications registered to batched sensors, the actual number of events that can be batched might be smaller because the hardware FiFo will be partially used to batch the other sensors. */
    fifoMaxEventCount: number;
    /* Number of events reserved for this sensor in the batch mode FIFO. This gives a guarantee on the minimum number of events that can be batched. */
    fifoReservedEventCount: number;
    /* Get the highest supported direct report mode rate level of the sensor. */
    highestDirectReportRateLevel: number;
    /* The sensor id that will be unique for the same app unless the device is factory reset. Return value of 0 means this sensor does not support this function; return value of -1 means this sensor can be uniquely identified in system by combination of its type and name. */
    id: number;
    /* 	The max delay for this sensor in microseconds. */
    maxDelay: number;
    /* maximum range of the sensor in the sensor's unit. */
    maximumRange: number;
    /* 	the minimum delay allowed between two events in microseconds or zero if this sensor only returns a value when the data it's measuring changes. Note that if the app does not have the Manifest.permission.HIGH_SAMPLING_RATE_SENSORS permission, the minimum delay is capped at 5000 microseconds (200 Hz). */
    minDelay: number;
    /* the power in mA used by this sensor while in use */
    power: number;
    /* Reporting mode for the input sensor, one of REPORTING_MODE_* constants. */
    reportingMode: number;
    /* resolution of the sensor in the sensor's unit.*/
    resolution: number;
    /* The type of this sensor as a string.*/
    stringType: string;
    /* generic type of this sensor. */
    type: number;
    /* vendor string of this sensor.*/
    vendor: string;
    /* version of the sensor's module.*/
    version: number;
    /* Returns true if the sensor supports sensor additional information API */
    isAdditionalInfoSupported: boolean;
    /* true if the sensor is a dynamic sensor (sensor added at runtime).*/
    isDynamicSensor: boolean;
    /* Returns true if the sensor is a wake-up sensor. */
    isWakeUpSensor: boolean;
}

/** One accelerometer reading: m/s^2 including gravity, Android axes on both platforms. */
export interface IAccelerometerSample {
    x: number;
    y: number;
    z: number;
    /** milliseconds since the epoch */
    timestamp: number;
}

export interface IWatchAccelerometerOptions {
    /** milliseconds between samples; default 40 (25 Hz), fastest 5. Android treats it as a hint. */
    frequency?: number;
}

export default class SensorManager{
    getSensorList():Promise<ISensor[]>;
    /**
     * Streams accelerometer readings to onSample until clearWatchAccelerometer(). A new watch replaces
     * the running one. onError gets a message when the device has no accelerometer or the sensor
     * cannot be started. The sensor is released in the background and restarts on resume.
     */
    watchAccelerometer(options: IWatchAccelerometerOptions | null | undefined, onSample: (sample: IAccelerometerSample) => void, onError?: (error: string) => void): void;
    /** Stops the running accelerometer watch; resolves also when none runs. */
    clearWatchAccelerometer(): Promise<void>;
}
