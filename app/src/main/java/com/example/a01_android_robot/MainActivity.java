package com.example.a01_android_robot;

import static com.example.a01_android_robot.MainActivity.ChanelEnum.SaveSetInFlash;

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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private String mkAddress = "00:21:13:00:44:F2";
    private final String TAG = getClass().getSimpleName();
    private static final int REQ_ENABLE_BLUETOOTH = 1001;

    private LinearLayout settingsBlock, workBlock, directionsLayout;
    private ScrollView inGameParamsScrollView;

    private TabLayout tabLayout;
    private Slider angle, position, timePeriod, spinSlider, speedSlider,
            rateSlider_1, rateSlider_2, rateSlider_3, rateSlider_4,
            rateSlider_5, rateSlider_6, rateSlider_7, rateSwitcher,
            inGameSlider_m4,inGameSlider_m3, inGameSlider_m2,inGameSlider_m1,inGameSlider_0,
            inGameSlider_p1,inGameSlider_p2,inGameSlider_p3,inGameSlider_p4, inGameSwitcher,
            SetSlider, SetPauseSlider;
    private RangeSlider positionLimits;

    /** Включена настройка минимума скорости */
    private CheckBox is_min;
    /** Это режим настроек */
    private CheckBox isSetState;
    /** Значение угла закручивания устанавливается вручную */
    private CheckBox angleManual;
    /** Значение направления выстрела устанавливается вручную */
    private CheckBox positionManual;
    /** Настройки наборов "Направление" и "В игре" будут сохраняться во flash память контроллера */
    private CheckBox saveSetInFlash;
    private TextView speedLimitsLabel;
    private TextView inMessageTextBox;
    private TextView outMessageTextBox;
    private TextView logTextBox;

    private Button pushBtn;
    private Button saveBtn;
    private Button directionSetSaveBtn;
    private Button inGameSetSaveBtn;

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

    private HashMap<String, String> infrastructureParams = new HashMap<String, String>() {};
    //Запрошены лимиты - это первый запрос к STM
    private boolean limitsRequested;
    private boolean startMode = false;
    /** Необходимо отправить сообщение с value события. Необходимо для некоторых CheckBox (inGame) */
    private boolean needSendMessage = true;

    private int minPosition = 400;//75;
    private int maxPosition = 2400;//105;
    private int minAngle = 400;//45;
    private int maxAngle = 2400;//135;
    private int minTime = 500; //2 выстрела в секунду
    private int maxTime = 3000; // выстрел каждые 3 секунды
    private int speedMin = 850; //значение минимума скорости, необходимое при смене режима настроек с минимума на максимум
    private int speedMax = 970; //значение максимума скорости, необходимое при смене режима настроек с минимума на максимум
    private String messageString = ""; //сообщение, отправляемое на устройство. отображается внизу экрана android

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Инициализируем все элементы на экране'
        settingsBlock = findViewById(R.id.settingsBlock);
        workBlock = findViewById(R.id.workBlock);
        directionsLayout = findViewById(R.id.directionsLayout);
        inGameParamsScrollView = findViewById(R.id.inGameParamsScrollView);
        is_min = findViewById(R.id.is_min);
        isSetState = findViewById(R.id.isSetState);
        angleManual = findViewById(R.id.angleManual);
        positionManual = findViewById(R.id.positionManual);
        saveSetInFlash = findViewById(R.id.saveSetInFlash);
        speedLimitsLabel =findViewById(R.id.speedLimitsLabel);
        outMessageTextBox = findViewById(R.id.outMessageTextBox);
        inMessageTextBox = findViewById(R.id.inMessageTextBox);
        logTextBox = findViewById(R.id.logTextBox);
        spinSlider = findViewById(R.id.spinSlider);
        speedSlider = findViewById(R.id.speedSlider);
        angle = findViewById(R.id.angle);
        position = findViewById(R.id.position);
        timePeriod = findViewById(R.id.time);
        pushBtn = findViewById(R.id.pushBtn);
        saveBtn = findViewById(R.id.saveBtn);
        directionSetSaveBtn = findViewById(R.id.directionSetSaveBtn);
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
        positionLimits = findViewById(R.id.positionLimits);
        inGameSlider_m4 = findViewById(R.id.inGameSlider_m4);
        inGameSlider_m3 = findViewById(R.id.inGameSlider_m3);
        inGameSlider_m2 = findViewById(R.id.inGameSlider_m2);
        inGameSlider_m1 = findViewById(R.id.inGameSlider_m1);
        inGameSlider_0 = findViewById(R.id.inGameSlider_0);
        inGameSlider_p1 = findViewById(R.id.inGameSlider_p1);
        inGameSlider_p2 = findViewById(R.id.inGameSlider_p2);
        inGameSlider_p3 = findViewById(R.id.inGameSlider_p3);
        inGameSlider_p4 = findViewById(R.id.inGameSlider_p4);
        inGameSwitcher = findViewById(R.id.inGameSwitcher);
        SetSlider = findViewById(R.id.SetSlider);
        SetPauseSlider = findViewById(R.id.SetPauseSlider);
        tabLayout = findViewById(R.id.tab_layout);


        //Инициализируем все остальные элементы
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d(TAG, "onCreate(): Ваше устройство не поддерживает bluetooth");
            finish();
        }

        mDeviceListAdapter = new DeviceListAdapter(this, R.layout.device_item, mDevices);

        // Установка обработчиков события
        spinSlider.addOnChangeListener(spinSliderListner);
        speedSlider.addOnChangeListener(speedSliderListner);
        angle.addOnChangeListener(angleSliderListner);
        position.addOnChangeListener(positionSliderChangeListner);
        timePeriod.addOnChangeListener(timePeriodSliderChangeListner);
        rateSwitcher.addOnChangeListener(rateSwitcherSliderChangeListner);
        inGameSwitcher.addOnChangeListener(InGameSwitcherSliderChangeListner);
        SetSlider.addOnChangeListener(SetSliderChangeListner);
        SetPauseSlider.addOnChangeListener(SetPauseSliderChangeListner);
        positionLimits.addOnChangeListener(positionLimitsSliderListner);
        pushBtn.setOnClickListener(pushBtnListner);
        saveBtn.setOnClickListener(saveBtnListner);
        directionSetSaveBtn.setOnClickListener(directionSetSaveBtnListner);
        inGameSetSaveBtn.setOnClickListener(inGameSetSaveBtnListner);
        is_min.setOnCheckedChangeListener(isMinCheckBoxChangeListner);
        angleManual.setOnCheckedChangeListener(angleManualCheckBoxChangeListener);
        positionManual.setOnCheckedChangeListener(positionManualCheckBoxChangeListener);
        isSetState.setOnCheckedChangeListener(isSetStateCheckBoxListner);
        saveSetInFlash.setOnCheckedChangeListener(saveSetInFlashCheckBoxListner);
        tabLayout.addOnTabSelectedListener(tabSelectedListner);

        //Данные 2 операции необходимы для правильной прорисовки ползунков после смены режима
        isSetState.setChecked(true);
        isSetState.setChecked(false);

        //enableBluetooth(); //ЭТОТ МЕТОД ВЫЗЫВАЕТСЯ ВНУТРИ searchDevices() НИЖЕ

        // Пробуем соединиться с устройством //TODO:
        searchDevices();
    }


    /** Обработчик события нажатия на кнопку "Пуск" */
    private View.OnClickListener pushBtnListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startMode = !startMode;
            setMessage(ChanelEnum.Start, startMode ? 1 : 0, true);
            pushBtn.setText(startMode ? "Стоп" : "Пуск");
            Log.d(TAG, "Нажата кнопка Пуск, со значением " + startMode);
        }
    };

    /** Обработчик события нажатия на кнопку "Сохранить" */
    private View.OnClickListener saveBtnListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setMessage(ChanelEnum.Save, 1, true);
            Log.d(TAG, "Нажата кнопка Сохранить");
        }
    };

    /** Обработка события нажатия на кнопку "V" во вкладке "Направления" */
    private View.OnClickListener directionSetSaveBtnListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setMessage(ChanelEnum.DirectionSet, GetDirectionSetValue(), true);
        }
    };

    /** Обработка события нажатия на кнопку "V" во вкладке "Набор В игре" */
    private View.OnClickListener inGameSetSaveBtnListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setMessage(ChanelEnum.InGameSet, GetInGameValues(), true);
        }
    };

    /** Обработка события выбора в checkBox включения/выключения режима настроек */
    private CompoundButton.OnCheckedChangeListener isSetStateCheckBoxListner =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        isSetState.setText("Настройки");
                        settingsBlock.setVisibility(View.VISIBLE);
                        workBlock.setVisibility(View.GONE);
                        angle.invalidate();
                        position.invalidate();
                        timePeriod.invalidate();
                    } else {
                        isSetState.setText("Игра");
                        settingsBlock.setVisibility(View.GONE);
                        workBlock.setVisibility(View.VISIBLE);
                        angle.invalidate();
                        position.invalidate();
                        timePeriod.invalidate();
                    }

                    setMessage(ChanelEnum.IsSetState, isChecked ? 1 : 0, true);
                    Log.d(TAG, "isSetState = " + isChecked);
                }
            };

    /** Обработчик события выбора в checkBox устанавливаемого значения скорости min\max */
    private CompoundButton.OnCheckedChangeListener isMinCheckBoxChangeListner = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
                speedLimitsLabel.setText("min\nSpeed");
                speedSlider.setValue(speedMin);
            } else {
                speedSlider.setValue(speedMax);
                speedLimitsLabel.setText("max\nSpeed");
            }
            setMessage(ChanelEnum.IsMin, isChecked ? 1 : 0, true);
            Log.d(TAG, "is_min = " + isChecked);
        }
    };

    /** Обработчик события выбора в checkBox варианта сохранения данных сетов настроек "Направлениz" и "В игре": flash|temp */
    private CompoundButton.OnCheckedChangeListener saveSetInFlashCheckBoxListner = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            saveSetInFlash.setText(isChecked ? "Flash" : "Temp");
            setMessage(SaveSetInFlash, isChecked ? 1 : 0, true);
            Log.d(TAG, "SaveSetInFlash = " + isChecked);
        }
    };

    /** Обработчик события выбора вкладки для настроек "Направления"/"Набор в игре" */
    private TabLayout.OnTabSelectedListener tabSelectedListner = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            switch (tab.getPosition()){
                case 0: inGameParamsScrollView.setVisibility(View.GONE);
                    directionsLayout.setVisibility(View.VISIBLE);
                    break;
                default: directionsLayout.setVisibility(View.GONE);
                    inGameParamsScrollView.setVisibility(View.VISIBLE);
                    break;
            }
            saveSetInFlash.setChecked(false);//при переключении между вкладками сбрасываем значение checkBox сохранения во flash в deafult == false
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    /** Сформировать значение, отправляемое на устройство для набора участвующих в игре режимов закручивания */
    private long GetInGameValues(){
        return (long)inGameSwitcher.getValue() * 1000000000 +
                (long) inGameSlider_m4.getValue() * 100000000 +
                (long) inGameSlider_m3.getValue() * 10000000 +
                (long) inGameSlider_m2.getValue() * 1000000 +
                (long) inGameSlider_m1.getValue() * 100000 +
                (long) inGameSlider_0.getValue() * 10000 +
                (long) inGameSlider_p1.getValue() * 1000 +
                (long) inGameSlider_p2.getValue() * 100 +
                (long) inGameSlider_p3.getValue() * 10 +
                (long) inGameSlider_p4.getValue();
    }

    /** Обработчик события выбора в checkBox ручной настройки угла закручивания */
    private CompoundButton.OnCheckedChangeListener angleManualCheckBoxChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setMessage(ChanelEnum.AngleManual, isChecked ? 1 : 0, true);
        }
    };

    /** Обработчик события выбора в checkBox ручной настройки направления выстрела */
    private CompoundButton.OnCheckedChangeListener positionManualCheckBoxChangeListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setMessage(ChanelEnum.PositionManual, isChecked ? 1 : 0, true);
                }
            };

    /** Обработчик события передвижения ползунка Spin слайдера */
    private Slider.OnChangeListener spinSliderListner = new Slider.OnChangeListener() {
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            Log.d(TAG, (!fromUser ? "pr_" : "") + "spin = " + value);
            setMessage(ChanelEnum.Spin, (int) value, fromUser);
        }
    };

    /** Обработчик события передвижения ползунка слайдера Speed */
    private Slider.OnChangeListener speedSliderListner = new Slider.OnChangeListener() {
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            int val = (int) value;
            if (is_min.isChecked()) {   // если это установка минимального значения
                Log.d(TAG, (!fromUser ? "pr_" : "") + "minSpeed = " + value);
                if (val >= speedMax) {
                    if (speedMin < speedMax) {
                        speedMin = speedMax;
                        setMessage(ChanelEnum.SpeedStart, speedMin, fromUser);
                    }
                    speedSlider.setValue(speedMax);
                    return;
                }
                speedMin = val;
                setMessage(ChanelEnum.SpeedStart, val, fromUser);
            } else {  //Если это максимальное значение
                Log.d(TAG, (!fromUser ? "pr_" : "") + "maxSpeed = " + value);

                if (val <= speedMin) {
                    if (speedMax > speedMin) {
                        speedMax = speedMin;
                        setMessage(ChanelEnum.SpeedEnd, speedMax, fromUser);
                    }
                    speedSlider.setValue(speedMin);
                    return;
                }
                speedMax = val;
                setMessage(ChanelEnum.SpeedEnd, val, fromUser);
            }
        }
    };

    /** Обработчик события передвижения ползунка слайдера угла закручивания */
    private Slider.OnChangeListener angleSliderListner = new Slider.OnChangeListener() {
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            Log.d(TAG, (!fromUser ? "pr_" : "") + "angle = " + value);
            setMessage(ChanelEnum.Angle, (int) value, fromUser);
        }
    };

    /** Обработчик события передвижения слайдера направления выстрела */
    private Slider.OnChangeListener positionSliderChangeListner = new Slider.OnChangeListener() {
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            Log.d(TAG, (!fromUser ? "pr_" : "") + "position = " + value);
            setMessage(ChanelEnum.Position, (int) value, fromUser);
        }
    };

    /** Обработчик события перемещения ползунка слайдера интервала времени */
    private Slider.OnChangeListener timePeriodSliderChangeListner = new Slider.OnChangeListener() {
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            Log.d(TAG, (!fromUser ? "pr_" : "") + "time = " + value);
            setMessage(ChanelEnum.TimePeriod, (int) value, fromUser);
        }
    };

    /** Обработчик события перемещения ползунка переключения наборов количества выстрелов по направлениям */
    private Slider.OnChangeListener rateSwitcherSliderChangeListner = new Slider.OnChangeListener() {
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            Log.d(TAG, (!fromUser ? "pr_" : "") + "RateSwitcher = " + value);
            setMessage(ChanelEnum.RateSwitcher, (int) value, fromUser);
        }
    };

    /** Обработчик события перемещения ползунка переключения наборов закручивания мяча */
    private Slider.OnChangeListener InGameSwitcherSliderChangeListner = new Slider.OnChangeListener() {
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            Log.d(TAG, (!fromUser ? "pr_" : "") + "InGameSwitcher = " + value);
            setMessage(ChanelEnum.InGameSwitcher, (int)value, fromUser);
        }
    };

    /** Обработчик события перемещения ползунка выбора количества мячей в игровом сете */
    private Slider.OnChangeListener SetSliderChangeListner = new Slider.OnChangeListener() {
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            Log((!fromUser ? "pr_" : "") + "SetSlider = " + value);
            setMessage(ChanelEnum.Set, (int)value, fromUser);
        }
    };

    /** Обработчик события перемещения ползунка выбора продолжительности паузы между игровыми сетами */
    private Slider.OnChangeListener SetPauseSliderChangeListner = new Slider.OnChangeListener() {
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            Log((!fromUser ? "pr_" : "") + "SetPauseSlider = " + value);
            setMessage(ChanelEnum.SetPause, (int)value, fromUser);
        }
    };

    /** Обработчик события передвижения ползунков на слайдере границ направления выстрела */
    private RangeSlider.OnChangeListener positionLimitsSliderListner = new RangeSlider.OnChangeListener() {
        @Override
        public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
            float from = (float) slider.getValues().toArray()[0];
            float to = (float) slider.getValues().toArray()[1];
            setMessage(ChanelEnum.PositionStart, (int) from, fromUser);
            setMessage(ChanelEnum.PositionEnd, (int) to, fromUser);
            float oldPositionValue = position.getValue();
            if(oldPositionValue < from || oldPositionValue > to) position.setValue(value);
            position.setValueFrom(from);
            position.setValueTo(to);
            Log.d(TAG, "positionLimitSlider = " + from + "-" + to);
        }
    };

    /** Сформировать отправляемое на устройство значение параметров частоты выстрелов по направлениям */
    private int GetDirectionSetValue(){
        int retValue = (int)rateSwitcher.getValue() * 10000000 +
                (int)rateSlider_1.getValue() * 1000000 +
                (int)rateSlider_2.getValue() * 100000 +
                (int)rateSlider_3.getValue() * 10000 +
                (int)rateSlider_4.getValue() * 1000 +
                (int)rateSlider_5.getValue() * 100 +
                (int)rateSlider_6.getValue() * 10 +
                (int)rateSlider_7.getValue();
        return retValue;
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

    private void setMessage(ChanelEnum chanel, long value, boolean needSendMessage) {
        if(!needSendMessage) return;
        if (connectedThread != null && connectThread.isConnected()) {
            String msg = value + (chanel == ChanelEnum.Spin ? 4 : 0) + chanel.name;
            connectedThread.write(msg.getBytes());
            Log.d(TAG, msg);
            outMessageTextBox.setTextColor(Color.parseColor("#ffffff"));
            outMessageTextBox.setText(msg);
            inMessageTextBox.setTextColor(Color.parseColor("#f21717"));
        }
        else{outMessageTextBox.setText("NoConnect");}
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
///////////////////////////////////////////////////////////////////////////////////////////////////////////////TODO: ДОБАВЛЕННЫЙ КОД
                BluetoothDevice foundDevice = mDevices.stream()
                        .filter(device -> device.getAddress().equals(mkAddress))
                        .findFirst()
                        .orElse(null);
                if (foundDevice != null) {
                    // найдено устройство с нужным значением MAC-адреса
                    connectThread = new ConnectThread(foundDevice);
                    connectThread.start();
                }
                else { showListDevices(); }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////TODO: ДОБАВЛЕННЫЙ КОД
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
                        setMessage(ChanelEnum.Limits, 0, true);
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
                            setMessage(ChanelEnum.Get, 0, true); //После получения данных о лимитах делаем запрос на STM для получения текущих значений инфраструктуры
                        }
                        else {//Если это последующие запросы, то устанавливаем значения инфраструктуры
                            parseReciviedDataAndSetInfrParams(buffer.toString());
//                            String bufferStr = buffer.toString();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    initialInfrastructure(bufferStr);
//                                }
//                            });
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
            HashMap<String, String> tmpParams = new HashMap<>(infrastructureParams);

            infrastructureParams.clear();
            for (String unit: units) {
                String[] keyVal = unit.split(":");
                infrastructureParams.put(keyVal[0], keyVal[1]);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tmpParams.forEach((key, value) -> {
                        final short tmp;
                        Log("RecivedData: " + key + " : " + value);

                        switch (key) {
                            case "x":
                                speedMin = Math.round((int)Short.parseShort(value)/10)*10;
                                if (is_min.isChecked()) speedSlider.setValue(speedMin);
                                break;
                            case "y":
                                speedMax = Math.round((int)Short.parseShort(value)/10)*10;
                                if (!is_min.isChecked()) speedSlider.setValue(speedMax);
                                break;
                            case "r":
                                spinSlider.setValue(Short.parseShort(value));
                                break;
                            case "a":
                                tmp = Short.parseShort(value);
                                angle.setValue(Math.round((tmp == 0 ? (minAngle + maxAngle)/ 2 : tmp)/10)*10);
                                break;
                            case "p":
                                tmp = Short.parseShort(value);
                                position.setValue(Math.round((tmp == 0 ? (minPosition + maxPosition)/ 2 : tmp)/10)*10);
                                break;
                            case "t":
                                tmp = Short.parseShort(value);
                                timePeriod.setValue(tmp == 0 ? minTime + 1 : tmp);
                                break;
                            case "D":
                                rateSwitcher.setValue((int)value.charAt(0) - 48);
                                rateSlider_1.setValue((int)value.charAt(1) - 48);
                                rateSlider_2.setValue((int)value.charAt(2) - 48);
                                rateSlider_3.setValue((int)value.charAt(3) - 48);
                                rateSlider_4.setValue((int)value.charAt(4) - 48);
                                rateSlider_5.setValue((int)value.charAt(5) - 48);
                                rateSlider_6.setValue((int)value.charAt(6) - 48);
                                rateSlider_7.setValue((int)value.charAt(7) - 48);
                                break;
                            case "G":
                                inGameSwitcher.setValue((int)value.charAt(0) - 48);
                                inGameSlider_m4.setValue((int)value.charAt(1) - 48);
                                inGameSlider_m3.setValue((int)value.charAt(2) - 48);
                                inGameSlider_m2.setValue((int)value.charAt(3) - 48);
                                inGameSlider_m1.setValue((int)value.charAt(4) - 48);
                                inGameSlider_0.setValue((int)value.charAt(5) - 48);
                                inGameSlider_p1.setValue((int)value.charAt(6) - 48);
                                inGameSlider_p2.setValue((int)value.charAt(7) - 48);
                                inGameSlider_p3.setValue((int)value.charAt(8) - 48);
                                inGameSlider_p4.setValue((int)value.charAt(9) - 48);
                                break;
                            case "Z":
                                logTextBox.setText(value);
                                break;
                            case "S":
                                SetSlider.setValue(Short.parseShort(value));
                                break;
                            case "s":
                                SetPauseSlider.setValue(Short.parseShort(value));
                                break;                        }
                    });

                    inMessageTextBox.setTextColor(Color.parseColor("#ffffff"));
                    inMessageTextBox.setText(data);
                    outMessageTextBox.setTextColor(Color.parseColor("#f21717"));
                }
            });
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
            switch (key){
                case "a":
                    angle.setValueFrom(Math.round(min/10)*10);
                    angle.setValueTo(Math.round(max/10)*10);
                    angle.setValue(Math.round(min/10)*10);
                    angle.setStepSize(10);
                    break;
                case "p":
                    int from = Math.round(min/10)*10;
                    int to = Math.round(max/10)*10;
                    position.setValueFrom(from);
                    position.setValueTo(to);
                    position.setValue(from);
                    position.setStepSize(10);
                    positionLimits.setValues((float)from, (float)to);
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

    /** Логирование данных */
    public void Log(String message) {
        Log.d(TAG, message);
    }

    //Enum команд
    public enum ChanelEnum{
        /** Запрос лимитов для инфраструктуры */
        Limits("l"),
        /** Получить настройки инфрастуктуры */
        Get("g"),
        /** Нажата кнопка "Пуск" */
        Start("b"),
        Save("c"),
        Angle("a"),
        Position("p"),
        /** Начальная (левая) позиция выстрела */
        PositionStart("P"),
        /** Конечная (правая) позиция выстрела */
        PositionEnd("Q"),
        /** Установка угла вручную */
        AngleManual("A"),
        /** Установка направления выстрела вручную */
        PositionManual("B"),
        /** Время между выстрелами */
        TimePeriod("t"),
        /** Уровень вращения -4<=>4 */
        Spin("r"),
        /** Минимальная скорость вращения двигателя */
        SpeedStart("x"),
        /* Максимальная скорость вращения двигателя */
        SpeedEnd("y"),
        /** Настраивается минимум скорости */
        IsMin("i"),
        /** Приложение находится в режиме настроек */
        IsSetState("w"),
        /** Переключение наборов частот выстрелов по направлениям */
        RateSwitcher("F"),
        /** Настройки частоты выстрелов по направлениям */
        DirectionSet("D"),
        /** Настройки включения режимов закручивания мяча в игру */
        InGameSet("G"),
        /** Настройки включения режимов закручивания мяча в игру */
        InGameSwitcher("E"),
        /** Сохранить данные наборов настроек "Направления" и "В игре" во flash */
        SaveSetInFlash("T"),
        /** Выбор количества мячей в игровом сете */
        Set("S"),
        /** Выбор продолжительности паузы между игровыми сетами */
        SetPause("s");

        private final String name;

        ChanelEnum(String s) {
            name = s;
        }
    }
}

