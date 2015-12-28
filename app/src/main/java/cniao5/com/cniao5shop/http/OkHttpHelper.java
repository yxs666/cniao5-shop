package cniao5.com.cniao5shop.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;



public class OkHttpHelper {






        public static final String TAG="OkHttpHelper";

        private  static  OkHttpHelper mInstance;
        private OkHttpClient mHttpClient;
        private Gson mGson;

        private Handler mHandler;



        static {
            mInstance = new OkHttpHelper();
        }

        private OkHttpHelper(){

            mHttpClient = new OkHttpClient();
            mHttpClient.setConnectTimeout(10, TimeUnit.SECONDS);
            mHttpClient.setReadTimeout(10,TimeUnit.SECONDS);
            mHttpClient.setWriteTimeout(30,TimeUnit.SECONDS);

            mGson = new Gson();

            mHandler = new Handler(Looper.getMainLooper());

        };

        public static  OkHttpHelper getInstance(){
            return  mInstance;
        }




        public void get(String url,BaseCallback callback){


            Request request = buildGetRequest(url);

            request(request,callback);

        }


        public void post(String url,Map<String,String> param, BaseCallback callback){

            Request request = buildPostRequest(url,param);
            request(request,callback);
        }





        public  void request(final Request request,final  BaseCallback callback){

            callback.onBeforeRequest(request);

            mHttpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Request request, IOException e) {
                    callbackFailure(callback, request, e);

                }

                @Override
                public void onResponse(Response response) throws IOException {

//                    callback.onResponse(response);
                    callbackResponse(callback,response);

                    if(response.isSuccessful()) {

                        String resultStr = response.body().string();

                        Log.d(TAG, "result=" + resultStr);

                        if (callback.mType == String.class){
                            callbackSuccess(callback,response,resultStr);
                        }
                        else {
                            try {

                                Object obj = mGson.fromJson(resultStr, callback.mType);
                                callbackSuccess(callback,response,obj);
                            }
                            catch (com.google.gson.JsonParseException e){ // Json解析的错误
                                callback.onError(response,response.code(),e);
                            }
                        }
                    }
                    else {
                        callbackError(callback,response,null);
                    }

                }
            });


        }


        private void callbackSuccess(final  BaseCallback callback , final Response response, final Object obj ){

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(response, obj);
                }
            });
        }


        private void callbackError(final  BaseCallback callback , final Response response, final Exception e ){

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onError(response,response.code(),e);
                }
            });
        }



    private void callbackFailure(final  BaseCallback callback , final Request request, final IOException e ){

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFailure(request,e);
            }
        });
    }


    private void callbackResponse(final  BaseCallback callback , final Response response ){

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onResponse(response);
            }
        });
    }



    private  Request buildPostRequest(String url,Map<String,String> params){

            return  buildRequest(url,HttpMethodType.POST,params);
        }

        private  Request buildGetRequest(String url){

            return  buildRequest(url,HttpMethodType.GET,null);
        }

        private  Request buildRequest(String url,HttpMethodType methodType,Map<String,String> params){


            Request.Builder builder = new Request.Builder()
                    .url(url);

            if (methodType == HttpMethodType.POST){
                RequestBody body = builderFormData(params);
                builder.post(body);
            }
            else if(methodType == HttpMethodType.GET){
                builder.get();
            }


            return builder.build();
        }



        private RequestBody builderFormData(Map<String,String> params){


            FormEncodingBuilder builder = new FormEncodingBuilder();

            if(params !=null){

                for (Map.Entry<String,String> entry :params.entrySet() ){

                    builder.add(entry.getKey(),entry.getValue());
                }
            }

            return  builder.build();

        }



        enum  HttpMethodType{

            GET,
            POST,

        }



}
