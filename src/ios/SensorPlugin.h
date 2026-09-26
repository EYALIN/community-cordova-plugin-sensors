#import <Cordova/CDV.h>

@interface SensorPlugin : CDVPlugin

// Method to get the list of sensors
- (void)getSensorList:(CDVInvokedUrlCommand *)command;

// Streams accelerometer readings ({x, y, z} in m/s^2 including gravity, Android axes; timestamp in ms)
// to the command's callback until clearWatchAccelerometer. Argument 0: {frequency: ms between samples}.
- (void)watchAccelerometer:(CDVInvokedUrlCommand *)command;

// Stops the running accelerometer watch, if any.
- (void)clearWatchAccelerometer:(CDVInvokedUrlCommand *)command;

@end
