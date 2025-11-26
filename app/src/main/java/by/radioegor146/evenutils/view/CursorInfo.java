package by.radioegor146.evenutils.view;

import android.graphics.Bitmap;

public class CursorInfo {
    private final Bitmap bitmap;
    private final int x;
    private final int y;

    public CursorInfo(Bitmap bitmap, int x, int y) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
