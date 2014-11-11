package com.ifightmonsters.yarra.network;

import android.net.Uri;
import android.util.Log;

import com.squareup.okhttp.Request;

import java.io.IOException;

/**
 * Created by Gregory on 10/31/2014.
 */
public class RadioReddit {

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

    public static StatusResponse getMainStatus() {

        Request request = new Request.Builder()
                .url(MAIN.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try {
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch (IOException e) {
            Log.i(LOG, e.toString());
            return new StatusResponse(e);
        }
    }

    public static StatusResponse getElectronicStatus() {
        Request request = new Request.Builder()
                .url(ELECTRONIC.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try {
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch (IOException e) {
            Log.i(LOG, e.toString());
            return new StatusResponse(e);
        }
    }

    public static StatusResponse getIndieStatus() {
        Request request = new Request.Builder()
                .url(INDIE.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try {
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch (IOException e) {
            Log.i(LOG, e.toString());
            return new StatusResponse(e);
        }
    }

    public static StatusResponse getHipHopStatus() {
        Request request = new Request.Builder()
                .url(HIPHOP.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try {
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch (IOException e) {
            Log.i(LOG, e.toString());
            return new StatusResponse(e);
        }
    }

    public static StatusResponse getRockStatus() {
        Request request = new Request.Builder()
                .url(ROCK.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try {
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch (IOException e) {
            Log.i(LOG, e.toString());
            return new StatusResponse(e);
        }
    }

    public static StatusResponse getMetalStatus() {
        Request request = new Request.Builder()
                .url(METAL.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try {
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch (IOException e) {
            Log.i(LOG, e.toString());
            return new StatusResponse(e);
        }
    }

    public static TalkResponse getTalkStatus() {
        Request request = new Request.Builder()
                .url(TALK.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try {
            return new TalkResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch (IOException e) {
            Log.i(LOG, e.toString());
            return new TalkResponse(e);
        }
    }

    public static StatusResponse getRandomStatus() {
        Request request = new Request.Builder()
                .url(RANDOM.toString())
                .addHeader(WebServices.USER_AGENT_KEY, WebServices.USER_AGENT_VALUE)
                .build();
        try {
            return new StatusResponse(WebServices.getClientInstance().newCall(request).execute());
        } catch (IOException e) {
            Log.i(LOG, e.toString());
            return new StatusResponse(e);
        }
    }

    private RadioReddit() {
    }

}
