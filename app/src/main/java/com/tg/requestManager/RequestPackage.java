package com.tg.requestManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class RequestPackage {
    //private String url = new AppGlobals().getAppServerAddressExt();

    public RequestPackage(String url, String method, HashMap<String, String> params) {
        this.url = url;
        this.method = method;
        this.params = params;
    }

    private final String url;
    private final String method;
    private Map<String, String> params;

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    //The method below is only called if the request method has been set to GET
    //GET requests sends data in the url and it has to be encoded correctly in order
    //for the server to understand the request. This method encodes the data in the
    //params variable so that the server can understand the request

    public String getEncodedParams() {

        if(params == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            String value = null;
            try {
                value = URLEncoder.encode(params.get(key), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key + "=" + value);
        }
        return sb.toString();
    }
}
