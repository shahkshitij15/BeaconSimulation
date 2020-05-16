package com.example.sqlite;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.service.RangedBeacon;

import java.util.Collection;

public class BeaconDetection extends AppCompatActivity implements BeaconConsumer {

    private static final String TAG = "BeaconDetection";
    private Button startBtn;
    private Button stopBtn;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;


    private BeaconManager beaconManager = null;
    private Region beaconRegion = null;

    private BackgroundPowerSaver backgroundPowerSaver;

    private final String CHANNEL_ID = "beacon_notification";
    private final int NOTIFICATION_ID = 001;
    private String userBeaconStr = "B9B4D247-CD24-46E8-B59B-AC213EACA13D 4 200";

    View main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_detection);

        startBtn = findViewById(R.id.start);
        stopBtn = findViewById(R.id.stop);
        main = findViewById(R.id.beaconDetection);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBeaconMonitoring();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopBeaconMonitoring();

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));

        RangedBeacon.setSampleExpirationMilliseconds(5000);
        backgroundPowerSaver = new BackgroundPowerSaver(this);

        beaconManager.bind(this);
    }

    private void stopBeaconMonitoring() {

        Log.d(TAG, "stopBeaconMonitoring called. ");
        Toast.makeText(this, "Stopped Monitoring", Toast.LENGTH_SHORT).show();
        main.setBackgroundColor(Color.rgb(0, 0, 0));

        try {
            // beaconManager.stopMonitoringBeaconsInRegion(beaconRegion);
            beaconManager.stopRangingBeaconsInRegion(new Region("myRangingUniqueId1", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            CharSequence name = "Personal Notification";
            String description = "Include all the personal Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setNotification()
    {
        createNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_event_note_notification);
        builder.setContentTitle("Simple Notification");
        builder.setContentText("GREEN");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID,builder.build());
    }

    private void startBeaconMonitoring() {

        Log.d(TAG, "startBeaconMonitoring called. ");
        Toast.makeText(this, "Started Monitoring", Toast.LENGTH_SHORT).show();

        try {

            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId1", null, null, null));

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                if (beacons.size() > 0) {

                    while (beacons.iterator().hasNext()) {

                        Beacon b = beacons.iterator().next();

                        String uuid = String.valueOf(b.getId1()).toUpperCase(); //UUID
                        String major = String.valueOf(b.getId2());  //Major
                        String minor = String.valueOf(b.getId3());  //Minor
                        double distance = b.getDistance();  //Distance
                        //double distance = Math.round(distance1 * 100.0) / 100.0;

                        String currBeaconStr = uuid + " " + major + " " + minor;
                        Log.d(TAG,currBeaconStr);
                        Toast.makeText(BeaconDetection.this,"beacon id: "+currBeaconStr,Toast.LENGTH_SHORT).show();

                        if (userBeaconStr.equals(currBeaconStr)) {      //userBeaconStr is your own custom beacon which you want to scan for

                            if (distance < 0.5) {

                                main.setBackgroundColor(Color.rgb(0, 255, 0));

                                Toast.makeText(BeaconDetection.this, "Beacon found", Toast.LENGTH_SHORT).show();


                            } else if (b.getDistance() < 10) {
                                main.setBackgroundColor(Color.rgb(0, 0, 255));
                                Toast.makeText(BeaconDetection.this,"Keep finding mate!",Toast.LENGTH_SHORT).show();
                            }

                        }

                        beacons.remove(b);
                    }
                    beacons.clear();

                }


            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (beaconManager != null) {
            if (beaconManager.isBound(BeaconDetection.this)) {
                beaconManager.setBackgroundMode(false);
                beaconManager.unbind(BeaconDetection.this);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.d("PERMISSION", "coarse location permission granted");
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Functionality limited");
                builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                builder.setPositiveButton(android.R.string.ok, null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });

                builder.show();
            }
        }

    }


    public void checkBluetooth() {

        //Bluetooth Checking
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device Does Not Support Bluetooth-Contact Volunteer", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "Bluetooth Turned On.", Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.enable();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
                }
            }
        }

    }

    public boolean haveNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
            return true;
        } else {
            return false;
        }
    }
}
