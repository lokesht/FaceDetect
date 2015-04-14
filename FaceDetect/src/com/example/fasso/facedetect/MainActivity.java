package com.example.fasso.facedetect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	Bitmap myBitmap;
	private static int height;
	private static int width;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		init();
	}

	public void init() {

		/** Calculate device Height and width */
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		height = metrics.heightPixels;
		width = metrics.widthPixels;

		/** Display just for Visibility */
		Toast.makeText(this, "Height: " + height + " " + "Width: " + width, Toast.LENGTH_LONG).show();

		/** Initialize with click event */
		Button btnAdd = (Button) findViewById(R.id.btn_add);
		btnAdd.setOnClickListener(this);

		Button btnGalary = (Button) findViewById(R.id.btn_galary);
		btnGalary.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.btn_add:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			startActivityForResult(intent, 1);
			break;

		case R.id.btn_galary:
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, 2);
			break;

		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				File f = new File(Environment.getExternalStorageDirectory().toString());
				for (File temp : f.listFiles()) {
					if (temp.getName().equals("temp.jpg")) {
						f = temp;
						break;
					}
				}
				try {
					BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
					BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;

					myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), BitmapFactoryOptionsbfo);

					/**
					 * Width would be same as device but height of image may vary as per ratio of
					 */
					myBitmap = Bitmap.createScaledBitmap(myBitmap, width, calculateNewHeight(myBitmap), true);

					View v = new myView(this);
					LinearLayout llImage = (LinearLayout) findViewById(R.id.ll_add_image);
					llImage.removeAllViews();
					llImage.addView(v);

					// String path = android.os.Environment.getExternalStorageDirectory() + File.separator + "Download";
					// f.delete();
					//
					// OutputStream outFile = null;
					// File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
					// try {
					// outFile = new FileOutputStream(file);
					// myBitmap.compress(Bitmap.CompressFormat.JPEG, 40, outFile);
					//
					//
					// outFile.flush();
					// outFile.close();
					//
					//
					// } catch (FileNotFoundException e) {
					// e.printStackTrace();
					// } catch (IOException e) {
					// e.printStackTrace();
					// } catch (Exception e) {
					// e.printStackTrace();
					// }
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (requestCode == 2) {

				Uri selectedImage = data.getData();
				String[] filePath = { MediaStore.Images.Media.DATA };

				Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
				c.moveToFirst();
				int columnIndex = c.getColumnIndex(filePath[0]);
				String picturePath = c.getString(columnIndex);
				c.close();

				/** Initialised BitMap Images */
				BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
				BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;

				myBitmap = BitmapFactory.decodeFile(picturePath, BitmapFactoryOptionsbfo);
				/**
				 * Width would be same as device but height of image may vary as per ratio of
				 */
				myBitmap = Bitmap.createScaledBitmap(myBitmap, width, calculateNewHeight(myBitmap), true);

				Log.w("path of image from gallery......******************.........", picturePath + "");
				View v = new myView(this);
				LinearLayout llImage = (LinearLayout) findViewById(R.id.ll_add_image);
				llImage.removeAllViews();
				llImage.addView(v);
			}
		}
	}

	/** height of image may vary as per ratio of Image with respect to width */
	public static int calculateNewHeight(Bitmap myBitmap) {
		float tmp = ((float) myBitmap.getHeight() / (float) myBitmap.getWidth());
		float newHeight = tmp * width;
		return (int) newHeight;
	}

	private class myView extends View {

		private int imageWidth, imageHeight;
		private int numberOfFace = 5;
		private FaceDetector myFaceDetect;
		private FaceDetector.Face[] myFace;

		float myEyesDistance;
		int numberOfFaceDetected;

		public myView(Context context) {

			super(context);

			BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
			BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;

			// myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.face_detection,BitmapFactoryOptionsbfo);
			if (myBitmap != null) {
				imageWidth = myBitmap.getWidth();
				imageHeight = myBitmap.getHeight();

				myFace = new FaceDetector.Face[numberOfFace];
				myFaceDetect = new FaceDetector(imageWidth, imageHeight, numberOfFace);
				numberOfFaceDetected = myFaceDetect.findFaces(myBitmap, myFace);

				Toast.makeText(MainActivity.this, "FaceCount=" + numberOfFaceDetected, Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {

			canvas.drawBitmap(myBitmap, 0, 0, null);

			Paint myPaint = new Paint();
			myPaint.setColor(Color.GREEN);
			myPaint.setStyle(Paint.Style.STROKE);
			myPaint.setStrokeWidth(3);

			for (int i = 0; i < numberOfFaceDetected; i++) {

				Face face = myFace[i];
				PointF myMidPoint = new PointF();
				face.getMidPoint(myMidPoint);
				myEyesDistance = face.eyesDistance();

				canvas.drawRect((int) (myMidPoint.x - myEyesDistance * 2), (int) (myMidPoint.y - myEyesDistance * 2),
						(int) (myMidPoint.x + myEyesDistance * 2), (int) (myMidPoint.y + myEyesDistance * 2), myPaint);
			}
		}

	}

}
