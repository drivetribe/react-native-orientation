/**
 * @flow
 */

import React from 'react';
import { AppRegistry, Button, StyleSheet, Text, View } from 'react-native';

import Orientation, {
  type OrientationType,
} from '@drivetribe/react-native-orientation';

type State = {
  initialOrientation: ?OrientationType,
  orientation: ?OrientationType,
  specificOrientation: ?OrientationType,
  deviceOrientation: number,
};

class demo extends React.PureComponent<*, State> {
  state = {
    initialOrientation: Orientation.getInitialOrientation(),
    orientation: null,
    specificOrientation: null,
    deviceOrientation: 0,
  };

  constructor(props: *) {
    super(props);
    Orientation.addOrientationListener(this._updateOrientation);
    Orientation.addSpecificOrientationListener(this._updateSpecificOrientation);
    Orientation.addDeviceOrientationListener(this._updateDeviceOrientation);
  }

  componentWillUnmount() {
    Orientation.removeOrientationListener(this._updateOrientation);
    Orientation.removeSpecificOrientationListener(
      this._updateSpecificOrientation,
    );
    Orientation.removeDeviceOrientationListener(this._updateDeviceOrientation);
  }

  _updateOrientation = (orientation: OrientationType) => {
    this.setState({ orientation });
  };

  _updateSpecificOrientation = (specificOrientation: OrientationType) => {
    this.setState({ specificOrientation });
  };

  _updateDeviceOrientation = (deviceOrientation: number) => {
    this.setState({ deviceOrientation });
  };

  render() {
    const {
      initialOrientation,
      orientation,
      specificOrientation,
      deviceOrientation,
    } = this.state;
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native Orientation Demo!
        </Text>
        <Text style={styles.instructions}>
          {`Initial Orientation: ${initialOrientation || 'null'}`}
        </Text>
        <Text style={styles.instructions}>
          {`Current Orientation: ${orientation || 'null'}`}
        </Text>
        <Text style={styles.instructions}>
          {`Specific Orientation: ${specificOrientation || 'null'}`}
        </Text>
        <Text style={styles.instructions}>
          {`Device Orientation: ${deviceOrientation}`}
        </Text>
        <Button
          title="Unlock All Orientations"
          onPress={Orientation.unlockAllOrientations}
        />
        <Button title="Lock To Portrait" onPress={Orientation.lockToPortrait} />
        <Button
          title="Lock To Landscape"
          onPress={Orientation.lockToLandscape}
        />
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
  },
});

AppRegistry.registerComponent('demo', () => demo);
