package by.radioegor146.evenutils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

import by.radioegor146.evenutils.ble.BleManager;
import by.radioegor146.evenutils.view.CursorInfo;
import by.radioegor146.evenutils.view.ViewRenderer;
import by.radioegor146.evenutils.view.ViewState;

public class BleBackgroundService extends Service {

    private static final String BLE_SHARED_PREFERENCES = "ble";
    private static final String USE_MAP_SHARED_PREFERENCE = "use_map";

    public static final String INITIALIZE_ACTION = "initialize";
    public static final String UPDATE_VIEW_STATE_ACTION = "update_view_state";
    public static final String VIEW_STATE_EXTRA = "view_state";

    private ViewRenderer renderer;
    private BleManager bleManager;
    private EvenRealitiesAdapter evenRealities;

    private ViewState latestViewState = null;
    private ViewState currentViewState = new ViewState(null, null, false);

    private Consumer<Boolean> connectionStateCallback = null;
    private boolean initialized = false;

    public class LocalBinder extends Binder {
        BleBackgroundService getService() {
            return BleBackgroundService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    private void handleViewStateUpdate(ViewState newViewState) {
        synchronized (this.bleManager.getQueue()) {
            latestViewState = newViewState;
            currentViewState = newViewState;
            if (this.bleManager.getQueue().isEmpty()) {
                this.putUpdateToQueue();
            }
        }
    }

    public void refresh() {
        latestViewState = currentViewState;
        putUpdateToQueue();
    }

    private void putUpdateToQueue() {
        ViewState usedViewState = latestViewState;
        latestViewState = null;
        if (usedViewState == null) {
            return;
        }
        Log.d(MainActivity.class.getName(), "Received new state: " + usedViewState);
        if (isUsingMapInsteadOfNews()) {
            Bitmap map = this.renderer.renderDualMap(usedViewState);
            CursorInfo cursorInfo = this.renderer.renderCursorOnDualMap(usedViewState);
            if (this.evenRealities.setMapImage(map, false)) {
                this.evenRealities.setCursorImage(cursorInfo.getBitmap(),
                        cursorInfo.getX(), cursorInfo.getY(), true);
                return;
            }
            this.evenRealities.setCursorImage(cursorInfo.getBitmap(),
                    cursorInfo.getX(), cursorInfo.getY(), false);
        } else {
            this.evenRealities.setNewsAt(1, "Now playing - " +
                    (usedViewState.isPlaying() ? "Playing" : "Paused"), usedViewState.hasMedia() ?
                    usedViewState.getCurrentPlayingTitle() + "\n" +
                            usedViewState.getCurrentPlayingArtist() : "Nothing");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(BleBackgroundService.class.getName(), "BLE background service started");
        this.renderer = new ViewRenderer(this);

        this.bleManager = new BleManager(this, state -> {
            if (this.connectionStateCallback != null) {
                this.connectionStateCallback.accept(state);
            }
            if (state) {
                refresh();
            }
        }, this::putUpdateToQueue);
        this.evenRealities = new EvenRealitiesAdapter(this.bleManager);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            if (!this.initialized) {
                this.initialized = true;
                this.bleManager.init();
            }
            return START_STICKY;
        }
        switch (Objects.requireNonNull(intent.getAction())) {
            case INITIALIZE_ACTION: {
                if (!this.initialized) {
                    this.initialized = true;
                    this.bleManager.init();
                    this.handleViewStateUpdate(new ViewState(null, null, false));
                }
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

    public void setConnectionStateCallback(Consumer<Boolean> connectionStateCallback) {
        this.connectionStateCallback = connectionStateCallback;
    }

    public boolean getConnectionStatus() {
        return this.bleManager.isConnected();
    }

    public boolean isUsingMapInsteadOfNews() {
        return getSharedPreferences(BLE_SHARED_PREFERENCES, Context.MODE_PRIVATE).getBoolean(USE_MAP_SHARED_PREFERENCE, false);
    }

    public void setUsingMapInsteadOfNews(boolean useMap) {
        SharedPreferences.Editor editor = getSharedPreferences(BLE_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putBoolean(USE_MAP_SHARED_PREFERENCE, useMap);
        editor.apply();
    }
}
