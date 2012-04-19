package com.pheromone.plugins;


import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;

public class Language extends Plugin{

	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		PluginResult result = null;
		Log.d("Action", action);
		if(action.equals("code")){
			String language = this.languageCode();
			result = new PluginResult(Status.OK, language);
			
		}
		return result;
	}
	
	public String languageCode(){
		String language = Locale.getDefault().getLanguage();
		Log.d("language", language);
		return language;
	}

}
