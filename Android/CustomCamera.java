package com.pheromone.plugins;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONArray;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.pheromone.stoppix.R;

public class CustomCamera extends Activity implements SurfaceHolder.Callback, OnClickListener {
	static final int FOTO_MODE = 0;
	Camera mCamera;
	boolean mPreviewRunning = false;
	private Context mContext = this;
	private File filePath;
	private int targetWidth;
	private int targetHeight;
	public JSONArray filenames = new JSONArray();
	public int picsTaken=0;
	
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private ImageButton addBtn;
	private ImageButton closeBtn;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		Bundle extras = getIntent().getExtras();
		
		this.targetWidth = extras.getInt("resizeWidth");
		this.targetHeight = extras.getInt("resizeHeight");
		this.filePath = new File(extras.getString("filePath"));
		
		Log.d("Filepath", extras.getString("filePath"));

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera_surface);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		mSurfaceView.setOnClickListener(this);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		addBtn = (ImageButton) findViewById(R.id.addButton);
		addBtn.setOnClickListener(this);
		closeBtn = (ImageButton) findViewById(R.id.closeButton);
		closeBtn.setOnClickListener(this);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] imageData, Camera c) {
			if (imageData != null) {
				if(StoreByteImage(imageData, 100)){
					mCamera.startPreview();
					picsTaken++;
					ObjectAnimator fadeOut = ObjectAnimator.ofFloat(addBtn, "alpha", 1f); 
					fadeOut.setDuration(300);
					fadeOut.start();
					addBtn.setClickable(true);
					closeBtn.setClickable(true);
				}				
			}
		}
	};
	
	private void closeCam(){
		Intent i = new Intent();
		Log.d("Tess",filenames.toString());
		i.putExtra("fileNames", filenames.toString());
		setResult(RESULT_OK, i);
		finish();
	}

	protected void onResume() {
		super.onResume();
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	protected void onStop() {
		super.onStop();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();

	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// XXX stopPreview() will crash if preview is not running
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}

		Camera.Parameters p = mCamera.getParameters();
		p.setPreviewSize(w, h);
		p.setFocusMode(p.FOCUS_MODE_AUTO);
		mCamera.setParameters(p);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCamera.startPreview();
		mPreviewRunning = true;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();
	}

	public void onClick(View target) {
		switch(target.getId()) {
		case R.id.addButton:
			//Animate the alpha from current value to 0
			//this will make it invisible
			ObjectAnimator fadeOut = ObjectAnimator.ofFloat(addBtn, "alpha", 0f);
			fadeOut.setDuration(300);
			fadeOut.start();
			addBtn.setClickable(false);
			closeBtn.setClickable(false);
			mCamera.autoFocus(onFocus);
			break;
		case R.id.closeButton:
			closeCam();
			break;
		case R.id.surface_camera:
			//mCamera.autoFocus(null);
			break;
		}
		
	}
	
	Camera.AutoFocusCallback onFocus = new Camera.AutoFocusCallback() {		
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			mCamera.takePicture(null, mPictureCallback, mPictureCallback);			
		}
	};
	
	public boolean StoreByteImage(byte[] imageData, int quality) {
		long fileName = System.currentTimeMillis() / 1000L;
		if(createDir()){
	        File sdImageMainDirectory = new File(filePath.getAbsolutePath()+"/"+fileName+".jpg");
			FileOutputStream fileOutputStream = null;
			try {
				BitmapFactory.Options options=new BitmapFactory.Options();
				options.inSampleSize = 5;
				
				Bitmap myImage = BitmapFactory.decodeByteArray(imageData, 0,
						imageData.length,options);
				
				myImage = scaleBitmap(myImage);
				
				fileOutputStream = new FileOutputStream(
						sdImageMainDirectory.toString());
								
	  
				BufferedOutputStream bos = new BufferedOutputStream(
						fileOutputStream);
	
				myImage.compress(CompressFormat.JPEG, quality, bos);
				Log.d("Img Height",myImage.getHeight()+"");
	
				bos.flush();
				bos.close();
	
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.filenames.put(fileName+".jpg");
			return true;
		}else{
			return false;
		}
	}
	
	private boolean createDir() {
		boolean ret = true;
	    if (!filePath.exists()) {
	        if (!filePath.mkdirs()) {
	            Log.e("TravellerLog :: ", "Problem creating Image folder");
	            ret = false;
	        }
	    }
	    return ret;
	}

	/**
     * Scales the bitmap according to the requested size.
     * 
     * @param bitmap        The bitmap to scale.
     * @return Bitmap       A new Bitmap object of the same bitmap after scaling. 
     */
    public Bitmap scaleBitmap(Bitmap bitmap) {
        int newWidth = this.targetWidth;
        int newHeight = this.targetHeight;
        int origWidth = bitmap.getWidth();
        int origHeight = bitmap.getHeight();

        // If no new width or height were specified return the original bitmap
        if (newWidth <= 0 && newHeight <= 0) {
            return bitmap;
        }
        // Only the width was specified
        else if (newWidth > 0 && newHeight <= 0) {
            newHeight = (newWidth * origHeight) / origWidth;
        }
        // only the height was specified
        else if (newWidth <= 0 && newHeight > 0) {
            newWidth = (newHeight * origWidth) / origHeight;
        }
        // If the user specified both a positive width and height
        // (potentially different aspect ratio) then the width or height is
        // scaled so that the image fits while maintaining aspect ratio.
        // Alternatively, the specified width and height could have been
        // kept and Bitmap.SCALE_TO_FIT specified when scaling, but this
        // would result in whitespace in the new image.
        else {
            double newRatio = newWidth / (double)newHeight;
            double origRatio = origWidth / (double)origHeight;

            if (origRatio > newRatio) {
                newHeight = (newWidth * origHeight) / origWidth;
            } else if (origRatio < newRatio) {
                newWidth = (newHeight * origWidth) / origHeight;
            }
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

}