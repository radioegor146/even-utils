package by.radioegor146.evenutils.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.io.IOException;
import java.io.InputStream;

import by.radioegor146.evenutils.EvenRealitiesAdapter;

public class ViewRenderer {

    private final Typeface evenFont;
    private final Paint evenFontPaint;
    private final Bitmap playingBitmap;
    private final Bitmap pausedBitmap;
    private final Bitmap emptyBitmap = Bitmap.createBitmap(EvenRealitiesAdapter.MAP_CURSOR_SIZE.getWidth(),
            EvenRealitiesAdapter.MAP_CURSOR_SIZE.getHeight(), Bitmap.Config.ARGB_8888);

    public ViewRenderer(Context context) {
        this.evenFont = Typeface.createFromAsset(context.getAssets(), "fonts/even.ttf");
        this.evenFontPaint = new Paint();
        this.evenFontPaint.setTypeface(this.evenFont);
        this.evenFontPaint.setColor(Color.WHITE);
        this.evenFontPaint.setTextSize(20);

        this.playingBitmap = loadBitmapFromAssets(context.getAssets(), "images/playing.png");
        this.pausedBitmap = loadBitmapFromAssets(context.getAssets(), "images/paused.png");
    }

    private static Bitmap loadBitmapFromAssets(AssetManager assetManager, String fileName) {
        InputStream inputStream = null;
        Bitmap bitmap = null;
        try {
            inputStream = assetManager.open(fileName);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bitmap;
    }

    public Bitmap renderDualMap(ViewState state) {
        Bitmap bitmap = Bitmap.createBitmap(EvenRealitiesAdapter.DUAL_MAP_SIZE.getWidth(),
                EvenRealitiesAdapter.DUAL_MAP_SIZE.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        evenFontPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(state.hasMedia() ? (state.getCurrentPlayingTitle() == null ? "N/A" :
                        state.getCurrentPlayingTitle()) : "Nothing is playing", 0,
                20, evenFontPaint);
        if (state.hasMedia()) {
            canvas.drawText(state.getCurrentPlayingArtist() == null ? "N/A" :
                            state.getCurrentPlayingArtist(), 0,
                    45, evenFontPaint);
        }
        return bitmap;
    }

    public CursorInfo renderCursorOnDualMap(ViewState state) {
        if (state.hasMedia()) {
            if (state.isPlaying()) {
                return new CursorInfo(this.playingBitmap, EvenRealitiesAdapter.DUAL_MAP_SIZE.getWidth() - 10 - this.playingBitmap.getWidth(), 10);
            }
            return new CursorInfo(this.pausedBitmap, EvenRealitiesAdapter.DUAL_MAP_SIZE.getWidth() - 10 - this.playingBitmap.getWidth(), 10);
        }
        return new CursorInfo(this.emptyBitmap, 0, 0);
    }
}
