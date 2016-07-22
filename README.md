Ti.Cameraparamters
==================

This is a Titanium module for exposing some camera properties like resolution.

Thanks to [Roamler](https://www.roamler.com/)  in Amsterdam for sponsoring and [Rene](http://renepot.net) for patience and support. 

<img src="https://secure.gravatar.com/avatar/325662ace9877e9af4291aff59ec9318.jpg?s=512&d=https%3A%2F%2Fa.slack-edge.com%2F7fa9%2Fimg%2Favatars%2Fava_0026-512.png" width=40/> <img src="https://www.roamler.com/images/logo-roamler-shield.png" height=40/>


Attention!

For older android versions including Kitkat it uses `android.hardware.Camera` and since API 21 (Lollipop) it uses the new API `android.hardware.camera2`. 
Up to Marshmellow the module needs CAMERA runtime permisions!

Because the access to hardware takes some time, the module works as an asynchronous task outside UIthread. Therefore the module works with a callback for result.

##Usage

Just include the module in `tiapp.xml`

```xml
<module platform="android">de.appwerft.cameraparameters</module>
```

Don't forget to add the permission needed in `tiapp.xml` or in your manifest:
```xml
<uses-permission android:name="android.permission.CAMERA"/>
```

Once included, you can use the module in your app. Example:

```javascript
require("de.appwerft.cameraparameters").getAllCameras({
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

Note: `chipSize`, `orientation` and `flashAvailable` are only available on the `Camera2` API. Therefore it is only available in API 21 (Lollipop) and later.
