package com.bpk.filescanner;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.bpk.filescanner.parcelable.Update;
import com.bpk.filescanner.fragment.ExtDataFragment;
import com.bpk.filescanner.fragment.FileDataFragment;
import com.bpk.filescanner.service.Scanner;

public class MainActivity extends RuntimePermissionsActivity implements View.OnClickListener {

    public static final String TAG        = "MainActivity";

    private BroadcastReceiver updateReceiver;

    private Update mostRecent;

    private Button startButton;
    private Button shareButton;
    private TextView percView;
    private TextView avgView;

    private FileDataFragment fileDataFragment;
    private ExtDataFragment extDataFragment;
    private final int REQUEST_STORAGE_PERMISSION =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!isScannerActive()){startService(new Intent(this,Scanner.class));}

        startButton = (Button) findViewById(R.id.button_start);
        startButton.setOnClickListener(this);

        Button pauseButton = (Button) findViewById(R.id.button_pause);
        pauseButton.setOnClickListener(this);

        shareButton = (Button) findViewById(R.id.button_share);
        shareButton.setOnClickListener(this);
        shareButton.setVisibility(View.INVISIBLE);

        percView = (TextView)findViewById(R.id.perc_view);
        avgView  = (TextView)findViewById(R.id.avg_view);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Scanner.TAG+".UPDATE");

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mostRecent = intent.getParcelableExtra(Scanner.EXTRA_TAG);
                refreshUI();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver,intentFilter);

        fileDataFragment = (FileDataFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_file_data);
        extDataFragment  = (ExtDataFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_ext_data);

    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION:
                startScan();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Scanner.EXTRA_TAG, mostRecent);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mostRecent = savedInstanceState.getParcelable(Scanner.EXTRA_TAG);
        if(mostRecent!=null){refreshUI();}
    }

    public void startScan(){
        Intent intent = new Intent(TAG+".START");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void pauseScan(){
        Intent intent = new Intent(TAG+".PAUSE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void requestUpdate(){
        Intent intent = new Intent(TAG+".UPDATE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void stopScan(){
        Intent intent = new Intent(TAG+".STOP");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void shareResults(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{getResources().getString(R.string.add_receipent)});
        i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.store_scanned));
        i.putExtra(Intent.EXTRA_TEXT   , mostRecent.totalMB + "MBs of data has been scanned.");

        startActivity(Intent.createChooser(i, "Send mail..."));

    }

    private boolean isScannerActive(){
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (Scanner.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void refreshUI() {
            //test
            fileDataFragment.updateData(mostRecent);
            extDataFragment.updateData(mostRecent);

            if (mostRecent.isDone == 1) {shareButton.setVisibility(View.VISIBLE);//startButton.setText(getResources().getString(R.string.button_start));
            }else{shareButton.setVisibility(View.INVISIBLE);}//startButton.setText(getResources().getString(R.string.button_restart));}

            percView.setText("Total:" + mostRecent.totalMB + "MBs");
            avgView.setText("Avg:" + (float) mostRecent.averageFileSize + "MBs");

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_start:
                MainActivity.super.requestAppPermissions(new
                                String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE}, R.string.action_settings
                        , REQUEST_STORAGE_PERMISSION);
            //    startScan();
                break;
            case R.id.button_pause:
                pauseScan();
                break;
            case R.id.button_share:
                shareResults();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        stopScan();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(updateReceiver);
    }
}
