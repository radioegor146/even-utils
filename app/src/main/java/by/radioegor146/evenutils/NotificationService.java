package by.radioegor146.evenutils;

import android.app.Notification;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.Objects;

import by.radioegor146.evenutils.view.ViewState;

public class NotificationService extends NotificationListenerService {
    private String lastStartedPlayingApp = null;

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        Notification notification = statusBarNotification.getNotification();
        Log.w(NotificationService.class.getName(), notification.toString());
        Log.w(NotificationService.class.getName(), notification.extras.toString());
        if (Objects.equals(notification.extras.getString(Notification.EXTRA_TEMPLATE),
                Notification.MediaStyle.class.getName())) {
            MediaSession.Token token = notification.extras.getParcelable(
                    Notification.EXTRA_MEDIA_SESSION, MediaSession.Token.class);
            if (token == null) {
                return;
            }
            MediaController controller = new MediaController(this, token);
            Log.w(NotificationService.class.getName(), String.valueOf(controller.getPlaybackInfo()));

            MediaMetadata metadata = controller.getMetadata();
            if (metadata == null) {
                return;
            }

            String packageName = statusBarNotification.getPackageName();

            String title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            String artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
            PlaybackState playbackState = controller.getPlaybackState();
            boolean isPlaying = playbackState != null && playbackState.getState() == PlaybackState.STATE_PLAYING;

            if (isPlaying) {
                lastStartedPlayingApp = packageName;
            } else {
                if (lastStartedPlayingApp != null && !lastStartedPlayingApp.equals(packageName)) {
                    return;
                }
            }

            Intent viewStateUpdateIntent = new Intent(this, BleBackgroundService.class);
            viewStateUpdateIntent.setAction(BleBackgroundService.UPDATE_VIEW_STATE_ACTION);
            viewStateUpdateIntent.putExtra(BleBackgroundService.VIEW_STATE_EXTRA, new ViewState(title, artist, isPlaying));
            this.startService(viewStateUpdateIntent);
        }
    }
}
