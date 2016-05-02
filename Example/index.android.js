/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */

import React, {
  AppRegistry,
  Component,
  DeviceEventEmitter,
  StyleSheet,
  NativeModules,
  Text,
  View
} from 'react-native';
import Fingerprint from 'react-native-fingerprint';
import FingerprintDialog from './FingerprintDialog';


const initialStatus = {
  isAuthorized: null,
  message: 'Touch sensor'
};

class Example extends Component {
  constructor() {
    super();
    this.state = {
      hasFingerprints: false,
      isHardwareDetected: false,
      authorized: false,
      status: initialStatus
    }
  }
  componentDidMount() {
    Fingerprint.init();
    DeviceEventEmitter.addListener('fingerprintAuthorized', (e) => {
      this.setState({status: {isAuthorized: true, message: 'Fingerprint recognized.'}});
    });
    DeviceEventEmitter.addListener('fingerprintError', (e) => {
      this.setState({status: {isAuthorized: false, message: e.message}});
    });
  }

  componentDidUpdate() {
    if (this.state.status.isAuthorized) {
      timeout(1300).then(() => this.setState({authorized: true, status: initialStatus}));
    }
    else if (this.state.status.isAuthorized === false) {
      timeout(1600).then(() => this.setState({status: initialStatus}));
    }
  }

  render() {
    console.log(this.state);
    return (
      <View style={styles.container}>
        {!this.state.authorized && <FingerprintDialog {...this.state.status}/>}
        {this.state.authorized && <Text>Super secret text</Text>}
      </View>
    );
  }
}

function timeout(ms) {
  return new Promise(resolve => {
      setTimeout(() => resolve(), ms);
  });
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});

AppRegistry.registerComponent('Example', () => Example);
