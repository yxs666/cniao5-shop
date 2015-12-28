package cniao5.com.cniao5shop.http;

import android.content.Context;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


import dmax.dialog.SpotsDialog;


public abstract class SpotsCallBack<T> extends BaseCallback<T> {


    private  Context mContext;

    private  SpotsDialog mDialog;

    public SpotsCallBack(Context context){

        mContext = context;

        initSpotsDialog();
    }



    private  void initSpotsDialog(){

        mDialog = new SpotsDialog(mContext,"拼命加载中...");

    }

    public  void showDialog(){
        mDialog.show();
    }

    public  void dismissDialog(){
        mDialog.dismiss();
    }


    public void setLoadMessage(int resId){
        mDialog.setMessage(mContext.getString(resId));
    }


    @Override
    public void onFailure(Request request, Exception e) {
        dismissDialog();
    }

    @Override
    public void onBeforeRequest(Request request) {

        showDialog();
    }

    @Override
    public void onResponse(Response response) {
        dismissDialog();
    }
}
