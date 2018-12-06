package com.tdm.estabilizador.sensors;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tdm.estabilizador.ui.main.presenter.MainActivityPresenter;

import java.util.List;

public class AcquireDataSensors extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor sensor;
    private static MainActivityPresenter presenter;
    private boolean getFirstAngles;
    private long sampleTime = 1;
    private long past = 0;
    private long now = 0;
    private float angleX =-90;
    private float angleY = 0;
    private float currentAngleX = 0;
    private float currentAngleY = 0;
    private float pwm1 = 0;
    private float pwm2 = 0;
    private float errorX;
    private float errorY;
    private float errorPastX;
    private float errorPastY;
    private float kp = 3;
    private float ki = (float) 1;

    public AcquireDataSensors() {
    }

    public static MainActivityPresenter getPresenter() {
        return presenter;
    }

    public static void setPresenter(MainActivityPresenter presenter) {
        AcquireDataSensors.presenter = presenter;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getFirstAngles = true;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> listaSensores = null;
        if (mSensorManager != null) {
            listaSensores = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            if(listaSensores.contains(mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR))){
                sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                mSensorManager.registerListener(this, sensor,
                        SensorManager.SENSOR_DELAY_UI, new Handler());
                presenter.showToast("Excelente, tienes el sensor indicado");
            }else{
                presenter.showToast("Perdónanos por no ser suficientes para ti");
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (sensor != null) {
            mSensorManager.unregisterListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        getDataSensor(event);
    }

    private void getDataSensor(SensorEvent event) {
        synchronized (this){
            switch (event.sensor.getType()){
                case Sensor.TYPE_ROTATION_VECTOR:
                    DataRotationVector(event);
                    break;
            }
        }
    }

    private void DataRotationVector(SensorEvent event){

        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(
                rotationMatrix, event.values);

        // Remap coordinate system
        float[] remappedRotationMatrix = new float[16];
        SensorManager.remapCoordinateSystem(rotationMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                remappedRotationMatrix);

        float[] orientations = new float[3];
        SensorManager.getOrientation(remappedRotationMatrix, orientations);
        for(int i = 0; i < 3; i++) {
            orientations[i] = (float)(Math.toDegrees(orientations[i]));
        }
        float angleX_sensor = orientations[2];
        float angleY_sensor = orientations[1];
        if(getFirstAngles){
            Log.d("Primera vez","si");
            getFirstAngles = false;
        }
        now = System.currentTimeMillis();
        long timeChange = now - past;
        if(timeChange >= sampleTime){
            errorX = angleX - angleX_sensor;
            errorY = angleY - angleY_sensor;

            errorPastX = errorX*sampleTime+errorPastX;
            errorPastY = errorY*sampleTime+errorPastY;

            float P_X = kp*errorX;
            float P_Y = kp*errorY;
            float I_X = ki*errorPastX;
            float I_Y = ki*errorPastY;

            pwm1 = P_X + I_X;
            pwm2 = P_Y + I_Y;

            past = now;
        }
        float[] data = {pwm1,pwm2};
        presenter.sendData(data);
        Log.d("PWM1",String.valueOf(pwm1));
        Log.d("PWM2",String.valueOf(pwm2));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
