package it.oltrenoi.ampkernelui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

import it.oltrenoi.ampkernelui.led.LedsDemo;
import it.oltrenoi.ampkernelui.led.Pca9685;
import it.oltrenoi.ampkernelui.oled.OledDemo;
import it.oltrenoi.ampkernelui.oled.Ssd1306;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

/**
 * Skeleton of the main AMP Kernel and UI activity.
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // I2C Device Name
    private static final String I2C_DEVICE_NAME = "I2C1";

    private Ssd1306 ssd1306;
    private OledDemo oledDemo;

    private Pca9685 pca9685;
    private LedsDemo ledsDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        this.listIoResources();

        try {
            pca9685 = new Pca9685(I2C_DEVICE_NAME);
        } catch (IOException e) {
            Log.e(TAG, "Error while opening leds", e);
            throw new RuntimeException(e);
        }
        Log.d(TAG, "Leds activity created");
        this.ledsDemo = new LedsDemo();
        this.ledsDemo.executeOnExecutor(THREAD_POOL_EXECUTOR, pca9685);

        try {
            ssd1306 = new Ssd1306(I2C_DEVICE_NAME);
        } catch (IOException e) {
            Log.e(TAG, "Error while opening screen", e);
            throw new RuntimeException(e);
        }
        Log.d(TAG, "OLED screen activity created");
        this.oledDemo = new OledDemo();
        this.oledDemo.executeOnExecutor(THREAD_POOL_EXECUTOR, ssd1306);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        // Stop AsyncTask
        this.oledDemo.cancel(true);
        // Close the device.
        try {
            ssd1306.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing SSD1306", e);
        } finally {
            ssd1306 = null;
        }

        // Stop AsyncTask
        this.ledsDemo.cancel(true);
        // Close the device.
        try {
            pca9685.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing Pca9685", e);
        } finally {
            pca9685 = null;
        }
    }

    private void listIoResources() {
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
    }
}
