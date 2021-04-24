package com.cityonedriver.taxi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.cityonedriver.R;
import com.cityonedriver.taxi.models.ModelCarTypes;
import java.util.ArrayList;

public class AdapterCarTypes extends BaseAdapter {

    Context mContext;
    ArrayList<ModelCarTypes.Result> carList;

    public AdapterCarTypes(Context mContext, ArrayList<ModelCarTypes.Result> carList) {
        this.mContext = mContext;
        this.carList = carList;
    }

    @Override
    public int getCount() {
        return carList == null ? 0 : carList.size();
    }

    @Override
    public Object getItem(int position) {
        return carList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_car_types,parent,false);

        TextView tvCarName = view.findViewById(R.id.tvCarName);
        tvCarName.setText(carList.get(position).getCar_name());

        return view;

    }

}
