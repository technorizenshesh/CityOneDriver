package com.cityonedriver.stores.models;

import java.io.Serializable;
import java.util.ArrayList;

public class ModelStoreOrders implements Serializable {

    private ArrayList<Result> result;
    private String message;
    private String status;

    public void setResult(ArrayList<Result> result){
        this.result = result;
    }
    public ArrayList<Result> getResult(){
        return this.result;
    }
    public void setMessage(String message){
        this.message = message;
    }
    public String getMessage(){
        return this.message;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public String getStatus(){
        return this.status;
    }

    public class Result implements Serializable {

        private String id;

        private String order_id;

        private String user_id;

        private String restaurant_id;

        private String total_amount;

        private String cart_id;

        private String date;

        private String time;

        private String address;

        private String lat;

        private String lon;

        private String date_time;

        private String status;

        private String req_datetime;

        private String send_drivers;

        private String driver_id;

        private String accept_time;

        private String pickup_time;

        private String delivered_time;

        private String restaurant_name;

        private String restaurant_address;

        private String restaurant_lat;

        private String restaurant_lon;

        private String restaurant_image;

        private String user_name;

        private String mobile;

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getUser_name() {
            return user_name;
        }

        public void setUser_name(String user_name) {
            this.user_name = user_name;
        }

        public void setId(String id){
            this.id = id;
        }
        public String getId(){
            return this.id;
        }
        public void setOrder_id(String order_id){
            this.order_id = order_id;
        }
        public String getOrder_id(){
            return this.order_id;
        }
        public void setUser_id(String user_id){
            this.user_id = user_id;
        }
        public String getUser_id(){
            return this.user_id;
        }
        public void setRestaurant_id(String restaurant_id){
            this.restaurant_id = restaurant_id;
        }
        public String getRestaurant_id(){
            return this.restaurant_id;
        }
        public void setTotal_amount(String total_amount){
            this.total_amount = total_amount;
        }
        public String getTotal_amount(){
            return this.total_amount;
        }
        public void setCart_id(String cart_id){
            this.cart_id = cart_id;
        }
        public String getCart_id(){
            return this.cart_id;
        }
        public void setDate(String date){
            this.date = date;
        }
        public String getDate(){
            return this.date;
        }
        public void setTime(String time){
            this.time = time;
        }
        public String getTime(){
            return this.time;
        }
        public void setAddress(String address){
            this.address = address;
        }
        public String getAddress(){
            return this.address;
        }
        public void setLat(String lat){
            this.lat = lat;
        }
        public String getLat(){
            return this.lat;
        }
        public void setLon(String lon){
            this.lon = lon;
        }
        public String getLon(){
            return this.lon;
        }
        public void setDate_time(String date_time){
            this.date_time = date_time;
        }
        public String getDate_time(){
            return this.date_time;
        }
        public void setStatus(String status){
            this.status = status;
        }
        public String getStatus(){
            return this.status;
        }
        public void setReq_datetime(String req_datetime){
            this.req_datetime = req_datetime;
        }
        public String getReq_datetime(){
            return this.req_datetime;
        }
        public void setSend_drivers(String send_drivers){
            this.send_drivers = send_drivers;
        }
        public String getSend_drivers(){
            return this.send_drivers;
        }
        public void setDriver_id(String driver_id){
            this.driver_id = driver_id;
        }
        public String getDriver_id(){
            return this.driver_id;
        }
        public void setAccept_time(String accept_time){
            this.accept_time = accept_time;
        }
        public String getAccept_time(){
            return this.accept_time;
        }
        public void setPickup_time(String pickup_time){
            this.pickup_time = pickup_time;
        }
        public String getPickup_time(){
            return this.pickup_time;
        }
        public void setDelivered_time(String delivered_time){
            this.delivered_time = delivered_time;
        }
        public String getDelivered_time(){
            return this.delivered_time;
        }
        public void setRestaurant_name(String restaurant_name){
            this.restaurant_name = restaurant_name;
        }
        public String getRestaurant_name(){
            return this.restaurant_name;
        }
        public void setRestaurant_address(String restaurant_address){
            this.restaurant_address = restaurant_address;
        }
        public String getRestaurant_address(){
            return this.restaurant_address;
        }
        public void setRestaurant_lat(String restaurant_lat){
            this.restaurant_lat = restaurant_lat;
        }
        public String getRestaurant_lat(){
            return this.restaurant_lat;
        }
        public void setRestaurant_lon(String restaurant_lon){
            this.restaurant_lon = restaurant_lon;
        }
        public String getRestaurant_lon(){
            return this.restaurant_lon;
        }
        public void setRestaurant_image(String restaurant_image){
            this.restaurant_image = restaurant_image;
        }
        public String getRestaurant_image(){
            return this.restaurant_image;
        }
    }




}
