package com.example.a01_android_robot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private SeekBar motor_1, motor_2, angle, position;
    private TextView speed_1, speed_2, angle_val, position_val;
    private int minMotorSpeed = 800;
    private int maxMotorSeed = 2300;
    private int minPosition = 75;
    private int maxPosition = 105;
    private int minAngle = 45;
    private int maxAngle = 135;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Инициализируем все ползунки
        motor_1 = findViewById(R.id.motor_1);
        motor_2 = findViewById(R.id.motor_2);
        angle = findViewById(R.id.angle);
        position = findViewById(R.id.position);

        //Инициализируем все отображалки значений ползунков
        speed_1 = findViewById(R.id.speed_1);
        speed_2 = findViewById(R.id.speed_2);
        angle_val = findViewById(R.id.angle_val);
        position_val = findViewById(R.id.position_val);

        //Устанавливаем максимальные и минимальные значения для всех ползунков
        motor_1.setMax(2300);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { motor_1.setMin(800); }
        motor_2.setMax(2300);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { motor_2.setMin(800); }
        angle.setMax(maxAngle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { angle.setMin(minAngle); }
        position.setMax(maxPosition);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { position.setMin(minPosition); }

        // Обработчики события изменения положения ползунков
        motor_1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speed_1.setText(String.valueOf(progress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        motor_2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speed_2.setText(String.valueOf(progress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        angle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                angle_val.setText(String.valueOf(progress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        position.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                position_val.setText(String.valueOf(progress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //Устанавливаем первоначальные значения для всех движков
        motor_1.setProgress(minMotorSpeed + 1);
        motor_2.setProgress(minMotorSpeed + 1);
        angle.setProgress((maxAngle + minAngle)/2);
        position.setProgress((minPosition + maxPosition)/2);
    }
}