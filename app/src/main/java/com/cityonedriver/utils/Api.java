package com.cityonedriver.utils;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Api {

    @FormUrlEncoded
    @POST("forgot_password")
    Call<ResponseBody> forgotPass(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("social_login")
    Call<ResponseBody> socialLogin(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("signup")
    Call<ResponseBody> signUpApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_restaurant_sub_category")
    Call<ResponseBody> getStoreByCatApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_cart")
    Call<ResponseBody> getStoreCartApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("delete_cart")
    Call<ResponseBody> deleteStoreItemApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_restaurant_details")
    Call<ResponseBody> getStoreDetailApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_order_history")
    Call<ResponseBody> getMyBookingApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_restaurant_child_category")
    Call<ResponseBody> getStoreItemsApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_count_cart")
    Call<ResponseBody> getCartCountApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_shipping_details")
    Call<ResponseBody> getShipDetailApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("place_order")
    Call<ResponseBody> bookingStoreApiCall(@FieldMap Map<String,String> params);

    @POST("get_restaurant_category")
    Call<ResponseBody> getStoreCatApiCall();

    @FormUrlEncoded
    @POST("get_all_shipping_request")
    Call<ResponseBody> getAllShRequestApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_my_transport")
    Call<ResponseBody> getMyTransportApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("login")
    Call<ResponseBody> loginApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("pending_order_history")
    Call<ResponseBody> storeOrderApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("delivered_order_history")
    Call<ResponseBody> deliveredApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("driver_accept_and_Cancel_order")
    Call<ResponseBody> acceptStoreApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("update_online_status")
    Call<ResponseBody> onlineOfflineStatusApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("accept_and_cancel_driver")
    Call<ResponseBody> driverShipStatusChangeApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("update_lat_lon")
    Call<ResponseBody> updateLocationApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("change_password")
    Call<ResponseBody> changePassApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("add_bid")
    Call<ResponseBody> addBidApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_running_booking_user")
    Call<ResponseBody> getActiveBooking(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_booking_history")
    Call<ResponseBody> getFinishedBooking(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_bid")
    Call<ResponseBody> getBidApiCall(@FieldMap Map<String,String> params);

    @POST("car_list")
    Call<ResponseBody> getCarTypesApi();

    @FormUrlEncoded
    @POST("get_booking_details")
    Call<ResponseBody> bookingDetails(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("insert_chat")
    Call<ResponseBody> insertChatApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("driver_accept_and_Cancel_request")
    Call<ResponseBody>  acceptCancelRequestForTaxi(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_chat")
    Call<ResponseBody> getAllMessagesCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("insert_chat_booking")
    Call<ResponseBody> insertChatBookingCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_chat_booking")
    Call<ResponseBody> getChatBookingCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("delete_bid")
    Call<ResponseBody> removeBidApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("add_to_cart")
    Call<ResponseBody> addToCartApiCall(@FieldMap Map<String,String> params);

    @FormUrlEncoded
    @POST("get_conversation")
    Call<ResponseBody> getConversation(@FieldMap Map<String,String> params);

}
