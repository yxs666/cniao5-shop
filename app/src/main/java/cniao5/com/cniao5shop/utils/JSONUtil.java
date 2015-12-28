/*
*JSONUtil.java
*Created on 2014-9-29 上午9:54 by Ivan
*Copyright(c)2014 Guangzhou Onion Information Technology Co., Ltd.
*http://www.cniao5.com
*/
package cniao5.com.cniao5shop.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * Created by Ivan on 14-9-29.
 * Copyright(c)2014 Guangzhou Onion Information Technology Co., Ltd.
 * http://www.cniao5.com
 */
public class JSONUtil {


    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();



    public static  Gson getGson(){
        return  gson;
    }



    public static <T> T fromJson(String json,Class<T> clz){

        return  gson.fromJson(json,clz);
    }


    public static <T> T fromJson(String json,Type type){

        return  gson.fromJson(json,type);
    }


    public static String toJSON(Object object){

       return gson.toJson(object);
    }

}
