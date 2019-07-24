package com.dylan_wang.capturescreen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import android.util.Log;

public class MainActivity extends Activity {
	
	
	
	/**
	 * 借鉴地址 http://blog.csdn.net/zdy0_2004/article/details/48979903
	 * 本项目借鉴 一种全新的截屏方法并自己修改去掉悬浮按钮做成后台实时运行截图方式
	 * 保存路径： getSDCardPath() + "/CaptureScreen/ScreenImages";
	 * 欢迎大家学习。
	 *  一种全新的截屏方法
	 * 
	 * 
	 * */
	private String TAG = "Service";
	private int result = 0;
	private Intent intent = null;
	private int REQUEST_MEDIA_PROJECTION = 1;
	private MediaProjectionManager mMediaProjectionManager;
	private boolean flag;
	ScreenService myService;
	int progress = 0;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
			mMediaProjectionManager = (MediaProjectionManager) getApplication()
					.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		startIntent();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void startIntent() {
		if (intent != null && result != 0) {
			Log.i(TAG, "user agree the application to capture screen");

			((ShotApplication) getApplication()).setResult(result);
			((ShotApplication) getApplication()).setIntent(intent);
			Intent intent = new Intent(getApplicationContext(),
					ScreenService.class);
			startService(intent);
			Log.i(TAG, "start service Service1");
		} else {
			startActivityForResult(
					mMediaProjectionManager.createScreenCaptureIntent(),
					REQUEST_MEDIA_PROJECTION);
			// Service1.mMediaProjectionManager1 = mMediaProjectionManager;
			((ShotApplication) getApplication())
					.setMediaProjectionManager(mMediaProjectionManager);
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_MEDIA_PROJECTION) {
			if (resultCode != Activity.RESULT_OK) {
				return;
			} else if (data != null && resultCode != 0) {
				Log.i(TAG, "user agree the application to capture screen");
				// Service1.mResultCode = resultCode;
				// Service1.mResultData = data;
				result = resultCode;
				intent = data;
				try {

					((ShotApplication) getApplication()).setResult(resultCode);
					((ShotApplication) getApplication()).setIntent(data);
					Intent intent = new Intent(getApplicationContext(),
							ScreenService.class);
					intent.putExtra("cs1", "aa");
					intent.putExtra("cs2", "bb");

					bindService(intent, conn, Context.BIND_AUTO_CREATE);
					// startService(intent);

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				Log.i(TAG, "start service Service1");

			}
		}
	}

	// 绑定service必须要传递的ServiceConnection对象
	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// 得到MyService类型的service对象
			flag = true;
			myService = ((ScreenService.MsgBinder) service).getService();
			// 监听进度值改变功
			myService.setOnProgressListener(new OnProgressListener() {
				@Override
				public void onProgress(Bitmap bitmap) {
					progress++;
					String SavePath = getSDCardPath() + "/CaptureScreen/ScreenImages";
					if (bitmap != null) {

						try {
							File path = new File(SavePath);
							// 文件
							String filepath = SavePath + "/" + progress + ":"
									+ progress + "Screen_1.png";
							File file = new File(filepath);
							if (!path.exists()) {
								path.mkdirs();
							}
							if (!file.exists()) {
								file.createNewFile();
							}
							FileOutputStream fos = null;
							fos = new FileOutputStream(file);
							if (null != fos) {
								bitmap.compress(Bitmap.CompressFormat.PNG, 100,
										fos);
								fos.flush();
								fos.close();
								Intent media = new Intent(
										Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
								Uri contentUri = Uri.fromFile(file);
								media.setData(contentUri);
								MainActivity.this.sendBroadcast(media);
								Log.i(TAG, "screen image saved");
							}

							/*
							 * File fileImage = new File(nameImage); if
							 * (!fileImage.exists()) {
							 * fileImage.createNewFile(); Log.i(TAG,
							 * "image file created"); } FileOutputStream out =
							 * new FileOutputStream(fileImage); if (out != null)
							 * { bitmap.compress(Bitmap.CompressFormat.PNG, 50,
							 * out); out.flush(); out.close(); Intent media =
							 * new Intent(
							 * Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); Uri
							 * contentUri = Uri.fromFile(fileImage);
							 * media.setData(contentUri);
							 * this.sendBroadcast(media); Log.i(TAG,
							 * "screen image saved"); }
							 */
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};

	/**
	 * 获取SDCard的目录路径
	 * 
	 * @return
	 */
	private String getSDCardPath() {
		File sdcardDir = null;
		// 判断SDCard是否存在
		boolean sdcardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdcardExist) {
			sdcardDir = Environment.getExternalStorageDirectory();
		}
		return sdcardDir.toString();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unBind();

	}

	private void unBind() {
		if (flag == true) {

			unbindService(conn);
			flag = false;
		}
	}
}
