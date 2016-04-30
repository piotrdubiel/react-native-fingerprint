/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */

import React, {
  AppRegistry,
  Component,
  StyleSheet,
  NativeModules,
  Text,
  View
} from 'react-native';
import Fingerprint from 'react-native-fingerprint';


class Example extends Component {
  constructor() {
    super();
    this.state = {
      hasFingerprints: false,
      isHardwareDetected: false,
    }
  }
  componentDidMount() {
    // this.setState({
    //   hasFingerprints: await Fingerprint.hasEnrolledFingerprints(),
    //   isHardwareDetected: await Fingerprint.isHardwareDetected(),
    // })
    Fingerprint.authenticate().then(() => {
      console.log('AUTHENTICATED', arguments);
    })
    .catch(() => {
      console.log('ERROR', arguments);
    });
  }
  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <Text style={styles.instructions}>
          Has fingerprints {this.state.hasFingerprints ? 'YES' : 'NO'}{"\n"}
          Is hardware detected {this.state.isHardwareDetected ? 'YES' : 'NO'}
        </Text>
        <Text style={styles.instructions}>
          Shake or press menu button for dev menu
        </Text>
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
});

AppRegistry.registerComponent('Example', () => Example);
