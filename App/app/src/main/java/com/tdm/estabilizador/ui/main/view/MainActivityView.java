package com.tdm.estabilizador.ui.main.view;

import android.app.Activity;

public interface MainActivityView {
    void showToast(String s);
    void showSnackBar(String s);
    void showLoading(boolean t);
    Activity getActivity();
    void sendData(byte[] x);
}
