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
import android.hardware.SensorManager;
import java.util.List;

public class SensorPlugin extends CordovaPlugin {
  private static final String TAG = "SensorPlugin";
  private PackageManager mPackageManager;
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
    if(action.equals("echo")) {
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
        obj.put("stringType",s.getStringType());
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
    }
    return true;
  }

}
