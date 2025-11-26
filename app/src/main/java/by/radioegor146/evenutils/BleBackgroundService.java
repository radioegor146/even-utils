package by.radioegor146.evenutils;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Objects;

import by.radioegor146.evenutils.ble.BleManager;
import by.radioegor146.evenutils.view.CursorInfo;
import by.radioegor146.evenutils.view.ViewRenderer;
import by.radioegor146.evenutils.view.ViewState;

public class BleBackgroundService extends Service {

    public static final String INITIALIZE_ACTION = "initialize";
    public static final String UPDATE_VIEW_STATE_ACTION = "update_view_state";
    public static final String VIEW_STATE_EXTRA = "view_state";

    private ViewRenderer renderer;
    private BleManager bleManager;
    private EvenRealitiesAdapter evenRealities;

    private ViewState latestViewState = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleViewStateUpdate(ViewState newViewState) {
        synchronized (this.bleManager.getQueue()) {
            latestViewState = newViewState;
            if (this.bleManager.getQueue().isEmpty()) {
                this.putUpdateToQueue();
            }
        }
    }

    private void putUpdateToQueue() {
        ViewState usedViewState = latestViewState;
        latestViewState = null;
        if (usedViewState == null) {
            return;
        }
        Log.w(MainActivity.class.getName(), "Received new state: " + usedViewState);
        Bitmap map = this.renderer.renderDualMap(usedViewState);
        CursorInfo cursorInfo = this.renderer.renderCursorOnDualMap(usedViewState);
        if (this.evenRealities.setMapImage(map, false)) {
            this.evenRealities.setCursorImage(cursorInfo.getBitmap(),
                    cursorInfo.getX(), cursorInfo.getY(), true);
            return;
        }
        this.evenRealities.setCursorImage(cursorInfo.getBitmap(),
                cursorInfo.getX(), cursorInfo.getY(), false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(BleBackgroundService.class.getName(), "BLE background service started");
        this.renderer = new ViewRenderer(this);

        this.bleManager = new BleManager(this, state -> {
            // runOnUiThread(() -> textViewConnectionStatus.setText("Connection status: " + (state ? "Connected" : "Disconnected")));
        }, this::putUpdateToQueue);
        this.evenRealities = new EvenRealitiesAdapter(this.bleManager);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            this.bleManager.init();
            return START_STICKY;
        }
        switch (Objects.requireNonNull(intent.getAction())) {
            case INITIALIZE_ACTION: {
                this.bleManager.init();
                this.handleViewStateUpdate(new ViewState(null, null, false));
                break;
            }
            case UPDATE_VIEW_STATE_ACTION: {
                ViewState viewState = intent.getParcelableExtra(VIEW_STATE_EXTRA, ViewState.class);
                this.handleViewStateUpdate(viewState);
                break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.bleManager != null) {
            this.bleManager.deInit();
        }
    }
}
