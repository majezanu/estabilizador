package com.tdm.estabilizador.ui.main.presenter;

import android.content.Intent;
import android.support.annotation.Nullable;

public interface MainActivityPresenter {
    void start();
    void stop();
    void showToast(String s);
    void sendData(float[] x);
    void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);
}
