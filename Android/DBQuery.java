package com.pheromone.plugins;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;
import org.quickconnect.dbaccess.*;

/**
 * @author savage
 *
 */
public class DBQuery extends Plugin {

	private static final String DATABASE_NAME = "data.db";

	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		PluginResult result = null;
		Log.d("Action", action);
		if(action.equals("select")){
			String json = this.select(data);
			result = new PluginResult(Status.OK, json);
			
		}else if(action.equals("query")){
			Log.d("Durr", "Hurr");
			String lastId = this.query(data);
			result = new PluginResult(Status.OK, lastId);
		}
		return result;
	}

	public String select(JSONArray data){
		String sql = new String();
		
		try {
			sql = data.getString(0);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		DataAccessResult retVal = null;
		try {
			 retVal = DataAccessObject.getData(ctx, DATABASE_NAME, sql, null);
		} catch (DataAccessException e) {
			 e.printStackTrace();
		}
		
		String[] columns = retVal.getColumnNames();
		
		ArrayList qResults = retVal.getResults();
		
		String pairedJSON = null;
		try {
			pairedJSON = this.keyBind(columns, qResults);
		} catch (JSONException e) {
			Log.e("Query Failed", "");
			e.printStackTrace();
		}
		Log.d("QueryR", pairedJSON);
		return pairedJSON;
	}
	
	public String query(JSONArray data){
		
		String sql = new String();
		try {
			sql = data.getString(0);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		DataAccessResult retVal = null;
		try {
			 retVal = DataAccessObject.setData(ctx, DATABASE_NAME, sql, null);
		} catch (DataAccessException e) {
			 e.printStackTrace();
		}
		
		
		
		String lastInsertedId = new String();
		if(sql.contains("INSERT")){
			Log.d("is insert", "Yuss");
			try {
				retVal = DataAccessObject.getData(ctx, DATABASE_NAME, "SELECT last_insert_rowid()", null);
			} catch (DataAccessException e) {
				e.printStackTrace();
			}
			ArrayList<ArrayList<String>> qResults = retVal.getResults();
			lastInsertedId = qResults.get(0).get(0).toString();
		}
		Log.d("Last ID", lastInsertedId);
		return lastInsertedId;
	}
	
	public String keyBind(String[] columns, ArrayList results) throws JSONException{
		JSONArray json = new JSONArray();
		for(int i=0;i<results.size();i++){
			JSONObject row = new JSONObject();
			JSONArray jsonRow = new JSONArray(results.get(i).toString());
			for(int k=0;k<jsonRow.length();k++){
				row.put(columns[k], jsonRow.get(k));
			}
			json.put(row);
		}
		return json.toString();
		
	}

}
