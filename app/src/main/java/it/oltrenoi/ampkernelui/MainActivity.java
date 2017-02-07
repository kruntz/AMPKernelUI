package it.oltrenoi.ampkernelui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.util.List;

import it.oltrenoi.ampkernelui.oled.BitmapHelper;
import it.oltrenoi.ampkernelui.oled.Ssd1306;
import it.oltrenoi.ampkernelui.oled.Ssd1306tmp;

/**
 * Skeleton of the main AMP Kernel and UI activity.
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // I2C Device Name
    private static final String I2C_DEVICE_NAME = "I2C1";
    private static final int FPS = 30; // Frames per second on draw thread
    private static final int BITMAP_FRAMES_PER_MOVE = 4; // Frames to show bitmap before moving it
    private SpiDevice spiDevice;
    private I2cDevice i2cDevice;
    private boolean mExpandingPixels = true;
    private int mDotMod = 1;
    private int mBitmapMod = 0;
    private int mTick = 0;
    //    private Modes mMode = Modes.BITMAP;
    private Modes mMode = Modes.CROSSHAIRS;
    //    private Modes mMode = Modes.DOTS;
    private Ssd1306 mScreen;

    private Handler mHandler = new Handler();
    private Bitmap mBitmap;
    private Runnable mDrawRunnable = new Runnable() {
        /**
         * Updates the display and tick counter.
         */
        @Override
        public void run() {
            // exit Runnable if the device is already closed
            if (mScreen == null) {
                return;
            }
            mTick++;
            try {
                switch (mMode) {
                    case DOTS:
                        drawExpandingDots();
                        break;
                    case BITMAP:
                        drawMovingBitmap();
                        break;
                    case CROSSHAIRS:
                    default:
                        drawCrosshairs();
                        break;
                }
                mScreen.show();
                mHandler.postDelayed(this, 1000 / FPS);
            } catch (IOException e) {
                Log.e(TAG, "Exception during screen update", e);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        PeripheralManagerService manager = new PeripheralManagerService();

        // Enumerate SPI busses
        List<String> spiBusList = manager.getSpiBusList();
        if (spiBusList.isEmpty()) {
            Log.i(TAG, "No SPI bus available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + spiBusList);
        }

        // Enumerate I2C busses
        List<String> i2cBusList = manager.getI2cBusList();
        if (i2cBusList.isEmpty()) {
            Log.i(TAG, "No I2C bus available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + i2cBusList);
        }

        // Enumerate GPIO ports
        List<String> portList = manager.getGpioList();
        if (portList.isEmpty()) {
            Log.i(TAG, "No GPIO port available on this device.");
        } else {
            Log.i(TAG, "List of available ports: " + portList);
        }

//        try (Ssd1306tmp oledDisplay = new Ssd1306tmp(I2C_DEVICE_NAME)) {
//            oledDisplay.begin();
//            Paint paint = new Paint();
//
//            paint.setUnderlineText(true);
//            paint.setFakeBoldText(true);
//            paint.setTextSize(8f);
//            oledDisplay.getCanvas().drawText("View Group 01", 0f, 0f, paint);
//            oledDisplay.displayImage();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            mScreen = new Ssd1306(I2C_DEVICE_NAME);
        } catch (IOException e) {
            Log.e(TAG, "Error while opening screen", e);
            throw new RuntimeException(e);
        }
        Log.d(TAG, "OLED screen activity created");
        mHandler.post(mDrawRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        // remove pending runnable from the handler
        mHandler.removeCallbacks(mDrawRunnable);
        // Close the device.
        try {
            mScreen.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing SSD1306", e);
        } finally {
            mScreen = null;
        }
    }

    /**
     * Draws crosshair pattern.
     */
    private void drawCrosshairs() {
        mScreen.clearPixels();
        int y = mTick % mScreen.getLcdHeight();
        for (int x = 0; x < mScreen.getLcdWidth(); x++) {
            mScreen.setPixel(x, y, true);
            mScreen.setPixel(x, mScreen.getLcdHeight() - (y + 1), true);
        }
        int x = mTick % mScreen.getLcdWidth();
        for (y = 0; y < mScreen.getLcdHeight(); y++) {
            mScreen.setPixel(x, y, true);
            mScreen.setPixel(mScreen.getLcdWidth() - (x + 1), y, true);
        }
    }

    /**
     * Draws expanding and contracting pixels.
     */
    private void drawExpandingDots() {
        if (mExpandingPixels) {
            for (int x = 0; x < mScreen.getLcdWidth(); x++) {
                for (int y = 0; y < mScreen.getLcdHeight() && mMode == Modes.DOTS; y++) {
                    mScreen.setPixel(x, y, (x % mDotMod) == 1 && (y % mDotMod) == 1);
                }
            }
            mDotMod++;
            if (mDotMod > mScreen.getLcdHeight()) {
                mExpandingPixels = false;
                mDotMod = mScreen.getLcdHeight();
            }
        } else {
            for (int x = 0; x < mScreen.getLcdWidth(); x++) {
                for (int y = 0; y < mScreen.getLcdHeight() && mMode == Modes.DOTS; y++) {
                    mScreen.setPixel(x, y, (x % mDotMod) == 1 && (y % mDotMod) == 1);
                }
            }
            mDotMod--;
            if (mDotMod < 1) {
                mExpandingPixels = true;
                mDotMod = 1;
            }
        }
    }

    /**
     * Draws a BMP in one of three positions.
     */
    private void drawMovingBitmap() {
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flower);
        }
        // Move the bmp every few ticks
        if (mTick % BITMAP_FRAMES_PER_MOVE == 0) {
            mScreen.clearPixels();
            // Move the bitmap back and forth based on mBitmapMod:
            // 0 - left aligned
            // 1 - centered
            // 2 - right aligned
            // 3 - centered
            int diff = mScreen.getLcdWidth() - mBitmap.getWidth();
            int mult = mBitmapMod == 3 ? 1 : mBitmapMod; // 0, 1, or 2
            int offset = mult * (diff / 2);
            BitmapHelper.setBmpData(mScreen, offset, 0, mBitmap, false);
            mBitmapMod = (mBitmapMod + 1) % 4;
        }
    }

    enum Modes {
        CROSSHAIRS,
        DOTS,
        BITMAP
    }
}
