package de.appwerft.cameraparameters;

import java.util.ArrayList;
import java.util.Timer;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.util.SizeF;

@SuppressWarnings("deprecation")
@Kroll.module(name = "Cameraparameters", id = "de.appwerft.cameraparameters")
public class CameraparametersModule extends KrollModule {
	private static final String LCAT = "CAMINFO";
	KrollFunction successCallback = null;
	KrollFunction errorCallback = null;
	long startTime = 0;
	static Timer mTimer = new Timer();
	KrollDict resultDict;
	ArrayList<KrollDict> listOfCameras;

	public CameraparametersModule() {
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(final TiApplication app) {
		Log.d(LCAT,
				"inside onAppCreate, if you see this, then the module is successful embedded");
	}

	private void askOldCamera() {
		Log.d(LCAT, "we choose node for old cameras Kitkat and older");
		int countOfCams = android.hardware.Camera.getNumberOfCameras();
		Log.d(LCAT, "we  have number of cams " + countOfCams);

		resultDict.put("count", countOfCams);
		resultDict.put("api", "android.hardware.Camera");
		resultDict.put("level", Build.VERSION.SDK_INT);
		for (int i = 0; i < countOfCams; i++) {
			KrollDict dict = new KrollDict();
			android.hardware.Camera cam = android.hardware.Camera.open(i);
			android.hardware.Camera.Parameters parameters = cam.getParameters();
			String flashMode = parameters.getFlashMode();
			android.hardware.Camera.Size size = parameters.getPictureSize();
			/* FRONT or REAR? */
			android.hardware.Camera.CameraInfo cInfo = new android.hardware.Camera.CameraInfo();
			android.hardware.Camera.getCameraInfo(i, cInfo);
			if (cInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
				dict.put("orientation", "front");
			} else if (cInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
				dict.put("orientation", "rear");
			}
			dict.put("megapixel",
					((double) size.width * (double) size.height) / 1000000.0f);
			dict.put("pixelResolution", size.width + "×" + size.height);
			dict.put("flashAvailable", (flashMode != null) ? true : false);
			listOfCameras.add(dict);
			cam.release();
		}
	}

	private void askNewCamera() {
		Log.d(LCAT, "we choose node for moderne cameras Lollipop and newer");
		try {
			Context context = TiApplication.getInstance()
					.getApplicationContext();
			android.hardware.camera2.CameraManager cameraManager = (android.hardware.camera2.CameraManager) context
					.getSystemService(Context.CAMERA_SERVICE);
			resultDict.put("count", cameraManager.getCameraIdList().length);
			resultDict.put("api", "android.hardware.camera2");
			resultDict.put("level", Build.VERSION.SDK_INT);
			for (String id : cameraManager.getCameraIdList()) {
				KrollDict dict = new KrollDict();
				android.hardware.camera2.CameraCharacteristics character = cameraManager
						.getCameraCharacteristics(id);

				Boolean flashAvailable = character
						.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE);

				SizeF physicalsize = character
						.get(android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);

				Rect activeArray = character
						.get(android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

				int cOrientation = character
						.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING);
				dict.put(
						"orientation",
						(cOrientation == android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT) ? "front"
								: "rear");
				dict.put("flashAvailable", flashAvailable);
				dict.put("pixelResolution", activeArray.width() + "×"
						+ activeArray.height());
				dict.put("megapixel",
						((double) activeArray.width() * (double) activeArray
								.height()) / 1000000.0f);
				dict.put("chipSize", physicalsize.getWidth() + "×"
						+ physicalsize.getHeight());
				listOfCameras.add(dict);
			}
		} catch (android.hardware.camera2.CameraAccessException e) {
			Log.e(LCAT, "Failed to interact with camera.", e);
			resultDict.put("error", "Failed to interact with camera.");
			if (errorCallback != null)
				errorCallback.call(getKrollObject(), resultDict);
		}

	}

	@Kroll.method
	public void getAllCameras(KrollDict opts) {
		Log.d(LCAT, "starting of getAllCameras");
		resultDict = new KrollDict();
		listOfCameras = new ArrayList<KrollDict>();
		startTime = System.currentTimeMillis();
		if (opts != null && opts instanceof KrollDict) {
			if (opts.containsKeyAndNotNull("onSuccess")) {
				successCallback = (KrollFunction) opts.get("onSuccess");
			}
			if (opts.containsKeyAndNotNull("onError")) {
				errorCallback = (KrollFunction) opts.get("onError");
			}
			Log.d(LCAT, "all parameters imported");
			Log.d(LCAT, "👿😈 Build.VERSION.SDK_INT=" + Build.VERSION.SDK_INT);
			AsyncTask<Void, Void, Void> doRequest = new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void[] dummy) {
					Log.d(LCAT, "async task (background) started");
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						Log.d(LCAT, " Marshmellow+  =>  need lifetime perms");
						Activity currentActivity = TiApplication.getInstance()
								.getRootOrCurrentActivity();
						if (currentActivity
								.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
							resultDict.put("error",
									"No permission to access camera manager.");
							if (errorCallback != null)
								errorCallback
										.call(getKrollObject(), resultDict);
						}
					} else {
						Log.d(LCAT,
								"no M, we don't need request of permissions");
					}
					Log.d(LCAT, "Build.VERSION.SDK_INT="
							+ Build.VERSION.SDK_INT);
					if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						askNewCamera();
					} else {
						askOldCamera();
					}
					resultDict.put("cameras", listOfCameras.toArray());
					long currentTime = System.currentTimeMillis();
					long runtime = currentTime - startTime;
					resultDict.put("runtime", runtime);
					resultDict.put("model", Build.MODEL);
					resultDict.put("manufacturer", Build.MANUFACTURER);
					resultDict.put("hardware", Build.HARDWARE);
					resultDict.put("device", Build.DEVICE);
					if (successCallback != null)
						successCallback.call(getKrollObject(), resultDict);
					return null;

				}

			};// async task
			doRequest.execute();
		} else
			Log.e(LCAT,
					"Parameter is an object with 2 callbacks (onSuccess and onError)");
	}
}
