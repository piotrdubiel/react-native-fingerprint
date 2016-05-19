import React, {
  AppRegistry,
  Component,
  DeviceEventEmitter,
  StyleSheet,
  Text,
  View
} from 'react-native'
import Fingerprint from 'react-native-fingerprint'
import FingerprintDialog from './FingerprintDialog'


const initialStatus = {
  isAuthorized: null,
  message: 'Touch sensor'
}

class Example extends Component {
  constructor() {
    super()
    this.state = {
      hasFingerprints: false,
      isHardwareDetected: false,
      authorized: false,
      status: initialStatus
    }
  }
  componentDidMount() {
    Fingerprint.init()
    DeviceEventEmitter.addListener('fingerprintAuthorized', this.onAuthorized)
    DeviceEventEmitter.addListener('fingerprintError', this.onError)
  }

  componentDidUpdate() {
    if (this.state.status.isAuthorized) {
      timeout(1300).then(this.hideDialog)
    }
    else if (this.state.status.isAuthorized === false) {
      timeout(1600).then(this.onReset)
    }
  }

  render() {
    return (
      <View style={styles.container}>
        {!this.state.authorized && <FingerprintDialog {...this.state.status}/>}
        {this.state.authorized && <Text>Super secret text</Text>}
      </View>
    )
  }

  onAuthorized() {
    this.setState({status: {isAuthorized: true, message: 'Fingerprint recognized.'}})
  }

  onError(e) {
    this.setState({status: {isAuthorized: false, message: e.message}})
  }

  onHideDialog() {
    this.setState({authorized: true, status: initialStatus})
  }

  onReset() {
    this.setState({status: initialStatus})
  }
}

function timeout(ms) {
  return new Promise(resolve => {
    setTimeout(() => resolve(), ms)
  })
}

const styles = StyleSheet.create({
  container: {
    flex: 1
  }
})

AppRegistry.registerComponent('Example', () => Example)
