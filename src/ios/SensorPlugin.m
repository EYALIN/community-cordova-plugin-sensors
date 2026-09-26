#import "SensorPlugin.h"
#import <CoreMotion/CoreMotion.h>

/** Standard gravity: CMAccelerometerData is in g, the JS API (like Android) is in m/s^2. */
static const double kSensorPluginStandardGravity = 9.80665;
/** Default sampling period for watchAccelerometer, in milliseconds. */
static const double kSensorPluginDefaultFrequencyMs = 40.0;
/** Fastest sampling period accepted (200 Hz), in milliseconds. */
static const double kSensorPluginMinFrequencyMs = 5.0;

@interface SensorPlugin ()
@property (strong, nonatomic) CMMotionManager *motionManager;
@property (copy, nonatomic) NSString *accelerometerCallbackId;
/** Seconds between samples of the running watch. */
@property (assign, nonatomic) NSTimeInterval accelerometerInterval;
/** The watch runs but Core Motion is stopped while the app is in the background. */
@property (assign, nonatomic) BOOL accelerometerPaused;
@end

@implementation SensorPlugin

- (void)pluginInitialize {
    self.motionManager = [[CMMotionManager alloc] init];
    NSNotificationCenter *center = [NSNotificationCenter defaultCenter];
    [center addObserver:self selector:@selector(pauseAccelerometer)
                   name:UIApplicationDidEnterBackgroundNotification object:nil];
    [center addObserver:self selector:@selector(resumeAccelerometer)
                   name:UIApplicationWillEnterForegroundNotification object:nil];
}

- (void)getSensorList:(CDVInvokedUrlCommand *)command {
    NSMutableArray *sensorsArray = [[NSMutableArray alloc] init];

    // Accelerometer
    if (self.motionManager.isAccelerometerAvailable) {
        [sensorsArray addObject:[self sensorDictionaryWithName:@"Accelerometer" type:@"Accelerometer"]];
    }

    // Gyroscope
    if (self.motionManager.isGyroAvailable) {
        [sensorsArray addObject:[self sensorDictionaryWithName:@"Gyroscope" type:@"Gyroscope"]];
    }

    // Magnetometer
    if (self.motionManager.isMagnetometerAvailable) {
        [sensorsArray addObject:[self sensorDictionaryWithName:@"Magnetometer" type:@"Magnetometer"]];
    }

    // Device Motion
    if (self.motionManager.isDeviceMotionAvailable) {
        [sensorsArray addObject:[self sensorDictionaryWithName:@"Device Motion" type:@"DeviceMotion"]];
    }

    // Creating the plugin result
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:sensorsArray];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (NSDictionary *)sensorDictionaryWithName:(NSString *)name type:(NSString *)type {
    return @{
        @"name": name,
        @"type": type,
        // Add other sensor properties as needed
    };
}

#pragma mark - watchAccelerometer / clearWatchAccelerometer

- (void)watchAccelerometer:(CDVInvokedUrlCommand *)command {
    if (!self.motionManager.isAccelerometerAvailable) {
        CDVPluginResult *error = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                  messageAsString:@"No accelerometer on this device"];
        [self.commandDelegate sendPluginResult:error callbackId:command.callbackId];
        return;
    }

    NSDictionary *options = [command argumentAtIndex:0 withDefault:nil andClass:[NSDictionary class]];
    double frequencyMs = [options[@"frequency"] respondsToSelector:@selector(doubleValue)]
        ? [options[@"frequency"] doubleValue] : kSensorPluginDefaultFrequencyMs;
    if (frequencyMs <= 0) {
        frequencyMs = kSensorPluginDefaultFrequencyMs;
    }
    frequencyMs = MAX(frequencyMs, kSensorPluginMinFrequencyMs);

    // A new watch replaces the previous one.
    [self stopAccelerometer];
    self.accelerometerCallbackId = command.callbackId;
    self.accelerometerInterval = frequencyMs / 1000.0;
    self.accelerometerPaused = NO;
    [self startAccelerometerUpdates];
}

/** Starts Core Motion for the running watch. Samples arrive on the main queue, like plugin calls. */
- (void)startAccelerometerUpdates {
    NSString *callbackId = self.accelerometerCallbackId;
    if (callbackId == nil) {
        return;
    }
    self.motionManager.accelerometerUpdateInterval = self.accelerometerInterval;

    __weak SensorPlugin *weakSelf = self;
    [self.motionManager startAccelerometerUpdatesToQueue:[NSOperationQueue mainQueue]
                                             withHandler:^(CMAccelerometerData *data, NSError *error) {
        SensorPlugin *strongSelf = weakSelf;
        if (strongSelf == nil || ![callbackId isEqualToString:strongSelf.accelerometerCallbackId]) {
            return;
        }
        if (error != nil || data == nil) {
            return;
        }
        // Core Motion reports g with the opposite sign to Android's SensorManager (a phone lying face
        // up reads z = -1 g here, +9.81 m/s^2 there); convert so both platforms send the same values.
        NSDictionary *sample = @{
            @"x": @(-data.acceleration.x * kSensorPluginStandardGravity),
            @"y": @(-data.acceleration.y * kSensorPluginStandardGravity),
            @"z": @(-data.acceleration.z * kSensorPluginStandardGravity),
            @"timestamp": @((long long)([[NSDate date] timeIntervalSince1970] * 1000.0)),
        };
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:sample];
        [result setKeepCallbackAsBool:YES];
        [strongSelf.commandDelegate sendPluginResult:result callbackId:callbackId];
    }];
}

/** In the background Core Motion is stopped (battery); the JS callback stays and resumes with the app. */
- (void)pauseAccelerometer {
    if (self.accelerometerCallbackId != nil && !self.accelerometerPaused) {
        self.accelerometerPaused = YES;
        [self.motionManager stopAccelerometerUpdates];
    }
}

- (void)resumeAccelerometer {
    if (self.accelerometerCallbackId != nil && self.accelerometerPaused) {
        self.accelerometerPaused = NO;
        [self startAccelerometerUpdates];
    }
}

- (void)clearWatchAccelerometer:(CDVInvokedUrlCommand *)command {
    [self stopAccelerometer];
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

/** Stops Core Motion updates and releases the JS callback of the running watch, if any. */
- (void)stopAccelerometer {
    if (self.motionManager.isAccelerometerActive) {
        [self.motionManager stopAccelerometerUpdates];
    }
    NSString *callbackId = self.accelerometerCallbackId;
    self.accelerometerCallbackId = nil;
    self.accelerometerPaused = NO;
    if (callbackId != nil) {
        CDVPluginResult *done = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
        [done setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:done callbackId:callbackId];
    }
}

- (void)onReset {
    [self stopAccelerometer];
    [super onReset];
}

- (void)dispose {
    NSNotificationCenter *center = [NSNotificationCenter defaultCenter];
    [center removeObserver:self name:UIApplicationDidEnterBackgroundNotification object:nil];
    [center removeObserver:self name:UIApplicationWillEnterForegroundNotification object:nil];
    [self stopAccelerometer];
    [super dispose];
}

@end
