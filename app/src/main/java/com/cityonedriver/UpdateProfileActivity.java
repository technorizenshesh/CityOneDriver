package com.cityonedriver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.os.Bundle;

import com.cityonedriver.databinding.ActivityUpdateProfileBinding;

public class UpdateProfileActivity extends AppCompatActivity {

    Context mContext = UpdateProfileActivity.this;
    ActivityUpdateProfileBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_update_profile);
        
        init();
        
    }

    private void init() {

    }


}