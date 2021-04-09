package com.cityonedriver.models;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.cityonedriver.R;
import com.squareup.picasso.Picasso;

public class ModelLogin {

    private Result result;
    private String message;
    private String status;

    public void setResult(Result result){
        this.result = result;
    }
    public Result getResult(){
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

    public class Result
    {
        private String id;

        private String type;

        private String user_name;

        private String mobile;

        private String email;

        private String password;

        private String image;

        private String dob;

        private String social_id;

        private String lat;

        private String lon;

        private String address;

        private String gender;

        private String category_id;

        private String online_by;

        private String first_name;

        private String rating;

        private String last_name;

        private String register_id;

        private String ios_register_id;

        private String status;

        private String online_status;

        private String date_time;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUser_name() {
            return user_name;
        }

        public void setUser_name(String user_name) {
            this.user_name = user_name;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }

        public String getSocial_id() {
            return social_id;
        }

        public void setSocial_id(String social_id) {
            this.social_id = social_id;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLon() {
            return lon;
        }

        public void setLon(String lon) {
            this.lon = lon;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getCategory_id() {
            return category_id;
        }

        public void setCategory_id(String category_id) {
            this.category_id = category_id;
        }

        public String getOnline_by() {
            return online_by;
        }

        public void setOnline_by(String online_by) {
            this.online_by = online_by;
        }

        public String getFirst_name() {
            return first_name;
        }

        public void setFirst_name(String first_name) {
            this.first_name = first_name;
        }

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public String getLast_name() {
            return last_name;
        }

        public void setLast_name(String last_name) {
            this.last_name = last_name;
        }

        public String getRegister_id() {
            return register_id;
        }

        public void setRegister_id(String register_id) {
            this.register_id = register_id;
        }

        public String getIos_register_id() {
            return ios_register_id;
        }

        public void setIos_register_id(String ios_register_id) {
            this.ios_register_id = ios_register_id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getOnline_status() {
            return online_status;
        }

        public void setOnline_status(String online_status) {
            this.online_status = online_status;
        }

        public String getDate_time() {
            return date_time;
        }

        public void setDate_time(String date_time) {
            this.date_time = date_time;
        }
    }

    @BindingAdapter({"android:image"})
    public static void loadImage(ImageView imageView, String imageUrl){
        Picasso.get().load(imageUrl)
                .placeholder(R.drawable.default_profile_icon)
                .error(R.drawable.default_profile_icon)
                .into(imageView);
    }

}
