package com.tdm.estabilizador.ui.main.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;

import com.tdm.estabilizador.ui.main.view.MainActivityView;
import com.tdm.estabilizador.utils.RunTimePermission;

public class MainActivityPresenterImpl implements MainActivityPresenter, RunTimePermission.RunTimePermissionListener {
    private MainActivityView view;
    private RunTimePermission runtimePermission;
    private Activity activity;
    private static final int REQUEST_VIDEO_CAPTURE = 21;
    public MainActivityPresenterImpl(MainActivityView view) {
        this.view = view;
        init();
    }

    private void init(){
        activity = view.getActivity();
    }
    @Override
    public void start() {
        requestPermissionForApp();
        //takeVideoIntent();
    }

    private void requestPermissionForApp() {

        runtimePermission = new RunTimePermission(activity);
        runtimePermission.requestPermission(new String[]{Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO}, this);
    }
    private void takeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    public void permissionGranted() {
        view.showSnackBar("¡Gracias por darnos permiso!");
        Log.d("Permisos","Me diste  permiso");
    }

    @Override
    public void permissionDenied() {
        view.showSnackBar("Perdónanos por no ser suficientes para ti");
    }
}
