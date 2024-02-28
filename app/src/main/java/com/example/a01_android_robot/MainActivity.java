package com.example.a01_android_robot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.Slider;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private static final int REQ_ENABLE_BLUETOOTH = 1001;
    //private SeekBar /*motor_1, motor_2,*/ angle, position, timePeriod;
    private Slider angle, position, timePeriod, spinSlider, speedSlider;
    private Slider rateSlider_1, rateSlider_2, rateSlider_3, rateSlider_4,
            rateSlider_5, rateSlider_6, rateSlider_7, rateSwitcher;
    private LinearLayout settingsBlock;
    private LinearLayout workBlock;
    private CheckBox is_used;
    private CheckBox is_min;
    private CheckBox isSetState;
    private TextView speedLimitsLabel, angle_val, position_val, time_val;
    private Button pushBtn;
    private Button saveBtn;
    private Button inGameSetSaveBtn;
    private boolean startMode = false;
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
    //Запрошены лимиты - это первый запрос к STM
    private boolean limitsRequested;

    private int minMotorSpeed = 850;
    private int maxMotorSeed = 1200;
    private int minPosition = 400;//75;
    private int maxPosition = 2400;//105;
    private int minAngle = 400;//45;
    private int maxAngle = 2400;//135;
    private int minTime = 500; //2 выстрела в секунду
    private int maxTime = 3000; // выстрел каждые 3 секунды
    private int speedMin = 850; //значение минимума скорости, необходимое при смене режима настроек с минимума на максимум
    private int speedMax = 970; //значение максимума скорости, необходимое при смене режима настроек с минимума на максимум

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Инициализируем все ползунки
//        motor_1 = findViewById(R.id.motor_1);
//        motor_2 = findViewById(R.id.motor_2);
        settingsBlock = findViewById(R.id.settingsBlock);
        workBlock = findViewById(R.id.workBlock);
        is_used = findViewById(R.id.is_used);
        is_min = findViewById(R.id.is_min);
        isSetState = findViewById(R.id.isSetState);
        speedLimitsLabel =findViewById(R.id.speedLimitsLabel);
        spinSlider = findViewById(R.id.spinSlider);
        speedSlider = findViewById(R.id.speedSlider);
        angle = findViewById(R.id.angle);
        position = findViewById(R.id.position);
        timePeriod = findViewById(R.id.time);
        pushBtn = findViewById(R.id.pushBtn);
        saveBtn = findViewById(R.id.saveBtn);
        inGameSetSaveBtn = findViewById(R.id.inGameSetSaveBtn);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Соединение...");
        progressDialog.setMessage("Подождите пожалуйста...");
        rateSlider_1 = findViewById(R.id.rateSlider_1);
        rateSlider_2 = findViewById(R.id.rateSlider_2);
        rateSlider_3 = findViewById(R.id.rateSlider_3);
        rateSlider_4 = findViewById(R.id.rateSlider_4);
        rateSlider_5 = findViewById(R.id.rateSlider_5);
        rateSlider_6 = findViewById(R.id.rateSlider_6);
        rateSlider_7 = findViewById(R.id.rateSlider_7);
        rateSwitcher = findViewById(R.id.rateSwitcher);


        //Инициализируем все остальные элементы
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d(TAG, "onCreate(): Ваше устройство не поддерживает bluetooth");
            finish();
        }

        mDeviceListAdapter = new DeviceListAdapter(this, R.layout.device_item, mDevices);

        //Инициализируем все отображалки значений ползунков
//        speed_1 = findViewById(R.id.speed_1);
//        speed_2 = findViewById(R.id.speed_2);
//        angle_val = findViewById(R.id.angle_val);
//        position_val = findViewById(R.id.position_val);
//        time_val = findViewById(R.id.time_val);

//        //Устанавливаем максимальные и минимальные значения для всех ползунков
//        motor_1.setMax(maxMotorSeed);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            motor_1.setMin(minMotorSpeed);
//        }
//        motor_2.setMax(maxMotorSeed);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            motor_2.setMin(minMotorSpeed);
//        }
//        angle.setMax(maxAngle);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            angle.setMin(minAngle);
//        }
//        position.setMax(maxPosition);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            position.setMin(minPosition);
//        }
//        timePeriod.setMax(maxTime);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            timePeriod.setMin(minTime);
//        }

        // Обработчики события изменения положения ползунков
//        motor_1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                speed_1.setText(String.valueOf(progress));
//                Log.d(TAG, "motor_1_speed = " + motor_1.getProgress());
//                setMessage(ChanelEnum.Motor_1, progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });

//        motor_2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                speed_2.setText(String.valueOf(progress));
//                Log.d(TAG, "motor_2_speed = " + motor_2.getProgress());
//                setMessage(ChanelEnum.Motor_2, progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });


        //Событие смены закручивания
        spinSlider.addOnChangeListener(
                new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                Log.d(TAG, "spin = " + value);
                setMessage(ChanelEnum.Spin, (int)value);

//////////////////////////////////Реализовать обработку события перемещения движков слайдера
            }
        });

        //Событие смены допустимых пределов силы атаки
        speedSlider.addOnChangeListener(
                new Slider.OnChangeListener() {
                    @Override
                    public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                        int val = (int)value;
                        if (is_min.isChecked()){   // если это установка минимального значения
                            Log.d(TAG, "minSpeed = " + value);
                            if(val >= speedMax) {
                                if(speedMin < speedMax){
                                    speedMin = speedMax;
                                    setMessage(ChanelEnum.SpeedStart, speedMin);
                                }
                                speedSlider.setValue(speedMax);
                                return;
                            }
                            speedMin = val;
                            setMessage(ChanelEnum.SpeedStart, val);
                        }
                        else {  //Если это максимальное значение
                            Log.d(TAG, "maxSpeed = " + value);

                            if(val <= speedMin) {
                                if(speedMax > speedMin){
                                    speedMax = speedMin;
                                    setMessage(ChanelEnum.SpeedEnd, speedMax);
                                }
                                speedSlider.setValue(speedMin);
                                return;
                            }
                            speedMax = val;
                            setMessage(ChanelEnum.SpeedEnd, val);
                        }
                    }
                });


        //Событие смены угла атаки
        angle.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                Log.d(TAG, "angle = " + value);
                setMessage(ChanelEnum.Angle, (int)value);
            }
        });

        //Смена позиции
        position.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                Log.d(TAG, "position = " + value);
                setMessage(ChanelEnum.Position, (int)value);
            }
        });

        //Смена интервала времени между выстрелами
        timePeriod.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                Log.d(TAG, "position = " + value);
                setMessage(ChanelEnum.TimePeriod, (int)value);
            }
        });

        //Обработка установка обработчика события нажатия на кнопку Пуск/Стоп
        pushBtn.setOnClickListener(pushBtnListner);
        saveBtn.setOnClickListener(saveBtnListner);
        inGameSetSaveBtn.setOnClickListener(inGameSetSaveBtnListner);


        //Установка обработчика события для checkBox включения/выключения данной настройки в игру
        is_used.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d(TAG, "is_used = " + isChecked);
                setMessage(ChanelEnum.IsUsed, isChecked ? 1 : 0);
//реализовать обработку checkBox
            }
        });

        //Установка обработчика события для checkBox выбора устанавливаемого значения скорости min\max
        is_min.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    speedLimitsLabel.setText("min\nSpeed");
                }
                else{
                    speedLimitsLabel.setText("max\nSpeed");
                }
                setMessage(ChanelEnum.IsMin, isChecked ? 1 : 0);
                Log.d(TAG, "is_min = " + isChecked);
            }
        });

        //Установка обработчика события для checkBox включения/выключения режима настроек
        isSetState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    isSetState.setText("Режим настроек");
                    settingsBlock.setVisibility(View.VISIBLE);
                    workBlock.setVisibility(View.GONE);
                    angle.invalidate();
                    position.invalidate();
                    timePeriod.invalidate();
                }
                else{
                    isSetState.setText("Рабочий режим");
                    settingsBlock.setVisibility(View.GONE);
                    workBlock.setVisibility(View.VISIBLE);
                    angle.invalidate();
                    position.invalidate();
                    timePeriod.invalidate();
                }

                setMessage(ChanelEnum.IsSetState, isChecked ? 1 : 0);
                Log.d(TAG, "isSetState = " + isChecked);
            }
        });

        isSetState.setChecked(true);
        isSetState.setChecked(false);

//        //Устанавливаем первоначальные значения для всех движков
//        initialInfrastructure();

        enableBluetooth();
    }


    //Обработчик события нажатия на кнопку "Пуск"
    private View.OnClickListener pushBtnListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startMode = !startMode;
            setMessage(ChanelEnum.Start, startMode ? 1 : 0);
            pushBtn.setText(startMode ? "Стоп" : "Пуск");
            Log.d(TAG, "Нажата кнопка Пуск, со значением " + startMode);
        }
    };

    //Обработчик события нажатия на кнопку "Сохранить"
    private View.OnClickListener saveBtnListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setMessage(ChanelEnum.Save, 1);
            Log.d(TAG, "Нажата кнопка Сохранить");
        }
    };

    //Обработка события нажатия на кнопку "V"
    private View.OnClickListener inGameSetSaveBtnListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setMessage(ChanelEnum.GameSet, GetGameSetValue());
        }
    };

    private int GetGameSetValue(){
        return (int)rateSwitcher.getValue() * 10000000 +
                (int)rateSlider_1.getValue() * 1000000 +
                (int)rateSlider_2.getValue() * 100000 +
                (int)rateSlider_3.getValue() * 10000 +
                (int)rateSlider_4.getValue() * 1000 +
                (int)rateSlider_5.getValue() * 100 +
                (int)rateSlider_6.getValue() * 10 +
                (int)rateSlider_7.getValue();
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
            String msg = value + (chanel == ChanelEnum.Spin ? 4 : 0) + chanel.name;
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
                        setMessage(ChanelEnum.Limits, 0);
                        limitsRequested = true;
//                        setMessage(ChanelEnum.Get, 0);

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
                        if (limitsRequested) {
                            parseReceivedDataAndSetLimits(buffer.toString().split("\\|"));// парсим и устанавливаем лимиты
                            limitsRequested = false;    //Сбрасываем флаг установки лимитов
                            setMessage(ChanelEnum.Get, 0); //После получения данных о лимитах делаем запрос на STM для получения текущих значений инфраструктуры
                        }
                        else {//Если это последующие запросы, то устанавливаем значения инфраструктуры
                            parseReciviedDataAndSetInfrParams(buffer.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initialInfrastructure();
                                }
                            });
                        }

                        buffer = buffer.delete(0,buffer.length());//Очищаем буфер для предотвращения дополнения старых данных вновь полученными
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
            String[] units = data.split("\\|");
            for (String unit: units) {
                String[] keyVal = unit.split(":");
                infrastructureParams.put(keyVal[0], keyVal[1]);
            }
        }

        //Парсит полученные от робота данные уже разделенные по символу "|" и устанавливает значения лимитов для ползунков
        private void parseReceivedDataAndSetLimits(String[] units){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (String unit: units) {
                        String[] keyAndLimits = unit.split(":");
                        int min = Short.parseShort(keyAndLimits[1]);
                        int max = Short.parseShort(keyAndLimits[2]);
                        setLimits(keyAndLimits[0], min,max);
                    }
                }
            });
        }

        public void setLimits(String key, int min, int max){
            //Устанавливаем максимальные и минимальные значения для всех ползунков
//            SeekBar infItem;
            switch (key){
                case "a":
                    angle.setValueFrom(Math.round(min/10)*10);
                    angle.setValueTo(Math.round(max/10)*10);
                    angle.setValue(Math.round(min/10)*10);
                    angle.setStepSize(10);
                    break;
                case "p":
                    position.setValueFrom(Math.round(min/10)*10);
                    position.setValueTo(Math.round(max/10)*10);
                    position.setValue(Math.round(min/10)*10);
                    position.setStepSize(10);
                    break;
                case "t":
                    timePeriod.setValueFrom(Math.round(min/100)*100);
                    timePeriod.setValueTo(Math.round(max/100)*100);
                    timePeriod.setValue(Math.round(min/100)*100);
                    timePeriod.setStepSize(100);
                    break;
                case "r":
                    spinSlider.setValueFrom(min);
                    spinSlider.setValueTo(max);
                    spinSlider.setValue(min);
                    spinSlider.setStepSize(1);
                    break;
                case "x":
                    is_min.setChecked(true);
                    speedSlider.setValueFrom(Math.round(min/10)*10);
                    speedSlider.setValueTo(Math.round(max/10)*10);
                    speedSlider.setValue(Math.round(min/10)*10);
                    speedSlider.setStepSize(10);
                    break;
                default: return;
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
        is_min.setChecked(true);
        isSetState.setChecked(false);
        int tmp;
        tmp = Short.parseShort(infrastructureParams.get(is_min.isChecked() ? "x" : "y"));
        speedSlider.setValue(Math.round(tmp/10)*10);
        tmp = Short.parseShort(infrastructureParams.get("r"));
        spinSlider.setValue(tmp);
        tmp = Short.parseShort(infrastructureParams.get("a"));
        angle.setValue(Math.round((tmp == 0 ? (minAngle + maxAngle)/ 2 : tmp)/10)*10);
        tmp = Short.parseShort(infrastructureParams.get("p"));
        position.setValue(Math.round((tmp == 0 ? (minPosition + maxPosition)/ 2 : tmp)/10)*10);
        tmp = Short.parseShort(infrastructureParams.get("t"));
        timePeriod.setValue(tmp == 0 ? minTime + 1 : tmp);
    }

    public enum ChanelEnum{
        //Запрос получения всех параметров робота для обновления значений на android
        Limits("l"),
        Get("g"),
        Start("b"),
        Save("c"),
        Motor_1("m"),
        Motor_2("n"),
        Angle("a"),
        Position("p"),
        TimePeriod("t"),
        Spin("r"),
        Speed("s"),
        SpeedStart("x"),
        SpeedEnd("y"),
        IsUsed("u"),
        IsMin("i"),
        IsSetState("w"),
        GameSet("G");

        private final String name;

        ChanelEnum(String s) {
            name = s;
        }
    }
}

