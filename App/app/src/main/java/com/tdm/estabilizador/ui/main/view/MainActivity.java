package com.tdm.estabilizador.ui.main.view;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.tdm.estabilizador.MyApplication;
import com.tdm.estabilizador.R;
import com.tdm.estabilizador.ui.main.presenter.MainActivityPresenter;
import com.tdm.estabilizador.ui.main.presenter.MainActivityPresenterImpl;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        presenter = new MainActivityPresenterImpl(this);
    }

    @OnClick(R.id.start)
    public void startOnClick(){
        presenter.start();
    }

    @Override
    public void showToast(String s) {
        if(toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(MyApplication.getContext(), s, Toast.LENGTH_LONG);
        toast.show();


    }

    @Override
    public void showSnackBar(String s) {
        if(snackbar != null){
            snackbar.dismiss();
        }
        snackbar = Snackbar.make(parent, s,Snackbar.LENGTH_LONG);
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
        presenter.onActivityResult(requestCode,resultCode,data);
    }
}
