Ti.Cameraparamters
==================

This is a Titanium module for exposing some camera infos.


Attention!

For older android versions including Kitkat it uses `android.hardware.Camera` and since Lollipop it uses the new API `android.hardware.camera2`. 
Upto Marshmellow the module needs CAMERA runtime permisions!

Because the access to hardware takes a couple of time the module works as async task outside UIthread. Therefore the module works with a callback for result.


##Usage

Just include 

```xml
<module platform="android">de.appwerft.cameraparameters</module>
```

Don't forget to test, if this line is in your manifest:
```xml
<uses-permission android:name="android.permission.CAMERA"/>
```

In you tiapp.xml. After this you can access the module by:

```javascript
require("de.appwerft.cameraparameters").getAllCams({
        onSuccess : function(_e) {
        console.log(_e);
    },
        onError : function(_e) {
        console.log(_e);
    }
});

```

In result payload you can get:

1. runtime in ms
2. used API
3. Array with camera detail infos.

Here an example output:

```javascript
{
    "runtime":2,
    "count":2,
    "cameras":[{
        "chipSize":"4.6032×3.5168",
        "pixelResolution":"3280×2464",
        "flashAvailable":true,
        "orientation":"rear",
        "megapixel":8.08192
    },{
        "chipSize":"2.4624×1.8544",
        "pixelResolution":"1288×968",
        "flashAvailable":false,
        "orientation":"front",
        "megapixel":1.246784
    }],
    "api":"android.hardware.camera2"
}
```