package com.tdm.estabilizador.ui.main.view;

import android.app.Activity;
import android.app.PendingIntent;
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

import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.UartConfig;
import com.tdm.estabilizador.MyApplication;
import com.tdm.estabilizador.R;
import com.tdm.estabilizador.handlers.UsbHandler;
import com.tdm.estabilizador.services.transmiters.UsbService;
import com.tdm.estabilizador.ui.main.presenter.MainActivityPresenter;
import com.tdm.estabilizador.ui.main.presenter.MainActivityPresenterImpl;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    private UsbService usbService;
    private UsbHandler mHandler;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED

                    showSnackBar("¡Gracias por darnos permiso!");
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    showSnackBar("Ups, lo sentimos, pero no nos diste permiso");
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    showSnackBar("¡Se ha conectado un dispositivo Arduino!");
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    showSnackBar("Uy!, ¿Por qué lo desconectaste?");
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    showSnackBar("USB device not supported");
                    break;
            }
        }
    };

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        presenter = new MainActivityPresenterImpl(this);
        mHandler = new UsbHandler(this);

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
    public void sendData(byte[] x) {
        if (usbService != null) { // if UsbService was correctly binded, Send data
            usbService.write(x);
        }else{
            showSnackBar("No se pudo abrir el puerto");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    public void readData(float f){
        Log.d("Data incoming",String.valueOf(f));
        showSnackBar(String.valueOf(f));
    }
}
