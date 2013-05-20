package com.example.android360;

import java.io.File;
import java.io.IOException;
import com.googlecode.tesseract.android.TessBaseAPI;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public String image_path;
	public EditText edit_text;
	public ImageView image;
	public boolean shoot=false;
	public static final String app_path = Environment.getExternalStorageDirectory().toString() + "/tesseract-ocr";
	public static final String lang = "deu";
	public Button cam_button;
	private static final String TAG = "ORC.java";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		cam_button = (Button) findViewById(R.id.button);
		cam_button.setOnClickListener(myhandler);
		edit_text = (EditText) findViewById(R.id.editText1);
		image = (ImageView) findViewById(R.id.imageView1);
		image_path = app_path + "/ocr.png";

		
		String[] paths = new String[] { app_path + "/" };
		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: " + path + " failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + path);
				}
			}
		}
		// lang.traineddata
		// http://code.google.com/p/tesseract-ocr/downloads/list
		if ((new File(app_path + "/tessdata/" + lang + ".traineddata")).exists()) {
				Log.e(TAG, "found " + lang + " traineddata"); 
		}else{
				Log.e(TAG, "traineddata not found");
		}
	
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i(TAG, "resultCode: " + resultCode);

		if (resultCode == -1) {
			Toast msg = Toast.makeText(getBaseContext(),
					"got the picture...",
					Toast.LENGTH_SHORT);
			msg.show();
			onPhoto();
		} else {
			Log.v(TAG, "picture error");
		}
	}
	
	
	protected void onPhoto() {
		shoot = true;

		// Bild verkleinern
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;
		Bitmap bitmap = BitmapFactory.decodeFile(image_path,opts);

		//http://stackoverflow.com/questions/4517634/
		try {
			ExifInterface exif = new ExifInterface(image_path);
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orient: " + orientation);

			int rotate = 0;

			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}
			Log.v(TAG, "Rotation: " + rotate);
			// Bild drehen
			//http://stackoverflow.com/questions/8608734/
			if (rotate != 0) {

				Matrix matrix = new Matrix();
				matrix.postRotate(rotate);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, 
						bitmap.getWidth(), bitmap.getHeight(), 
				                              matrix, false);
				// Bitmap.Config ARGB_8888 	Each pixel is stored on 4 bytes
				// https://github.com/rmtheis/android-ocr/
				bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
			}
		} catch (IOException e) {
			Log.e(TAG, "can not rotate the image: " + e.toString());
		}
	
		// Bild in der ImageView der Anwendung anzeigen
		image.setImageBitmap( bitmap );	

		// Tesseract initialisieren 
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		
		
		//deutsche Sprachdaten laden String lang = "deu"
		baseApi.init(app_path, lang);
		
		// z.B. nur Zahlen erkennen
		//baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,"1234567890");
		//baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		
		// Bild laden und OCR starten 
		baseApi.setImage(bitmap);
		
		String ocr = baseApi.getUTF8Text();
		baseApi.end();

		Log.v(TAG, "OCR-TEXT: " + ocr);
		// alle Sonderzeichen und überflüssigen Leerzeichen raus werfen
		ocr = ocr.replaceAll("[^a-zA-Z0-9]+", " ");
 
		edit_text.setText(ocr);		
	
		
	}

	 protected void startCameraActivity()
	    {
	    	File file = new File( image_path );
	    	Uri outputFileUri = Uri.fromFile( file );
	    	
	    	Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
	    	intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
	    	
	    	startActivityForResult( intent, 0 );
	    }
	 
	 
	  View.OnClickListener myhandler = new View.OnClickListener() {
    	  public void onClick(View v) {
    	      if( cam_button.getId() == ((Button)v).getId() ){
    	    	  startCameraActivity();
    	      }
    
    	  }
    	};
	 
	 
	 
	
}
