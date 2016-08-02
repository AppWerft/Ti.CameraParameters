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


Here  example outputs:
```javascript
 {
    "model":"SM-T230",
    "hardware":"pxa1088",
    "level":19,
    "count":2,
    "manufacturer":"samsung",
    "device":"degaswifi",
    "runtime":2593,
    "api":"android.hardware.Camera",
    "cameras":[{
        "orientation":"rear",
        "pixelResolution":"2048×1536",
        "flashAvailable":false,
        "megapixel":3.145728
    },{
        "orientation":"front",
        "pixelResolution":"1280×960",
        "flashAvailable":false,
        "megapixel":1.2288
}]}


 {
    "model":"GT-I9195I",
    "hardware":"qcom",
    "level":19,
    "count":2,
    "manufacturer":"samsung",
    "device":"serranovelte",
    "runtime":755,
    "api":"android.hardware.Camera",
    "cameras":[{
        "orientation":"rear",
        "pixelResolution":"3264×2448",
        "flashAvailable":true,
        "megapixel":7.990272
    },{
        "orientation":"front",
        "pixelResolution":"1392×1392",
        "flashAvailable":true,
        "megapixel":1.937664
}]}

 {
    "device":"hammerhead",
    "runtime":667,
    "api":"android.hardware.camera2",
    "count":2,
    "manufacturer":"LGE",
    "hardware":"hammerhead",
    "level":23,
    "model":"Nexus 5",
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
}]}


{
    "device":"CatS40",
    "runtime":1103,
    "api":"android.hardware.camera2",
    "count":2,
    "manufacturer":"BullittGroupLimited",
    "hardware":"qcom",
    "level":22,
    "model":"S40",
    "cameras":[{
        "chipSize":"4.19528×3.150077",
        "pixelResolution":"3264×2448",
        "flashAvailable":true,
        "orientation":"rear",
        "megapixel":7.990272
    },{
        "chipSize":"2.8328347×2.0981839",
        "pixelResolution":"1600×1200",
        "flashAvailable":false,
        "orientation":"front",
        "megapixel":1.92
}]}

{
    "device":"flo",
    "runtime":141,
    "api":"android.hardware.camera2",
    "count":2,
    "manufacturer":"asus",
    "hardware":"flo",
    "level":21,
    "model":"Nexus 7",
    "cameras":[{
        "chipSize":"3.673598×2.738401",
        "pixelResolution":"2592×1944",
        "flashAvailable":false,
        "orientation":"rear",
        "megapixel":5.038848
    },{
        "chipSize":"2.459996×1.4800001",
        "pixelResolution":"1280×768",
        "flashAvailable":false,
        "orientation":"front",
        "megapixel":0.98304
}]}


{
    "device":"OnePlus",
    "runtime":383,
    "api":"android.hardware.Camera",
    "count":2,
    "manufacturer":"OnePlus",
    "hardware":"qcom",
    "level":22,
    "model":"ONE E1003",
    "cameras":[{
        "pixelResolution":"4160×3120",
        "size":{
            "height":3120,
            "width":4160
        },
        "flashAvailable":true,
        "orientation":"rear",
        "megapixel":12.9792
    },{
        "pixelResolution":"3264×2448",
        "size":{
            "height":2448,
            "width":3264
        },
        "flashAvailable":false,
        "orientation":"front",
        "megapixel":7.990272
}]}

{
    "model":"GT-I9300",
    "hardware":"smdk4x12",
    "level":18,"count":2,
    "manufacturer":"samsung",
    "device":"m0","runtime":1354,"api":"android.hardware.Camera","cameras":[{"orientation":"rear","pixelResolution":"3264×2448","flashAvailable":true,"megapixel":7.990272,"size":{"height":2448,"width":3264}},{"orientation":"front","pixelResolution":"1392×1392","flashAvailable":false,"megapixel":1.937664,"size":{"height":1392,"width":1392}}]}
```

Note: `chipSize` is only available on the `Camera2` API. Therefore it is only available in API 21 (Lollipop) and later.
