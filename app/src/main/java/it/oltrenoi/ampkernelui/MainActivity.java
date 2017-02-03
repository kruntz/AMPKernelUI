package it.oltrenoi.ampkernelui;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.util.List;

import it.oltrenoi.ampkernelui.oled.Ssd1306;

/**
 * Skeleton of the main AMP Kernel and UI activity.
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // SPI Device Name
    private static final String SPI_DEVICE_NAME = "SPITEST";

    // I2C Device Name
    private static final String I2C_DEVICE_NAME = "I2CTEST";
    // I2C Slave Address
    private static final int I2C_ADDRESS = 0x7a;


    private SpiDevice spiDevice;
    private I2cDevice i2cDevice;

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

        // Attempt to access the SPI device
        try {
            spiDevice = manager.openSpiDevice(SPI_DEVICE_NAME);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access SPI device", e);
        }

        // Enumerate I2C busses
        List<String> i2cBusList = manager.getI2cBusList();
        if (i2cBusList.isEmpty()) {
            Log.i(TAG, "No I2C bus available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + i2cBusList);
        }

        // Attempt to access the I2C device
        try {
            i2cDevice = manager.openI2cDevice(I2C_DEVICE_NAME, I2C_ADDRESS);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access I2C device", e);
        }

        // Enumerate GPIO ports
        List<String> portList = manager.getGpioList();
        if (portList.isEmpty()) {
            Log.i(TAG, "No GPIO port available on this device.");
        } else {
            Log.i(TAG, "List of available ports: " + portList);
        }

        Ssd1306 oledDisplay = new Ssd1306(i2cDevice);
        oledDisplay.begin();
        Paint paint = new Paint();

        paint.setUnderlineText(true);
        paint.setFakeBoldText(true);
        paint.setTextSize(8f);
        oledDisplay.getCanvas().drawText("View Group 01", 0f, 0f, paint);
        oledDisplay.displayImage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        if (spiDevice != null) {
            try {
                spiDevice.close();
                spiDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close SPI device", e);
            }
        }

        if (i2cDevice != null) {
            try {
                i2cDevice.close();
                i2cDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C device", e);
            }
        }
    }
}
