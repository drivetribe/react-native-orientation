package com.github.yamill.orientation;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
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

class OrientationModule extends ReactContextBaseJavaModule {


    private final OrientationEventListener mOrientationEventListener;
    private final WindowManager windowManager;
    private final ContentResolver contentResolver;
    private String mOrientation;
    private String mSpecificOrientation;
    private final String[] mOrientations;

    private static final String LANDSCAPE = "LANDSCAPE";
    private static final String LANDSCAPE_LEFT = "LANDSCAPE-LEFT";
    private static final String LANDSCAPE_RIGHT = "LANDSCAPE-RIGHT";
    private static final String PORTRAIT = "PORTRAIT";
    private static final String PORTRAIT_UPSIDEDOWN = "PORTRAITUPSIDEDOWN";
    private static final String ORIENTATION_UNKNOWN = "UNKNOWN";

    private static final int ACTIVE_SECTOR_SIZE = 45;
    private static final String[] ORIENTATIONS_PORTRAIT_DEVICE = { PORTRAIT, LANDSCAPE_RIGHT, PORTRAIT_UPSIDEDOWN, LANDSCAPE_LEFT };
    private static final String[] ORIENTATIONS_LANDSCAPE_DEVICE = { LANDSCAPE_LEFT, PORTRAIT, LANDSCAPE_RIGHT, PORTRAIT_UPSIDEDOWN };

    OrientationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.windowManager = (WindowManager) getReactApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        this.contentResolver = reactContext.getContentResolver();

        mOrientations = isLandscapeDevice() ? ORIENTATIONS_LANDSCAPE_DEVICE : ORIENTATIONS_PORTRAIT_DEVICE;

        LifecycleEventListener mLifecycleEventListener = createLifecycleEventListener();
        reactContext.addLifecycleEventListener(mLifecycleEventListener);

        mOrientationEventListener = createOrientationEventListener(reactContext);
    }

    private OrientationEventListener createOrientationEventListener(final ReactApplicationContext reactContext) {
        return new OrientationEventListener(reactContext, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientationValue) {
                if (isDeviceOrientationLocked() || !reactContext.hasActiveCatalystInstance())
                    return;


                if (mOrientation != null && mSpecificOrientation != null) {
                    final int halfSector = ACTIVE_SECTOR_SIZE / 2;
                    if ((orientationValue % 90) > halfSector
                            && (orientationValue % 90) < (90 - halfSector)) {
                        return;
                    }
                }

                final String orientation = getOrientationString(orientationValue);
                final String specificOrientation = getSpecificOrientationString(orientationValue);

                final DeviceEventManagerModule.RCTDeviceEventEmitter deviceEventEmitter =
                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);

                if (!orientation.equals(mOrientation)) {
                    mOrientation = orientation;
                    WritableMap params = Arguments.createMap();
                    params.putString("orientation", orientation);
                    deviceEventEmitter.emit("orientationDidChange", params);
                }

                if (!specificOrientation.equals(mSpecificOrientation)) {
                    mSpecificOrientation = specificOrientation;
                    WritableMap params = Arguments.createMap();
                    params.putString("specificOrientation", specificOrientation);
                    deviceEventEmitter.emit("specificOrientationDidChange", params);
                }
            }
        };
    }

    private LifecycleEventListener createLifecycleEventListener() {
        return new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                if (mOrientationEventListener.canDetectOrientation()) {
                    mOrientationEventListener.enable();
                }
            }

            @Override
            public void onHostPause() {
                mOrientationEventListener.disable();
            }

            @Override
            public void onHostDestroy() {
                mOrientationEventListener.disable();
            }
        };
    }

    @Override
    public String getName() {
        return "Orientation";
    }

    @ReactMethod
    public void getOrientation(Callback callback) {
        callback.invoke(null, mOrientation);
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
        constants.put("initialOrientation", mOrientation);
        return constants;
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

    private String getSpecificOrientationString(int orientationValue) {
        if (orientationValue < 0) {
            return ORIENTATION_UNKNOWN;
        }
        final int index = (int) ((float) orientationValue / 90f + 0.5f) % 4;
        return mOrientations[index];
    }

    private String getOrientationString(int orientationValue) {
        final String specificOrientation = getSpecificOrientationString(orientationValue);
        switch (specificOrientation) {
            case LANDSCAPE_LEFT:
            case LANDSCAPE_RIGHT:
                return LANDSCAPE;
            case PORTRAIT:
            case PORTRAIT_UPSIDEDOWN:
                return PORTRAIT;
            default:
                return ORIENTATION_UNKNOWN;
        }
    }

}
