package de.appwerft.cameraparameters;

import java.util.ArrayList;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera.Size;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.os.Build;
import android.util.SizeF;

import android.hardware.camera2.CameraMetadata;

@SuppressWarnings("deprecation")
public class TiCameraParameters {
	public static final String LCAT = CameraparametersModule.LCAT;

	public static Object[] askCameraGeneric() {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return askCamera2();
		} else {
			return askCamera();
		}
	}

	public static Object[] askCamera() {
		KrollDict resultDict = new KrollDict();
		List<KrollDict> listOfCameras = new ArrayList<KrollDict>();
		Log.d(LCAT, "we choose node for old cameras Kitkat and older");
		int countOfCams = android.hardware.Camera.getNumberOfCameras();
		Log.d(LCAT, "we  have number of cams " + countOfCams);
		resultDict.put("count", countOfCams);
		resultDict.put("api", "android.hardware.Camera");
		for (int i = 0; i < countOfCams; i++) {
			KrollDict dict = new KrollDict();
			android.hardware.Camera cam = android.hardware.Camera.open(i);
			android.hardware.Camera.Parameters parameters = cam.getParameters();
			String flashMode = parameters.getFlashMode();
			/* best resolution: */
			android.hardware.Camera.Size size = getBestSize(parameters);
			if (size != null) {
				KrollDict sizeDict = new KrollDict();
				sizeDict.put("width", size.width);
				sizeDict.put("height", size.height);
				dict.put("maxSize", sizeDict);
			}
			/* all available */
			ArrayList<KrollDict> listOfSizes = getAllSizes(parameters);
			if (listOfSizes != null)
				dict.put("sizes", listOfSizes.toArray());
			/* FRONT or REAR? */
			android.hardware.Camera.CameraInfo cInfo = new android.hardware.Camera.CameraInfo();
			android.hardware.Camera.getCameraInfo(i, cInfo);
			if (cInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
				dict.put("orientation", "front");
			} else if (cInfo.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
				dict.put("orientation", "rear");
			}
			dict.put("maxMegapixel",
					((double) size.width * (double) size.height) / 1000000.0f);
			dict.put("maxPixelResolution", size.width + "×" + size.height);
			dict.put("flashAvailable", (flashMode != null) ? true : false);
			listOfCameras.add(dict);
			cam.release();
		}
		return listOfCameras.toArray();
	}

	private static Object[] askCamera2() {
		KrollDict resultDict = new KrollDict();
		List<KrollDict> listOfCameras = new ArrayList<KrollDict>();
		Log.d(LCAT, "we choose node for moderne cameras Lollipop and newer");
		try {
			Context context = TiApplication.getInstance()
					.getApplicationContext();
			android.hardware.camera2.CameraManager cameraManager = (android.hardware.camera2.CameraManager) context
					.getSystemService(Context.CAMERA_SERVICE);
			resultDict.put("count", cameraManager.getCameraIdList().length);
			resultDict.put("api", "android.hardware.camera2");
			for (String id : cameraManager.getCameraIdList()) {
				KrollDict dict = new KrollDict();
				CameraCharacteristics character = cameraManager
						.getCameraCharacteristics(id);

				dict.put("autofocusModes", character
						.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES));
				dict.put("availableEffects", character
						.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS));
				dict.put("availableModes", character
						.get(CameraCharacteristics.CONTROL_AVAILABLE_MODES));
				dict.put(
						"availableSceneModes",
						character
								.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES));
				dict.put(
						"availableVideoStabilizationModes",
						character
								.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES));
				dict.put("availableAutoWhiteBalanceModes", character
						.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES));

				dict.put("availableAutoWhiteBalanceLockModes", character
						.get(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE));
				dict.put("maxRegionsAutoExposer", character
						.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE));
				dict.put("maxRegionsAutoFocus", character
						.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF));

				dict.put("maxRegionsAutoWhiteBalance", character
						.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB));
				dict.put(
						"minFocusDistance",
						character
								.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE));

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
				dict.put("pixelResolution", activeArray.width() + "×"
						+ activeArray.height());
				dict.put("megapixel",
						((double) activeArray.width() * (double) activeArray
								.height()) / 1000000.0f);
				dict.put("chipSize", physicalsize.getWidth() + "×"
						+ physicalsize.getHeight());
				listOfCameras.add(dict);
			}
			return listOfCameras.toArray();
		} catch (android.hardware.camera2.CameraAccessException e) {
			Log.e(LCAT, "Failed to interact with camera.", e);
			return null;
		}

	}

	// http://stackoverflow.com/questions/7968930/camera-app-crashed-some-devices

	private static android.hardware.Camera.Size getBestSize(
			android.hardware.Camera.Parameters parameters) {
		android.hardware.Camera.Size result = null;
		List<Size> supported = parameters.getSupportedPictureSizes();
		if (supported == null)
			return null;
		for (android.hardware.Camera.Size size : supported) {
			if (result == null) {
				result = size;
			} else {
				int resultArea = result.width * result.height;
				int newArea = size.width * size.height;
				if (newArea > resultArea) {
					result = size;
				}
			}
		}
		return (result);
	}

	private static ArrayList<KrollDict> getAllSizes(
			android.hardware.Camera.Parameters parameters) {
		ArrayList<KrollDict> list = new ArrayList<KrollDict>();
		List<Size> supported = parameters.getSupportedPictureSizes();
		if (supported == null)
			return null;
		for (android.hardware.Camera.Size size : supported) {
			KrollDict dict = new KrollDict();
			dict.put("width", size.width);
			dict.put("height", size.height);
			list.add(dict);
		}
		return (list);
	}
}
