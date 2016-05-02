import React, {
  Animated,
  Component,
  Image,
  PixelRatio,
  StyleSheet,
  Text,
  TouchableNativeFeedback,
  View,
} from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';


export default
class FingerprintDialog extends Component {
  constructor() {
    super();
    this.state = {
      top: new Animated.Value(-1000),
      opacity: new Animated.Value(0)
    };
  }

  componentDidMount() {
    Animated.sequence([
      Animated.timing(this.state.opacity, {duration: 300, toValue: 0.7}),
      Animated.timing(this.state.top, {duration: 300, toValue: 0})
    ]).start();
  }

  render() {
    const {isAuthorized, message} = this.props;
    let icon = <Icon size={30} name='fingerprint' color='#009688'/>
    if (isAuthorized) {
      icon = <Icon size={30} name='check-circle' color='#009688'/>;
    }
    else if (isAuthorized === false) {
      icon = <Icon size={30} name='error' color='#f4511e'/>;
    }

    return (
      <Animated.View style={[styles.dialogContainer, {top: this.state.top}]}>
        <Animated.View style={[styles.overlay, {opacity: this.state.opacity}]}/>
        <View style={styles.dialog}>
          <Text style={styles.title}>Sign in</Text>
          <Text style={styles.description}>Confim fingerprint to continue</Text>
          <View style={styles.content}>
            {icon}
            <Text style={styles.status}>{message}</Text>
          </View>
          <View style={styles.buttons}>
            <TouchableNativeFeedback background={TouchableNativeFeedback.SelectableBackground()}>
              <View>
                <Text style={styles.button}>CANCEL</Text>
              </View>
            </TouchableNativeFeedback>
            <TouchableNativeFeedback background={TouchableNativeFeedback.SelectableBackground()}>
              <View>
                <Text style={styles.button}>USE PASSWORD</Text>
              </View>
            </TouchableNativeFeedback>
          </View>
        </View>
      </Animated.View>
    );
  }
}

const styles = StyleSheet.create({
  overlay: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    backgroundColor: 'black',
    opacity: 0,
  },
  dialogContainer: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    alignItems: 'center',
    justifyContent: 'center',
  },
  dialog: {
    backgroundColor: 'white',
    padding: 20,
    width: 300,
    paddingBottom: 10,
    borderRadius: 2,
  },
  title: {
    fontWeight: '500',
    fontSize: 20,
    color: '#222',
    marginBottom: 16,
  },
  description: {
    fontSize: 16,
    fontWeight: '400',
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 16,
  },
  status: {
    marginHorizontal: 12,
    color: '#BFBFBF',
    flex: 1,
  },
  buttons: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'flex-end'
  },
  button: {
    textAlign: 'center',
    margin: 10,
    fontWeight: '500',
    color: '#009688',
  }
})
