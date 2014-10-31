package com.ifightmonsters.yarra.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ifightmonsters.yarra.entities.Status;
import com.ifightmonsters.yarra.entities.typeadapter.TalkTypeAdapter;
import com.squareup.okhttp.Response;

import java.io.UnsupportedEncodingException;

/**
 * Created by Gregory on 10/31/2014.
 */
public class TalkResponse extends BaseResponse{

    private Status status;

    public TalkResponse(Exception e){
        super(e);
    }

    public TalkResponse(Response response){
        super(response);
        Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Status.class, new TalkTypeAdapter()).create();
        try{
            status = gson.fromJson(new String(body, WebServices.CHARSET_UTF_8), Status.class);
        } catch(UnsupportedEncodingException e){
            Log.wtf(sLog, e);
        }
    }

    public Status getStatus() {
        return status;
    }

}
