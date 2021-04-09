package com.cityonedriver.shipping.models;

import java.io.Serializable;
import java.util.ArrayList;

public class ModelMyTransports implements Serializable {

    private ArrayList<ModelMyTransports.Result> result;
    private String message;
    private String status;

    public void setResult(ArrayList<ModelMyTransports.Result> result){
        this.result = result;
    }
    public ArrayList<ModelMyTransports.Result> getResult(){
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

    public class Result implements Serializable
    {
        private String id;

        private String pickup_location;

        private String drop_location;

        private String parcel_quantity;

        private String shipping_id;

        private String bid_status;

        private String user_id;

        private String user_name;

        private String user_image;

        public String getUser_name() {
            return user_name;
        }

        public void setUser_name(String user_name) {
            this.user_name = user_name;
        }

        public String getUser_image() {
            return user_image;
        }

        public void setUser_image(String user_image) {
            this.user_image = user_image;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getShipping_id() {
            return shipping_id;
        }

        public void setShipping_id(String shipping_id) {
            this.shipping_id = shipping_id;
        }

        public String getBid_status() {
            return bid_status;
        }

        public void setBid_status(String bid_status) {
            this.bid_status = bid_status;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setPickup_location(String pickup_location){
            this.pickup_location = pickup_location;
        }
        public String getPickup_location(){
            return this.pickup_location;
        }
        public void setDrop_location(String drop_location){
            this.drop_location = drop_location;
        }
        public String getDrop_location(){
            return this.drop_location;
        }
        public void setParcel_quantity(String parcel_quantity){
            this.parcel_quantity = parcel_quantity;
        }
        public String getParcel_quantity(){
            return this.parcel_quantity;
        }
    }

}
