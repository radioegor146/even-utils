package by.radioegor146.evenutils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.session.MediaSessionManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
    private Button buttonTest;

    @SuppressLint("SetTextI18n")
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
        this.buttonTest = findViewById(R.id.buttonTest);

        this.buttonTest.setOnClickListener(v -> {
        });

        if (this.initPermissions()) {
            Intent initalizeIntent = new Intent(this, BleBackgroundService.class);
            initalizeIntent.setAction(BleBackgroundService.INITIALIZE_ACTION);
            this.startService(initalizeIntent);
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
                this.initPermissions();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}