package sensorplugin;
import android.content.Context;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.pm.PackageManager;
import android.util.Log;
import java.util.HashSet;
import android.content.pm.FeatureInfo;
import java.util.Date;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.List;
import org.apache.cordova.LOG;

public class SensorPlugin extends CordovaPlugin {
  private static final String TAG = "SensorPlugin";
  private PackageManager mPackageManager;

  /** Default sampling period for watchAccelerometer, in milliseconds (the cordova-plugin-device-motion default). */
  private static final int DEFAULT_ACCELEROMETER_FREQUENCY_MS = 40;
  /** SensorManager caps apps without HIGH_SAMPLING_RATE_SENSORS at 200 Hz; faster asks are clamped here. */
  private static final int MIN_ACCELEROMETER_FREQUENCY_MS = 5;

  private SensorManager accelerometerManager;
  private SensorEventListener accelerometerListener;
  private Sensor accelerometerSensor;
  private int accelerometerPeriodUs;
  /** The watch runs but its listener is unregistered while the app is in the background. */
  private boolean accelerometerPaused;
  private volatile CallbackContext accelerometerCallback;
    /**
     * Delete externalCacheDirectory on app start
     *
     * @param cordova Cordova-instance
     * @param webView CordovaWebView-instance
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

       /**
         * Returns the application context.
         */
        private Context getContext() {
            return cordova.getActivity();
        }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if (action.equals("watchAccelerometer")) {
      watchAccelerometer(args.optJSONObject(0), callbackContext);
      return true;
    }
    if (action.equals("clearWatchAccelerometer")) {
      stopAccelerometer();
      callbackContext.success();
      return true;
    }
          try {

    if(action.equals("getSensorList")) {
    JSONArray arr = new JSONArray();
        mPackageManager = getContext().getPackageManager();
        SensorManager oSM = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorsList = oSM.getSensorList(Sensor.TYPE_ALL);
        int count = 1;
        for (Sensor s : sensorsList) {
            JSONObject obj = new JSONObject();
        obj.put("name",s.getName());
        obj.put("fifoMaxEventCount",s.getFifoMaxEventCount());
        obj.put("fifoReservedEventCount",s.getFifoReservedEventCount());
        obj.put("highestDirectReportRateLevel",s.getHighestDirectReportRateLevel());
        obj.put("id",s.getId());
        obj.put("maxDelay",s.getMaxDelay());
        obj.put("maximumRange",s.getMaximumRange());
        obj.put("minDelay",s.getMinDelay());
        obj.put("power",s.getPower());
        obj.put("reportingMode",s.getReportingMode());
        obj.put("resolution",s.getResolution());
        String stringType = s.getStringType();
        if (stringType != null) {
           stringType = stringType.replace("android.sensor.","");
        }
        obj.put("stringType",stringType);
        obj.put("type",s.getType());
        obj.put("vendor",s.getVendor());
        obj.put("version",s.getVersion());
        obj.put("isAdditionalInfoSupported",s.isAdditionalInfoSupported());
        obj.put("isDynamicSensor",s.isDynamicSensor());
        obj.put("isWakeUpSensor",s.isWakeUpSensor());
        arr.put(obj);
        }
      String phrase = args.getString(0);
      final PluginResult result = new PluginResult(PluginResult.Status.OK, arr);
          callbackContext.sendPluginResult(result);
         } else if(action.equals("getDate")) {
              // An example of returning data back to the web layer
              final PluginResult result = new PluginResult(PluginResult.Status.OK, (new Date()).toString());
              callbackContext.sendPluginResult(result);
            } else {
              return false;
            }
     } catch (Exception e) {
         // The JS side already got the error: returning false would add an "invalid action" on top.
         // Log.d throws on a null message, which e.getMessage() can be.
         Log.d(TAG, "execute " + action + ": " + e);
         callbackContext.error(String.valueOf(e.getMessage()));
    }
    return true;
  }


  // ==================== watchAccelerometer / clearWatchAccelerometer ====================
  //
  // execute() runs on Cordova's bridge thread, the lifecycle callbacks (onPause, onResume, onReset,
  // onDestroy) on the UI thread and onSensorChanged on the main looper, so every method that reads
  // or writes the watch state is synchronized.

  /**
   * Streams TYPE_ACCELEROMETER readings to one JS callback (keepCallback) until
   * clearWatchAccelerometer. Values are m/s^2 including gravity, as SensorManager reports them:
   * {x, y, z, timestamp (ms since epoch)}. A new watch replaces the previous one.
   */
  private synchronized void watchAccelerometer(JSONObject options, final CallbackContext callbackContext) {
    int frequencyMs = options != null ? options.optInt("frequency", DEFAULT_ACCELEROMETER_FREQUENCY_MS) : DEFAULT_ACCELEROMETER_FREQUENCY_MS;
    if (frequencyMs <= 0) {
      frequencyMs = DEFAULT_ACCELEROMETER_FREQUENCY_MS;
    }
    if (frequencyMs < MIN_ACCELEROMETER_FREQUENCY_MS) {
      frequencyMs = MIN_ACCELEROMETER_FREQUENCY_MS;
    }
    SensorManager manager = (SensorManager) cordova.getActivity().getSystemService(Context.SENSOR_SERVICE);
    Sensor sensor = manager != null ? manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) : null;
    if (sensor == null) {
      callbackContext.error("No accelerometer on this device");
      return;
    }
    stopAccelerometer();
    accelerometerManager = manager;
    accelerometerSensor = sensor;
    // registerListener takes microseconds; values 0..3 would be read as SENSOR_DELAY_* constants
    // (excluded by the 5 ms floor) and a very long period must not overflow the int.
    accelerometerPeriodUs = (int) Math.min((long) frequencyMs * 1000L, (long) Integer.MAX_VALUE);
    accelerometerCallback = callbackContext;
    accelerometerListener = new SensorEventListener() {
      @Override
      public void onSensorChanged(SensorEvent event) {
        CallbackContext callback = accelerometerCallback;
        if (callback != callbackContext || event.values == null || event.values.length < 3) {
          return;
        }
        try {
          JSONObject sample = new JSONObject();
          sample.put("x", (double) event.values[0]);
          sample.put("y", (double) event.values[1]);
          sample.put("z", (double) event.values[2]);
          sample.put("timestamp", System.currentTimeMillis());
          PluginResult result = new PluginResult(PluginResult.Status.OK, sample);
          result.setKeepCallback(true);
          callback.sendPluginResult(result);
        } catch (JSONException e) {
          Log.d(TAG, "accelerometer sample: " + e.getMessage());
        }
      }

      @Override
      public void onAccuracyChanged(Sensor s, int accuracy) {
      }
    };
    accelerometerPaused = false;
    boolean registered = manager.registerListener(accelerometerListener, sensor, accelerometerPeriodUs);
    if (!registered) {
      clearAccelerometerState();
      callbackContext.error("Could not start the accelerometer");
    }
  }

  /** Unregisters the listener and releases the JS callback of the running watch, if any. */
  private synchronized void stopAccelerometer() {
    if (accelerometerManager != null && accelerometerListener != null) {
      accelerometerManager.unregisterListener(accelerometerListener);
    }
    CallbackContext callback = accelerometerCallback;
    clearAccelerometerState();
    if (callback != null) {
      PluginResult done = new PluginResult(PluginResult.Status.NO_RESULT);
      done.setKeepCallback(false);
      callback.sendPluginResult(done);
    }
  }

  private void clearAccelerometerState() {
    accelerometerListener = null;
    accelerometerManager = null;
    accelerometerSensor = null;
    accelerometerCallback = null;
    accelerometerPaused = false;
  }

  /** In the background the sensor is released (battery); the JS callback stays and resumes with the app. */
  @Override
  public synchronized void onPause(boolean multitasking) {
    super.onPause(multitasking);
    if (accelerometerManager != null && accelerometerListener != null && !accelerometerPaused) {
      accelerometerManager.unregisterListener(accelerometerListener);
      accelerometerPaused = true;
    }
  }

  @Override
  public synchronized void onResume(boolean multitasking) {
    super.onResume(multitasking);
    if (accelerometerPaused && accelerometerManager != null && accelerometerListener != null && accelerometerSensor != null) {
      accelerometerPaused = false;
      if (!accelerometerManager.registerListener(accelerometerListener, accelerometerSensor, accelerometerPeriodUs)) {
        CallbackContext callback = accelerometerCallback;
        clearAccelerometerState();
        if (callback != null) {
          callback.error("Could not restart the accelerometer");
        }
      }
    }
  }

  @Override
  public void onReset() {
    stopAccelerometer();
  }

  @Override
  public void onDestroy() {
    stopAccelerometer();
  }
}
