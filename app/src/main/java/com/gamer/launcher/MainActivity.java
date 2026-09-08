package com.gamer.launcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    private TextView hudStatus;
    private Button boostButton;
    private Button appDrawerButton;

    private boolean turboEnabled = false;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable statsUpdater = new Runnable() {
        @Override
        public void run() {
            updateSystemStats();
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        hudStatus = findViewById(R.id.hudStatus);
        boostButton = findViewById(R.id.boostButton);
        appDrawerButton = findViewById(R.id.appDrawerButton);

        boostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                turboEnabled = !turboEnabled;

                if (turboEnabled) {
                    boostButton.setText("TURBO MODE: ACTIVE");
                } else {
                    boostButton.setText("ACTIVATE TURBO MODE");
                }

                updateSystemStats();
            }
        });

        appDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAppDrawer();
            }
        });

        handler.post(statsUpdater);
    }

    private void updateSystemStats() {

        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo memoryInfo =
                new ActivityManager.MemoryInfo();

        activityManager.getMemoryInfo(memoryInfo);

        long usedRam =
                memoryInfo.totalMem - memoryInfo.availMem;

        long usedMb =
                usedRam / (1024 * 1024);

        long totalMb =
                memoryInfo.totalMem / (1024 * 1024);

        Intent batteryIntent =
                registerReceiver(
                        null,
                        new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                );

        int batteryLevel = 0;

        if (batteryIntent != null) {
            batteryLevel =
                    batteryIntent.getIntExtra(
                            BatteryManager.EXTRA_LEVEL,
                            0
                    );
        }

        String mode =
                turboEnabled ? "TURBO" : "BALANCED";

        String status =
                "RAM  " + usedMb + " / " + totalMb + " MB"
                + "\nBATTERY  " + batteryLevel + "%"
                + "\nPROFILE  " + mode;

        hudStatus.setText(status);
    }

    private void openAppDrawer() {

        final PackageManager packageManager =
                getPackageManager();

        Intent launcherIntent =
                new Intent(Intent.ACTION_MAIN);

        launcherIntent.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        final List<ResolveInfo> apps =
                packageManager.queryIntentActivities(
                        launcherIntent,
                        0
                );

        Collections.sort(
                apps,
                new Comparator<ResolveInfo>() {
                    @Override
                    public int compare(
                            ResolveInfo first,
                            ResolveInfo second) {

                        String firstName =
                                first.loadLabel(packageManager)
                                        .toString();

                        String secondName =
                                second.loadLabel(packageManager)
                                        .toString();

                        return firstName.compareToIgnoreCase(
                                secondName
                        );
                    }
                }
        );

        final ArrayList<String> appNames =
                new ArrayList<>();

        for (ResolveInfo app : apps) {
            appNames.add(
                    app.loadLabel(packageManager)
                            .toString()
            );
        }

        final ListView listView =
                new ListView(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        appNames
                );

        listView.setAdapter(adapter);

        final AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("GAMER APP DRAWER")
                        .setView(listView)
                        .setNegativeButton("CLOSE", null)
                        .create();

        listView.setOnItemClickListener(
                (parent, view, position, id) -> {

                    ResolveInfo selected =
                            apps.get(position);

                    String packageName =
                            selected.activityInfo.packageName;

                    Intent launchIntent =
                            packageManager
                                    .getLaunchIntentForPackage(
                                            packageName
                                    );

                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }

                    dialog.dismiss();
                }
        );

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(statsUpdater);
    }
}