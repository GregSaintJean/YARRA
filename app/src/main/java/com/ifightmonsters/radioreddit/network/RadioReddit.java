package com.ifightmonsters.radioreddit.network;

import android.net.Uri;
import android.util.Log;

import com.squareup.okhttp.Request;

import java.io.IOException;

public final class RadioReddit {

    private static final String LOG = "RadioReddit";

    //Station status Uris
    private static final Uri MAIN = Uri.parse("http://radioreddit.com/api/status.json");
    private static final Uri ELECTRONIC = Uri.parse("http://radioreddit.com/api/electronic/status.json");
    private static final Uri INDIE = Uri.parse("http://radioreddit.com/api/indie/status.json");
    private static final Uri HIPHOP = Uri.parse("http://radioreddit.com/api/hiphop/status.json");
    private static final Uri ROCK = Uri.parse("http://radioreddit.com/api/rock/status.json");
    private static final Uri METAL = Uri.parse("http://radioreddit.com/api/metal/status.json");
    private static final Uri TALK = Uri.parse("http://radioreddit.com/api/talk/status.json");
    private static final Uri RANDOM = Uri.parse("http://radioreddit.com/api/random/status.json");

    //TODO These should be deleted in the near future, maybe
    /* If the database ever has no data and can't reach the server
       the app should attempt to fallback to these uris.

       These uris should be updated based regularly to make sure
       that the app always reaches the station. When they are updated
       they should include the last updated time.

       Last modified: 10/10/2014 12:07 PM
     */
    public static final Uri[] BACKUP_STATIONS = {
            Uri.parse("http://cdn.audiopump.co/radioreddit/main_mp3_128k"),
            Uri.parse("http://cdn.audiopump.co/radioreddit/electronic_mp3_128k"),
            Uri.parse("http://cdn.audiopump.co/radioreddit/indie_mp3_128k"),
            Uri.parse("http://cdn.audiopump.co/radioreddit/hiphop_mp3_128k"),
            Uri.parse("http://cdn.audiopump.co/radioreddit/rock_mp3_128k"),
            Uri.parse("http://cdn.audiopump.co/radioreddit/metal_mp3_128k"),
            Uri.parse("http://cdn.audiopump.co/radioreddit/talk_mp3_128k"),
            Uri.parse("http://cdn.audiopump.co/radioreddit/random_mp3_128k")
    };

    public static StatusResponse getMainStatus(){

        Request request = new Request.Builder()
                .url(MAIN.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try{
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch(IOException e){
            Log.i(LOG, e.toString());
        }

        return null;
    }

    public static StatusResponse getElectronicStatus(){
        Request request = new Request.Builder()
                .url(ELECTRONIC.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try{
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch(IOException e){
            Log.i(LOG, e.toString());
        }

        return null;
    }

    public static StatusResponse getIndieStatus(){
        Request request = new Request.Builder()
                .url(INDIE.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try{
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch(IOException e){
            Log.i(LOG, e.toString());
        }

        return null;
    }

    public static StatusResponse getHipHopStatus(){
        Request request = new Request.Builder()
                .url(HIPHOP.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try{
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch(IOException e){
            Log.i(LOG, e.toString());
        }

        return null;
    }

    public static StatusResponse getRockStatus(){
        Request request = new Request.Builder()
                .url(ROCK.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try{
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch(IOException e){
            Log.i(LOG, e.toString());
        }

        return null;
    }

    public static StatusResponse getMetalStatus(){
        Request request = new Request.Builder()
                .url(METAL.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try{
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch(IOException e){
            Log.i(LOG, e.toString());
        }

        return null;
    }

    public static TalkResponse getTalkStatus(){
        Request request = new Request.Builder()
                .url(TALK.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try{
            return new TalkResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch(IOException e){
            Log.i(LOG, e.toString());
        }

        return null;
    }

    public static StatusResponse getRandomStatus(){
        Request request = new Request.Builder()
                .url(RANDOM.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try{
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch(IOException e){
            Log.i(LOG, e.toString());
        }

        return null;
    }

    private RadioReddit(){}
}
