# Release Notes

## 1.0.5 (2026-09-26)

- New `watchAccelerometer({frequency}, onSample, onError)` / `clearWatchAccelerometer()`: streams
  accelerometer readings (`{x, y, z, timestamp}`, m/s^2 including gravity, Android axes on both
  platforms) until cleared. Android: `SensorManager` `TYPE_ACCELEROMETER` with `keepCallback`;
  iOS: `CMMotionManager` accelerometer updates, converted from g (and Core Motion's opposite sign)
  to Android's values. A new watch replaces the running one; the sensor is released while the app is
  in the background and restarts on resume; the watch stops on page reload and plugin teardown.
- iOS: `CoreMotion.framework` is now declared in `plugin.xml` instead of relying on module auto-linking.
- Android: a failing `getSensorList()` no longer reports an extra "invalid action" after its error,
  and an exception without a message no longer crashes the error logging. Unknown actions now report
  "invalid action" instead of never answering.
- Package: npm metadata (repository, keywords, license, `files`), `plugin.xml` description and
  keywords; the stale `package-lock.json` copied from another plugin is gone.
- `getSensorList()` is otherwise unchanged.
