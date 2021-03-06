## react-native-orientation
Listen to device orientation changes in react-native and set preferred orientation on screen to screen basis.

### Installation

#### via npm

Run `npm install react-native-orientation --save`

### Linking

#### Using rnpm (iOS + Android)

`react-native link react-native-orientation`

#### Manually

**iOS**

1. Add `node_modules/react-native-orientation/iOS/RCTOrientation.xcodeproj` to your xcode project, usually under the `Libraries` group
1. Add `libRCTOrientation.a` (from `Products` under `RCTOrientation.xcodeproj`) to build target's `Linked Frameworks and Libraries` list


**Android**

1. In `android/setting.gradle`

    ```gradle
    ...
    include ':react-native-orientation', ':app'
    project(':react-native-orientation').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-orientation/android')
    ```

2. In `android/app/build.gradle`

    ```gradle
    ...
    dependencies {
        ...
        compile project(':react-native-orientation')
    }
    ```

3. Register module (in MainApplication.java)

    ```java
    import com.github.yamill.orientation.OrientationPackage;  // <--- import

    public class MainApplication extends Application implements ReactApplication {
      ......

      @Override
      protected List<ReactPackage> getPackages() {
          return Arrays.<ReactPackage>asList(
              new MainReactPackage(),
              new OrientationPackage()      <------- Add this
          );
      }

      ......

    }
    ```

### Configuration

#### Android
Add the following to your project's `MainActivity.java`:

```java
import com.github.yamill.orientation.ConfigurationChangeManager;

@Override
public void onConfigurationChanged(Configuration newConfig) {
   super.onConfigurationChanged(newConfig);
   ConfigurationChangeManager.getInstance().onConfigurationChanged(newConfig);
}
```

#### iOS

Add the following to your project's `AppDelegate.m`:

```objc
#import "Orientation.h" // <--- import

@implementation AppDelegate

  // ...

  - (UIInterfaceOrientationMask)application:(UIApplication *)application supportedInterfaceOrientationsForWindow:(UIWindow *)window {
    return [Orientation getOrientation];
  }

@end
```

## Usage

Whenever you want to use it within React Native code now you can:
`import Orientation from 'react-native-orientation';`

```javascript
  _orientationDidChange = (orientation: string) => {
    if (orientation == 'LANDSCAPE') {
      //do something with landscape layout
    } else {
      //do something with portrait layout
    }
  }

  constructor() {
    //The getOrientation method is async. It happens sometimes that
    //you need the orientation at the moment the js starts running on device.
    //getInitialOrientation returns directly because its a constant set at the
    //beginning of the js code.
    var initial = Orientation.getInitialOrientation();
    if (initial === 'PORTRAIT') {
      //do stuff
    } else {
      //do other stuff
    }
  }

  componentDidMount() {
    Orientation.lockToPortrait(); //this will lock the view to Portrait
    //Orientation.lockToLandscape(); //this will lock the view to Landscape
    //Orientation.unlockAllOrientations(); //this will unlock the view to all Orientations

    Orientation.addOrientationListener(this._orientationDidChange);
  }

  componentWillUnmount() {
    Orientation.getOrientation((err,orientation)=> {
      console.log("Current Device Orientation: ", orientation);
    });
    Orientation.removeOrientationListener(this._orientationDidChange);
  }
```

## Events

- `addOrientationListener(function(orientation))`

orientation can return either `LANDSCAPE` `PORTRAIT` `UNKNOWN`
also `PORTRAITUPSIDEDOWN` is now different from `PORTRAIT`

- `removeOrientationListener(function(orientation))`

- `addSpecificOrientationListener(function(specificOrientation))`

specificOrientation can return either `LANDSCAPE-LEFT` `LANDSCAPE-RIGHT` `PORTRAIT` `UNKNOWN` `PORTRAITUPSIDEDOWN`

- `removeSpecificOrientationListener(function(specificOrientation))`

## Functions

- `lockToPortrait()`
- `lockToLandscape()`
- `lockToLandscapeLeft()`
- `lockToLandscapeRight()`
- `unlockAllOrientations()`
- `getOrientation(function(err, orientation)`

orientation can return either `LANDSCAPE` `PORTRAIT` `UNKNOWN` `PORTRAITUPSIDEDOWN`

- `getSpecificOrientation(function(err, specificOrientation)`

specificOrientation can return either `LANDSCAPE-LEFT` `LANDSCAPE-RIGHT` `PORTRAIT` `UNKNOWN` `PORTRAITUPSIDEDOWN`
