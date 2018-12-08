package com.example.messi_lp.touchdemo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static String TAG="SHELL";
    private Button mStart_bt;
    private Button mQuit_bt;
    private TextView mdata_tv;
    private EditText mCommand_et;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mdata_tv=findViewById(R.id.textview);
        mQuit_bt=findViewById(R.id.root_bt);
        mCommand_et=findViewById(R.id.command_et);
        mdata_tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        mStart_bt=findViewById(R.id.getevent_bt);
        mQuit_bt.setOnClickListener(v ->quit());
        mStart_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdata_tv.setText("");
                startGetevent();
                mStart_bt.setEnabled(false);
            }
        });
    }

    @Override
    public void onDestroy(){
        compositeDisposable.dispose();
        super.onDestroy();

    }
    public void quit(){
        compositeDisposable.dispose();
        mStart_bt.setEnabled(true);


    }


    public void startGetevent(){
        final StringBuilder sb=new StringBuilder();
        String command;
        command=mCommand_et.getText().toString();
        Disposable disposable=Observable.create(new ObservableOnSubscribe<String>(){
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Process process = null;
                BufferedReader successResult = null;
                BufferedReader errorResult = null;
                StringBuilder successMsg = null;
                StringBuilder errorMsg = null;
                DataOutputStream os = null;
                try {
                    process=Runtime.getRuntime().exec(MyShell.COMMAND_SU);
                    os=new DataOutputStream(process.getOutputStream());
                    if (TextUtils.isEmpty(command)) {
                        String temp="getevent -l";
                        os.write(temp.getBytes());
                    }else
                        os.write(command.getBytes());

                    os.writeBytes(MyShell.COMMAND_LINE_END);
                    os.flush();
                    Log.i(TAG, "getEvent: start");


                    Log.i(TAG, "getEvent: restart");

                    successMsg = new StringBuilder();
                    errorMsg = new StringBuilder();
                    successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String s;
                    while ((s = successResult.readLine()) != null) {
                        emitter.onNext(s);
                        Log.i(TAG, "getEvent: "+s);
                    }
                    while ((s = errorResult.readLine()) != null) {
                        emitter.onNext(s);
                    }


                }catch (IOException io){
                    io.printStackTrace();
                } finally {
                    try {
                        if (os != null) {
                            os.close();
                        }
                        if (successResult != null) {
                            successResult.close();
                        }
                        if (errorResult != null) {
                            errorResult.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (process != null) {
                        process.destroy();
                    }
                }
                Log.i(TAG, "subscribe: onNext");
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.i(TAG, "accept: accept");
                        sb.append(s);
                        sb.append('\n');
                        mdata_tv.setText(sb.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
        compositeDisposable.add(disposable);

    }
    public String getEvent(@NonNull String command){
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            process=Runtime.getRuntime().exec(MyShell.COMMAND_SU);
            os=new DataOutputStream(process.getOutputStream());
            os.write(command.getBytes());
            os.writeBytes(MyShell.COMMAND_LINE_END);
            os.flush();
            Log.i(TAG, "getEvent: start");

            Thread.sleep(10000);

            Log.i(TAG, "getEvent: restart");

            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
                Log.i(TAG, "getEvent: "+s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }


        }catch (IOException io){
            io.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                    if (successResult != null) {
                        successResult.close();
                    }
                    if (errorResult != null) {
                        errorResult.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (process != null) {
                    process.destroy();
                }
        }
        return successMsg.length() !=0 ?successMsg.toString()
                                            :errorMsg.toString();


    }
}
