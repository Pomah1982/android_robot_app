package com.example.a01_android_robot;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
    private LayoutInflater mLayoutInflater;
    private int mResourceView;
    private ArrayList<BluetoothDevice> mDevices = new ArrayList<>();

    public DeviceListAdapter(@NonNull Context context, int resource, ArrayList<BluetoothDevice> devices) {
        super(context, resource);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResourceView = resource;
        mDevices = devices;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mResourceView, null);

        BluetoothDevice device = mDevices.get(position);

        TextView tvName = convertView.findViewById(R.id.tvNameDevice);
        TextView tvAddress = convertView.findViewById(R.id.tvAddressDevice);

        if (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        tvName.setText(device.getName());
        tvAddress.setText(device.getAddress());

        return convertView;
    }
}
