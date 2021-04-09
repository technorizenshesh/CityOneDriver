package com.cityonedriver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.cityonedriver.databinding.ActivityDashboardBinding;
import com.cityonedriver.models.ModelLogin;
import com.cityonedriver.stores.activities.StoreOrdersActivity;
import com.cityonedriver.utils.AppConstant;
import com.cityonedriver.utils.SharedPref;

public class DashboardActivity extends AppCompatActivity {

    Context mContext = DashboardActivity.this;
    ActivityDashboardBinding binding;
    SharedPref sharedPref;
    ModelLogin modelLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_dashboard);
        sharedPref = SharedPref.getInstance(mContext);
        modelLogin = sharedPref.getUserDetails(AppConstant.USER_DETAILS);

        init();

    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.setUser(modelLogin.getResult());
    }

    private void init() {

        binding.cvStores.setOnClickListener(v -> {
            startActivity(new Intent(mContext, StoreOrdersActivity.class));
        });

        binding.tvLogout.setOnClickListener(v -> {
            logoutAppDialog();
        });

    }

    private void logoutAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(getString(R.string.logout_text))
                .setCancelable(false)
                .setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sharedPref.clearAllPreferences();
                        Intent loginscreen = new Intent(mContext, LoginActivity.class);
                        loginscreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        NotificationManager nManager = ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE));
                        nManager.cancelAll();
                        startActivity(loginscreen);
                        finishAffinity();
                    }
                }).setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

}