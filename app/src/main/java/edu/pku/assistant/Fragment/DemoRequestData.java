package edu.pku.assistant.Fragment;


import android.util.Log;

import edu.pku.assistant.Tool.Constants;
import in.srain.cube.request.CacheAbleRequest;
import in.srain.cube.request.CacheAbleRequestHandler;
import in.srain.cube.request.CacheAbleRequestJsonHandler;
import in.srain.cube.request.JsonData;
import in.srain.cube.request.RequestFinishHandler;
import in.srain.cube.request.RequestJsonHandler;
import in.srain.cube.request.SimpleRequest;

public class DemoRequestData {

    public static void getImageList(final RequestFinishHandler<JsonData> requestFinishHandler) {

        RequestJsonHandler requestHandler = new RequestJsonHandler() {
            @Override
            public void onRequestFinish(JsonData jsonData) {
                requestFinishHandler.onRequestFinish(jsonData);
            }
        };

        SimpleRequest<JsonData> request = new SimpleRequest<JsonData>(requestHandler);
        String url = Constants.base_url + "contact/index.php/group/getname";
        request.getRequestData().setRequestUrl(url);
        request.send();
        Log.d("demo request", "survived");
    }
}