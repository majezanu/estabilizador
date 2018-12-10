package com.tdm.estabilizador.ui.main.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;
import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.UartConfig;
import com.tdm.estabilizador.MyApplication;
import com.tdm.estabilizador.R;
import com.tdm.estabilizador.handlers.UsbHandler;
import com.tdm.estabilizador.services.transmiters.UsbService;
import com.tdm.estabilizador.ui.main.presenter.MainActivityPresenter;
import com.tdm.estabilizador.ui.main.presenter.MainActivityPresenterImpl;
import com.harrysoft.androidbluetoothserial.BluetoothManager;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements MainActivityView {
    ////////////////// Widgets /////////////////////////////////////////////////
    @BindView(R.id.start)
    Button start;

    @BindView(R.id.main_parent)
    ConstraintLayout parent;

    private Snackbar snackbar;
    private Toast toast;
    ////////////////// Fields ////////////////////////////////////////////////////
    private MainActivityPresenter presenter;
    private BluetoothManager bluetoothManager = BluetoothManager.getInstance();
    private SimpleBluetoothDeviceInterface deviceInterface;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        presenter = new MainActivityPresenterImpl(this);
        if (bluetoothManager == null) {
            showSnackBar("¡Lo sentimos!, no tenemos bluetooth para funcionar");
        }else{
            bluetoothManager.openSerialDevice("98:D3:31:70:6B:0B")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onConnected, this::onError);
        }
    }
    private void onConnected(BluetoothSerialDevice connectedDevice) {
        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        deviceInterface = connectedDevice.toSimpleDeviceInterface();
        // Listen to bluetooth events
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError);

       showSnackBar("Excelente, te has conectado al Arduino");
    }

    private void onMessageSent(String message) {
       Log.d("Message",message);

    }

    private void onMessageReceived(String message) {
        Log.d("Message",message);
    }

    private void onError(Throwable error) {
       Log.d("Error",error.getMessage());
    }

    @OnClick(R.id.start)
    public void startOnClick() {
        presenter.start();
    }

    @Override
    public void showToast(String s) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(MyApplication.getContext(), s, Toast.LENGTH_LONG);
        toast.show();


    }

    @Override
    public void showSnackBar(String s) {
        if (snackbar != null) {
            snackbar.dismiss();
        }
        snackbar = Snackbar.make(parent, s, Snackbar.LENGTH_LONG);
        snackbar.show();

    }

    @Override
    public void showLoading(boolean t) {

    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void sendData(String x) {
        if(deviceInterface != null){
            deviceInterface.sendMessage(x);
        }


    }


}
