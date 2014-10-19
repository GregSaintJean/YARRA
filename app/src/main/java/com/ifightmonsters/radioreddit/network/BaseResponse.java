package com.ifightmonsters.radioreddit.network;

import android.util.Log;
import android.util.Pair;

import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;

/**
 * Created by Gregory on 10/4/2014.
 */
public abstract class BaseResponse {

    protected static String sLog;

    protected int code;
    protected String message;
    protected byte[] body;
    protected List<Pair<String, String>> headers;

    public BaseResponse(Response response){
        sLog = this.getClass().getSimpleName();
        this.code = response.code();
        this.message = response.message();
        Log.i(sLog, "Status code: " + code);
        Log.i(sLog, "Status message: " + message);
        try{
            this.body = response.body().bytes();
        } catch(IOException e){
            Log.wtf(sLog, e);
        }
        //TODO take down headers

    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public byte[] getBody() {
        return body;
    }

    public List<Pair<String, String>> getHeaders() {
        return headers;
    }
}
