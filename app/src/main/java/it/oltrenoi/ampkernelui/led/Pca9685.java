package it.oltrenoi.ampkernelui.led;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by kruntz on 08/02/17.
 */


public class Pca9685 implements Closeable {

    /**
     * I2C address for this peripheral
     */
    public static final int I2C_ADDRESS = 0x40;
    private static final String TAG = Pca9685.class.getSimpleName();

    private I2cDevice mI2cDevice;

    /**
     * Create a new Pca9685 driver connected to the named I2C bus
     *
     * @param i2cName I2C bus name the display is connected to
     * @throws IOException
     */
    public Pca9685(String i2cName) throws IOException {
        this(i2cName, I2C_ADDRESS);
    }

    /**
     * Create a new Pca9685 driver connected to the named I2C bus and address
     *
     * @param i2cName    I2C bus name the display is connected to
     * @param i2cAddress I2C address of the display
     * @throws IOException
     */
    public Pca9685(String i2cName, int i2cAddress) throws IOException {
        I2cDevice device = new PeripheralManagerService().openI2cDevice(i2cName, i2cAddress);
        try {
            init(device);
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (IOException | RuntimeException ignored) {
            }
            throw e;
        }
    }

    /**
     * Create a new Pca9685 driver connected to the given device
     *
     * @param device I2C device of the display
     * @throws IOException
     */
//    public Pca9685(I2cDevice device) throws IOException {
//        init(device);
//    }

    /**
     * Recommended start sequence for initializing the communications with the OLED display.
     * WARNING: If you change this code, power cycle your display before testing.
     *
     * @throws IOException
     */
    private void init(I2cDevice device) throws IOException {
        mI2cDevice = device;

        // Recommended initialization sequence
        // Select MODE2 register
        // All pin's outputs are configured with a totem pole structure
        mI2cDevice.writeRegByte(0x01, (byte) 0x04);
        // Select MODE1 register
        // Response to LED all-call I2C address
        mI2cDevice.writeRegByte(0x00, (byte) 0x01);
//        Thread.sleep(5);

        // Read 1 byte of data from address 0x00(0)
        byte mode = (byte) mI2cDevice.readRegByte(0x00);

        // Select MODE1 register
        // Set sleep mode
        mI2cDevice.writeRegByte(0x00, (byte) 0x10);
        // Select PRE_SCALE register
        // Set prescale frequency to 60 Hz
        mI2cDevice.writeRegByte(0xFE, (byte) 0x40);

        // Select MODE1 register
        // Response to LED all-call I2C address
        mI2cDevice.writeRegByte(0x00, (byte) mode);
//        Thread.sleep(5);
        // Select MODE1 register
        // Restart
        mI2cDevice.writeRegByte(0x00, (byte) (mode | 0x80));

        // Select LED0_ON_L register
        mI2cDevice.writeRegByte(6, (byte) 0x00);
        // Select LED0_ON_H register
        mI2cDevice.writeRegByte(7, (byte) 0x00);
        // Select LED1_ON_L register
        mI2cDevice.writeRegByte(10, (byte) 0x00);
        // Select LED1_ON_H register
        mI2cDevice.writeRegByte(11, (byte) 0x00);
        // Select LED2_ON_L register
        mI2cDevice.writeRegByte(14, (byte) 0x00);
        // Select LED2_ON_H register
        mI2cDevice.writeRegByte(15, (byte) 0x00);
        // Select LED0_ON_L register
        mI2cDevice.writeRegByte(6 + 12, (byte) 0x00);
        // Select LED0_ON_H register
        mI2cDevice.writeRegByte(7 + 12, (byte) 0x00);
        // Select LED1_ON_L register
        mI2cDevice.writeRegByte(10 + 12, (byte) 0x00);
        // Select LED1_ON_H register
        mI2cDevice.writeRegByte(11 + 12, (byte) 0x00);
        // Select LED2_ON_L register
        mI2cDevice.writeRegByte(14 + 12, (byte) 0x00);
        // Select LED2_ON_H register
        mI2cDevice.writeRegByte(15 + 12, (byte) 0x00);
    }

    public void allLeds(final int pwmOffValue) throws IOException {
        // Select ALL_LED_ON_L register
        mI2cDevice.writeRegByte(0xFA, (byte) 0x00);
        // Select ALL_LED_ON_H register
        mI2cDevice.writeRegByte(0xFB, (byte) 0x00);
        // Select ALL_LED_OFF_L register
        mI2cDevice.writeRegByte(0xFC, (byte) (pwmOffValue & 0xFF));
        // Select ALL_LED_OFF_H register
        mI2cDevice.writeRegByte(0xFD, (byte) (pwmOffValue / 256));
    }

    public void rgbLeds(final int led, final int redPwmOffValue, final int greenPwmOffValue, final int bluePwmOffValue) throws IOException {
//        mI2cDevice.writeRegWord(12 * led + 8, (short) redPwmOffValue);
//        mI2cDevice.writeRegWord(12 * led + 12, (short) greenPwmOffValue);
//        mI2cDevice.writeRegWord(12 * led + 16, (short) bluePwmOffValue);
        // Select LED0_OFF_L register
        mI2cDevice.writeRegByte(12 * led + 8, (byte) (redPwmOffValue & 0xFF));
        // Select LED0_OFF_H register
        mI2cDevice.writeRegByte(12 * led + 9, (byte) (redPwmOffValue / 256));
        // Select LED1_OFF_L register
        mI2cDevice.writeRegByte(12 * led + 12, (byte) (greenPwmOffValue & 0xFF));
        // Select LED1_OFF_H register
        mI2cDevice.writeRegByte(12 * led + 13, (byte) (greenPwmOffValue / 256));
        // Select LED2_OFF_L register
        mI2cDevice.writeRegByte(12 * led + 16, (byte) (bluePwmOffValue & 0xFF));
        // Select LED2_OFF_H register
        mI2cDevice.writeRegByte(12 * led + 17, (byte) (bluePwmOffValue / 256));
    }

    @Override
    public void close() throws IOException {
        this.allLeds(0);

        if (mI2cDevice != null) {
            try {
                mI2cDevice.close();
            } finally {
                mI2cDevice = null;
            }
        }
    }
}
