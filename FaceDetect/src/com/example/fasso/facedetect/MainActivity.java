package com.example.fasso.facedetect;

import java.io.File;

import com.example.fasso.log.L;

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

		/** Show default Image */
		BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
		BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;

		/** In case want to Upload Image from drwable folder */
		myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.face_detection, BitmapFactoryOptionsbfo);
		myBitmap = Bitmap.createScaledBitmap(myBitmap, width, calculateNewHeight(myBitmap), true);
		View v = new myView(this);
		addToLayout(v);
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
					addToLayout(v);

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (requestCode == 2) {

				Uri selectedImage = data.getData();
				String[] filePath = { MediaStore.Images.Media.DATA };

				Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
				c.moveToFirst();
				String picturePath = c.getString(c.getColumnIndex(filePath[0]));
				c.close();

				/** Initialized Bitmap Images with RGB 565 */
				BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
				BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;

				myBitmap = BitmapFactory.decodeFile(picturePath, BitmapFactoryOptionsbfo);

				/**
				 * Width would be same as device but height of image may vary as per ratio of
				 */
				myBitmap = Bitmap.createScaledBitmap(myBitmap, width, calculateNewHeight(myBitmap), true);

				/** Marking Face in this Class */
				View v = new myView(this);
				addToLayout(v);

			}
		}
	}

	public void addToLayout(View v) {
		LinearLayout llImage = (LinearLayout) findViewById(R.id.ll_add_image);

		/** Remove Previous any added view */
		llImage.removeAllViews();
		llImage.invalidate();

		/** Attach New View in layout with face marking */
		llImage.addView(v);
	}

	/** height of image may vary as per ratio of Image with respect to width */
	public static int calculateNewHeight(Bitmap myBitmap) {
		float tmp = ((float) myBitmap.getHeight() / (float) myBitmap.getWidth());
		float newHeight = tmp * width;
		return (int) newHeight;
	}

	/** Custom View to detect Face in Given Image */
	private class myView extends View {

		private int imageWidth, imageHeight;
		private int numberOfFace = 10;
		private FaceDetector myFaceDetect;
		private FaceDetector.Face[] myFace;

		float myEyesDistance;
		int numberOfFaceDetected;

		public myView(Context context) {

			super(context);

			if (myBitmap != null) {
				imageWidth = myBitmap.getWidth();
				imageHeight = myBitmap.getHeight();

				/** Max number of faces to be detected*/
				myFace = new FaceDetector.Face[numberOfFace];
				
				/**Creates a FaceDetector, configured with the size of the images to be analyzed and the 
				 * numberOfFace = maximum number of faces that can be detected. 
				 * Note that the width of the image must be even.*/
				myFaceDetect = new FaceDetector(imageWidth, imageHeight, numberOfFace);
				
				/**Finds all the faces found in a given android.graphics.Bitmap*/
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
				
				L.t(this.getContext(), myMidPoint.x+"" + myMidPoint.y+" - "+myEyesDistance);

				canvas.drawRect((int) (myMidPoint.x - myEyesDistance * 2), (int) (myMidPoint.y - myEyesDistance * 2),
						(int) (myMidPoint.x + myEyesDistance * 2), (int) (myMidPoint.y + myEyesDistance * 2), myPaint);
			}
		}

	}

}
