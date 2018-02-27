package com.google.flutterwithps;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.github.privacystreams.commons.comparison.Comparators;
import io.github.privacystreams.communication.Contact;
import io.github.privacystreams.core.Callback;
import io.github.privacystreams.core.Item;
import io.github.privacystreams.core.UQI;
import io.github.privacystreams.core.exceptions.PSException;
import io.github.privacystreams.core.purposes.Purpose;
import io.github.privacystreams.device.DeviceState;

/**
 * FlutterWithPsPlugin
 */
public class FlutterWithPsPlugin implements MethodCallHandler, EventChannel.StreamHandler {
  private static final String STREAM_CHANNEL_NAME = "com.google.flutter/ps_stream";
  private static final String METHOD_CHANNEL_NAME = "com.google.flutter/ps_method";
  private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE};
  private EventChannel.EventSink events;
  private final Activity activity;
  private UQI uqi;

  FlutterWithPsPlugin(Activity activity) {
    this.activity = activity;
    uqi = new UQI(activity);

  }

  /**
   * Return the current state of the permissions needed.
   */

  private boolean checkPermissions() {
      for(String permission: REQUIRED_PERMISSIONS){
          if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
              return false;
      }
    return true;
  }


  /**
   * Plugin registration.
   */
  public static void registerWith(Registrar registrar) {
    final FlutterWithPsPlugin instance = new FlutterWithPsPlugin(registrar.activity());

    final MethodChannel methodChannel = new MethodChannel(registrar.messenger(), METHOD_CHANNEL_NAME);
      methodChannel.setMethodCallHandler(instance);

    final EventChannel eventChannel = new EventChannel(registrar.messenger(), STREAM_CHANNEL_NAME);
    eventChannel.setStreamHandler(instance);


  }
  class MyAsyncTask extends AsyncTask<Object, Object, Object> {
      private String name_keyword;
      private Result result;

      MyAsyncTask(String name_keyword, Result result) {
          this.name_keyword = name_keyword;
          this.result = result;
      }


      @Override
        protected Object doInBackground(Object[] objects) {
          List<List<String>> emailAddresses = new ArrayList<>();
          try {
              emailAddresses = uqi.getData(Contact.getAll(),
                      Purpose.TEST("get email address based on a name keyword"))
                      .filter(Comparators.eq(Contact.NAME, name_keyword))
                      .asList(Contact.EMAILS);
          } catch (PSException e) {
              e.printStackTrace();
          }

          if(!emailAddresses.isEmpty()){

              result.success(name_keyword + "'s email address: " + emailAddresses.get(0).get(0));
          }
          else{
              result.success("Not found any matched records..");
          }

          return null;
        }
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("findEmailAddress")) {
        String name_keyword = call.argument("fullname");

        System.out.println(name_keyword);
        if (name_keyword != null){
            new MyAsyncTask(name_keyword, result).execute();
        }
        else {
            result.success("Not found any..");
        }
    }
    else{
        result.notImplemented();
    }
  }

  @Override
  public void onListen(Object o, EventChannel.EventSink eventSink) {
    events = eventSink;

    if (!checkPermissions()){
        Toast toast = Toast.makeText(activity, "Required Permissions", Toast.LENGTH_LONG);
        toast.show();
        return;
    }

    uqi.getData(DeviceState
            .asUpdates(5000, DeviceState.Masks.BATTERY_LEVEL| DeviceState.Masks.CONNECTION_INFO),
                        Purpose.TEST("requesting device info every 5 seconds"))
            .forEach(new Callback<Item>() {
              @Override
              protected void onInput(Item item) {
                HashMap deviceState = new HashMap();
                deviceState.put(DeviceState.WIFI_BSSID, item.getValueByField(DeviceState.WIFI_BSSID));
                deviceState.put(DeviceState.IS_CONNECTED, item.getValueByField(DeviceState.IS_CONNECTED));
                deviceState.put(DeviceState.BATTERY_LEVEL, item.getValueByField(DeviceState.BATTERY_LEVEL));
                events.success(deviceState);
              }
            });
  }

  @Override
  public void onCancel(Object o) {
      uqi.stopAll();
  }
}
