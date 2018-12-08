package com.tdm.estabilizador.handlers;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.tdm.estabilizador.services.transmiters.UsbService;
import com.tdm.estabilizador.ui.main.view.MainActivity;

import java.lang.ref.WeakReference;

public class UsbHandler extends Handler {
    private final WeakReference<MainActivity> mActivity;

    public UsbHandler(MainActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        Message message = msg;
        switch (msg.what) {
            case UsbService.MESSAGE_FROM_SERIAL_PORT:
                //float data = (float) msg.obj;
                //mActivity.get().readData(data);
                break;
            case UsbService.CTS_CHANGE:
                Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                break;
            case UsbService.DSR_CHANGE:
                Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                break;
        }
    }
}
