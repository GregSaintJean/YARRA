package com.ifightmonsters.radioreddit.network;

import com.squareup.okhttp.OkHttpClient;

public final class WebServices {

    private static final String LOG = "WebServices";

    public static final String USER_AGENT_KEY = "User-Agent";
    public static final String USER_AGENT_VALUE = "Radio Reddit for Android";
    public static final String CHARSET_UTF_8 = "utf-8";

    private static OkHttpClient sClient;

    public static OkHttpClient getClientInstance(){
        if(sClient == null){
            sClient = new OkHttpClient();
        }
        return sClient;
    }

    private WebServices(){}
}
