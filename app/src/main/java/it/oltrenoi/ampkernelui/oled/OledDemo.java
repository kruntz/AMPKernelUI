package it.oltrenoi.ampkernelui.oled;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

/**
 * Created by kruntz on 09/02/17.
 */

public class OledDemo extends AsyncTask<Ssd1306, Void, Void> {
    private static final String TAG = OledDemo.class.getSimpleName();

    private static final int BITMAP_FRAMES_PER_MOVE = 4; // Frames to show bitmap before moving it

    private boolean expand = true;
    private int mDotMod = 1;
    private int mBitmapMod = 0;
    private int mTick = 0;
    private Modes mode = Modes.CROSSHAIRS;
    private Ssd1306 ssd1306;
    private Bitmap bitmap;
    private Canvas canvas;

    @Override
    protected Void doInBackground(Ssd1306... ssd1306s) {
        ssd1306 = ssd1306s[0];

        // exit Runnable if the device is already closed
        while (!this.isCancelled() && ssd1306 != null) {
            switch (mode) {
                case DOTS:
                    drawExpandingDots();
                    break;
                case BITMAP:
                    drawMovingBitmap();
                    break;
                case CROSSHAIRS:
                    drawCrosshairs();
                    break;
                case TEXT:
                    drawText();
                default:
                    break;
            }
            try {
                ssd1306.show();
            } catch (IOException e) {
                Log.e(TAG, "Cannot show on OLED", e);
            }
            this.mTick++;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
                break;
            }
        }

        return null;
    }

    /**
     * Draws text.
     */
    private void drawText() {
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(ssd1306.getLcdWidth(), ssd1306.getLcdHeight(), Bitmap.Config.ARGB_4444);
        }
        Paint paint = new Paint();
        paint.setUnderlineText(true);
        paint.setFakeBoldText(true);
        paint.setTextSize(50f);
        paint.setAlpha(255);
        this.canvas = new Canvas(this.bitmap);
        this.canvas.drawRect(5, 5, ssd1306.getLcdWidth() - 5, ssd1306.getLcdHeight() - 5, paint);
        this.canvas.drawText("View Group 01", 0f, 30f, paint);
        ssd1306.clearPixels();
        BitmapHelper.setBmpData(ssd1306, 0, 0, bitmap, true);
    }

    /**
     * Draws crosshair pattern.
     */
    private void drawCrosshairs() {
        ssd1306.clearPixels();
        int y = mTick % ssd1306.getLcdHeight();
        for (int x = 0; x < ssd1306.getLcdWidth(); x++) {
            ssd1306.setPixel(x, y, true);
            ssd1306.setPixel(x, ssd1306.getLcdHeight() - (y + 1), true);
        }
        int x = mTick % ssd1306.getLcdWidth();
        for (y = 0; y < ssd1306.getLcdHeight(); y++) {
            ssd1306.setPixel(x, y, true);
            ssd1306.setPixel(ssd1306.getLcdWidth() - (x + 1), y, true);
        }
    }

    /**
     * Draws expanding and contracting pixels.
     */
    private void drawExpandingDots() {
        if (expand) {
            for (int x = 0; x < ssd1306.getLcdWidth(); x++) {
                for (int y = 0; y < ssd1306.getLcdHeight() && mode == Modes.DOTS; y++) {
                    ssd1306.setPixel(x, y, (x % mDotMod) == 1 && (y % mDotMod) == 1);
                }
            }
            mDotMod++;
            if (mDotMod > ssd1306.getLcdHeight()) {
                expand = false;
                mDotMod = ssd1306.getLcdHeight();
            }
        } else {
            for (int x = 0; x < ssd1306.getLcdWidth(); x++) {
                for (int y = 0; y < ssd1306.getLcdHeight() && mode == Modes.DOTS; y++) {
                    ssd1306.setPixel(x, y, (x % mDotMod) == 1 && (y % mDotMod) == 1);
                }
            }
            mDotMod--;
            if (mDotMod < 1) {
                expand = true;
                mDotMod = 1;
            }
        }
    }

    /**
     * Draws a BMP in one of three positions.
     */
    private void drawMovingBitmap() {
        if (bitmap == null) {
//            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flower);
        }
        // Move the bmp every few ticks
        if (mTick % BITMAP_FRAMES_PER_MOVE == 0) {
            ssd1306.clearPixels();
            // Move the bitmap back and forth based on mBitmapMod:
            // 0 - left aligned
            // 1 - centered
            // 2 - right aligned
            // 3 - centered
            int diff = ssd1306.getLcdWidth() - bitmap.getWidth();
            int mult = mBitmapMod == 3 ? 1 : mBitmapMod; // 0, 1, or 2
            int offset = mult * (diff / 2);
            BitmapHelper.setBmpData(ssd1306, offset, 0, bitmap, false);
            mBitmapMod = (mBitmapMod + 1) % 4;
        }
    }

    private enum Modes {
        CROSSHAIRS,
        DOTS,
        BITMAP,
        TEXT
    }
}

