package com.dylan_wang.capturescreen;

import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.LinearLayout;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class ScreenService extends Service {
	private LinearLayout mFloatLayout = null;
	private WindowManager mWindowManager = null;
	private static final String TAG = "MainActivity";
	private OnProgressListener listener;
	private MediaProjection mMediaProjection = null;
	private VirtualDisplay mVirtualDisplay = null;
	public static int mResultCode = 0;
	public static Intent mResultData = null;
	public static MediaProjectionManager mMediaProjectionManager1 = null;
	private WindowManager mWindowManager1 = null;
	private int windowWidth = 0;
	private int windowHeight = 0;
	private ImageReader mImageReader = null;
	private DisplayMetrics metrics = null;
	private int mScreenDensity = 0;
	private int progress = 0;
	public static final int maxProgress = 100;

	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:

				try {
					startVirtual();
					Thread.sleep(500);
					startCapture();
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case 1:

				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		// createFloatView();

		createVirtualEnvironment();
		startDownload();
	}

	public void setOnProgressListener(OnProgressListener listener) {
		this.listener = listener;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new MsgBinder();
	}

	public void startDownload() {

		new Thread(new Runnable() {
			@Override
			public void run() {

				while (progress < maxProgress) {
					if (listener != null) {

						progress++;

						myHandler.sendEmptyMessage(0);

					}
				}

			}
		}).start();
	}

	public void startShoot() {
		myHandler.sendEmptyMessage(0);
	}

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void createVirtualEnvironment() {

		mMediaProjectionManager1 = (MediaProjectionManager) getApplication()
				.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
		mWindowManager1 = (WindowManager) getApplication().getSystemService(
				Context.WINDOW_SERVICE);
		Display d = mWindowManager1.getDefaultDisplay();
		Point p = new Point();
		d.getSize(p);
		windowWidth = p.x;
		windowHeight = p.y;
		metrics = new DisplayMetrics();
		mWindowManager1.getDefaultDisplay().getMetrics(metrics);
		mScreenDensity = metrics.densityDpi;
		mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1,
				2);

		Log.i(TAG, "prepared the virtual environment");
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public void startVirtual() {
		if (mMediaProjection != null) {
			Log.i(TAG, "want to display virtual");
			virtualDisplay();
		} else {
			Log.i(TAG, "start screen capture intent");
			Log.i(TAG, "want to build mediaprojection and display virtual");
			setUpMediaProjection();
			virtualDisplay();
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public void setUpMediaProjection() {
		mResultData = ((ShotApplication) getApplication()).getIntent();
		mResultCode = ((ShotApplication) getApplication()).getResult();
		mMediaProjectionManager1 = ((ShotApplication) getApplication())
				.getMediaProjectionManager();
		mMediaProjection = mMediaProjectionManager1.getMediaProjection(
				mResultCode, mResultData);
		Log.i(TAG, "mMediaProjection defined");
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void virtualDisplay() {
		mVirtualDisplay = mMediaProjection.createVirtualDisplay(
				"screen-mirror", windowWidth, windowHeight, mScreenDensity,
				DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
				mImageReader.getSurface(), null, null);
		Log.i(TAG, "virtual displayed");
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void startCapture() {
		// strDate = dateFormat.format(new java.util.Date());
		// nameImage = pathImage + "" + progress + "" + strDate + ".png";

		Image image = mImageReader.acquireLatestImage();
		int width = image.getWidth();
		int height = image.getHeight();
		final Image.Plane[] planes = image.getPlanes();
		final ByteBuffer buffer = planes[0].getBuffer();
		int pixelStride = planes[0].getPixelStride();
		int rowStride = planes[0].getRowStride();
		int rowPadding = rowStride - pixelStride * width;
		Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride,
				height, Bitmap.Config.ARGB_8888);
		bitmap.copyPixelsFromBuffer(buffer);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
		image.close();
		Log.i(TAG, "image data captured");

		listener.onProgress(bitmap);

	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void tearDownMediaProjection() {
		if (mMediaProjection != null) {
			mMediaProjection.stop();
			mMediaProjection = null;
		}
		Log.i(TAG, "mMediaProjection undefined");
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void stopVirtual() {
		if (mVirtualDisplay == null) {
			return;
		}
		mVirtualDisplay.release();
		mVirtualDisplay = null;
		Log.i(TAG, "virtual display stopped");
	}

	@Override
	public void onDestroy() {
		// to remove mFloatLayout from windowManager
		super.onDestroy();
		if (mFloatLayout != null) {
			mWindowManager.removeView(mFloatLayout);
		}
		tearDownMediaProjection();
		Log.i(TAG, "application destroy");
	}

	public class MsgBinder extends Binder {
		public ScreenService getService() {
			return ScreenService.this;
		}
	}
}