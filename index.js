/**
 * @flow
 */

import { DeviceEventEmitter, NativeModules } from 'react-native';
const Orientation = NativeModules.Orientation;

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
      Orientation.getOrientation((error, orientation) => {
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
      Orientation.getSpecificOrientation((error, orientation) => {
        if (orientation) {
          resolve(orientation);
        } else {
          reject(error);
        }
      });
    });
  },
  getDeviceOrientation(): Promise<OrientationType> {
    return new Promise((resolve: *, reject: *) => {
      Orientation.getSpecificOrientation((error, orientation) => {
        if (orientation) {
          resolve(orientation);
        } else {
          reject(error);
        }
      });
    });
  },
  lockToPortrait() {
    Orientation.lockToPortrait();
  },
  lockToLandscape() {
    Orientation.lockToLandscape();
  },
  lockToLandscapeRight() {
    Orientation.lockToLandscapeRight();
  },
  lockToLandscapeLeft() {
    Orientation.lockToLandscapeLeft();
  },
  unlockAllOrientations() {
    Orientation.unlockAllOrientations();
  },
  addOrientationListener(cb: OrientationListener) {
    var key = getKey(cb);
    listeners[key] = DeviceEventEmitter.addListener(
      orientationDidChangeEvent,
      (body) => {
        cb(body.orientation);
      },
    );
  },
  removeOrientationListener(cb: OrientationListener) {
    var key = getKey(cb);
    if (!listeners[key]) {
      return;
    }
    listeners[key].remove();
    listeners[key] = null;
  },
  addSpecificOrientationListener(cb: OrientationListener) {
    var key = getKey(cb);
    listeners[key] = DeviceEventEmitter.addListener(
      specificOrientationDidChangeEvent,
      (body) => {
        cb(body.specificOrientation);
      },
    );
  },
  removeSpecificOrientationListener(cb: OrientationListener) {
    var key = getKey(cb);
    if (!listeners[key]) {
      return;
    }
    listeners[key].remove();
    listeners[key] = null;
  },
  addDeviceOrientationListener(cb: OrientationListener) {
    var key = getKey(cb);
    listeners[key] = DeviceEventEmitter.addListener(
      deviceOrientationDidChangeEvent,
      (body) => {
        cb(body.deviceOrientation);
      },
    );
  },
  removeDeviceOrientationListener(cb: OrientationListener) {
    var key = getKey(cb);
    if (!listeners[key]) {
      return;
    }
    listeners[key].remove();
    listeners[key] = null;
  },
  getInitialOrientation() {
    return Orientation.initialOrientation;
  },
};
