package by.radioegor146.evenutils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.session.MediaSessionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import by.radioegor146.evenutils.ble.BleManager;
import by.radioegor146.evenutils.view.CursorInfo;
import by.radioegor146.evenutils.view.ViewRenderer;
import by.radioegor146.evenutils.view.ViewState;

public class MainActivity extends AppCompatActivity {

    private TextView textViewConnectionStatus;
    private Switch switchMode;
    private Button buttonTest;

    private BleBackgroundService bleService;
    private boolean bound = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleBackgroundService.LocalBinder binder = (BleBackgroundService.LocalBinder) service;
            bleService = binder.getService();
            bound = true;

            updateConnectionStatus(bleService.getConnectionStatus());
            bleService.setConnectionStateCallback(state -> {
                updateConnectionStatus(state);
            });
            runOnUiThread(() -> {
                switchMode.setChecked(bleService.isUsingMapInsteadOfNews());
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            bleService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.textViewConnectionStatus = findViewById(R.id.textViewConnectionStatus);

        this.switchMode = findViewById(R.id.switchMode);
        this.switchMode.setOnCheckedChangeListener((v, checked) -> {
            if (this.bound) {
                this.bleService.setUsingMapInsteadOfNews(checked);
                this.bleService.refresh();
            }
        });

        this.buttonTest = findViewById(R.id.buttonTest);
        this.buttonTest.setOnClickListener(v -> {
            if (this.bound) {
                this.bleService.refresh();
            }
        });

        this.initializeBleService();
    }

    public boolean isNotificationServiceEnabled() {
        String packageName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && TextUtils.equals(packageName, cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initializeNotifications() {
        if (!this.isNotificationServiceEnabled()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        }
    }

    private void initializeBleService() {
        if (this.initPermissions()) {
            Intent initalizeIntent = new Intent(this, BleBackgroundService.class);
            initalizeIntent.setAction(BleBackgroundService.INITIALIZE_ACTION);
            this.startService(initalizeIntent);
            this.bindService(initalizeIntent, connection, Context.BIND_AUTO_CREATE);
            this.initializeNotifications();
        }
    }

    private boolean initPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 0);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.BLUETOOTH_CONNECT) && grantResults[i]
                    == PackageManager.PERMISSION_GRANTED) {
                this.initializeBleService();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateConnectionStatus(boolean state) {
        runOnUiThread(() -> this.textViewConnectionStatus.setText(
                "Connection status: " + (state ? "Connected" : "Disconnected")));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.bound) {
            this.unbindService(connection);
            this.bound = false;
        }
    }
}