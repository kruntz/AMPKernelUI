package it.oltrenoi.ampkernelui.led;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

/**
 * Created by kruntz on 09/02/17.
 */

public class LedsDemo extends AsyncTask<Pca9685, Void, Void> {
    private static final String TAG = LedsDemo.class.getSimpleName();

    @Override
    protected Void doInBackground(Pca9685... pca9685s) {
        Pca9685 pca9685 = pca9685s[0];

        try {
            while (true) {
                // exit Runnable if the device is already closed
                if (this.isCancelled() || pca9685 == null) {
                    return null;
                }

                int red = 0;
                int green = 1365;
                int blue = 2730;
                boolean redPwmUp = true;
                boolean greenPwmUp = true;
                boolean bluePwmUp = true;
                int sweeps = 0;

                while (sweeps < 2) {
                    if (red < 0) {
                        red = 0;
                        redPwmUp = true;
                        sweeps++;
                    }
                    if (green < 0) {
                        green = 0;
                        greenPwmUp = true;
                    }
                    if (blue < 0) {
                        blue = 0;
                        bluePwmUp = true;
                    }
                    if (red >= 4095) {
                        red = 4095;
                        redPwmUp = false;
                    }
                    if (green >= 4095) {
                        green = 4095;
                        greenPwmUp = false;
                    }
                    if (blue >= 4095) {
                        blue = 4095;
                        bluePwmUp = false;
                    }
                    pca9685.rgbLeds(0, red, green, blue);
                    pca9685.rgbLeds(1, red, green, blue);
                    if (redPwmUp) {
                        red += 128;
                    } else {
                        red -= 128;
                    }
                    if (greenPwmUp) {
                        green += 128;
                    } else {
                        green -= 128;
                    }
                    if (bluePwmUp) {
                        blue += 128;
                    } else {
                        blue -= 128;
                    }
                    Thread.sleep(50);
                }

                pca9685.rgbLeds(0, 4095, 0, 0);
                pca9685.rgbLeds(1, 4095, 0, 0);
                Thread.sleep(2000);

                pca9685.rgbLeds(0, 4095, 4095, 0);
                pca9685.rgbLeds(1, 4095, 4095, 0);
                Thread.sleep(2000);

                pca9685.rgbLeds(0, 4095, 0, 4095);
                pca9685.rgbLeds(1, 4095, 0, 4095);
                Thread.sleep(2000);

                pca9685.rgbLeds(0, 0, 4095, 0);
                pca9685.rgbLeds(1, 0, 4095, 0);
                Thread.sleep(2000);

                pca9685.rgbLeds(0, 0, 4095, 4095);
                pca9685.rgbLeds(1, 0, 4095, 4095);
                Thread.sleep(2000);

                pca9685.rgbLeds(0, 0, 0, 4095);
                pca9685.rgbLeds(1, 0, 0, 4095);
                Thread.sleep(2000);
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception during leds update", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "Thread interrupted", e);
        }
        return null;
    }
}
