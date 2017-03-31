package de.appwerft.cameraparameters;

import java.util.Timer;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

@Kroll.module(name = "Cameraparameters", id = "de.appwerft.cameraparameters")
public class CameraparametersModule extends KrollModule {
	private final class CameraRequestHandler extends
			AsyncTask<Void, Void, KrollDict> {
		@Override
		protected KrollDict doInBackground(Void[] dummy) {
			Object[] listOfCameras = TiCameraParameters.askCameraGeneric();
			if (listOfCameras != null)
				resultDict.put("cameras", listOfCameras);
			return resultDict;
		}

		protected void onPostExecute(KrollDict resultDict) {
			long currentTime = System.currentTimeMillis();
			long runtime = currentTime - startTime;
			resultDict.put("runtime", runtime);
			resultDict.put("model", Build.MODEL);
			resultDict.put("version", Build.VERSION.SDK_INT);
			resultDict.put("versionRelease", Build.VERSION.RELEASE);
			resultDict.put("manufacturer", Build.MANUFACTURER);
			resultDict.put("hardware", Build.HARDWARE);
			resultDict.put("device", Build.DEVICE);
			if (successCallback != null)
				successCallback.call(getKrollObject(), resultDict);
			TiApplication.getInstance().getApplicationContext()
					.getApplicationInfo().toString();
		}
	}

	public static final String LCAT = "CAMINFO 📷 📷";
	KrollFunction successCallback = null;
	KrollFunction errorCallback = null;
	long startTime = 0;
	static Timer mTimer = new Timer();
	KrollDict resultDict;
	KrollFunction onGranted, onRevoked;

	public CameraparametersModule() {
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(final TiApplication app) {
		Log.d(LCAT,
				"inside onAppCreate, if you see this, then the module is successful embedded");
	}

	@Kroll.method
	public boolean isPermissionGranted() {
		if (Build.VERSION.SDK_INT >= 23) {
			Activity thisActivity = TiApplication.getInstance()
					.getCurrentActivity();
			if (ContextCompat.checkSelfPermission(thisActivity,
					android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
				Log.v(LCAT, "Permission is granted");
				return true;
			} else {
				Log.v(LCAT, "Permission is revoked");
				return false;
			}
		} else { // permission is automatically granted on sdk<23 upon
					// installation
			Log.v(LCAT, "Permission is granted");
			return true;
		}
	}

	@Kroll.method
	public void requestPermission(KrollDict args) {
		if (args.containsKeyAndNotNull("onGranted")) {
			Object cb = args.get("onGranted");
			if (cb instanceof KrollFunction) {
				onGranted = (KrollFunction) cb;
			}
		}
		if (args.containsKeyAndNotNull("onRevoked")) {
			Object cb = args.get("onRevoked");
			if (cb instanceof KrollFunction) {
				onRevoked = (KrollFunction) cb;
			}
		}
		if (Build.VERSION.SDK_INT >= 23) {
			Activity thisActivity = TiApplication.getInstance()
					.getCurrentActivity();
			if (ContextCompat.checkSelfPermission(thisActivity,
					android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
				Log.v(LCAT, "Permission is granted");
				if (onGranted != null)
					onGranted.call(getKrollObject(), new KrollDict());
			} else {
				Log.v(LCAT, "Permission is revoked");
				ActivityCompat.requestPermissions(thisActivity,
						new String[] { Manifest.permission.CAMERA }, 1);

			}
		} else { // permission is automatically granted on sdk<23 upon
					// installation
			if (onGranted != null)
				onGranted.call(getKrollObject(), new KrollDict());
		}

	}

	@Kroll.method
	public void getAllCameras(@Kroll.argument(optional = true) KrollDict opts) {
		if (isPermissionGranted() == false) {
			Log.e(LCAT, "no permission for camera granted");
			return;
		}
		resultDict = new KrollDict();
		startTime = System.currentTimeMillis();
		if (opts != null && opts instanceof KrollDict) {
			if (opts.containsKeyAndNotNull("onSuccess")) {
				successCallback = (KrollFunction) opts.get("onSuccess");
			}
			if (opts.containsKeyAndNotNull("onError")) {
				errorCallback = (KrollFunction) opts.get("onError");
			}
			AsyncTask<Void, Void, KrollDict> doRequest = new CameraRequestHandler();
			doRequest.execute();
		} else
			Log.e(LCAT,
					"Parameter is an object with 2 callbacks (onSuccess and onError)");
	}
}
