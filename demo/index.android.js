import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  TouchableOpacity,
  View
} from 'react-native';

import Orientation from '@drivetribe/react-native-orientation';

type State = {
  initialOrientation?: string,
  orientation?: string,
  specificOrientation?: string,
  deviceOrientation?: number,
}

class demo extends Component {

  state = {};

  componentWillMount() {
    this.setState({ initialOrientation: Orientation.getInitialOrientation() });
    Orientation.addOrientationListener(this._updateOrientation);
    Orientation.addSpecificOrientationListener(this._updateSpecificOrientation);
    Orientation.addDeviceOrientationListener(this._updateDeviceOrientation);
  }

  componentWillUnmount() {
    Orientation.removeOrientationListener(this._updateOrientation);
    Orientation.removeSpecificOrientationListener(this._updateSpecificOrientation);
    Orientation.removeDeviceOrientationListener(this._updateDeviceOrientation);
  }

  _updateOrientation = (orientation) => {
    this.setState({ orientation });
  };

  _updateSpecificOrientation = (specificOrientation) => {
    this.setState({ specificOrientation });
  };

  _updateDeviceOrientation = (deviceOrientation) => {
    this.setState({ deviceOrientation });
  };

  render() {
    const { initialOrientation, orientation, specificOrientation, deviceOrientation } = this.state;
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native Orientation Demo!
        </Text>
        <Text style={styles.instructions}>
          {`Initial Orientation: ${initialOrientation}`}
        </Text>
        <Text style={styles.instructions}>
          {`Current Orientation: ${orientation}`}
        </Text>
        <Text style={styles.instructions}>
          {`Specific Orientation: ${specificOrientation}`}
        </Text>
        <Text style={styles.instructions}>
          {`Device Orientation: ${deviceOrientation}`}
        </Text>
        <TouchableOpacity
          onPress={Orientation.unlockAllOrientations}
          style={styles.button}
        >
          <Text style={styles.instructions}>
            Unlock All Orientations
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={Orientation.lockToPortrait}
          style={styles.button}
        >
          <Text style={styles.instructions}>
            Lock To Portrait
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={Orientation.lockToLandscape}
          style={styles.button}
        >
          <Text style={styles.instructions}>
            Lock To Landscape
          </Text>
        </TouchableOpacity>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
  button: {
    padding: 5,
    margin: 5,
    borderWidth: 1,
    borderColor: 'white',
    borderRadius: 3,
    backgroundColor: 'grey',
  }
});

AppRegistry.registerComponent('demo', () => demo);
