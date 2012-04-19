package com.pheromone.plugins;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.*;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;

public class MediaQuery extends Plugin {

	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		
		// TODO Gérer les playlists
		
		PluginResult result = null;
		Log.d("Action", action);
		if(action.equals("listArtists")){
			JSONArray json = this.listArtists();
			result = new PluginResult(Status.OK, json);
		}else if(action.equals("listAlbumsFromArtist")){
			JSONArray json = null;
			try {
				json = this.listAlbumsFromArtist(data.getString(0));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			result = new PluginResult(Status.OK, json);
		}else if(action.equals("listSongsFromArtist")){
			JSONArray json = null;
			try {
				json = this.listSongsFrom(data.getString(0), "artist");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			result = new PluginResult(Status.OK, json);
		}else if(action.equals("listSongsFromAlbum")){
			JSONArray json = null;
			try {
				json = this.listSongsFrom(data.getString(0), "album");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			result = new PluginResult(Status.OK, json);
		}
		return result;
	}
	
	public JSONArray listArtists(){
		

		String[] projection = {
		        MediaStore.Audio.Artists._ID,
		        MediaStore.Audio.Artists.ARTIST
		};

		Cursor cursor = ctx.managedQuery(
		        MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
		        projection,
		        null,
		        null,
		        null);

		JSONArray songs = new JSONArray();
		List<String> artists = new ArrayList<String>();
		
		while(cursor.moveToNext()){
				try {
					songs.put(new JSONObject().put("id", cursor.getString(0)).put("name", cursor.getString(1)));
				} catch (JSONException e) {
					e.printStackTrace();
				}
		}
		return songs;
	}
	
	public JSONArray listAlbumsFromArtist(String artistId){
		
		String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.ARTIST_ID + " = "+artistId;

		String[] projection = {
				MediaStore.Audio.Media.ARTIST_ID,
				MediaStore.Audio.Media.ALBUM,
		        MediaStore.Audio.Media.ALBUM_ID
		        
		};

		Cursor cursor = ctx.managedQuery(
		        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
		        projection,
		        selection,
		        null,
		        null);

		JSONArray songs = new JSONArray();
		List<String> albums = new ArrayList<String>();
		
		while(cursor.moveToNext()){
			// TODO: Voir si on peut pas juste récupérer la liste des ARTISTES, au lieu de trier chaque toune.
			if(!albums.contains(cursor.getString(2))){				
				albums.add(cursor.getString(2));
				Log.d("AlbumName", cursor.getString(1));
				String artwork = getAlbumArtwork(cursor.getLong(2));
				//String artwork = "";
				
				try {					
					songs.put(new JSONObject().put("id", cursor.getString(2)).put("name", cursor.getString(1)).put("art", artwork));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return songs;
	}
	
	private String getAlbumArtwork(Long album_id){
		Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
		Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
		ContentResolver res = ctx.getContentResolver();
		InputStream in = null;
		Log.d("Album URI", String.valueOf(uri));
		try {
			in = res.openInputStream(uri);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "";
		}
		Log.d("Album Id", String.valueOf(album_id));
		Bitmap artwork = BitmapFactory.decodeStream(in);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		artwork.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		String base64Img = Base64.encodeToString(byteArray, Base64.DEFAULT);
		
		return base64Img;
	}
	
	public JSONArray listSongsFrom(String fromId, String from){
		String selection = null;
		if(from == "artist"){
			selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.ARTIST_ID + " = "+fromId;
		}else if(from == "album"){
			selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.ALBUM_ID + " = "+fromId;
		}

		String[] projection = {
				MediaStore.Audio.Media._ID,
		        MediaStore.Audio.Media.ARTIST_ID,
		        MediaStore.Audio.Media.ARTIST,
		        MediaStore.Audio.Media.ALBUM_ID,
		        MediaStore.Audio.Media.ALBUM,
		        MediaStore.Audio.Media.TITLE,
		        MediaStore.Audio.Media.DURATION,
		        MediaStore.Audio.Media.DATA
		};

		Cursor cursor = ctx.managedQuery(
		        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
		        projection,
		        selection,
		        null,
		        null);

		JSONArray songs = new JSONArray();		
		while(cursor.moveToNext()){
				try {
					Log.d("songlength", cursor.getString(6));
					songs.put(new JSONObject()
							.put("id", cursor.getString(0))
							.put("artist", cursor.getString(2))
							.put("album", cursor.getString(4))
							.put("title", cursor.getString(5))
							.put("duration", cursor.getString(6))
							.put("path", cursor.getString(7).replace("/mnt/sdcard", ""))
							
							);
				} catch (JSONException e) {
					e.printStackTrace();
				}
		}
		return songs;
	}

	
}