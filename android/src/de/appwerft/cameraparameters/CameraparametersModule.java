package de.appwerft.cameraparameters;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

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
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
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

	public CameraparametersModule() {
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(final TiApplication app) {

	}

	@Kroll.method
	public void getAllCams(KrollDict opts) {
		this.getAllCameras(opts);
	}

	@Kroll.method
	public void getAllCameras(KrollDict opts) {
		// import callbacks:
		startTime = System.currentTimeMillis();
		if (opts != null && opts instanceof KrollDict) {
			if (opts.containsKeyAndNotNull("onSuccess")) {
				successCallback = (KrollFunction) opts.get("onSuccess");
			}
			if (opts.containsKeyAndNotNull("onError")) {
				errorCallback = (KrollFunction) opts.get("onError");
			}
			Log.d(LCAT, "👿😈 Build.VERSION.SDK_INT=" + Build.VERSION.SDK_INT);
			AsyncTask<Void, Void, Void> doRequest = new AsyncTask<Void, Void, Void>() {
				@SuppressWarnings("deprecation")
				@Override
				protected Void doInBackground(Void[] dummy) {
					Activity currentActivity = TiApplication.getInstance()
							.getCurrentActivity();
					KrollDict resultDict = new KrollDict();
					ArrayList<KrollDict> listOfCameras = new ArrayList<KrollDict>();
					Log.d(LCAT, " Marshmellow+  =>  need lifetime perms");
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						if (currentActivity
								.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
							resultDict.put("error",
									"No permission to access camera manager.");
							if (errorCallback != null)
								errorCallback
										.call(getKrollObject(), resultDict);
						}
					}
					Log.d(LCAT, "Build.VERSION.SDK_INT="
							+ Build.VERSION.SDK_INT);
					if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						Log.d(LCAT, "we choose node for moderne cameras");
						try {
							Context context = TiApplication.getInstance()
									.getApplicationContext();
							CameraManager cameraManager = (CameraManager) context
									.getSystemService(Context.CAMERA_SERVICE);
							resultDict.put("count",
									cameraManager.getCameraIdList().length);
							resultDict.put("api", "android.hardware.camera2");
							resultDict.put("level", Build.VERSION.SDK_INT);
							for (String id : cameraManager.getCameraIdList()) {
								KrollDict dict = new KrollDict();
								CameraCharacteristics character = cameraManager
										.getCameraCharacteristics(id);

								Boolean flashAvailable = character
										.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);

								SizeF physicalsize = character
										.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);

								Rect activeArray = character
										.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

								int cOrientation = character
										.get(CameraCharacteristics.LENS_FACING);
								dict.put(
										"orientation",
										(cOrientation == CameraCharacteristics.LENS_FACING_FRONT) ? "front"
												: "rear");
								dict.put("flashAvailable", flashAvailable);
								dict.put("pixelResolution", activeArray.width()
										+ "×" + activeArray.height());
								dict.put("megapixel", ((double) activeArray
										.width() * (double) activeArray
										.height()) / 1000000.0f);
								dict.put("chipSize", physicalsize.getWidth()
										+ "×" + physicalsize.getHeight());
								listOfCameras.add(dict);
							}
						} catch (CameraAccessException e) {
							Log.e(LCAT, "Failed to interact with camera.", e);
							resultDict.put("error",
									"Failed to interact with camera.");
							if (errorCallback != null)
								errorCallback
										.call(getKrollObject(), resultDict);
						}
					} else {
						Log.d(LCAT, "we choose node for old cameras (else)");
						int countOfCams = android.hardware.Camera
								.getNumberOfCameras();
						resultDict.put("count", countOfCams);
						resultDict.put("api", "android.hardware.Camera");
						resultDict.put("level", Build.VERSION.SDK_INT);
						for (int i = 0; i < countOfCams; i++) {
							KrollDict dict = new KrollDict();
							Camera cam = android.hardware.Camera.open(i);
							android.hardware.Camera.Parameters parameters = cam
									.getParameters();
							String flashMode = parameters.getFlashMode();
							android.hardware.Camera.Size size = parameters
									.getPictureSize();
							/* FRONT or REAR? */
							Camera.CameraInfo cInfo = new Camera.CameraInfo();
							Camera.getCameraInfo(i, cInfo);
							if (cInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
								dict.put("orientation", "front");
							} else if (cInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
								dict.put("orientation", "rear");
							}
							dict.put(
									"megapixel",
									((double) size.width * (double) size.height) / 1000000.0f);
							dict.put("pixelResolution", size.width + "×"
									+ size.height);
							dict.put("flashAvailable",
									(flashMode != null) ? true : false);
							listOfCameras.add(dict);
							cam.release();
						}
					}
					resultDict.put("cameras", listOfCameras.toArray());
					long currentTime = System.currentTimeMillis();
					long runtime = currentTime - startTime;
					resultDict.put("runtime", runtime);
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
