package com.pheromone.plugins;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

/**
 * Custom Camera Phonegap Plugin. Saves the photo in a specified folder (with project Id). 2 Things needed to make the plugin work. Add this to the manifest:
 * <activity android:name=".StopPixActivity" android:label="@string/app_name" android:screenOrientation="landscape" android:configChanges="orientation|keyboardHidden"><intent-filter><action android:name="android.intent.action.MAIN" /><category android:name="android.intent.category.LAUNCHER" /></intent-filter></activity>
 * And add the camera_surface.xml i your res/layout folder.
 * @author savage
 * @param projectId     The projectId, referring to the folder to save the pic into.
 * @param width			Width of the image to be taken
 * @param height		Height of the image to be taken
 * @return JSONArray    Returns an array of filenames.
 */
public class CameraBridge extends Plugin {
	Uri myPicture = null;
	private int targetWidth = 600;
	private int targetHeight = 450;
	public String callbackId;

	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		this.callbackId = callbackId;		
		Log.d("Action", action);
		if(action.equals("getPicture")){
			String projectId = new String();
			try {
				projectId = data.getString(0);
				if(!data.isNull(1) && !data.isNull(2)){
					targetWidth = data.getInt(1);
					targetHeight = data.getInt(2);
				}
				this.getPicture(projectId);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			
		}
		PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        return r;
	}

	private void getPicture(String projectId) {
		Intent i = new Intent("com.pheromone.plugins.intent.action.ShowCustomCamera");			
		i.putExtra("filePath", Environment.getExternalStorageDirectory().getAbsolutePath()+"/Stoppix/"+projectId);
		i.putExtra("resizeWidth",targetWidth);
		i.putExtra("resizeHeight",targetHeight);
		this.ctx.startActivityForResult((Plugin) this, i, 0);
	}
		
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("resquestCode", requestCode+"");
		switch(requestCode) {
	    	case (0) : {
	    		if (resultCode == Activity.RESULT_OK){
	    			Log.d("Success ActivityResult", data.getStringExtra("fileNames"));
	    			this.success(new PluginResult(PluginResult.Status.OK, data.getStringExtra("fileNames")), this.callbackId);   
	    		}
	    		break;
	    	} 
		}
	}    
}
