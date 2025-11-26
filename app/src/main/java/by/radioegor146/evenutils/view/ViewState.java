package by.radioegor146.evenutils.view;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class ViewState implements Serializable {
    private final String currentPlayingTitle;
    private final String currentPlayingArtist;
    private final boolean isPlaying;

    public ViewState(String currentPlayingTitle, String currentPlayingArtist, boolean isPlaying) {
        this.currentPlayingTitle = currentPlayingTitle;
        this.currentPlayingArtist = currentPlayingArtist;
        this.isPlaying = isPlaying;
    }

    public String getCurrentPlayingTitle() {
        return currentPlayingTitle;
    }

    public String getCurrentPlayingArtist() {
        return currentPlayingArtist;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean hasMedia() {
        return this.currentPlayingTitle != null || this.currentPlayingArtist != null;
    }

    @NonNull
    @Override
    public String toString() {
        return "ViewState{" +
                "currentPlayingTitle='" + currentPlayingTitle + '\'' +
                ", currentPlayingArtist='" + currentPlayingArtist + '\'' +
                ", isPlaying=" + isPlaying +
                '}';
    }
}
