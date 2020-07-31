/**
 * @flow
 */

import { DeviceEventEmitter, NativeModules } from 'react-native';
const native = NativeModules.Orientation;

const orientationDidChangeEvent = 'orientationDidChange';
const specificOrientationDidChangeEvent = 'specificOrientationDidChange';
const deviceOrientationDidChangeEvent = 'deviceOrientationDidChange';
const META = '__listener_id';

let listeners = {};
let id = 0;

export type OrientationType =
  | 'PORTRAIT'
  | 'LANDSCAPE'
  | 'LANDSCAPE-LEFT'
  | 'LANDSCAPE-RIGHT'
  | 'PORTRAITUPSIDEDOWN'
  | 'UNKNOWN';

type OrientationListener = (orientation: OrientationType) => void;
type DeviceOrientationListener = (orientation: number) => void;

function getKey(listener) {
  if (!listener.hasOwnProperty(META)) {
    if (!Object.isExtensible(listener)) {
      return 'F';
    }
    Object.defineProperty(listener, META, {
      value: 'L' + ++id,
    });
  }
  // $FlowFixMe
  return listener[META];
}

module.exports = {
  getOrientation(): Promise<OrientationType> {
    return new Promise((resolve: *, reject: *) => {
      native.getOrientation((error, orientation) => {
        if (orientation) {
          resolve(orientation);
        } else {
          reject(error);
        }
      });
    });
  },
  getSpecificOrientation(): Promise<OrientationType> {
    return new Promise((resolve: *, reject: *) => {
      native.getSpecificOrientation((error, orientation) => {
        if (orientation) {
          resolve(orientation);
        } else {
          reject(error);
        }
      });
    });
  },
  getDeviceOrientation(): Promise<number> {
    return new Promise((resolve: *, reject: *) => {
      native.getDeviceOrientation((error, orientation) => {
        if (orientation) {
          resolve(orientation);
        } else {
          reject(error);
        }
      });
    });
  },
  lockToPortrait() {
    native.lockToPortrait();
  },
  lockToLandscape() {
    native.lockToLandscape();
  },
  lockToLandscapeRight() {
    native.lockToLandscapeRight();
  },
  lockToLandscapeLeft() {
    native.lockToLandscapeLeft();
  },
  unlockAllOrientations() {
    native.unlockAllOrientations();
  },
  addOrientationListener(cb: OrientationListener) {
    const key = getKey(cb);
    listeners[key] = DeviceEventEmitter.addListener(
      orientationDidChangeEvent,
      (body) => {
        cb(body.orientation);
      },
    );
  },
  removeOrientationListener(cb: OrientationListener) {
    const key = getKey(cb);
    if (!listeners[key]) {
      return;
    }
    listeners[key].remove();
    listeners[key] = null;
  },
  addSpecificOrientationListener(cb: OrientationListener) {
    const key = getKey(cb);
    listeners[key] = DeviceEventEmitter.addListener(
      specificOrientationDidChangeEvent,
      (body) => {
        cb(body.specificOrientation);
      },
    );
  },
  removeSpecificOrientationListener(cb: OrientationListener) {
    const key = getKey(cb);
    if (!listeners[key]) {
      return;
    }
    listeners[key].remove();
    listeners[key] = null;
  },
  addDeviceOrientationListener(cb: DeviceOrientationListener) {
    const key = getKey(cb);
    listeners[key] = DeviceEventEmitter.addListener(
      deviceOrientationDidChangeEvent,
      (body) => {
        cb(body.deviceOrientation);
      },
    );
  },
  removeDeviceOrientationListener(cb: DeviceOrientationListener) {
    const key = getKey(cb);
    if (!listeners[key]) {
      return;
    }
    listeners[key].remove();
    listeners[key] = null;
  },
  getInitialOrientation() {
    return native.initialOrientation;
  },
};
