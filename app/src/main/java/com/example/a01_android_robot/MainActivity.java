package com.example.a01_android_robot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.TypedArrayUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private static final int REQ_ENABLE_BLUETOOTH = 1001;
    private SeekBar motor_1, motor_2, angle, position, timePeriod;
    private TextView speed_1, speed_2, angle_val, position_val, time_val;
    private Button pushBtn;
    private BluetoothAdapter bluetoothAdapter;
    private ProgressDialog mProgressDialog;
    private ArrayList<BluetoothDevice> mDevices = new ArrayList<>();
    private ListView listDevices;
    private DeviceListAdapter mDeviceListAdapter;
    private BluetoothSocket mBluetoothSocket;
    private OutputStream mOutputStream;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private ProgressDialog progressDialog;

    private HashMap<String, String> infrastructureParams = new HashMap<String, String>() {
        {put(ChanelEnum.Motor_1.name(), "801");put(ChanelEnum.Motor_2.name(), "801");put(ChanelEnum.Angle.name(), "1470");put(ChanelEnum.Position.name(), "1470");}
    };

    private int minMotorSpeed = 800;
    private int maxMotorSeed = 2300;
    private int minPosition = 544;//75;
    private int maxPosition = 2400;//105;
    private int minAngle = 544;//45;
    private int maxAngle = 2400;//135;
    private int minTime = 500; //2 выстрела в секунду
    private int maxTime = 3000; // выстрел каждые 3 секунды

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Инициализируем все ползунки
        motor_1 = findViewById(R.id.motor_1);
        motor_2 = findViewById(R.id.motor_2);
        angle = findViewById(R.id.angle);
        position = findViewById(R.id.position);
        timePeriod = findViewById(R.id.time);
        pushBtn = findViewById(R.id.pushBtn);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Соединение...");
        progressDialog.setMessage("Подождите пожалуйста...");

        //Инициализируем все остальные элементы
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d(TAG, "onCreate(): Ваше устройство не поддерживает bluetooth");
            finish();
        }

        mDeviceListAdapter = new DeviceListAdapter(this, R.layout.device_item, mDevices);

        //Инициализируем все отображалки значений ползунков
        speed_1 = findViewById(R.id.speed_1);
        speed_2 = findViewById(R.id.speed_2);
        angle_val = findViewById(R.id.angle_val);
        position_val = findViewById(R.id.position_val);
        time_val = findViewById(R.id.time_val);

        //Устанавливаем максимальные и минимальные значения для всех ползунков
        motor_1.setMax(maxMotorSeed);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            motor_1.setMin(minMotorSpeed);
        }
        motor_2.setMax(maxMotorSeed);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            motor_2.setMin(minMotorSpeed);
        }
        angle.setMax(maxAngle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            angle.setMin(minAngle);
        }
        position.setMax(maxPosition);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            position.setMin(minPosition);
        }
        timePeriod.setMax(maxTime);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            timePeriod.setMin(minTime);
        }

        // Обработчики события изменения положения ползунков
        motor_1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speed_1.setText(String.valueOf(progress));
                Log.d(TAG, "motor_1_speed = " + motor_1.getProgress());
                setMessage(ChanelEnum.Motor_1, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        motor_2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speed_2.setText(String.valueOf(progress));
                Log.d(TAG, "motor_2_speed = " + motor_2.getProgress());
                setMessage(ChanelEnum.Motor_2, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        angle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                angle_val.setText(String.valueOf(progress));
                Log.d(TAG, "angle = " + angle.getProgress());
                setMessage(ChanelEnum.Angle, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        position.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                position_val.setText(String.valueOf(progress));
                Log.d(TAG, "position = " + position.getProgress());
                setMessage(ChanelEnum.Position, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        timePeriod.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                time_val.setText(String.valueOf(progress));
                Log.d(TAG, "time = " + timePeriod.getProgress());
                setMessage(ChanelEnum.TimePeriod, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        pushBtn.setOnClickListener(pushBtnListner);

//        //Устанавливаем первоначальные значения для всех движков
//        initialInfrastructure();

        enableBluetooth();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.Item_search) {
            searchDevices();
        }
        if (item.getItemId() == R.id.disconnect && connectedThread != null) {
            connectThread.cancel();
            connectedThread.cancel();
            showListDevices();
        }
        if (item.getItemId() == R.id.Item_exit) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchDevices() {
        Log.d(TAG, "searchDevices()");
        enableBluetooth();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.BLUETOOTH_SCAN},
                    1111);
        }
        checkPermissionLocation();

        if (!bluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "searchDevices: начинаем поиск утройств");
            bluetoothAdapter.startDiscovery();
        }
        if (bluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "searchDevices: поиск уже был запущен, перезапускаем его еще раз");
            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mRecevier, filter);
    }

    private void showListDevices() {
        Log.d(TAG, "showListDevices()");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Найденные устройства");

        View view = getLayoutInflater().inflate(R.layout.list_devices_view, null);
        listDevices = view.findViewById(R.id.list_devices);
        listDevices.setAdapter(mDeviceListAdapter);
        listDevices.setOnItemClickListener(ItemOnClickListener);

        builder.setView(view);
        builder.setNegativeButton("OK", null);
        builder.create();
        builder.show();
    }

    private void checkPermissionLocation() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int check = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
            check += checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            if (check != 0) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1002);
            }
        }
    }

    private void enableBluetooth() {
        Log.d(TAG, "enableBluetooth()");
        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableBluetooth: BlueTooth выключен, пытаемся включить");
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothAdapter.enable();
            Intent intent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(intent, REQ_ENABLE_BLUETOOTH);
        }
    }

    private void setMessage(ChanelEnum chanel, int value) {
        if (connectedThread != null && connectThread.isConnected()) {
            String msg = value + chanel.name;
            connectedThread.write(msg.getBytes());
            Log.d(TAG, msg);
        }
    }

    private void startConnection(BluetoothDevice device) {
        if (device != null) {
            try {
                Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                mBluetoothSocket = (BluetoothSocket) method.invoke(device, 1);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mBluetoothSocket.connect();
                mOutputStream = mBluetoothSocket.getOutputStream();
                showToastMessage("Подключение прошло успешно");
            } catch (Exception e) {
                showToastMessage("Ошибка подключения");
                e.printStackTrace();
            }
        }
    }

    private void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "enableBluetooth: Повтоно пытаемся отправить запрос на включение buetooth");
            enableBluetooth();
        }
    }

    //Обработчик события нажатия на кнопку "Пуск"
    private View.OnClickListener pushBtnListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Нажата кнопка Пуск");
            setMessage(ChanelEnum.Button, 1);
        }
    };

    private final AdapterView.OnItemClickListener ItemOnClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = mDevices.get(position);
            if (device != null) {
                connectThread = new ConnectThread(device);
                connectThread.start();
                int v = view.getVisibility();
                String i = view.getTransitionName();
            }
        }
    };

    private BroadcastReceiver mRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Log.d(TAG, "onReceive: ACTION_DISCOVERY_STARTED");
                showToastMessage("Начат поиск устройств");
                mProgressDialog = ProgressDialog.show(MainActivity.this, "Поиск устройств", "Пожалуйста подождите...");
            }
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED");
                showToastMessage("Поиск утройств завершен");
                mProgressDialog.dismiss();
                showListDevices();
            }
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                Log.d(TAG, "onReceive: ACTION_FOUND");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    if (!mDevices.contains(device)) {
                        mDeviceListAdapter.mDevices.add(device);
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mRecevier);
        try {
            if (mBluetoothSocket != null) mBluetoothSocket.close();
            if (mOutputStream != null) mOutputStream.close();
            if(connectThread != null) connectThread.cancel();
            if(connectedThread != null) connectedThread.cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket bluetoothSocket = null;
        private boolean success = false;

        public ConnectThread(BluetoothDevice device) {
            try {
                Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                bluetoothSocket = (BluetoothSocket) method.invoke(device, 1);
                progressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                bluetoothSocket.connect();
                success = true;
                progressDialog.dismiss();
            } catch (IOException e) {
                progressDialog.dismiss();
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Не могу соединиться!", Toast.LENGTH_SHORT).show();
                    }
                });
                cancel();
            }
            if (success) {
                connectedThread = new ConnectedThread(bluetoothSocket);
                connectedThread.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Запрашиваем у робота его текущие настройки для обновления в android
                        setMessage(ChanelEnum.Get, 0);

                        Toast.makeText(MainActivity.this, "Подключение прошло успешно", Toast.LENGTH_SHORT).show();
//                        View view = getLayoutInflater().inflate(R.layout.list_devices_view, null);
//                        int viewVisibility = view.getVisibility();
//                        view.clearAnimation();
//                        view.setVisibility(View.GONE);
//                        int jjj = view.getVisibility();
//                        String lkjl = view.getTransitionName();
                        //Todo СДЕЛАТЬ, ЧТОБЫ ОКНО СО СПИСКОМ УСТРОЙСТВ ЗАКРЫВАЛОСЬ (ПОКА ПРОСТО ВЫВОДИТСЯ СООБЩЕНИЕ ОБ УДАЧНОМ ПОДКЛЮЧЕНИИ)
                    }
                });
            }
        }

        public boolean isConnected() {
            return bluetoothSocket.isConnected();
        }

        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread{
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private boolean isConnected = false;
        public ConnectedThread(BluetoothSocket bluetoothSocket) {
            InputStream inputStream_ = null;
            OutputStream outputStream_ = null;

            try {
                inputStream_ = bluetoothSocket.getInputStream();
                outputStream_ = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.inputStream = inputStream_;
            this.outputStream = outputStream_;
            isConnected = true;
        }

        @Override
        public void run() {
            //todo https://www.youtube.com/watch?v=pPjiBPUB0wo 7:14
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            StringBuffer buffer = new StringBuffer();

            while (isConnected) {
                try {
                    int bytes = (char) bis.read();
                    if (bytes != 101) {buffer.append((char)bytes);}// если принятый символ не является 'e'(используется как конец передачи), то добавляем его в буфер
                    else {  // если принятый символ == 'e', то передача данных завершена. Приступаем к парсингу полученной строки
                        parseReciviedDataAndSetInfrParams(buffer.toString());
                        buffer.delete(0,buffer.length());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initialInfrastructure();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try { bis.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }

        //Парсит полученные от робота данные и устанавливает полученные параметры в HashMap infrastructureParams
        private void parseReciviedDataAndSetInfrParams(String data) {
            //Todo НАДО РЕАЛИЗОВАТЬ ПАРСИНГ ПРИШЕДШЕЙ СТРОКИ
            String[] pais = data.split("\\|");
            for (String pair: pais) {
                String[] keyVal = pair.split(":");
                infrastructureParams.put(keyVal[0], keyVal[1]);
            }
        }

        public void write(byte[] buffer) {
            if (outputStream != null) {
                try {
                    outputStream.write(buffer);
                    outputStream.flush();
                } catch (IOException e) {
                    showToastMessage("Ошибка отправки команды");
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            try {
                isConnected = false;
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initialInfrastructure() {
        //Todo реализация установки принятых от робота данных в значения ползунков -- НЕ ПРОВЕРЯЛ КАК РАБОТАЕТ, ЕЛИ НАДО, ТО ОТКОРРЕКТИРОВАТЬ
        // не уверен, что Chanel.mt1 и пр. возвращает строку при обращении к нему
        int tmp = Short.parseShort(infrastructureParams.get("m"/*ChanelEnum.Motor_1.name()*/));
        motor_1.setProgress(tmp == 0 ? minMotorSpeed + 1 : tmp);
        tmp = Short.parseShort(infrastructureParams.get("n"/*ChanelEnum.Motor_2.name()*/));
        motor_2.setProgress(tmp == 0 ? minMotorSpeed + 1 : tmp);
        tmp = Short.parseShort(infrastructureParams.get("a"/*ChanelEnum.Angle.name()*/));
        angle.setProgress(tmp == 0 ? (minAngle + maxAngle)/ 2 : tmp);
        tmp = Short.parseShort(infrastructureParams.get("p"/*ChanelEnum.Position.name()*/));
        position.setProgress(tmp == 0 ? (minPosition + maxPosition)/ 2 : tmp);
        tmp = Short.parseShort(infrastructureParams.get("t"/*ChanelEnum.TimePeriod.name()*/));
        timePeriod.setProgress(tmp == 0 ? minTime + 1 : tmp);
    }

    public enum ChanelEnum{
        //Запрос получения всех параметров робота для обновления значений на android
        Get("g"),
        Button("b"),
        Motor_1("m"),
        Motor_2("n"),
        Angle("a"),
        Position("p"),
        TimePeriod("t");

        private final String name;

        ChanelEnum(String s) {
            name = s;
        }
    }
}

