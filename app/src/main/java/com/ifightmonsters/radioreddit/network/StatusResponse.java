package com.ifightmonsters.radioreddit.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ifightmonsters.radioreddit.entities.Status;
import com.ifightmonsters.radioreddit.entities.typeadapter.StatusTypeAdapter;
import com.squareup.okhttp.Response;

import java.io.UnsupportedEncodingException;

/**
 * Created by Gregory on 10/4/2014.
 */
public class StatusResponse extends BaseResponse {

    private Status status;

    public StatusResponse(Response response){
        super(response);
        Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Status.class, new StatusTypeAdapter()).create();
        try{
            status = gson.fromJson(new String(body, WebServices.CHARSET_UTF_8), Status.class);
        } catch(UnsupportedEncodingException e){
            Log.wtf(sLog, e);
        }
    }

    public Status getStatus() {
        return status;
    }

    public String getStringBody(){
        try{
            return new String(body, WebServices.CHARSET_UTF_8);
        } catch(UnsupportedEncodingException e){
            Log.wtf(sLog, e);
        }

        return null;
    }
}
