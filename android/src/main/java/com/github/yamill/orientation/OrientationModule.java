package com.github.yamill.orientation;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

class OrientationModule extends ReactContextBaseJavaModule implements ConfigurationChangeListener {

    private final OrientationEventListener mOrientationEventListener;
    private final WindowManager windowManager;
    private final ContentResolver contentResolver;

    private String mSpecificOrientation;
    private int mDeviceOrientation = -1;
    private final String[] mOrientations;

    private static final String LANDSCAPE = "LANDSCAPE";
    private static final String LANDSCAPE_LEFT = "LANDSCAPE-LEFT";
    private static final String LANDSCAPE_RIGHT = "LANDSCAPE-RIGHT";
    private static final String PORTRAIT = "PORTRAIT";
    private static final String PORTRAIT_UPSIDEDOWN = "PORTRAITUPSIDEDOWN";
    private static final String ORIENTATION_UNKNOWN = "UNKNOWN";
    private static final int ACTIVE_SECTOR_SIZE = 45;
    private static final String[] ORIENTATIONS_PORTRAIT_DEVICE = {PORTRAIT, LANDSCAPE_RIGHT, PORTRAIT_UPSIDEDOWN, LANDSCAPE_LEFT};
    private static final String[] ORIENTATIONS_LANDSCAPE_DEVICE = {LANDSCAPE_LEFT, PORTRAIT, LANDSCAPE_RIGHT, PORTRAIT_UPSIDEDOWN};

    OrientationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.windowManager = (WindowManager) getReactApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        this.contentResolver = reactContext.getContentResolver();

        mOrientations = isLandscapeDevice() ? ORIENTATIONS_LANDSCAPE_DEVICE : ORIENTATIONS_PORTRAIT_DEVICE;

        reactContext.addLifecycleEventListener(createLifecycleEventListener());

        mOrientationEventListener = createOrientationEventListener();
    }

    private OrientationEventListener createOrientationEventListener() {
        return new OrientationEventListener(getReactApplicationContext(), SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientationValue) {
                if (isDeviceOrientationLocked() || !getReactApplicationContext().hasActiveCatalystInstance())
                    return;


                if (mSpecificOrientation != null) {
                    final int halfSector = ACTIVE_SECTOR_SIZE / 2;
                    if ((orientationValue % 90) > halfSector
                            && (orientationValue % 90) < (90 - halfSector)) {
                        return;
                    }
                }

                final String specificOrientation = getSpecificOrientationString(orientationValue);
                final int deviceOrientation = getRoundedDeviceOrientation(orientationValue);

                if (!specificOrientation.equals(mSpecificOrientation)) {
                    mSpecificOrientation = specificOrientation;
                    WritableMap params = Arguments.createMap();
                    params.putString("specificOrientation", specificOrientation);
                    emitOrientationEvent("specificOrientationDidChange", params);
                }

                if (deviceOrientation != mDeviceOrientation) {
                    mDeviceOrientation = deviceOrientation;
                    WritableMap params = Arguments.createMap();
                    params.putInt("deviceOrientation", deviceOrientation);
                    emitOrientationEvent("deviceOrientationDidChange", params);
                }
            }
        };
    }

    @Override
    public void onConfigurationChange(Configuration newConfig) {
        ReactApplicationContext reactContext = getReactApplicationContext();
        if (isDeviceOrientationLocked() || !reactContext.hasActiveCatalystInstance())
            return;

        String orientationValue = getLayoutOrientationString(newConfig.orientation);

        WritableMap params = Arguments.createMap();
        params.putString("orientation", orientationValue);
        emitOrientationEvent("orientationDidChange", params);
    }

    private void emitOrientationEvent(String eventName, WritableMap params) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    private LifecycleEventListener createLifecycleEventListener() {
        return new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                ConfigurationChangeManager.getInstance().addListener(OrientationModule.this);
                if (mOrientationEventListener.canDetectOrientation()) {
                    mOrientationEventListener.enable();
                }
            }

            @Override
            public void onHostPause() {
                unregisterListeners();
            }

            @Override
            public void onHostDestroy() {
                unregisterListeners();
            }
        };
    }

    @Override
    public void onCatalystInstanceDestroy() {
        unregisterListeners();
    }

    private void unregisterListeners() {
        ConfigurationChangeManager.getInstance().removeListener(OrientationModule.this);
        mOrientationEventListener.disable();
    }

    @Override
    public String getName() {
        return "Orientation";
    }

    @ReactMethod
    public void getOrientation(Callback callback) {
        callback.invoke(null, getLayoutOrientationString(getLayoutOrientation()));
    }

    @ReactMethod
    public void getSpecificOrientation(Callback callback) {
        callback.invoke(null, mSpecificOrientation);
    }

    @ReactMethod
    public void getDeviceOrientation(Callback callback) {
        callback.invoke(null, mDeviceOrientation);
    }

    @ReactMethod
    public void lockToPortrait() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @ReactMethod
    public void lockToLandscape() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    @ReactMethod
    public void lockToLandscapeLeft() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @ReactMethod
    public void lockToLandscapeRight() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    @ReactMethod
    public void unlockAllOrientations() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public @Nullable Map<String, Object> getConstants() {
        HashMap<String, Object> constants = new HashMap<>();
        constants.put("initialOrientation", getLayoutOrientationString(getLayoutOrientation()));
        return constants;
    }

    private int getLayoutOrientation() {
        return getReactApplicationContext().getResources().getConfiguration().orientation;
    }

    private String getLayoutOrientationString(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return LANDSCAPE;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return PORTRAIT;
        } else {
            return ORIENTATION_UNKNOWN;
        }
    }

    private boolean isDeviceOrientationLocked() {
        return Settings.System.getInt(
                contentResolver,
                Settings.System.ACCELEROMETER_ROTATION, 0) == 0;
    }

    private boolean isLandscapeDevice() {
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x > size.y;
    }

    private int getRoundedDeviceOrientation(int orientationValue) {
        if (orientationValue < 0) {
            return -1;
        }
        final int index = (int) ((float) orientationValue / 90f + 0.5f) % 4;
        return index * 90;
    }

    private String getSpecificOrientationString(int orientationValue) {
        if (orientationValue < 0) {
            return getLayoutOrientationString(getLayoutOrientation());
        }
        final int index = (int) ((float) orientationValue / 90f + 0.5f) % 4;
        return mOrientations[index];
    }

}
