package com.callmesp.vbook.api;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Administrator on 2017/4/16.
 */

public interface MainApi {

    @POST("http://111.231.134.55:4000/register")
    Observable<ResponseBody> getEmail(@Header("method") String head, @Body RequestBody requestBody);

    @POST("http://111.231.134.55:4000/register")
    Observable<ResponseBody> toRegister(@Header("method") String head, @Body RequestBody requestBody);

    @POST("http://111.231.134.55:4000/register")
    Observable<ResponseBody> toLogin(@Header("method") String head, @Body RequestBody requestBody);

    @POST("http://111.231.134.55:4000/register")
    Observable<ResponseBody> postOrder(@Header("method") String head,@Body RequestBody requestBody);
}
