package com.tdm.estabilizador.sensors;

import android.app.Application;
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

import com.tdm.estabilizador.MyApplication;
import com.tdm.estabilizador.ui.main.presenter.MainActivityPresenter;

import java.util.List;

public class AcquireDataSensors extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor sensor;
    private static MainActivityPresenter presenter;
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
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> listaSensores = null;
        if (mSensorManager != null) {
            listaSensores = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            if(listaSensores.contains(mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR))){
                sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                mSensorManager.registerListener(this, sensor,
                        SensorManager.SENSOR_DELAY_UI, new Handler());
                presenter.showToast("Excelente, tienes el sensor indicado");
            }else if(listaSensores.contains(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))) {
                sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mSensorManager.registerListener(this, sensor,
                        SensorManager.SENSOR_DELAY_UI, new Handler());
                presenter.showToast("Excelente, tienes el segundo sensor indicado");
            }
            else{
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
        switch (event.sensor.getType()){
            case Sensor.TYPE_ROTATION_VECTOR:
                DataRotationVector(event);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                DataAccelerometer(event);
                break;
        }


    }
    private void DataAccelerometer(SensorEvent event){
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        presenter.showToast("La aceleración es: "+ String.valueOf(accelationSquareRoot));
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
        for(float f: orientations) {
            f = (float)(Math.toDegrees(f));
        }
        presenter.showToast("El angulo es: "+ String.valueOf(orientations[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
